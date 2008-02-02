package uk.ac.cam.ch.crystaleye.site.feeds;

import static uk.ac.cam.ch.crystaleye.CrystalEyeConstants.CMLRSS_DESC_VALUE_PREFIX;
import static uk.ac.cam.ch.crystaleye.CrystalEyeConstants.CMLRSS_DIR_NAME;
import static uk.ac.cam.ch.crystaleye.CrystalEyeConstants.COMPLETE_CML_MIME;
import static uk.ac.cam.ch.crystaleye.CrystalEyeConstants.COMPLETE_CML_MIME_REGEX;
import static uk.ac.cam.ch.crystaleye.CrystalEyeConstants.FEED_FILE_NAME;
import static uk.ac.cam.ch.crystaleye.CrystalEyeConstants.RSS;
import static uk.ac.cam.ch.crystaleye.CrystalEyeConstants.RSS_ALL_DIR_NAME;
import static uk.ac.cam.ch.crystaleye.CrystalEyeConstants.RSS_ATOMS_DIR_NAME;
import static uk.ac.cam.ch.crystaleye.CrystalEyeConstants.RSS_BOND_DIR_NAME;
import static uk.ac.cam.ch.crystaleye.CrystalEyeConstants.RSS_CLASS_DIR_NAME;
import static uk.ac.cam.ch.crystaleye.CrystalEyeConstants.RSS_DESC_VALUE_PREFIX;
import static uk.ac.cam.ch.crystaleye.CrystalEyeConstants.RSS_DIR_NAME;
import static uk.ac.cam.ch.crystaleye.CrystalEyeConstants.RSS_JOURNAL_DIR_NAME;
import static uk.ac.cam.ch.crystaleye.CrystalEyeConstants.WEBPAGE;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.NodeFactory;
import nu.xom.Nodes;
import nu.xom.ParsingException;
import nux.xom.io.StaxParser;
import nux.xom.io.StaxUtil;

import org.xmlcml.cif.CIFUtil;
import org.xmlcml.cml.base.CMLConstants;
import org.xmlcml.cml.base.CMLRuntimeException;
import org.xmlcml.cml.element.CMLAtom;
import org.xmlcml.cml.element.CMLBond;
import org.xmlcml.cml.element.CMLCml;
import org.xmlcml.cml.element.CMLMolecule;
import org.xmlcml.molutil.ChemicalElement;

import uk.ac.cam.ch.crystaleye.AbstractManager;
import uk.ac.cam.ch.crystaleye.CrystalEyeUtils;
import uk.ac.cam.ch.crystaleye.IOUtils;
import uk.ac.cam.ch.crystaleye.IssueDate;
import uk.ac.cam.ch.crystaleye.Utils;
import uk.ac.cam.ch.crystaleye.CrystalEyeUtils.CompoundClass;
import uk.ac.cam.ch.crystaleye.properties.SiteProperties;
import uk.ac.cam.ch.crystaleye.site.feeds.CMLRSSEntry.FeedType;
import uk.ac.cam.ch.crystaleye.templates.feeds.Atom1;
import uk.ac.cam.ch.crystaleye.templates.feeds.Rss1;
import uk.ac.cam.ch.crystaleye.templates.feeds.Rss2;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndLinkImpl;

public class RssManager extends AbstractManager implements CMLConstants {

	private SiteProperties properties;

	String publisherAbbreviation;
	String publisherTitle;
	String journalAbbreviation;
	String journalTitle;
	String year;
	String issueNum;

	String rootFeedsDir;
	String rootWebFeedsDir;
	String[] feedTypes;
	String[] urlSafeFeedTypes;
	String summaryWriteDir;
	String webSummaryWriteDir;

	String webJournalDirPath;

	String author = "Chris Talbot";

	public RssManager() {
		;
	}

	public RssManager(File propertiesFile) {
		this.setProperties(propertiesFile);
	}

	public RssManager(String propertiesPath) {
		this(new File(propertiesPath));
	}

	private void setProperties(File propertiesFile) {
		properties = new SiteProperties(propertiesFile);

		rootFeedsDir = properties.getRssWriteDir();
		rootWebFeedsDir = properties.getRootWebFeedsDir();
		feedTypes = properties.getFeedTypes();
		urlSafeFeedTypes = properties.getUrlSafeFeedTypes();
		summaryWriteDir = properties.getSummaryWriteDir();
		webSummaryWriteDir = properties.getWebSummaryWriteDir();		
	}

