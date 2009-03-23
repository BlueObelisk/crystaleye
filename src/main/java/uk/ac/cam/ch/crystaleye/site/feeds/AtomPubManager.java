package uk.ac.cam.ch.crystaleye.site.feeds;

import static uk.ac.cam.ch.crystaleye.CrystalEyeConstants.ATOMPUB;
import static uk.ac.cam.ch.crystaleye.CrystalEyeConstants.CIF_HTML_SUMMARY_MIME;
import static uk.ac.cam.ch.crystaleye.CrystalEyeConstants.COMPLETE_CML_MIME_REGEX;
import static uk.ac.cam.ch.crystaleye.CrystalEyeConstants.DATE_MIME;
import static uk.ac.cam.ch.crystaleye.CrystalEyeConstants.SMALL_PNG_MIME;
import static uk.ac.cam.ch.crystaleye.CrystalEyeConstants.WEBPAGE;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nu.xom.Element;

import org.apache.log4j.Logger;
import org.xmlcml.cml.base.CMLConstants;

import uk.ac.cam.ch.crystaleye.AbstractManager;
import uk.ac.cam.ch.crystaleye.CrystalEyeUtils;
import uk.ac.cam.ch.crystaleye.IOUtils;
import uk.ac.cam.ch.crystaleye.IssueDate;
import uk.ac.cam.ch.crystaleye.Utils;
import uk.ac.cam.ch.crystaleye.properties.SiteProperties;
import uk.ac.cam.ch.crystaleye.templates.feeds.AtomPubTemplate;

public class AtomPubManager extends AbstractManager implements CMLConstants {
	
	private static final Logger LOG = Logger.getLogger(AtomPubManager.class);

	public static final String CURRENT_FEED_NAME = "feed.xml";
	public static final String FEED_TITLE = "CrystalEye:  All Structures";
	public static final String FEED_SUBTITLE = "Feed summarising all the structures in CrystalEye.";
	public static final String FEED_AUTHOR = "Chris Talbot";
	public String FEED_LINK;

	private SiteProperties properties;

	public AtomPubManager(File propertiesFile) {
		this.setProperties(propertiesFile);
	}

	public AtomPubManager(String propertiesPath) {
		this(new File(propertiesPath));
	}

	private void setProperties(File propertiesFile) {
		properties = new SiteProperties(propertiesFile);
	}

	public void execute() {
		String[] publisherAbbreviations = properties.getPublisherAbbreviations();
		for (String publisherAbbreviation : publisherAbbreviations) {
			String[] journalAbbreviations = properties.getPublisherJournalAbbreviations(publisherAbbreviation);
			for (String journalAbbreviation : journalAbbreviations) {
				String downloadLogPath = properties.getDownloadLogPath();
				List<IssueDate> unprocessedDates = this.getUnprocessedDates(downloadLogPath, publisherAbbreviation, journalAbbreviation, ATOMPUB, WEBPAGE);
				if (unprocessedDates.size() != 0) {
					for (IssueDate date : unprocessedDates) {
						String summaryWriteDir = properties.getSummaryWriteDir();
						String year = date.getYear();
						String issueNum = date.getIssue();
						String issueWriteDir = Utils.convertFileSeparators(summaryWriteDir+File.separator+
								publisherAbbreviation+File.separator+journalAbbreviation+File.separator+
								year+File.separator+issueNum);
						this.process(issueWriteDir);
						updateProps(downloadLogPath, publisherAbbreviation, journalAbbreviation, year, issueNum, ATOMPUB);
					}
				} else {
					LOG.info("No dates to process at this time for "+publisherAbbreviation+" journal "+journalAbbreviation);
				}
			}
		}
	}

	public void process(String issueWriteDir) {
		FEED_LINK = properties.getAtomPubRootUrl()+CURRENT_FEED_NAME;

		List<File> fileList = new ArrayList<File>();
		if (new File(issueWriteDir).exists()) {
			fileList = CrystalEyeUtils.getSummaryDirFileList(issueWriteDir, "[^\\._]*_[^\\.]*"+COMPLETE_CML_MIME_REGEX);
		}
		Map<Date, File> map = new TreeMap<Date, File>();
		for (File file : fileList) {
			String path = file.getAbsolutePath();
			String datePath = path.replaceAll("\\.complete\\.cml\\.xml", DATE_MIME);
			Date date = CrystalEyeUtils.parseString2Date(Utils.file2String(datePath));
			map.put(date, file);
		}

		fileList = new ArrayList<File>();
		for (Iterator it = map.values().iterator(); it.hasNext(); ) {
			fileList.add((File)it.next());
		}

		String currentFeedPath = properties.getAtomPubRootDir()+File.separator+CURRENT_FEED_NAME;
		File currentFile = new File(currentFeedPath);
		AtomPubFeed a = null;
		if (currentFile.exists()) {
			a = new AtomPubFeed(currentFile);
		} else {
			AtomPubTemplate tp = new AtomPubTemplate(FEED_TITLE, FEED_SUBTITLE, FEED_AUTHOR, FEED_LINK);
			a = tp.getFeedSkeleton();
		}
		updateFeeds(a, fileList, 0); 
	}

