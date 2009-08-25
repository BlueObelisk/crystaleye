package wwmm.crystaleye.site.feeds;

import static wwmm.crystaleye.CrystalEyeConstants.COMPLETE_CML_MIME;
import static wwmm.crystaleye.CrystalEyeConstants.COMPLETE_CML_MIME_REGEX;
import static wwmm.crystaleye.CrystalEyeConstants.FEED_FILE_NAME;
import static wwmm.crystaleye.CrystalEyeConstants.RSS;
import static wwmm.crystaleye.CrystalEyeConstants.RSS_ALL_DIR_NAME;
import static wwmm.crystaleye.CrystalEyeConstants.RSS_ATOMS_DIR_NAME;
import static wwmm.crystaleye.CrystalEyeConstants.RSS_BOND_DIR_NAME;
import static wwmm.crystaleye.CrystalEyeConstants.RSS_CLASS_DIR_NAME;
import static wwmm.crystaleye.CrystalEyeConstants.RSS_DESC_VALUE_PREFIX;
import static wwmm.crystaleye.CrystalEyeConstants.RSS_DIR_NAME;
import static wwmm.crystaleye.CrystalEyeConstants.RSS_JOURNAL_DIR_NAME;
import static wwmm.crystaleye.CrystalEyeConstants.WEBPAGE;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nu.xom.Nodes;

import org.apache.log4j.Logger;
import org.xmlcml.cif.CIFUtil;
import org.xmlcml.cml.base.CMLConstants;
import org.xmlcml.cml.element.CMLAtom;
import org.xmlcml.cml.element.CMLBond;
import org.xmlcml.cml.element.CMLCml;
import org.xmlcml.cml.element.CMLMolecule;
import org.xmlcml.molutil.ChemicalElement;

import wwmm.crystaleye.AbstractManager;
import wwmm.crystaleye.CrystalEyeUtils;
import wwmm.crystaleye.IOUtils;
import wwmm.crystaleye.IssueDate;
import wwmm.crystaleye.Utils;
import wwmm.crystaleye.CrystalEyeUtils.CompoundClass;
import wwmm.crystaleye.properties.SiteProperties;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndLinkImpl;

public class RSSManager extends AbstractManager implements CMLConstants {
	
	public enum FeedType {
		ATOM_1,
		RSS_1,
		RSS_2;
	}
	
	private static final Logger LOG = Logger.getLogger(RSSManager.class);

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

	public RSSManager() {
		;
	}

	public RSSManager(File propertiesFile) {
		this.setProperties(propertiesFile);
	}

	public RSSManager(String propertiesPath) {
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
					LOG.info("No dates to process at this time for "+this.publisherTitle+", "+this.journalTitle);
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
				updateAllOtherRSSFeeds(fileList);
			}
		}
	}	

	private void updateAllOtherRSSFeeds(List<File> fileList) {
		Map<String, List<File>> allMap = new HashMap<String, List<File>>();
		Map<String, List<File>> atomMap = new HashMap<String, List<File>>();
		Map<String, List<File>> bondMap = new HashMap<String, List<File>>();
		Map<String, List<File>> classMap = new HashMap<String, List<File>>();

		for (File cmlFile : fileList) {		
			CMLCml cml = null;
			try {
				cml = (CMLCml)IOUtils.parseCmlFile(cmlFile).getRootElement();
			} catch (Exception e) {
				System.err.println("CRYSTALEYE ERROR: whilst reading CML file: "+cmlFile.getAbsolutePath());
				continue;
			}
			CMLMolecule molecule = (CMLMolecule)cml.getFirstCMLChild(CMLMolecule.TAG);

			// process atoms RSS
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
			Nodes classNodes = cml.query(".//cml:scalar[@dictRef='iucr:compoundClass']", X_CML);
			String className = null;
			if (classNodes.size() == 1) {
				className = classNodes.get(0).getValue();
			} else if (classNodes.size() > 1) {
				throw new RuntimeException("Structure should only have one class node: "+cmlFile.getAbsolutePath());
			} else if (classNodes.size() == 0) {
				throw new RuntimeException("Structure should have a class node: "+cmlFile.getAbsolutePath());
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

				Nodes nodes = cml.query(".//cml:scalar[@dictRef=\"idf:doi\"]", X_CML);
				String doi = "";
				if (nodes.size() != 0) {
					doi = nodes.get(0).getValue();
				}						
				Nodes titleNodes = cml.query(".//cml:scalar[@dictRef='iucr:_publ_section_title']", X_CML);
				String title = "";
				if (titleNodes.size() != 0) {
					title = titleNodes.get(0).getValue();
					title = cifTitle2String(title);
				}

				String rssDescValue = RSS_DESC_VALUE_PREFIX+"DataBlock "+blockId+" in CIF "+cifName.toUpperCase()+" (DOI:"+doi+") from issue "+issueNum+"/"+year+" of "+publisherTitle+", "+journalTitle+".";
				String entryLink = webJournalDirPath+"/data/"+articleId+"/"+cifId+"/"+cifId+".cif.summary.html";
				String cmlLink = webJournalDirPath+"/data/"+articleId+"/"+cifId+"/"+cifId+COMPLETE_CML_MIME;

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
				if (id.length() <= 2) {
					rssFeedPostfix = "/"+RSS_ATOMS_DIR_NAME+"/"+id+"/"+RSS_DIR_NAME+"/"+feedType+"/"+FEED_FILE_NAME;
				} else if (id.contains("-")) {
					rssFeedPostfix = "/"+RSS_BOND_DIR_NAME+"/"+id+"/"+RSS_DIR_NAME+"/"+feedType+"/"+FEED_FILE_NAME;
				} else if (id.equals(CompoundClass.ORGANIC.toString()) || id.equals(CompoundClass.INORGANIC.toString()) || 
						id.equals(CompoundClass.ORGANOMETALLIC.toString())) {
					rssFeedPostfix = "/"+RSS_CLASS_DIR_NAME+"/"+id+"/"+RSS_DIR_NAME+"/"+feedType+"/"+FEED_FILE_NAME;
				} else if ("journal".equals(id)) {
					rssFeedPostfix = "/"+RSS_JOURNAL_DIR_NAME+"/"+publisherAbbreviation+"/"+journalAbbreviation+"/"+RSS_DIR_NAME+"/"+feedType+"/"+FEED_FILE_NAME;
				} else if ("all".equals(id)) {
					rssFeedPostfix = "/"+RSS_ALL_DIR_NAME+"/"+RSS_DIR_NAME+"/"+feedType+"/"+FEED_FILE_NAME;
				} else {
					throw new RuntimeException("Should never reach here.");
				}	
				String rssPath = rootFeedsDir+rssFeedPostfix;
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
			throw new RuntimeException("RSS type "+feedType+" not supported.");
		}
	}

	private String cifTitle2String(String title) {
		title = CIFUtil.translateCIF2ISO(title);
		title = title.replaceAll("\\\\", "");

		String patternStr = "\\^(\\d+)\\^";
		String replaceStr = "<sup>$1</sup>";
		Pattern pattern = Pattern.compile(patternStr);
		Matcher matcher = pattern.matcher(title);
		title = matcher.replaceAll(replaceStr);
		return title;
	}

	public static void main(String[] args) {
		//RssManager rss = new RssManager("e:/crystaleye-test/docs/cif-flow-props.txt");
		RSSManager rss = new RSSManager("e:/data-test/docs/cif-flow-props.txt");
		rss.execute();
	}
}
