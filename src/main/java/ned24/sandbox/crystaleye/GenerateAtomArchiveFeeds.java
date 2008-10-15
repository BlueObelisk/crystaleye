package ned24.sandbox.crystaleye;

import static wwmm.crystaleye.CrystalEyeConstants.CIF_HTML_SUMMARY_MIME;
import static wwmm.crystaleye.CrystalEyeConstants.SMALL_PNG_MIME;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nu.xom.Element;
import wwmm.crystaleye.properties.SiteProperties;
import wwmm.crystaleye.site.feeds.AtomEnclosure;
import wwmm.crystaleye.site.feeds.AtomEntry;
import wwmm.crystaleye.site.feeds.AtomHtmlContent;
import wwmm.crystaleye.site.feeds.AtomPubFeed;
import wwmm.crystaleye.templates.feeds.AtomPubTemplate;
import wwmm.crystaleye.util.XmlIOUtils;

public class GenerateAtomArchiveFeeds {

	public static final String CURRENT_FEED_NAME = "feed.xml";
	public static final String FEED_TITLE = "CrystalEye:  All Structures";
	public static final String FEED_SUBTITLE = "Feed summarising all the structures in CrystalEye.";
	public static final String FEED_AUTHOR = "Chris Talbot";
	public String FEED_LINK;

	String dataPath;
	String fileListPath;

	SiteProperties properties;

	public GenerateAtomArchiveFeeds(String dataPath, String fileListPath, File propsFile) {
		this.dataPath = dataPath;
		this.fileListPath = fileListPath;
		properties = new SiteProperties(propsFile);
	}

	public void run() {
		List<File> fileList = new ArrayList<File>();
		System.out.println("started getting filelist");
		BufferedReader input = null;
		try {
			input = new BufferedReader(new FileReader(fileListPath));
			String line = null;
			while (( line = input.readLine()) != null){
				if (line != null && !"".equals(line)) {
					fileList.add(new File(line));
				}
			}
			input.close();
		}
		catch (FileNotFoundException ex) {
			throw new RuntimeException("Could not find file: "+fileListPath);
		}
		catch (IOException ex){
			throw new RuntimeException("Error reading file: "+fileListPath);
		}
		finally {
			try {
				if (input!= null) {
					input.close();
				}
			}
			catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		System.out.println("written file list");
		System.out.println("found CML files: "+fileList.size());

		String currentFeedPath = properties.getAtomPubRootDir()+File.separator+CURRENT_FEED_NAME;
		File currentFile = new File(currentFeedPath);
		AtomPubFeed a = null;
		System.out.println(currentFeedPath);
		if (currentFile.exists()) {
			System.out.println("exists");
			a = new AtomPubFeed(currentFile);
		} else {
			System.out.println("doesn't exist");
			AtomPubTemplate tp = new AtomPubTemplate(FEED_TITLE, FEED_SUBTITLE, FEED_AUTHOR, FEED_LINK);
			a = tp.getFeedSkeleton();
		}
		updateFeeds(a, fileList, 0); 
	}

	public void updateFeeds(AtomPubFeed currentFeed, List<File> fileList, int position) {
		//System.out.println("==========================================");
		//System.out.println("start pos: "+position);
		//System.out.println("filelist size: "+fileList.size());
		System.out.println("new feed sir?");

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
		//System.out.println("gah: "+entriesLeft+" "+numFilesLeft);
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
			currentFeed.setId(properties.getAtomPubRootUrl()+CURRENT_FEED_NAME);
		} else {
			currentFeed.setLinkElement("self", properties.getAtomPubRootUrl()+getArchiveFeedName(penultimateNum+1));
			currentFeed.setId(properties.getAtomPubRootUrl()+getArchiveFeedName(penultimateNum+1));
			writePath = getArchiveFeedPath(penultimateNum+1);
		}

		addUUIDsToTable(uuidMap);
		XmlIOUtils.writePrettyXML(currentFeed.getFeed(), writePath);
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
		if (!file.exists()) {
			XmlIOUtils.writeText(sb.toString(), file.getAbsolutePath());
		} else {
			XmlIOUtils.appendToFile(file, sb.toString());
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
		XmlIOUtils.writePrettyXML(a.getFeed(), lastArchivePath);
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

	public void writeFileList(List<File> fileList) {
		StringBuilder sb = new StringBuilder();
		for (File file : fileList) {
			sb.append(file.getAbsolutePath()+"\n");
		}
		XmlIOUtils.writeText(sb.toString(), fileListPath);
	}

	public static void main(String[] args) {
		String propsPath = "/usr/local/crystaleye/docs/cif-flow-props.txt";
		String dataPath = "/data_soft/www/crystaleye/summary/";
		String fileListPath = "/usr/local/crystaleye/docs/atomPubOrderForPreTimeStampFiles.txt";
		GenerateAtomArchiveFeeds gen = new GenerateAtomArchiveFeeds(dataPath, fileListPath, new File(propsPath));
		gen.run();
	}
}
