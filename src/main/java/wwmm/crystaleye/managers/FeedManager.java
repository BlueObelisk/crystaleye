package wwmm.crystaleye.managers;

import static org.xmlcml.cml.base.CMLConstants.CML_XPATH;
import static wwmm.crystaleye.CrystalEyeConstants.ATOM_1_NS;
import static wwmm.crystaleye.CrystalEyeConstants.COMPLETE_CML_MIME;
import static wwmm.crystaleye.CrystalEyeConstants.COMPLETE_CML_MIME_REGEX;
import static wwmm.crystaleye.CrystalEyeConstants.FEED_FILE_NAME;
import static wwmm.crystaleye.CrystalEyeConstants.RSS;
import static wwmm.crystaleye.CrystalEyeConstants.RSS_ALL_DIR_NAME;
import static wwmm.crystaleye.CrystalEyeConstants.RSS_ATOMS_DIR_NAME;
import static wwmm.crystaleye.CrystalEyeConstants.RSS_BOND_DIR_NAME;
import static wwmm.crystaleye.CrystalEyeConstants.RSS_CLASS_DIR_NAME;
import static wwmm.crystaleye.CrystalEyeConstants.RSS_DESC_VALUE_PREFIX;
import static wwmm.crystaleye.CrystalEyeConstants.RSS_JOURNAL_DIR_NAME;
import static wwmm.crystaleye.CrystalEyeConstants.SMALL_PNG_MIME;
import static wwmm.crystaleye.CrystalEyeConstants.WEBPAGE;
import static wwmm.crystaleye.CrystalEyeConstants.XHTML_NS;

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

import nu.xom.Attribute;
import nu.xom.Element;
import nu.xom.Nodes;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.xmlcml.cif.CIFUtil;
import org.xmlcml.cml.element.CMLAtom;
import org.xmlcml.cml.element.CMLBond;
import org.xmlcml.cml.element.CMLCml;
import org.xmlcml.cml.element.CMLMolecule;
import org.xmlcml.molutil.ChemicalElement;

import wwmm.atomarchiver.AtomArchiveFeed;
import wwmm.crystaleye.AbstractManager;
import wwmm.crystaleye.CrystalEyeJournals;
import wwmm.crystaleye.IssueDate;
import wwmm.crystaleye.JournalDetails;
import wwmm.crystaleye.tools.FeedCreationTool;
import wwmm.crystaleye.util.ChemistryUtils.CompoundClass;
import wwmm.crystaleye.util.CrystalEyeUtils;
import wwmm.crystaleye.util.Utils;

public class FeedManager extends AbstractManager {

	private static final Logger LOG = Logger.getLogger(FeedManager.class);

	private String publisherAbbreviation;
	private String publisherTitle;
	private String journalAbbreviation;
	private String journalTitle;
	private String year;
	private String issueNum;

	private String feedDir;
	private String feedDirUrl;
	private String summaryDir;
	private String summaryDirUrl;
	private String webJournalDirPath;
	private String author = "Chris Talbot";

	private FeedManager() {
		;
	}

	public FeedManager(File propertiesFile) {
		this.setProperties(propertiesFile);
	}

	public void execute() {
		feedDir = properties.getFeedDir();
		feedDirUrl = properties.getFeedDirUrl();
		summaryDir = properties.getSummaryDir();
		summaryDirUrl = properties.getWebSummaryWriteDir();	
		
		File feedDirFile = new File(feedDir);
		if (feedDirFile.list().length < 2) {
			FeedCreationTool feedTool = new FeedCreationTool(feedDirFile, feedDirUrl);
			feedTool.createJournalRssFeeds();
			feedTool.createAtomsRssFeeds();
			feedTool.createBondRssFeeds();
			feedTool.createClassRssFeeds();
		}

		String processLogPath = properties.getProcessLogPath();
		for (JournalDetails journalDetails : new CrystalEyeJournals().getDetails()) {
			publisherAbbreviation = StringEscapeUtils.escapeHtml(journalDetails.getPublisherAbbreviation());
			journalAbbreviation = StringEscapeUtils.escapeHtml(journalDetails.getJournalAbbreviation());
			journalTitle = StringEscapeUtils.escapeHtml(journalDetails.getJournalTitle());
			publisherTitle = StringEscapeUtils.escapeHtml(journalDetails.getPublisherTitle());
			List<IssueDate> unprocessedDates = this.getUnprocessedDates(processLogPath, publisherAbbreviation, journalAbbreviation, RSS, WEBPAGE);
			if (unprocessedDates.size() != 0) {
				for (IssueDate date : unprocessedDates) {
					this.year = date.getYear();
					this.issueNum = date.getIssue();
					String issueSummaryWriteDir = FilenameUtils.separatorsToUnix(summaryDir+"/"+
							this.publisherAbbreviation+"/"+this.journalAbbreviation+
							"/"+year+"/"+issueNum);
					this.process(issueSummaryWriteDir);
					updateProcessLog(processLogPath, publisherAbbreviation, journalAbbreviation, year, issueNum, RSS);
				}
			} else {
				LOG.info("No dates to process at this time for "+this.publisherTitle+", "+this.journalTitle);
			}
		}
	}