	public void updateFeeds(AtomPubFeed currentFeed, List<File> fileList, int position) {		
		Map<UUID, File> uuidMap = new HashMap<UUID, File>();
		int MAX_ENTRIES = properties.getAtomPubFeedMaxEntries();
		int entriesLeft = -1;
		if (currentFeed == null) {
			entriesLeft = MAX_ENTRIES;
			AtomPubTemplate tp = new AtomPubTemplate(FEED_TITLE, FEED_SUBTITLE, FEED_AUTHOR, FEED_LINK);
			currentFeed = tp.getFeedSkeleton();
		} else {
			entriesLeft = MAX_ENTRIES-currentFeed.getEntries().size();
		}

		int startPosition = position;
		int numFilesLeft = fileList.size()-startPosition;

		int c = Math.min(entriesLeft, numFilesLeft);
		for (int i = 0; i < c; i++) {
			File file = fileList.get(i+startPosition);
			String path = file.getAbsolutePath();

			String cmlUrl = dataPathToUrl(path);
			String htmlUrl = cmlUrl.replaceAll("\\.complete\\.cml\\.xml", CIF_HTML_SUMMARY_MIME);

			List<String> pngPathList = new ArrayList<String>();
			List<String> pngUrlList = new ArrayList<String>();
			for (File f : file.getParentFile().listFiles()) {
				String pngPath = f.getAbsolutePath();
				if (pngPath.endsWith(SMALL_PNG_MIME)) {
					pngPathList.add(pngPath);
					pngUrlList.add(dataPathToUrl(pngPath));
				}
			}

			Element content = null;
			if (pngUrlList.size() > 0) {
				content = new AtomHtmlContent(pngUrlList).create();
			}

			List<AtomEnclosure> encList = new ArrayList<AtomEnclosure>();
			AtomEnclosure enc = new AtomEnclosure(cmlUrl, null, (int)file.length(), "chemical/x-cml");
			encList.add(enc);
			for (String pngPath : pngPathList) {
				AtomEnclosure en = new AtomEnclosure(dataPathToUrl(pngPath), null, (int)new File(pngPath).length(), "image/png");
				encList.add(en);
			}

			String[] parts = null;
			if ("\\".equals(File.separator)) {
				parts = path.split("\\\\");
			} else {
				parts = path.split("/");
			}
			int len = parts.length;
			String s = parts[len-2];
			String[] ss = s.split("_");
			String article = parts[len-3];
			String iss = parts[len-6]+"/"+parts[len-5];
			String pubAbb = parts[len-8];
			String pub = properties.getPublisherTitle(pubAbb);
			String jrnlAbb = parts[len-7];
			String jrnl = properties.getJournalTitle(pubAbb, jrnlAbb);
			String title = "Summary page for crystal structure from DataBlock "+ss[1]+" in CIF "+ss[0]+" from article "+article+" in issue "+iss+" of "+pub+", "+jrnl+".";
			UUID id = UUID.randomUUID();
			uuidMap.put(id, file);
			AtomEntry entry = new AtomEntry(title, htmlUrl, getUUIDString(id), title, encList, content);
			currentFeed.addEntry(entry);

			position++;
			entriesLeft--;
			numFilesLeft--;
		}

		int penultimateNum = getPenultimateFeedNum();
		boolean isCurrentDoc = false;
		if (entriesLeft >= numFilesLeft) {
			// this is the currentdoc
			String prevLink = null;
			if (penultimateNum > 0) {
				prevLink = getArchiveFeedUrl(penultimateNum);
			}
			currentFeed.setAsCurrentDoc(prevLink);
			isCurrentDoc = true;
		} else {
			// doc following this is the currentdoc
			String prevLink = null;
			String nextLink = null;
			if (penultimateNum > 0) {
				prevLink = getArchiveFeedUrl(penultimateNum);
			}
			if (position+MAX_ENTRIES < numFilesLeft) {
				nextLink = getArchiveFeedUrl(penultimateNum+2);
			}
			currentFeed.setAsArchiveDoc(getCurrentFeedUrl(), prevLink, nextLink);
			if (penultimateNum > 0) {
				addNextArchiveElementFeed(penultimateNum);
			}
			isCurrentDoc = false;
		}

		String writePath = null;
		if (isCurrentDoc) {
			writePath = properties.getAtomPubRootDir()+File.separator+CURRENT_FEED_NAME;
			String currentFeedUrl = properties.getAtomPubRootUrl()+CURRENT_FEED_NAME;
			currentFeed.setId(currentFeedUrl);
			currentFeed.setLinkElement(AtomPubFeed.SELF_REL, currentFeedUrl);
		} else {
			currentFeed.setLinkElement(AtomPubFeed.SELF_REL, properties.getAtomPubRootUrl()+getArchiveFeedName(penultimateNum+1));
			currentFeed.setId(properties.getAtomPubRootUrl()+getArchiveFeedName(penultimateNum+1));
			writePath = getArchiveFeedPath(penultimateNum+1);
		}

		addUUIDsToTable(uuidMap);
		IOUtils.writePrettyXML(currentFeed.getFeed(), writePath);
		if (numFilesLeft > 0) {
			updateFeeds(null, fileList, position);
		}
	}
	