	public void execute() {
		String[] publisherAbbreviations = properties.getPublisherAbbreviations();
		for (String publisherAbbreviation : publisherAbbreviations) {
			String[] journalAbbreviations = properties.getPublisherJournalAbbreviations(publisherAbbreviation);
			String[] journalTitles = properties.getPublisherJournalTitles(publisherAbbreviation);
			int count = 0;
			for (String journalAbbreviation : journalAbbreviations) {
				this.publisherTitle = properties.getPublisherTitle(publisherAbbreviation);
				this.journalTitle = journalTitles[count];
				String downloadLogPath = properties.getDownloadLogPath();
				List<IssueDate> unprocessedDates = this.getUnprocessedDates(downloadLogPath, publisherAbbreviation, journalAbbreviation, RSS, WEBPAGE);
				if (unprocessedDates.size() != 0) {
					for (IssueDate date : unprocessedDates) {
						this.publisherAbbreviation = publisherAbbreviation;
						this.journalAbbreviation = journalAbbreviation;
						this.year = date.getYear();
						this.issueNum = date.getIssue();
						String issueSummaryWriteDir = Utils.convertFileSeparators(summaryWriteDir+File.separator+
								this.publisherAbbreviation+File.separator+this.journalAbbreviation+
								File.separator+year+File.separator+issueNum);
						this.process(issueSummaryWriteDir);
						updateProps(downloadLogPath, publisherAbbreviation, journalAbbreviation, year, issueNum, RSS);
					}
				} else {
					System.out.println("No dates to process at this time for "+this.publisherTitle+", "+this.journalTitle);
				}
				count++;
			}
		}
	}

	public void process(String issueWriteDir) {
		List<File> fileList = new ArrayList<File>();
		if (new File(issueWriteDir).exists()) {
			fileList = CrystalEyeUtils.getSummaryDirFileList(issueWriteDir, "[^\\._]*_[^\\.]*"+COMPLETE_CML_MIME_REGEX);
			if (fileList.size() > 0) {
				webJournalDirPath = webSummaryWriteDir+"/"+publisherAbbreviation+"/"+journalAbbreviation+"/"+year+"/"+issueNum;
				updateJournalCmlrssFeeds(fileList);
				updateAllOtherRSSAndCmlrssFeeds(fileList);
			}
		}
	}	

	private void updateJournalCmlrssFeeds(List<File> cmlFileList) {
		for (int i = 0; i < feedTypes.length; i++) {
			String feedType = urlSafeFeedTypes[i];
			String prefix = "/"+RSS_JOURNAL_DIR_NAME+"/"+publisherAbbreviation+"/"+journalAbbreviation+"/"+CMLRSS_DIR_NAME+"/"+feedType+"/"+"feed.xml";
			String cmlRssUrl = rootWebFeedsDir+prefix;
			String cmlRssWritePath = rootFeedsDir+prefix;
			String archiveUrl = rootFeedsDir+"/"+RSS_JOURNAL_DIR_NAME+"/"+publisherAbbreviation+"/"+journalAbbreviation+"/"+CMLRSS_DIR_NAME+"/"+feedType;
			System.out.println("Creating new Journal RSS feed at "+cmlRssWritePath);
			this.createNewJournalCmlrssFeed(cmlFileList, cmlRssUrl, cmlRssWritePath, archiveUrl, feedTypes[i]);
		}
	}