	public void process(String issueWriteDir) {
		List<File> fileList = new ArrayList<File>();
		if (new File(issueWriteDir).exists()) {
			fileList = CrystalEyeUtils.getSummaryDirFileList(issueWriteDir, "[^\\._]*_[^\\.]*"+COMPLETE_CML_MIME_REGEX);
			if (fileList.size() > 0) {
				webJournalDirPath = summaryDirUrl+"/"+publisherAbbreviation+"/"+journalAbbreviation+"/"+year+"/"+issueNum;
				updateRSSFeeds(fileList);
			}
		}
	}	

	private void updateRSSFeeds(List<File> fileList) {
		Map<String, List<File>> allMap = new HashMap<String, List<File>>();
		Map<String, List<File>> atomMap = new HashMap<String, List<File>>();
		Map<String, List<File>> bondMap = new HashMap<String, List<File>>();
		Map<String, List<File>> classMap = new HashMap<String, List<File>>();

		for (File cmlFile : fileList) {		
			CMLCml cml = null;
			try {
				cml = (CMLCml)Utils.parseCml(cmlFile).getRootElement();
			} catch (Exception e) {
				LOG.warn("Exception whilst reading CML file ("+cmlFile.getAbsolutePath()+"), due to: "+e.getMessage());
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
			Nodes classNodes = cml.query(".//cml:scalar[@dictRef='iucr:compoundClass']", CML_XPATH);
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
			List<wwmm.atomarchiver.AtomEntry> entryList = new ArrayList<wwmm.atomarchiver.AtomEntry>(map.size());
			Map.Entry mapEntry = (Map.Entry)it.next();
			String id = (String)mapEntry.getKey();
			List<File> cmlFileList = (List<File>)mapEntry.getValue();

			for (File cmlFile : cmlFileList) {
				CMLCml cml = null;
				try {
					cml = (CMLCml)Utils.parseCml(cmlFile).getRootElement();
				} catch (Exception e) {
					LOG.warn("Exception whilst reading CML file ("+cmlFile.getAbsolutePath()+"), due to: "+e.getMessage());
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
				Nodes titleNodes = cml.query(".//cml:scalar[@dictRef='iucr:_publ_section_title']", CML_XPATH);
				String title = "";
				if (titleNodes.size() != 0) {
					title = titleNodes.get(0).getValue();
					title = cifTitle2String(title);
				}

				String rssDescValue = RSS_DESC_VALUE_PREFIX+"DataBlock "+blockId+" in CIF "+cifName.toUpperCase()+" (DOI:"+doi+") from issue "+issueNum+"/"+year+" of "+publisherTitle+", "+journalTitle+".";
				if ("".equals(title)) {
					title = rssDescValue;
				}

				String entryLink = webJournalDirPath+"/data/"+articleId+"/"+cifId+"/"+cifId+".cif.summary.html";
				String cmlLink = webJournalDirPath+"/data/"+articleId+"/"+cifId+"/"+cifId+COMPLETE_CML_MIME;

				wwmm.atomarchiver.AtomEntry entry = new wwmm.atomarchiver.AtomEntry();
				entry.setTitle(title);
				entry.setLinkUrl(entryLink);
				entry.setIdWithRandomUUID();
				entry.setSummary(rssDescValue);
				entry.setAuthor(author);
				wwmm.atomarchiver.AtomEnclosure cmlEnc = new wwmm.atomarchiver.AtomEnclosure();
				List<wwmm.atomarchiver.AtomEnclosure> encList = new ArrayList<wwmm.atomarchiver.AtomEnclosure>(1);
				cmlEnc.setUrl(cmlLink);
				cmlEnc.setLength((int)cmlFile.length());
				cmlEnc.setType("chemical/x-cml");
				cmlEnc.setTitle("Crystal structure data in CML");
				encList.add(cmlEnc);

				List<String> pngPathList = new ArrayList<String>();
				for (File f : cmlFile.getParentFile().listFiles()) {
					String pngPath = f.getAbsolutePath();
					if (pngPath.endsWith(SMALL_PNG_MIME)) {
						pngPathList.add(pngPath);
					}
				}
				if (pngPathList.size() > 0) {
					List<Element> cs = new ArrayList<Element>(1);
					Element content = createEntryHtmlContent(pngPathList);
					cs.add(content);
					entry.setOtherContent(cs);
				}
				for (String pngPath : pngPathList) {
					wwmm.atomarchiver.AtomEnclosure pngEnc = new wwmm.atomarchiver.AtomEnclosure();
					pngEnc.setUrl(dataPathToUrl(pngPath));
					pngEnc.setTitle("Moiety 2D structure diagram");
					pngEnc.setLength((int)new File(pngPath).length());
					pngEnc.setType("image/png");
					encList.add(pngEnc);
				}
				entry.setEnclosures(encList);
				entryList.add(entry);
			}

			String rssFeedPostfix = "";
			String feedTitle = "";
			String feedSubtitle = "CrystalEye: summarizing recently published crystallography.";
			if (id.length() <= 2) {
				rssFeedPostfix = "/"+RSS_ATOMS_DIR_NAME+"/"+id+"/"+FEED_FILE_NAME;
				feedTitle = getElementFeedTitle(id);
			} else if (id.contains("-")) {
				rssFeedPostfix = "/"+RSS_BOND_DIR_NAME+"/"+id+"/"+FEED_FILE_NAME;
				feedTitle = getBondFeedTitle(id);
			} else if (id.equals(CompoundClass.ORGANIC.toString()) || id.equals(CompoundClass.INORGANIC.toString()) || 
					id.equals(CompoundClass.ORGANOMETALLIC.toString())) {
				rssFeedPostfix = "/"+RSS_CLASS_DIR_NAME+"/"+id+"/"+FEED_FILE_NAME;
				feedTitle = getCompoundClassFeedTitle(id);
			} else if ("journal".equals(id)) {
				rssFeedPostfix = "/"+RSS_JOURNAL_DIR_NAME+"/"+publisherAbbreviation+"/"+journalAbbreviation+"/"+FEED_FILE_NAME;
				feedTitle = getJournalFeedTitle(publisherTitle, journalTitle);
			} else if ("all".equals(id)) {
				rssFeedPostfix = "/"+RSS_ALL_DIR_NAME+"/"+FEED_FILE_NAME;
				feedTitle = getAllFeedTitle();
			} else {
				throw new RuntimeException("BUG: should never reach here.");
			}	
			String feedFilepath = feedDir+rssFeedPostfix;
			File feedFile = new File(feedFilepath);
			String feedUrl = feedDirUrl+rssFeedPostfix;
			AtomArchiveFeed archiveFeed = new AtomArchiveFeed();
			archiveFeed.initFeedWithRandomUuidAsId(feedFile, feedUrl, feedTitle, feedSubtitle, author);
			archiveFeed.addEntries(feedFile, entryList);
		}

	}

	private Element createEntryHtmlContent(List<String> pngPathList) {
		Element content = new Element("content", ATOM_1_NS);
		content.addAttribute(new Attribute("type", "xhtml"));	
		Element div = new Element("div", XHTML_NS);
		content.appendChild(div);
		for (String s : pngPathList) {
			Element img = new Element("img", XHTML_NS);
			div.appendChild(img);
			img.addAttribute(new Attribute("src", dataPathToUrl(s)));
		}
		return content;
	}

	private String dataPathToUrl(String path) {
		String dir = properties.getSummaryDir();
		String webDir = properties.getWebSummaryWriteDir();
		dir = FilenameUtils.separatorsToUnix(dir);
		webDir = FilenameUtils.separatorsToUnix(webDir);
		path = FilenameUtils.separatorsToUnix(path);
		return path.replaceAll(dir, webDir);
	}

	private String getAllFeedTitle() {
		return "CrystalEye: all structures";
	}

	private String getJournalFeedTitle(String publisher, String journal) {
		return "CrystalEye: structures from "+publisher+", "+journal;
	}

	private String getCompoundClassFeedTitle(String clazz) {
		return "CrystalEye: "+clazz+" structures";
	}

	private String getElementFeedTitle(String element) {
		return "CrystalEye: structures containing "+element; 
	}

	private String getBondFeedTitle(String bond) {
		return "CrystalEye: Structures containing bonds of "+bond;
	}

	private String cifTitle2String(String title) {
		try {
			String isoTitle = CIFUtil.translateCIF2ISO(title);
			title = isoTitle;
		} catch (Exception e) {
			LOG.warn("Problem: "+e.getMessage());
		}
		title = title.replaceAll("\\\\", "");

		String patternStr = "\\^(\\d+)\\^";
		String replaceStr = "<sup>$1</sup>";
		Pattern pattern = Pattern.compile(patternStr);
		Matcher matcher = pattern.matcher(title);
		title = matcher.replaceAll(replaceStr);
		return title;
	}

	public static void main(String[] args) {
		File feedFile = new File("c:/workspace/archive-feed-test/feed.xml");
		
		AtomArchiveFeed archiveFeed = new AtomArchiveFeed();
		archiveFeed.initFeedWithRandomUuidAsId(feedFile, "http://random.com/feed.xml", StringEscapeUtils.escapeHtml("Journal of Chemical & Engineering"), "Feed subtitle here", "Me!");
		wwmm.atomarchiver.AtomEntry entry = new wwmm.atomarchiver.AtomEntry();
		List<wwmm.atomarchiver.AtomEntry> entryList = new ArrayList<wwmm.atomarchiver.AtomEntry>();
		entryList.add(entry);
		archiveFeed.addEntries(feedFile, entryList);
		
		System.out.println(StringEscapeUtils.escapeHtml("Journal of Chemical & Engineering"));
	}
}