	private void addUUIDsToTable(Map<UUID, File> map) {
		String tablePath = properties.getUuidToUrlFilePath();
		File file = new File(tablePath);
		StringBuilder sb = new StringBuilder();
		for (Iterator it = map.entrySet().iterator(); it.hasNext(); ) {
	        Map.Entry entry = (Map.Entry)it.next();
	        String uuid = getUUIDString((UUID)entry.getKey());
	        String path = ((File)entry.getValue()).getAbsolutePath();
	        sb.append(uuid+" = "+path+"\n");
	    }
		
		String url = dataPathToUrl(file.getAbsolutePath());
		if (!file.exists()) {
			IOUtils.writeText(sb.toString(), url);
		} else {
			IOUtils.appendToFile(new File(url), sb.toString());
		}
	}
	
	private String getUUIDString(UUID id) {
		return "urn:uuid:"+id.toString();
	}

	private String dataPathToUrl(String path) {
		String dir = properties.getSummaryWriteDir();
		return path.replaceAll(dir, properties.getWebSummaryWriteDir());
	}

	public void addNextArchiveElementFeed(int num) {
		// get last archived feed and add a 'next-archive' link
		String lastArchivePath = getArchiveFeedPath(num);
		AtomPubFeed a = new AtomPubFeed(new File(lastArchivePath));
		a.setLinkElement(AtomPubFeed.NEXT_ARCHIVE_REL, getArchiveFeedUrl(num+1));
		IOUtils.writePrettyXML(a.getFeed(), lastArchivePath);
	}

	public int getPenultimateFeedNum() {
		Pattern p = Pattern.compile("feed-(\\d+)\\.xml");
		List<Integer> intList = new ArrayList<Integer>();
		for (File file : new File(properties.getAtomPubRootDir()).listFiles()) {
			String name = file.getName();
			Matcher m = p.matcher(name);
			if (m.find()) {
				intList.add(Integer.valueOf(m.group(1)));
			}
		}

		int penultimateFeedNum = -1;
		if (intList.size() > 0) {
			Collections.sort(intList);
			Collections.reverse(intList);
			penultimateFeedNum = intList.get(0);
		} else {
			penultimateFeedNum = 0;
		}
		return penultimateFeedNum;
	}

	public String getArchiveFeedPath(int i) {
		return properties.getAtomPubRootDir()+getArchiveFeedName(i);
	}

	public String getArchiveFeedUrl(int i) {
		return properties.getAtomPubRootUrl()+getArchiveFeedName(i);
	}

	public String getArchiveFeedName(int i) {
		return "feed-"+i+".xml";
	}

	public String getCurrentFeedUrl() {
		return properties.getAtomPubRootUrl()+CURRENT_FEED_NAME;
	}

	public static void main(String[] args) {
		AtomPubManager d = new AtomPubManager("e:/data-test/docs/cif-flow-props.txt");
		d.execute();
	}
}