	private void createNewJournalCmlrssFeed(List<File> cmlFileList, String cmlRssUrl, String cmlRssWritePath, String archiveUrl, String feedType) {
		/*
		 *  don't do this any more, just write over the old journal feeds
		try {
			archiveCmlRssFeed(cmlRssWritePath, archiveUrl);
		} catch (Exception e) {
			throw new CrystalEyeRuntimeException("Problem archiving CMLRSS feed.", e);
		}
		 */

		String feedTitle = "CrystalEye CMLRSS: "+this.publisherTitle+", "+this.journalTitle;
		String feedDescription = "CrystalEye CMLRSS: "+this.publisherTitle+", "+this.journalTitle+", "+year+", "+issueNum;

		Document feedDoc = null;
		FeedType type = getFeedType(feedType);
		try {
			if (type.equals(FeedType.ATOM_1)) {
				feedDoc = new Atom1(feedTitle, feedDescription, author, cmlRssUrl).getFeed();
			} else if (type.equals(FeedType.RSS_1)) {
				feedDoc = new Rss1(feedTitle, feedDescription, author, cmlRssUrl).getFeed();
			} else if (type.equals(FeedType.RSS_2)) {
				feedDoc = new Rss2(feedTitle, feedDescription, author, cmlRssUrl).getFeed();
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new CMLRuntimeException("Problem parsing feed.");
		}
		IOUtils.writeXML(feedDoc, cmlRssWritePath);

		List<CMLRSSEntryDetails> credList = new LinkedList<CMLRSSEntryDetails>();
		for (File cmlFile : cmlFileList) {
			// set feed entries 
			System.out.println("Reading "+cmlFile.getAbsolutePath()+" whilst creating a new Journal RSS feed.");

			Document doc = null;
			try {
				doc = IOUtils.parseCmlFile(cmlFile);
			} catch (Exception e) {
				System.err.println("CRYSTALEYE ERROR: whilst reading CML file: "+cmlFile.getAbsolutePath());
				continue;
			}
			CMLCml cml = (CMLCml)doc.getRootElement();
			Nodes nodes = cml.query("//cml:scalar[@dictRef=\"idf:doi\"]", CML_XPATH);
			String doi = "";
			if (nodes.size() != 0) {
				doi = nodes.get(0).getValue();
			}

			String title = CrystalEyeUtils.getStructureTitleFromTOC(cmlFile);
			if (title.equals("")) {
				title = CrystalEyeUtils.getStructureTitleFromCml(cml);
			}

			String filePath = cmlFile.getAbsolutePath();
			String fileName = filePath.substring(filePath.lastIndexOf(File.separator)+1);
			String cifId = fileName.substring(0,fileName.indexOf("."));
			String cifName = cifId.substring(0, cifId.lastIndexOf("_"));
			cifName = cifName.replaceAll("sup[\\d*]", "");
			String blockId = cifId.substring(cifId.lastIndexOf("_")+1);
			String articleId = cmlFile.getParentFile().getParentFile().getName();

			String description = "CML generated from DataBlock "+blockId+" in CIF "+cifName.toUpperCase()+" (DOI:"+doi+") from issue "+issueNum+"/"+year+" of "+publisherTitle+", "+journalTitle+".";
			String htmlLink = webJournalDirPath+"/data/"+articleId+"/"+cifId+"/"+cifId+".cif.summary.html";
			String cmlLink = webJournalDirPath+"/data/"+articleId+"/"+cifId+"/"+cifId+COMPLETE_CML_MIME;

			credList.add(new CMLRSSEntryDetails(title, author, htmlLink, description, htmlLink, cmlLink, cmlFile.getAbsolutePath(), htmlLink));		
		}
		new CMLRSSHandler(cmlRssWritePath, type, credList).addEntries();
	}

	private void updateAllOtherRSSAndCmlrssFeeds(List<File> fileList) {
		Map<String, List<File>> allMap = new HashMap<String, List<File>>();
		Map<String, List<File>> atomMap = new HashMap<String, List<File>>();
		Map<String, List<File>> bondMap = new HashMap<String, List<File>>();
		Map<String, List<File>> classMap = new HashMap<String, List<File>>();

		for (File cmlFile : fileList) {
			System.out.println("Updating RSS feeds from CML file "+cmlFile.getAbsolutePath());
			
			CMLCml cml = null;
			try {
				cml = (CMLCml)IOUtils.parseCmlFile(cmlFile).getRootElement();
			} catch (Exception e) {
				System.err.println("CRYSTALEYE ERROR: whilst reading CML file: "+cmlFile.getAbsolutePath());
				continue;
			}
			CMLMolecule molecule = (CMLMolecule)cml.getFirstCMLChild(CMLMolecule.TAG);

			// process atoms RSS and CMLRSS
			Set<ChemicalElement> ceSet = new HashSet<ChemicalElement>();
			for (CMLAtom atom : molecule.getAtoms()) {
				ceSet.add(atom.getChemicalElement());
			}
			for (ChemicalElement ce : ceSet) {
				String symbol = ce.getSymbol();
				String[] dontDo = {"H", "C", "N", "O"};
				boolean skip = false;
				for (String d : dontDo) {
					if (d.equals(symbol)) skip = true;
				}
				if (!skip) {
					if (atomMap.containsKey(symbol)) {
						List<File> list = atomMap.get(symbol);
						list.add(cmlFile);
						atomMap.put(symbol, list);
					} else {
						List<File> list = new ArrayList<File>();
						list.add(cmlFile);
						atomMap.put(symbol, list);
					}
				}
			}

			// process bonds
			Set<String> bondIdSet = new HashSet<String>();
			for (CMLBond bond : molecule.getBonds()) {
				bondIdSet.add(getBondElementsString(bond));
			}
			for (String bondId : bondIdSet) {
				String[] dontDo = {"H-C", "C-C", "H-N", "C-N", "C-O", "H-O"};
				boolean skip = false;
				for (String d : dontDo) {
					if (bondId.equals(d)) skip = true;
				}
				if (!skip) {
					if (bondMap.containsKey(bondId)) {
						List<File> list = bondMap.get(bondId);
						list.add(cmlFile);
						bondMap.put(bondId, list);
					} else {
						List<File> list = new ArrayList<File>();
						list.add(cmlFile);
						bondMap.put(bondId, list);
					}
				}
			}

			// process classes
			Nodes classNodes = cml.query(".//cml:scalar[@dictRef='iucr:compoundClass']", CML_XPATH);
			String className = null;
			if (classNodes.size() == 1) {
				className = classNodes.get(0).getValue();
			} else if (classNodes.size() > 1) {
				throw new CMLRuntimeException("Structure should only have one class node: "+cmlFile.getAbsolutePath());
			} else if (classNodes.size() == 0) {
				throw new CMLRuntimeException("Structure should have a class node: "+cmlFile.getAbsolutePath());
			}
			if (classMap.containsKey(className)) {
				List<File> list = classMap.get(className);
				list.add(cmlFile);
				classMap.put(className, list);
			} else {
				List<File> list = new ArrayList<File>();
				list.add(cmlFile);
				classMap.put(className, list);
			}
		}
		allMap.put("all", fileList);
		allMap.put("journal", fileList);

		updateOtherFeeds(allMap);
		updateOtherFeeds(atomMap);
		updateOtherFeeds(bondMap);
		updateOtherFeeds(classMap);
	}

	private String getBondElementsString(CMLBond bond) {
		List<CMLAtom> atoms = bond.getAtoms();
		CMLAtom atom0 = atoms.get(0);
		CMLAtom atom1 = atoms.get(1);
		int a0 = atom0.getAtomicNumber();
		int a1 = atom1.getAtomicNumber();
		String type0 = atom0.getElementType();
		String type1 = atom1.getElementType();;
		String elementTypes = "";
		if (a0 > a1) {
			elementTypes = type1+"-"+type0;
		} else {
			elementTypes = type0+"-"+type1;
		}

		return elementTypes;
	}

	private void updateOtherFeeds(Map<String, List<File>> map) {
		for (Iterator it = map.entrySet().iterator(); it.hasNext(); ) {
			List<CMLRSSEntryDetails> credList = new ArrayList<CMLRSSEntryDetails>(map.size());
			List<SyndEntry> entryList = new ArrayList<SyndEntry>(map.size());
			Map.Entry mapEntry = (Map.Entry)it.next();
			String id = (String)mapEntry.getKey();
			List<File> cmlFileList = (List<File>)mapEntry.getValue();

			for (File cmlFile : cmlFileList) {
				CMLCml cml = null;
				try {
					cml = (CMLCml)IOUtils.parseCmlFile(cmlFile).getRootElement();
				} catch (Exception e) {
					System.err.println("CRYSTALEYE ERROR: whilst reading CML file: "+cmlFile.getAbsolutePath());
					continue;
				}
				String cifId = cmlFile.getParentFile().getName();
				String blockId = cifId.substring(cifId.lastIndexOf("_")+1);
				String cifName = cifId.substring(0, cifId.lastIndexOf("_"));
				cifName = cifName.replaceAll("sup[\\d*]", "");	
				String articleId = cmlFile.getParentFile().getParentFile().getName();

				Nodes nodes = cml.query(".//cml:scalar[@dictRef=\"idf:doi\"]", CML_XPATH);
				String doi = "";
				if (nodes.size() != 0) {
					doi = nodes.get(0).getValue();
				}						

				String title = CrystalEyeUtils.getStructureTitleFromTOC(cmlFile);
				if (title.equals("")) {
					title = CrystalEyeUtils.getStructureTitleFromCml(cml);
				}

				String rssDescValue = RSS_DESC_VALUE_PREFIX+"DataBlock "+blockId+" in CIF "+cifName.toUpperCase()+" (DOI:"+doi+") from issue "+issueNum+"/"+year+" of "+publisherTitle+", "+journalTitle+".";
				String cmlrssDescValue = CMLRSS_DESC_VALUE_PREFIX+"DataBlock "+blockId+" in CIF "+cifName.toUpperCase()+" (DOI:"+doi+") from issue "+issueNum+"/"+year+" of "+publisherTitle+", "+journalTitle+".";
				String entryLink = webJournalDirPath+"/data/"+articleId+"/"+cifId+"/"+cifId+".cif.summary.html";
				String cmlLink = webJournalDirPath+"/data/"+articleId+"/"+cifId+"/"+cifId+COMPLETE_CML_MIME;

				CMLRSSEntryDetails cred = new CMLRSSEntryDetails(title, author, entryLink, cmlrssDescValue, entryLink, cmlLink, cmlFile.getAbsolutePath(), entryLink);
				credList.add(cred);
				List<SyndLinkImpl> otherLinks = new ArrayList<SyndLinkImpl>(2);
				SyndLinkImpl cmlLinkImpl = new SyndLinkImpl();
				cmlLinkImpl.setHref(cmlLink);
				cmlLinkImpl.setHreflang("en");
				cmlLinkImpl.setRel("enclosure");
				otherLinks.add(cmlLinkImpl);
				if ("".equals(title)) { 
					title = "No title supplied";
				}
				SyndEntry entry = RSSHandler.createEntry(title, entryLink, otherLinks, rssDescValue, author);
				entryList.add(entry);
			}
			for (int i = 0; i < feedTypes.length; i++) {
				String feedType = urlSafeFeedTypes[i];
				FeedType type = getFeedType(feedTypes[i]);
				String rssFeedPostfix = "";
				String cmlrssFeedPostfix = "";
				if (id.length() <= 2) {
					rssFeedPostfix = "/"+RSS_ATOMS_DIR_NAME+"/"+id+"/"+RSS_DIR_NAME+"/"+feedType+"/"+FEED_FILE_NAME;
					cmlrssFeedPostfix = "/"+RSS_ATOMS_DIR_NAME+"/"+id+"/"+CMLRSS_DIR_NAME+"/"+feedType+"/"+FEED_FILE_NAME;
				} else if (id.contains("-")) {
					rssFeedPostfix = "/"+RSS_BOND_DIR_NAME+"/"+id+"/"+RSS_DIR_NAME+"/"+feedType+"/"+FEED_FILE_NAME;
					cmlrssFeedPostfix = "/"+RSS_BOND_DIR_NAME+"/"+id+"/"+CMLRSS_DIR_NAME+"/"+feedType+"/"+FEED_FILE_NAME;
				} else if (id.equals(CompoundClass.ORGANIC.toString()) || id.equals(CompoundClass.INORGANIC.toString()) || 
						id.equals(CompoundClass.ORGANOMETALLIC.toString())) {
					rssFeedPostfix = "/"+RSS_CLASS_DIR_NAME+"/"+id+"/"+RSS_DIR_NAME+"/"+feedType+"/"+FEED_FILE_NAME;
					cmlrssFeedPostfix = "/"+RSS_CLASS_DIR_NAME+"/"+id+"/"+CMLRSS_DIR_NAME+"/"+feedType+"/"+FEED_FILE_NAME;
				} else if ("journal".equals(id)) {
					rssFeedPostfix = "/"+RSS_JOURNAL_DIR_NAME+"/"+publisherAbbreviation+"/"+journalAbbreviation+"/"+RSS_DIR_NAME+"/"+feedType+"/"+FEED_FILE_NAME;
				} else if ("all".equals(id)) {
					rssFeedPostfix = "/"+RSS_ALL_DIR_NAME+"/"+RSS_DIR_NAME+"/"+feedType+"/"+FEED_FILE_NAME;
				} else {
					System.out.println("id: "+id);
					throw new RuntimeException("Should never reach here.");
				}	
				String cmlrssPath = rootFeedsDir+cmlrssFeedPostfix;
				String rssPath = rootFeedsDir+rssFeedPostfix;
				if (!"journal".equals(id) && !"all".equals(id)) {
					System.out.println("Updating feed: "+cmlrssPath);
					new CMLRSSHandler(cmlrssPath, type, credList).addEntries();
				}
				System.out.println("Updating feed: "+rssPath);
				new RSSHandler(rssPath, entryList).addEntries();
			}
		}

	}

	private FeedType getFeedType(String feedType) {
		if ("rss_1.0".equals(feedType)) {
			return FeedType.RSS_1;
		} else if ("rss_2.0".equals(feedType)) {
			return FeedType.RSS_2;
		} else if ("atom_1.0".equals(feedType)) {
			return FeedType.ATOM_1;
		} else {
			throw new CMLRuntimeException("RSS type "+feedType+" not supported.");
		}
	}

	public static void main(String[] args) {
		//RssManager rss = new RssManager("e:/crystaleye-test/docs/cif-flow-props.txt");
		RssManager rss = new RssManager("e:/data-test/docs/cif-flow-props.txt");
		rss.execute();
	}
}
