package wwmm.crystaleye.managers;

import static org.xmlcml.cml.base.CMLConstants.CML_NS;
import static org.xmlcml.cml.base.CMLConstants.CML_XPATH;
import static wwmm.crystaleye.CrystalEyeConstants.CML2FOO;
import static wwmm.crystaleye.CrystalEyeConstants.COMPLETE_CML_MIME;
import static wwmm.crystaleye.CrystalEyeConstants.COMPLETE_CML_MIME_REGEX;
import static wwmm.crystaleye.CrystalEyeConstants.POLYMERIC_FLAG_DICTREF;
import static wwmm.crystaleye.CrystalEyeConstants.WEBPAGE;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Nodes;
import nu.xom.XPathContext;

import org.apache.commons.httpclient.URIException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.xmlcml.cif.CIFUtil;
import org.xmlcml.cml.converters.cif.CrystalEyeUtils.FragmentType;
import org.xmlcml.cml.element.CMLArray;
import org.xmlcml.cml.element.CMLArrayList;
import org.xmlcml.cml.element.CMLAtom;
import org.xmlcml.cml.element.CMLCml;
import org.xmlcml.cml.element.CMLCrystal;
import org.xmlcml.cml.element.CMLFormula;
import org.xmlcml.cml.element.CMLMetadata;
import org.xmlcml.cml.element.CMLMolecule;
import org.xmlcml.cml.element.CMLMolecule.HydrogenControl;
import org.xmlcml.cml.element.CMLScalar;
import org.xmlcml.cml.element.CMLSymmetry;
import org.xmlcml.cml.element.CMLTable;
import org.xmlcml.cml.element.CMLTable.TableType;
import org.xmlcml.cml.element.CMLTableRowList;
import org.xmlcml.cml.tools.DisorderTool;
import org.xmlcml.cml.tools.MoleculeTool;
import org.xmlcml.cml.tools.TableRowListTool;
import org.xmlcml.cml.tools.TableTool;

import wwmm.crystaleye.AbstractManager;
import wwmm.crystaleye.CrystalEyeJournals;
import wwmm.crystaleye.IssueDate;
import wwmm.crystaleye.JournalDetails;
import wwmm.crystaleye.site.templates.CifSummaryToc;
import wwmm.crystaleye.site.templates.FragmentSummaryToc;
import wwmm.crystaleye.site.templates.MoietySummaryToc;
import wwmm.crystaleye.site.templates.SingleCifSummary;
import wwmm.crystaleye.site.templates.SingleStructureSummary;
import wwmm.crystaleye.tools.SiteCreation;
import wwmm.crystaleye.util.CMLUtils;
import wwmm.crystaleye.util.ChemistryUtils;
import wwmm.crystaleye.util.ChemistryUtils.CompoundClass;
import wwmm.crystaleye.util.CrystalEyeUtils;
import wwmm.crystaleye.util.CrystalEyeUtils.DisorderType;
import wwmm.crystaleye.util.FreemarkerUtils;
import wwmm.crystaleye.util.Utils;
import wwmm.pubcrawler.core.CrawlerHttpClient;
import freemarker.template.SimpleHash;
import freemarker.template.SimpleSequence;
import freemarker.template.TemplateHashModel;

public class WebpageManager extends AbstractManager {

	private static final Logger LOG = Logger.getLogger(WebpageManager.class);

	private String writeDir;
	private String publisherAbbreviation;
	private String publisherTitle;
	private String journalAbbreviation;
	private String journalTitle;
	private String year;
	private String issueNum;

	private String jmolLoadForSummary;
	private String imageLoadForSummary;
	private int summaryRowCount;
	private int structureCount;
	private int maxImageForSummary;

	private String jmolLoadForFrags;
	private String imageLoadForFrags;
	private int fragRowCount;
	private int fragCount;

	private String jmolLoadForMois;
	private String imageLoadForMois;
	private int moiRowCount;
	private int moiCount;

	public WebpageManager() {
		;
	}

	public WebpageManager(File propertiesFile) {
		this.setProperties(propertiesFile);
	}

	public void execute() {
		String processLogPath = properties.getProcessLogPath();
		writeDir = properties.getCifDir();
		String webDir = properties.getWebDir();
		SiteCreation siteTool = new SiteCreation(new File(webDir));
		siteTool.createWebsite();
		for (JournalDetails journalDetails : new CrystalEyeJournals().getDetails()) {
			publisherAbbreviation = journalDetails.getPublisherAbbreviation();
			journalAbbreviation = journalDetails.getJournalAbbreviation();
			journalTitle = journalDetails.getJournalTitle();
			publisherTitle = journalDetails.getPublisherTitle();
			List<IssueDate> unprocessedDates = this.getUnprocessedDates(processLogPath, publisherAbbreviation, journalAbbreviation, WEBPAGE, CML2FOO);
			if (unprocessedDates.size() != 0) {
				for (IssueDate date : unprocessedDates) {
					String issueWriteDir = FilenameUtils.separatorsToUnix(writeDir+"/"+
							publisherAbbreviation+"/"+journalAbbreviation+
							"/"+date.getYear()+"/"+date.getIssue());
					this.process(issueWriteDir, publisherAbbreviation, journalAbbreviation, date.getYear(), date.getIssue());
					updateProcessLog(processLogPath, publisherAbbreviation, journalAbbreviation, year, issueNum, WEBPAGE);
				}
			} else {
				LOG.info("No dates to process at this time for "+publisherTitle+", "+journalTitle);
			}
		}
	}

	public void process(String issueWriteDir, String publisherAbbreviation, String journalAbbreviation, String year, String issue) {
		jmolLoadForSummary = "";
		imageLoadForSummary = "";
		summaryRowCount = 0;
		structureCount = 0;
		maxImageForSummary = 0;

		jmolLoadForFrags = "";
		imageLoadForFrags = "";
		fragRowCount = 0;
		fragCount = 0;

		jmolLoadForMois = "";
		imageLoadForMois = "";
		moiRowCount = 0;
		moiCount = 0;

		this.publisherAbbreviation = publisherAbbreviation;
		this.journalAbbreviation = journalAbbreviation;
		this.year = year;
		this.issueNum = issue;
		if (!new File(issueWriteDir).exists()) {
			LOG.warn("Could not find expected issue directory at: "+issueWriteDir);
			return;
		}
		String issueSummaryDir = properties.getSummaryDir()+"/"+publisherAbbreviation+"/"+journalAbbreviation+"/"+year+"/"+issueNum+"/";
		String issueDataDir = issueSummaryDir+"data/";

		for (File file : new File (issueWriteDir).listFiles()) {
			if (file.isDirectory()) {
				try {
					LOG.info("Copying webpage directory ("+file+") to "+issueDataDir);
					FileUtils.copyDirectoryToDirectory(file, new File(issueDataDir));
				} catch (IOException e) {
					LOG.warn("Problem copying directory: "+e.getMessage());
				}
			}
		}

		List<File> fileList = CrystalEyeUtils.getDataDirFileList(issueDataDir, "[^\\._]*_[^\\.]*"+COMPLETE_CML_MIME_REGEX);
		if (fileList.size() == 0) {
			LOG.warn("Probable bug, could not find any complete CML files in : "+issueDataDir);
			return;
		}
		for (File cmlFile : fileList ) {
			LOG.info("Creating webpages for CML file "+cmlFile.getAbsolutePath());
			// create moiety and fragment tocs
			this.createMoietyAndFragmentTocs(cmlFile);
			// create moiety and fragment summary html pages
			this.createMoietyAndFragmentHtmls(cmlFile);
			// create cif summaries
			this.createCifSummaries(cmlFile);
		}

		createTableOfContents(fileList, issueSummaryDir);
		updateSummaryLinkPage(properties.getSummaryDir());
	}

	private TemplateHashModel getTemplateMap(File journalDir) {
		SimpleHash templateMap = new SimpleHash();
		templateMap.put("pageTitle", "CrystalEye: Browse Structures");
		templateMap.put("publisherFullTitle", publisherTitle);
		templateMap.put("publisherAbbreviation", publisherAbbreviation);
		templateMap.put("journalFullTitle", journalTitle);
		templateMap.put("journalAbbreviation", journalAbbreviation);
		templateMap.put("pathToRoot", "../");
		templateMap.put("currentMenuSelected", "summary");

		SimpleSequence years = new SimpleSequence();
		List<File> yearDirs = Arrays.asList(journalDir.listFiles());
		Collections.reverse(yearDirs);
		for (File yearDir : yearDirs) {
			SimpleHash year = new SimpleHash();
			year.put("num", yearDir.getName());
			SimpleSequence issues = new SimpleSequence();
			List<File> issueDirs = Arrays.asList(yearDir.listFiles());
			for (File issueDir : issueDirs) {
				SimpleHash issue = new SimpleHash();
				issue.put("num", issueDir.getName());
				issues.add(issue);
			}
			year.put("issues", issues);
			years.add(year);
		}
		templateMap.put("years", years);
		return templateMap;
	}

	private void updateSummaryLinkPage(String summaryWriteDir) {
		String path = summaryWriteDir+"/"+publisherAbbreviation+"-"+journalAbbreviation+".html";
		//String journalDirPath =  writeDir+"/"+publisherAbbreviation+"/"+journalAbbreviation;
		String journalDirPath =  summaryWriteDir+"/"+publisherAbbreviation+"/"+journalAbbreviation;
		File journalDir = new File(journalDirPath);
		TemplateHashModel templateMap = getTemplateMap(journalDir);
		FreemarkerUtils.writeHtmlTemplate("journal-issue-index.ftl", new File(path), templateMap);
	}

	private void createTableOfContents(List<File> cmlFileList, String issueSummaryDir) {
		String entryLink = issueSummaryDir+"index.html";
		createOverallCifSummaryPage(entryLink, cmlFileList);
		this.getFilesForSummaryDisplay(cmlFileList, issueSummaryDir);
	}

	private void createCifSummaries(File cmlFile) {
		String cifParentPath = cmlFile.getParent();
		String cmlPath = cmlFile.getAbsolutePath();
		String fileName = cmlPath.substring(cmlPath.lastIndexOf(File.separator)+1);
		String id = fileName.substring(0,fileName.indexOf("."));
		String summaryPage = this.createSingleCifSummaryPage(cmlFile);
		if (summaryPage == null) {
			return;
		}
		Utils.writeText(new File(cifParentPath+"/"+id+".cif.summary.html"), summaryPage);
	}

	private void createMoietyAndFragmentTocs(File cmlFile) {
		String moiPagePath = Utils.getPathMinusMimeSet(cmlFile)+".moieties.toc.html";
		String moiPage = this.createOverallMoietySummaryPages(cmlFile);
		if (moiPage != null) {
			Utils.writeText(new File(moiPagePath), moiPage);
		}
	}

	private void createMoietyAndFragmentHtmls(File cmlFile) {
		File moietyParentFolder = new File(cmlFile.getParentFile(), "moieties");
		if (moietyParentFolder.exists()) {
			File[] moiFolders = moietyParentFolder.listFiles();
			for (File moiFolder : moiFolders) {
				if (moiFolder.isDirectory()) {
					File[] moiFiles = moiFolder.listFiles();
					for (File moiFile : moiFiles) {
						String path = moiFile.getAbsolutePath();
						if (path.matches("[^\\.]*"+COMPLETE_CML_MIME_REGEX)) {
							String fileName = path.substring(path.lastIndexOf(File.separator)+1);
							String id = fileName.substring(0,fileName.indexOf("."));
							String summaryPage = this.createSingleStructureSummary(moiFile, "Moiety Summary", 5);
							if (summaryPage == null) {
								continue;
							}
							Utils.writeText(new File(moiFolder+"/"+id+".moiety.summary.html"), summaryPage);
						}
					}
				}
			}
		}
	}

	private String createSingleStructureSummary(File structCmlFile, String title, int folderDepth) {
		String displayPathPrefix = "";
		for (int i = 0; i < folderDepth; i++) {
			displayPathPrefix += "../";
		}

		String cmlPath = structCmlFile.getAbsolutePath();
		CMLMolecule mol = null;
		try {
			mol = (CMLMolecule)Utils.parseCml(cmlPath).getRootElement();
		} catch(Exception e) {
			LOG.warn("Error parsing CML file ("+e.getMessage()+"), due to: "+e.getMessage());
		}
		if (mol == null) {
			return null;
		}
		String fileName = cmlPath.substring(cmlPath.lastIndexOf(File.separator)+1);
		String id = fileName.substring(0,fileName.indexOf("."));

		Nodes nodes = mol.query("//cml:identifier[@convention=\"iupac:inchi\"]", CML_XPATH);
		String inchi = "";
		if (nodes.size() != 0) {
			inchi = nodes.get(0).getValue();
			inchi = inchi.replaceAll("-", "-<wbr>");
		}
		String smiles = "";
		nodes = mol.query("./cml:identifier[@convention=\"daylight:smiles\"]", CML_XPATH);
		if (nodes.size() != 0) {
			smiles = nodes.get(0).getValue();
			smiles = smiles.replaceAll("\\)", ")<wbr>");
		}
		nodes = mol.query("//cml:scalar[@dictRef=\"idf:doi\"]", CML_XPATH);
		String doi = "";
		if (nodes.size() != 0) {
			doi = nodes.get(0).getValue();
		}
		if (doi.startsWith("http://dx.doi.org/")) {
			doi = doi.substring(18);
		}

		File newFile = structCmlFile;
		for (int i = 0; i < folderDepth-2; i++) {
			newFile = newFile.getParentFile();
		}	
		String prefix = newFile.getName()+".cif.summary.html";

		Nodes unprocNodes = mol.query(".//"+CMLScalar.NS+"[contains(@dictRef,'"+DisorderTool.UNRESOLVED_DISORDER_DICTREF+"')]", CML_XPATH);
		Nodes procNodes = mol.query(".//"+CMLScalar.NS+"[contains(@dictRef,'"+DisorderTool.RESOLVED_DISORDER_DICTREF+"')]", CML_XPATH);
		DisorderType disordered = null;
		if (unprocNodes.size() > 0) {
			disordered = DisorderType.UNPROCESSED;
		} else if (procNodes.size() > 0) {
			disordered = DisorderType.PROCESSED;
		} else {
			disordered = DisorderType.NONE;
		}

		String crystComp = createMoietyComponentsSection(structCmlFile, id);
		SingleStructureSummary scs = new SingleStructureSummary(publisherTitle, journalTitle, year, issueNum, doi, title, displayPathPrefix,
				id, crystComp, inchi, smiles, folderDepth, prefix, disordered);

		String page = scs.getWebPage() ;
		return page;
	}

	private String createSingleCifSummaryPage(File cmlFile) {
		String cmlPath = cmlFile.getAbsolutePath();
		String fileName = cmlPath.substring(cmlPath.lastIndexOf(File.separator)+1);
		String id = fileName.substring(0,fileName.indexOf("."));
		CMLCml cml = null;
		try {
			cml = (CMLCml)Utils.parseCml(cmlPath).getRootElement();
		} catch(Exception e) {
			LOG.warn("Cannot parse CML file ("+cmlFile+"), due to: "+e.getMessage());
		}
		if (cml == null) {
			return "";
		}
		Elements formulaElements = cml.getChildCMLElements(CMLFormula.TAG);
		String formulaMoi = "";
		String formulaSum = "";

		for (int i = 0; i < formulaElements.size(); i++) {
			CMLFormula formula = (CMLFormula) formulaElements.get(i);
			try {
				formula.normalize();
				if ("iucr:_chemical_formula_moiety".equalsIgnoreCase(formula.getDictRef())) {
					StringWriter sw = new StringWriter();
					formula.writeHTML(sw);
					formulaMoi = sw.toString();
					formulaMoi = formulaMoi.replaceAll("\\(", "<wbr />\\(");
					sw.close();
				}
			} catch (Exception e) {
				LOG.warn("Error writing formula moiety HTML: "+e.getMessage());
			}
			try {
				if ("iucr:_chemical_formula_sum".equalsIgnoreCase(formula.getDictRef())) {
					StringWriter sw = new StringWriter();
					formula.writeHTML(sw);
					formulaSum = sw.toString();
					formulaSum = formulaSum.replaceAll("\\(", "<wbr />\\(");
					sw.close();
				}
			} catch (IOException e) {
				LOG.warn("Error writing formula sum HTML: "+e.getMessage());
			}
		}

		Nodes doiNodes = cml.query("//cml:scalar[@dictRef=\"idf:doi\"]", CML_XPATH);
		String doi = "";
		if (doiNodes.size() != 0) {
			doi = doiNodes.get(0).getValue();
		}
		if (doi.startsWith("http://dx.doi.org/")) {
			doi = doi.substring(18);
		}
		String inchi = "";
		Nodes molNodes = cml.query("./cml:molecule", CML_XPATH);
		if (molNodes.size() > 0) {
			CMLMolecule mol = (CMLMolecule)molNodes.get(0);
			molNodes = mol.query("./cml:identifier[@convention=\"iupac:inchi\"]", CML_XPATH);
			if (molNodes.size() != 0) {
				inchi = molNodes.get(0).getValue();
				inchi = inchi.replaceAll("-", "-<wbr>");
			}
		}
		String smiles = "";
		Nodes molNodes2 = cml.query("./cml:molecule", CML_XPATH);
		if (molNodes2.size() > 0) {
			CMLMolecule mol = (CMLMolecule)molNodes2.get(0);
			molNodes2 = mol.query("./cml:identifier[@convention=\"daylight:smiles\"]", CML_XPATH);
			if (molNodes2.size() != 0) {
				smiles = molNodes2.get(0).getValue();
				smiles = smiles.replaceAll("\\)", ")<wbr>");
			}
		}
		Nodes titleNodes = cml.query(".//cml:scalar[@dictRef=\"iucr:_publ_section_title\"]", CML_XPATH);
		String title = "";
		try {
			if (titleNodes.size() != 0) {
				title = titleNodes.get(0).getValue();
				title = CIFUtil.translateCIF2ISO(title);
				title = title.replaceAll("\\\\", "");

				String patternStr = "\\^(\\d+)\\^";
				String replaceStr = "<sup>$1</sup>";
				Pattern pattern = Pattern.compile(patternStr);
				Matcher matcher = pattern.matcher(title);
				title = matcher.replaceAll(replaceStr);

				patternStr = "~(\\d+)~";
				replaceStr = "<sub>$1</sub>";
				pattern = Pattern.compile(patternStr);
				matcher = pattern.matcher(title);
				title = matcher.replaceAll(replaceStr);
			}
		} catch (Exception e) {
			LOG.warn("Could not translate CIF string to ISO: "+title);
			title = "";
		}

		String contactAuthor = "";
		Nodes authorNodes = cml.query(".//cml:scalar[contains(@dictRef,'iucr:_publ_contact_author_name')]", CML_XPATH);
		if (authorNodes.size() != 0) {
			contactAuthor = authorNodes.get(0).getValue();
		}
		String authorEmail = "";
		Nodes emailNodes = cml.query(".//cml:scalar[contains(@dictRef,'iucr:_publ_contact_author_email')]", CML_XPATH);
		if (emailNodes.size() != 0) {
			authorEmail = emailNodes.get(0).getValue();
		}

		String compoundClass = "";
		Nodes classNodes = cml.query(".//cml:scalar[contains(@dictRef,'iucr:compoundClass')]", CML_XPATH);
		if (classNodes.size() != 0) {
			compoundClass = classNodes.get(0).getValue();
		}

		String cellSetting = "";
		Nodes cellNodes = cml.query(".//cml:scalar[contains(@dictRef,'iucr:_symmetry_cell_setting')]", CML_XPATH);
		if (cellNodes.size() != 0) {
			cellSetting = cellNodes.get(0).getValue();
		}
		Nodes crystalNodes = cml.query(".//cml:crystal", CML_XPATH);
		String groupHM = "";
		if (crystalNodes.size() == 1) {
			CMLCrystal crystal = (CMLCrystal)crystalNodes.get(0);
			CMLSymmetry symmetry = (CMLSymmetry)crystal.getFirstCMLChild(CMLSymmetry.TAG);
			groupHM = symmetry.getSpaceGroup();
		}
		String groupHall = "";
		Nodes hallNodes = cml.query(".//cml:scalar[contains(@dictRef,'iucr:_symmetry_space_group_name_hall')]", CML_XPATH);
		if (hallNodes.size() != 0) {
			groupHall = hallNodes.get(0).getValue();
		}
		String temp = "";
		Nodes tempNodes = cml.query(".//cml:scalar[contains(@dictRef,'iucr:_cell_measurement_temperature')]", CML_XPATH);
		if (tempNodes.size() != 0) {
			temp = tempNodes.get(0).getValue();
		}
		String dateRecorded = "";
		Nodes dateNodes = cml.query(".//cml:scalar[contains(@dictRef,'iucr:_journal_date_recd_electronic')]", CML_XPATH);
		if (dateNodes.size() != 0) {
			dateRecorded = dateNodes.get(0).getValue();
		}
		String rObs = "";
		Nodes rFGTNodes = cml.query(".//cml:scalar[contains(@dictRef,'iucr:_refine_ls_r_factor_gt')]", CML_XPATH);
		if (rFGTNodes.size() != 0) {
			rObs = rFGTNodes.get(0).getValue();
		}
		String rAll = "";
		Nodes rFAllNodes = cml.query(".//cml:scalar[contains(@dictRef,'iucr:_refine_ls_r_factor_all')]", CML_XPATH);
		if (rFAllNodes.size() != 0) {
			rAll = rFAllNodes.get(0).getValue();
		}
		String wRObs = "";
		Nodes WRGTNodes = cml.query(".//cml:scalar[contains(@dictRef,'iucr:_refine_ls_wr_factor_gt')]", CML_XPATH);
		if (WRGTNodes.size() != 0) {
			wRObs = WRGTNodes.get(0).getValue();
		}
		String wRAll = "";
		Nodes wRAllNodes = cml.query(".//"+CMLScalar.NS+"[contains(@dictRef,'iucr:_refine_ls_wr_factor_ref')]", CML_XPATH);
		if (wRAllNodes.size() != 0) {
			wRAll = wRAllNodes.get(0).getValue();
		}	
		String crystComp = createCrystalComponentsSection(cmlFile, id);

		Nodes unprocNodes = cml.query(".//"+CMLScalar.NS+"[contains(@dictRef,'"+DisorderTool.UNRESOLVED_DISORDER_DICTREF+"')]", CML_XPATH);
		Nodes procNodes = cml.query(".//"+CMLScalar.NS+"[contains(@dictRef,'"+DisorderTool.RESOLVED_DISORDER_DICTREF+"')]", CML_XPATH);
		DisorderType disordered = null;
		if (unprocNodes.size() > 0) {
			disordered = DisorderType.UNPROCESSED;
		} else if (procNodes.size() > 0) {
			disordered = DisorderType.PROCESSED;
		} else {
			disordered = DisorderType.NONE;
		}

		boolean isPolymeric = false;
		Nodes polymericNodes = cml.query(".//"+CMLMetadata.NS+"[@dictRef='"+
				POLYMERIC_FLAG_DICTREF+"']", CML_XPATH);
		if (polymericNodes.size() > 0) {
			isPolymeric = true;
		}

		// note that for CIFs from Elsevier, we cannot create a link to original CIF as
		// it is in the form of a zip file.
		String originalCifUrl = "";
		if ("acta".equals(publisherAbbreviation)) {
			String originalId = id.substring(0,id.indexOf("_"));
			originalCifUrl = "http://scripts.iucr.org/cgi-bin/sendcif?"+originalId;
		} else if ("rsc".equals(publisherAbbreviation)) {
			String prefix = "http://pubs.rsc.org/suppdata";
			String yearId = id.substring(0,2);
			String originalId = id.substring(0,id.indexOf("_"));
			originalId = originalId.replaceAll("sup[\\d]*", "");
			originalCifUrl = prefix+"/"+journalAbbreviation+"/"+yearId+"/"+originalId+"/"+originalId+".txt";
		} else if ("acs".equals(publisherAbbreviation)) {
			String originalId = id.replaceAll("sup[\\d]*", "");
			String prefix = "http://pubs3.acs.org/acs/journals/supporting_information.page?in_manuscript=";
			originalCifUrl = prefix+originalId;
		} else if ("chemSocJapan".equals(publisherAbbreviation)) {
			//TODO - how to create the original CIF link here?
			String prefix = "http://www.jstage.jst.go.jp/article/cl/36/4/36_510/_appendix/1";
			originalCifUrl = "";
		}

		boolean notAllowedToRecommunicateCif = "acs".equals(publisherAbbreviation) || "elsevier".equals(publisherAbbreviation);

		SingleCifSummary scs = new SingleCifSummary(publisherTitle, journalTitle, year, issueNum, title,
				id, contactAuthor, authorEmail, doi, compoundClass,
				dateRecorded, formulaSum, formulaMoi, cellSetting, 
				groupHM, groupHall, temp, rObs, rAll, wRObs, wRAll,
				crystComp, inchi, smiles, disordered, isPolymeric, originalCifUrl, notAllowedToRecommunicateCif);
		String page = scs.getWebpage();

		return page;
	}

	private String createMoietyComponentsSection(File file, String id) {
		String section = "";
		String path = file.getAbsolutePath();
		String minusMime = path.substring(0, path.indexOf(COMPLETE_CML_MIME));
		boolean frags = false;
		if (new File(minusMime+".fragments.toc.html").exists()) {
			frags = true;
		}
		if (frags) {
			section = 	"<tr>"+	
			"<td colspan=\"2\">"+
			"<h3>Moiety Components</h3>"+
			"</td>"+
			"</tr>"+
			"<tr>"+
			"<td colspan=\"2\" bgcolor=\"#99ddff\">"+
			"<a href=\"./"+id+".fragments.toc.html\">"+
			"Fragments"+
			"</a>"+
			"</td>"+
			"</tr>";
		}	
		return section;
	}

	private String createCrystalComponentsSection(File file, String id) {
		String section = "";
		String path = file.getAbsolutePath();
		String minusMime = path.substring(0, path.indexOf(COMPLETE_CML_MIME));
		boolean mois = false;
		if (new File(minusMime+".moieties.toc.html").exists()) {
			mois = true;
		}
		if (mois) {
			section = 	"<tr>"+	
			"<td colspan=\"2\">"+
			"<h3>Crystal Components</h3>"+
			"</td>"+
			"</tr>"+
			"<tr>"+
			"<td colspan=\"2\" bgcolor=\"#99ddff\">"+
			"<a href=\"./"+id+".moieties.toc.html\">"+
			"Moieties"+
			"</a>"+
			"</td>"+
			"</tr>";
		}				
		return section;
	}

	private String createOverallMoietySummaryPages(File cmlFile) {
		// reset this for each summary page
		this.moiRowCount = 0;
		List<File> moiList = new ArrayList<File>();

		File parent = cmlFile.getParentFile();
		String moiFolderName = parent+"/"+"moieties";
		File moiParentFolder = new File(moiFolderName);
		File[] moiFolders = moiParentFolder.listFiles();
		if (moiFolders != null) {
			for (File moiFolder : moiFolders) {
				if (moiFolder.isDirectory()) {
					// carry on processing moieties
					File[] files = moiFolder.listFiles();
					for (File file : files) {
						if (file.getAbsolutePath().matches("[^\\.]*"+COMPLETE_CML_MIME_REGEX)) {
							moiList.add(file);
						}	
					}
				}
			}
		}
		// if no moieties have been found then return null
		if (moiList.size() == 0) {
			return null;
		}
		for (File moiCmlFile : moiList) {
			//			process fragments
			String fragPagePath = Utils.getPathMinusMimeSet(moiCmlFile)+".fragments.toc.html";
			String fragPage = this.createOverallFragmentSummaryPages(moiCmlFile);
			if (fragPage != null) {
				Utils.writeText(new File(fragPagePath), fragPage);
			}
			for (FragmentType fragType : FragmentType.values()) {
				String name = fragType.toString();
				String fragFolder = moiCmlFile.getParentFile().getAbsolutePath()+"/"+"fragments"+"/"+name;
				File fragFile = new File(fragFolder);
				if (fragFile.exists()) {
					File[] files = fragFile.listFiles();
					for (File file : files) {
						String path = file.getAbsolutePath();
						if (path.matches("[^\\.]*"+COMPLETE_CML_MIME_REGEX)) {
							String fileName = path.substring(path.lastIndexOf(File.separator)+1);
							String id = fileName.substring(0,fileName.indexOf("."));
							String summaryPage = this.createSingleStructureSummary(file, "Fragment Summary", 7);
							Utils.writeText(new File(fragFolder+"/"+id+".fragment.summary.html"), summaryPage);
						}
					}
				}
			}
		}
		moiCount = 0;
		StringBuffer sb = new StringBuffer();
		if (moiList.size() > 0) {
			String moiTable = createOverallMoietyTable(moiList);
			sb.append(moiTable);
		}
		String moiContent = sb.toString();
		MoietySummaryToc oms = new MoietySummaryToc(moiContent, String.valueOf(moiCount), jmolLoadForMois, imageLoadForMois);
		return oms.getWebpage();
	}

	private String createOverallFragmentSummaryPages(File cmlFile) {
		// reset this for each summary page
		this.fragRowCount = 0;

		List<File> ringNucList = new ArrayList<File>();
		List<File> ringSp1List = new ArrayList<File>();
		List<File> ringSp2List = new ArrayList<File>();
		List<File> ligandList = new ArrayList<File>();
		List<File> atomNucList = new ArrayList<File>();
		List<File> atomSp1List = new ArrayList<File>();
		List<File> atomSp2List = new ArrayList<File>();
		List<File> chainList = new ArrayList<File>();
		List<File> clusterList = new ArrayList<File>();
		List<File> clusterSp1List = new ArrayList<File>();
		List<File> clusterSp2List = new ArrayList<File>();

		Set<String> inchiSet = new HashSet<String>(); 

		File moietyFolder = cmlFile.getParentFile();
		File fragmentParentFolder = new File(moietyFolder, "fragments");
		if (fragmentParentFolder.exists()) {
			for (FragmentType fragType : FragmentType.values()) {
				String fragmentName = fragType.toString();
				File fragFolder = new File(fragmentParentFolder, fragmentName);
				if (fragFolder.exists()) {
					File[] files = fragFolder.listFiles();
					for (File file : files) {
						if (file.getAbsolutePath().matches("[^\\.]*"+COMPLETE_CML_MIME_REGEX)) {
							Document doc = null;
							try {
								doc = Utils.parseCml(file);
							} catch (Exception e) {
								LOG.warn("Error parsing CML file: "+e.getMessage());
							}
							if (doc == null) {
								continue;
							}
							CMLMolecule molecule = (CMLMolecule)doc.getRootElement();
							Nodes inchis = molecule.query("//cml:identifier[@convention='iupac:inchi']", CML_XPATH);
							if (inchis.size() > 0) {
								String inchi = ((Element)inchis.get(0)).getValue();
								if (!inchiSet.contains(inchi)) {
									inchiSet.add(inchi);
									if ("ligand".equalsIgnoreCase(fragmentName)) ligandList.add(file);
									if ("ring-nuc".equalsIgnoreCase(fragmentName)) ringNucList.add(file);
									if ("ring-nuc-sprout-1".equalsIgnoreCase(fragmentName)) ringSp1List.add(file);
									if ("ring-nuc-sprout-2".equalsIgnoreCase(fragmentName)) ringSp2List.add(file);
									if ("atom-nuc".equalsIgnoreCase(fragmentName)) atomNucList.add(file);
									if ("atom-nuc-sprout-1".equalsIgnoreCase(fragmentName)) atomSp1List.add(file);
									if ("atom-nuc-sprout-2".equalsIgnoreCase(fragmentName)) atomSp2List.add(file);
									if ("chain-nuc".equalsIgnoreCase(fragmentName)) chainList.add(file);
									if ("cluster-nuc".equalsIgnoreCase(fragmentName)) clusterList.add(file);
									if ("cluster-nuc-sprout-1".equalsIgnoreCase(fragmentName)) clusterSp1List.add(file);
									if ("cluster-nuc-sprout-2".equalsIgnoreCase(fragmentName)) clusterSp2List.add(file);
								}
							}
						}	
					}
				}
			}	
		}

		// if no fragments have been found then return null
		if (ringNucList.size() == 0 && ringSp1List.size() == 0 
				&& ringSp2List.size() == 0 && ligandList.size() == 0 
				&& atomNucList.size() == 0 && atomSp1List.size() == 0
				&& atomSp2List.size() == 0 && chainList.size() == 0
				&& clusterList.size() == 0 && clusterSp1List.size() == 0
				&& clusterSp2List.size() == 0) {
			return null;
		}

		fragCount = 0;
		StringBuffer sb = new StringBuffer();
		if (ligandList.size() > 0) {
			String ligandTable = createOverallFragmentTable(ligandList);
			sb.append("<p>Ligands</p>"+ligandTable);
		}
		if (ringNucList.size() > 0) {
			String ringTable = createOverallFragmentTable(ringNucList);
			sb.append("<p>Ring-nuclei</p>"+ringTable);
		}
		if (ringSp1List.size() > 0) {
			String ringTable = createOverallFragmentTable(ringSp1List);
			sb.append("<p>Ring-nuclei (sprouted once)</p>"+ringTable);
		}
		if (ringSp2List.size() > 0) {
			String ringTable = createOverallFragmentTable(ringSp2List);
			sb.append("<p>Ring-nuclei (sprouted twice)</p>"+ringTable);
		}
		if (atomNucList.size() > 0) {
			String atomTable = createOverallFragmentTable(atomNucList);
			sb.append("<p>Metal Centres</p>"+atomTable);
		}
		if (atomSp1List.size() > 0) {
			String atomTable = createOverallFragmentTable(atomSp1List);
			sb.append("<p>Metal Centres (sprouted once)</p>"+atomTable);
		}
		if (atomSp2List.size() > 0) {
			String atomTable = createOverallFragmentTable(atomSp2List);
			sb.append("<p>Metal Centres (sprouted twice)</p>"+atomTable);
		}
		if (clusterList.size() > 0) {
			String clusterTable = createOverallFragmentTable(clusterList);
			sb.append("<p>Metal Clusters</p>"+clusterTable);
		}
		if (clusterSp1List.size() > 0) {
			String clusterTable = createOverallFragmentTable(clusterList);
			sb.append("<p>Metal Clusters (sprouted once)</p>"+clusterTable);
		}
		if (clusterSp2List.size() > 0) {
			String clusterTable = createOverallFragmentTable(clusterList);
			sb.append("<p>Metal Clusters (sprouted twice)</p>"+clusterTable);
		}
		if (chainList.size() > 0) {
			String atomTable = createOverallFragmentTable(chainList);
			sb.append("<p>Chains</p>"+atomTable);
		}
		String fragmentContent = sb.toString();
		FragmentSummaryToc ofs = new FragmentSummaryToc(fragmentContent, String.valueOf(fragCount),
				jmolLoadForFrags, imageLoadForFrags);
		return ofs.getWebpage();
	}

	private String createOverallFragmentTable(List<File> fragFileList) {
		CMLTable table = new CMLTable();
		table.setTableType(TableType.COLUMN_BASED);
		int columns = 0;

		CMLArray formulaArray = new CMLArray();
		formulaArray.setTitle("Formula");
		formulaArray.setDelimiter("|");
		table.addArray(formulaArray);
		columns++;

		CMLArray summaryArray = new CMLArray();
		summaryArray.setTitle("Summary");
		summaryArray.setDelimiter("|");
		table.addArray(summaryArray);
		columns++;

		table.setColumns(columns);
		int rows = 0;

		for (File file : fragFileList) {
			addOverallFragmentRowValues(file, table, formulaArray, summaryArray);
			rows++;
		}

		table.setColumns(columns);
		StringWriter sw = new StringWriter();
		try {
			table.writeHTML(sw);
		} catch (IOException e) {
			LOG.warn("Exception whilst creating HTML: "+e.getMessage());
		}

		return sw.getBuffer().toString();
	}

	private String createOverallMoietyTable(List<File> moiFileList) {
		CMLTable table = new CMLTable();
		table.setTableType(TableType.COLUMN_BASED);
		int columns = 0;

		CMLArray formulaArray = new CMLArray();
		formulaArray.setTitle("Formula");
		formulaArray.setDelimiter("|");
		table.addArray(formulaArray);
		columns++;

		CMLArray summaryArray = new CMLArray();
		summaryArray.setTitle("Summary");
		summaryArray.setDelimiter("|");
		table.addArray(summaryArray);
		columns++;

		table.setColumns(columns);
		int rows = 0;

		for (File file : moiFileList) {
			addOverallMoietyRowValues(file, table, formulaArray, summaryArray);
			rows++;
		}

		table.setColumns(columns);
		StringWriter sw = new StringWriter();
		try {
			table.writeHTML(sw);
		} catch (IOException e) {
			LOG.warn("Exception whilst creating HTML: "+e.getMessage());
		}

		return sw.getBuffer().toString();
	}

	private void addOverallMoietyRowValues(File cmlFile, CMLTable table, CMLArray formulaArray, CMLArray summaryArray) {
		Document doc = null;
		try {
			doc = Utils.parseCml(cmlFile);
		} catch(Exception e) {
			LOG.warn("Error parsing CML file: "+e.getMessage());
		}
		if (doc == null) {
			return;
		}
		CMLMolecule mol = (CMLMolecule) doc.getRootElement();
		String moietyId = cmlFile.getParentFile().getName();

		StringWriter sw = new StringWriter();
		String formula = "";
		try {
			CMLFormula form = MoleculeTool.getOrCreateTool(mol).calculateFormula(HydrogenControl.USE_EXPLICIT_HYDROGENS);
			form.writeHTML(sw);
			formula = sw.toString();
		} catch (IOException e) {
			throw new RuntimeException("Unable to write formula HTML", e);
		}

		// check for disorder in the structure, if so then need to indicate this on the page
		Nodes unprocNodes = mol.query(".//"+CMLScalar.NS+"[contains(@dictRef,'"+DisorderTool.UNRESOLVED_DISORDER_DICTREF+"')]", CML_XPATH);
		Nodes procNodes = mol.query(".//"+CMLScalar.NS+"[contains(@dictRef,'"+DisorderTool.RESOLVED_DISORDER_DICTREF+"')]", CML_XPATH);
		if (unprocNodes.size() > 0) {
			formula += " <span style=\"color: red; font-size: 12px;\">((DU))</span>";
		} else if (procNodes.size() > 0) {
			formula += " <span style=\"color: green; font-size: 12px;\">((DP))</span>";
		}

		moiCount++;
		formulaArray.append("<a id=\""+moiCount+"\" href=\"javascript:showThisStructure"+
				"('./moieties/"+moietyId+"/"+moietyId+".small.png',1,'load./moieties/"+moietyId+"/"+moietyId+COMPLETE_CML_MIME+";',"+moiCount+");\">"+
				formula.replaceAll("\n", "")+"</a>");	
		summaryArray.append("<a style=\"text-decoration: underline;\" href=\"./moieties/"+moietyId+"/"+moietyId+".moiety.summary.html\">view</a>");
		if (moiRowCount == 0) {
			this.jmolLoadForMois = "\"load ./moieties/"+moietyId+"/"+moietyId+COMPLETE_CML_MIME+"\"";
			this.imageLoadForMois = "\"./moieties/"+moietyId+"/"+moietyId+".small.png\"";
		}
		moiRowCount++;
	}

	private void addOverallFragmentRowValues(File cmlFile, CMLTable table, CMLArray formulaArray, CMLArray summaryArray) {
		Document doc = null;
		try {
			doc = Utils.parseCml(cmlFile);
		} catch(Exception e) {
			LOG.warn("Error parsing CML file: "+e.getMessage());
		}
		if (doc == null) {
			return;
		}
		CMLMolecule mol = (CMLMolecule) doc.getRootElement();
		String cmlPath = cmlFile.getAbsolutePath();
		String fileName = cmlPath.substring(cmlPath.lastIndexOf(File.separator)+1);
		String fragId = fileName.substring(0,fileName.indexOf("."));
		String fragType = cmlFile.getParentFile().getName();

		StringWriter sw = new StringWriter();
		String formula = "";
		try {
			CMLFormula form = MoleculeTool.getOrCreateTool(mol).calculateFormula(HydrogenControl.USE_EXPLICIT_HYDROGENS);
			form.writeHTML(sw);
			formula = sw.toString();
		} catch (IOException e) {
			throw new RuntimeException("Unable to write formula HTML", e);
		}

		fragCount++;
		formulaArray.append("<a id=\""+fragCount+"\" href=\"javascript:showThisStructure"+
				"('./fragments/"+fragType+"/"+fragId+".small.png',1,'load./fragments/"+fragType+"/"+fragId+COMPLETE_CML_MIME+";',"+fragCount+");\">"+
				formula.replaceAll("\n", "")+"</a>");	
		summaryArray.append("<a style=\"text-decoration: underline;\" href=\"./fragments/"+fragType+"/"+fragId+".fragment.summary.html\">view</a>");
		if (fragRowCount == 0) {
			this.jmolLoadForFrags = "\"load ./fragments/"+fragType+"/"+fragId+COMPLETE_CML_MIME+"\"";
			this.imageLoadForFrags = "\"./fragments/"+fragType+"/"+fragId+".small.png\"";
		}
		fragRowCount++;
	}

	private void createOverallCifSummaryPage(String pagePath, List<File> cmlFileList) {
		List<File> organicList = new ArrayList<File>();
		List<File> organometallicList = new ArrayList<File>();
		List<File> inorganicList = new ArrayList<File>();
		for (File file : cmlFileList) {
			Document doc = null;
			try {
				doc = Utils.parseCml(file);
			} catch(Exception e) {
				LOG.warn("Cannot parse "+file.getAbsolutePath()+": "+e.getMessage());
			}
			if (doc == null) {
				continue;
			}
			XPathContext x = new XPathContext("x", CML_NS);
			Nodes nodes = doc.query("//x:scalar[@dictRef='iucr:compoundClass']", x);
			if (nodes.size() > 0) {

				String compClass = nodes.get(0).getValue();
				if (compClass.equalsIgnoreCase(CompoundClass.ORGANIC.toString())) {
					organicList.add(file);
				} else if (compClass.equalsIgnoreCase(CompoundClass.ORGANOMETALLIC.toString())) {
					organometallicList.add(file);
				} else if (compClass.equalsIgnoreCase(CompoundClass.INORGANIC.toString())) {
					inorganicList.add(file);
				}
			}
		}

		StringBuffer sb = new StringBuffer();
		if (organicList.size() > 0) {
			String organicTable = this.createOverallCifSummaryTable(organicList);
			sb.append("<p>Organic Structures</p>"+organicTable);
		}
		if (organometallicList.size() > 0) {
			String organometallicTable = this.createOverallCifSummaryTable(organometallicList);
			sb.append("<p>Organometallic Structures</p>"+organometallicTable);
		}
		if (inorganicList.size() > 0) {
			String inorganicTable = this.createOverallCifSummaryTable(inorganicList);
			sb.append("<p>Inorganic Structures</p>"+inorganicTable);
		}
		String table = sb.toString();
		String title = "Crystallography Summary";
		String header = publisherTitle+"<br />"+journalTitle+", "+year+", issue "+issueNum;
		CifSummaryToc ocs = new CifSummaryToc(title, header, table, String.valueOf(structureCount), jmolLoadForSummary, imageLoadForSummary, String.valueOf(maxImageForSummary), 0);	
		Utils.writeText(new File(pagePath), ocs.getWebpage());
	}

	private String createOverallCifSummaryTable(List<File> cmlFileList) {
		CMLTable table = new CMLTable();
		table.setTableType(TableType.COLUMN_BASED);
		int columns = 0;

		CMLArray formulaArray = new CMLArray();
		formulaArray.setTitle("Published Formula (clickable)");
		formulaArray.setDelimiter("|");
		table.addArray(formulaArray);
		columns++;

		CMLArray doiArray = new CMLArray();
		doiArray.setTitle("Article");
		doiArray.setDelimiter("|");
		table.addArray(doiArray);
		columns++;

		CMLArray summaryArray = new CMLArray();
		summaryArray.setTitle("Summary");
		summaryArray.setDelimiter("|");
		table.addArray(summaryArray);
		columns++;

		table.setColumns(columns);
		int rows = 0;

		for (File file : cmlFileList) {
			addOverallCifRowValues(file, table, formulaArray, doiArray, summaryArray);
			rows++;
		}

		table.setColumns(columns);

		CMLArrayList arrayList = new CMLArrayList();
		arrayList.addArray(formulaArray);
		arrayList.addArray(doiArray);
		arrayList.addArray(summaryArray);
		StringWriter sw = new StringWriter();
		try {
			sw.write("<table border='1'>");
			CMLTableRowList tableRowList = TableTool.createTableRowList(arrayList);
			new TableRowListTool(tableRowList).writeHTML(sw);
			sw.write("\n</table>");
		} catch (IOException e) {
			LOG.warn("Exception whilst creating HTML: "+e.getMessage());
		} finally {
			IOUtils.closeQuietly(sw);
		}

		return sw.getBuffer().toString();
	}

	private void addOverallCifRowValues(File cmlFile, CMLTable table, CMLArray formulaArray, CMLArray doiArray, CMLArray summaryArray) {		
		Document doc = null;
		try {
			doc = Utils.parseCml(cmlFile);
		} catch(Exception e) {
			LOG.warn("Error parsing CML file: "+e.getMessage());
		}
		CMLCml cml = (CMLCml) doc.getRootElement();
		String cmlPath = cmlFile.getAbsolutePath();
		String fileName = cmlPath.substring(cmlPath.lastIndexOf(File.separator)+1);
		String id = fileName.substring(0,fileName.indexOf("."));
		String articleId = cmlFile.getParentFile().getParentFile().getName();

		/*
		 * attempt to retrieve DOI from cml dom.
		 */
		XPathContext x = new XPathContext("x", CML_NS);
		Nodes doiNodes = cml.query(".//x:scalar[@dictRef='idf:doi']", x);
		String doiStr = "";
		boolean doi = false;
		if (doiNodes.size() > 0) {
			doi = true;
			doiStr = doiNodes.get(0).getValue();
		}
		if (doiStr.startsWith("http://dx.doi.org/")) {
			doiStr = doiStr.substring(18);
		}

		/*
		 * retrieve formula - AS PUBLISHED IN THE CIF - from the CML.
		 * if cannot find the chemical_formula_moiety then uses the chemical_formula_sum.
		 */ 
		Elements formulaElements = cml.getChildCMLElements(CMLFormula.TAG);
		String moietyS = ".";
		String sumS = ".";
		StringWriter sw = new StringWriter();
		for (int i = 0; i < formulaElements.size(); i++) {
			try {
				CMLFormula formula = (CMLFormula) formulaElements.get(i);
				if ("iucr:_chemical_formula_moiety".equalsIgnoreCase(formula.getDictRef())) {
					formula.writeHTML(sw);
					moietyS = sw.toString();
				}
				if ("iucr:_chemical_formula_sum".equalsIgnoreCase(formula.getDictRef())) {
					formula.writeHTML(sw);
					sumS = sw.toString();
				}
				sw.close();
			} catch (IOException e) {
				throw new RuntimeException("Problem writing HTML of formula.", e);
			}
		}
		if ("<span class='formula'>".equalsIgnoreCase(moietyS) ||
				moietyS == null || 
				"".equalsIgnoreCase(moietyS) ||
				".".equalsIgnoreCase(moietyS)) {
			moietyS = sumS;
		}

		// check for disorder in the structure, if so then need to indicate this on the page
		Nodes unprocNodes = cml.query(".//"+CMLScalar.NS+"[contains(@dictRef,'"+DisorderTool.UNRESOLVED_DISORDER_DICTREF+"')]", CML_XPATH);
		Nodes procNodes = cml.query(".//"+CMLScalar.NS+"[contains(@dictRef,'"+DisorderTool.RESOLVED_DISORDER_DICTREF+"')]", CML_XPATH);
		if (unprocNodes.size() > 0) {
			moietyS += " <span style=\"color: red; font-size: 12px;\">((DU))</span>";
		} else if (procNodes.size() > 0) {
			moietyS += " <span style=\"color: green; font-size: 12px;\">((DP))</span>";
		}

		String compoundClass = "";
		Nodes classNodes = cml.query(".//cml:scalar[contains(@dictRef,'iucr:compoundClass')]", CML_XPATH);
		if (classNodes.size() != 0) {
			compoundClass = classNodes.get(0).getValue();
		}
		boolean isPolymeric = false;
		Nodes polymericNodes = cml.query(".//"+CMLMetadata.NS+"[@dictRef='"+
				POLYMERIC_FLAG_DICTREF+"']", CML_XPATH);
		if (polymericNodes.size() > 0) {
			isPolymeric = true;
		}

		if(isPolymeric) {
			moietyS += " <span style=\"color: blue; font-size: 12px;\">[[P]]</span>";
		}
		if (compoundClass.equals(CompoundClass.INORGANIC.toString()) || isPolymeric) {
			structureCount++;
			formulaArray.append("<a id=\""+structureCount+"\" href=\"javascript:showThisStructure"+
					"('./display/placeholder.bmp',"+1+",'load./data/"+articleId+"/"+id+"/"+id+COMPLETE_CML_MIME+";',"+structureCount+");\">"+
					moietyS+"</a>");
			if (doi && !"acs".equals(publisherAbbreviation) && !"elsevier".equals(publisherAbbreviation)) {
				doiArray.append("<a style=\"text-decoration: underline;\" href=\"http://dx.doi.org/"+doiStr+"\">view</a>");
			} else {
				doiArray.append("N/A");
			}
			summaryArray.append("<a style=\"text-decoration: underline;\" href=\"./data/"+articleId+"/"+id+"/"+id+".cif.summary.html\">view</a>");
			if (summaryRowCount == 0) {
				this.jmolLoadForSummary = "\"load ./data/"+articleId+"/"+id+"/"+id+COMPLETE_CML_MIME+"; set unitcell on\"";
				this.imageLoadForSummary = "'./display/placeholder.bmp'";
				this.maxImageForSummary = 1;
			}
		} else {
			int maxImage = 0;
			for (CMLMolecule mo : CrystalEyeUtils.getUniqueSubMolecules((CMLMolecule)cml.getFirstCMLChild(CMLMolecule.TAG))) {
				try {
					if (ChemistryUtils.isBoringMolecule(mo)) {
						continue;
					}
				} catch (Exception e) {
					// in case errors due to atoms with zero occupancy
					// don't do anything - assume isn't boring
				}
				Nodes nonUnitOccNodes = mo.query(".//"+CMLAtom.NS+"[@occupancy[. < 1]]", CML_XPATH);
				if (DisorderTool.isDisordered(mo) || mo.hasCloseContacts() || nonUnitOccNodes.size() != 0 || !CMLUtils.hasBondOrdersAndCharges(mo)) {
					continue;
				}
				maxImage++;
			}
			structureCount++;
			if (maxImage == 0) {
				formulaArray.append("<a id=\""+structureCount+"\" href=\"javascript:showThisStructure"+
						"('./display/placeholder.bmp',1,'load./data/"+articleId+"/"+id+"/"+id+COMPLETE_CML_MIME+";',"+structureCount+");\">"+
						moietyS+"</a>");
			} else {
				formulaArray.append("<a id=\""+structureCount+"\" href=\"javascript:showThisStructure"+
						"('./data/"+articleId+"/"+id+"/"+id+"_1.small.png',"+maxImage+",'load./data/"+articleId+"/"+id+"/"+id+COMPLETE_CML_MIME+";',"+structureCount+");\">"+
						moietyS+"</a>");
			}
			if (doi && !"acs".equals(publisherAbbreviation) && !"elsevier".equals(publisherAbbreviation)) {
				doiArray.append("<a style=\"text-decoration: underline;\" href=\"http://dx.doi.org/"+doiStr+"\">view</a>");
			} else {
				doiArray.append("N/A");
			}
			summaryArray.append("<a style=\"text-decoration: underline;\" href=\"./data/"+articleId+"/"+id+"/"+id+".cif.summary.html\">view</a>");
			if (summaryRowCount == 0) {
				this.jmolLoadForSummary = "\"load ./data/"+articleId+"/"+id+"/"+id+COMPLETE_CML_MIME+"; set unitcell on\"";
				if (maxImage == 0) {
					this.imageLoadForSummary = "'./display/placeholder.bmp'";
				} else {
					this.imageLoadForSummary = "\"./data/"+articleId+"/"+id+"/"+id+"_1.small.png\"";
				}
				this.maxImageForSummary = maxImage;
			}
		}
		summaryRowCount++;
	}

	private void getFilesForSummaryDisplay(List<File> cmlFileList, String issueSummaryDir) {
		String dataDirPath = issueSummaryDir+"/data";
		String displayDirPath = issueSummaryDir+"/display";
		File dataDir = new File(dataDirPath);
		if (!dataDir.exists()) {
			dataDir.mkdirs();
		}
		File displayDir = new File(displayDirPath);
		if (!displayDir.exists()) {
			displayDir.mkdirs();
		}

		CrawlerHttpClient httpClient = new CrawlerHttpClient();
		httpClient.writeResourceToFile("http://wwmm.ch.cam.ac.uk/download/ned24/cifsummary/JmolApplet0.jar", new File(issueSummaryDir+"/"+"JmolApplet0.jar"));
		httpClient.writeResourceToFile("http://wwmm.ch.cam.ac.uk/download/ned24/cifsummary/JmolApplet1.jar", new File(issueSummaryDir+"/"+"JmolApplet1.jar"));
		httpClient.writeResourceToFile("http://wwmm.ch.cam.ac.uk/download/ned24/cifsummary/JmolApplet2.jar", new File(issueSummaryDir+"/"+"JmolApplet2.jar"));
		httpClient.writeResourceToFile("http://wwmm.ch.cam.ac.uk/download/ned24/cifsummary/JmolApplet3.jar", new File(issueSummaryDir+"/"+"JmolApplet3.jar"));
		httpClient.writeResourceToFile("http://wwmm.ch.cam.ac.uk/download/ned24/cifsummary/JmolApplet4.jar", new File(issueSummaryDir+"/"+"JmolApplet4.jar"));
		httpClient.writeResourceToFile("http://wwmm.ch.cam.ac.uk/download/ned24/cifsummary/JmolApplet5.jar", new File(issueSummaryDir+"/"+"JmolApplet5.jar"));
		httpClient.writeResourceToFile("http://wwmm.ch.cam.ac.uk/download/ned24/cifsummary/JmolApplet6.jar", new File(issueSummaryDir+"/"+"JmolApplet6.jar"));
		httpClient.writeResourceToFile("http://wwmm.ch.cam.ac.uk/download/ned24/cifsummary/summary.js", new File(issueSummaryDir+"/"+"summary.js"));
		httpClient.writeResourceToFile("http://wwmm.ch.cam.ac.uk/download/ned24/cifsummary/Jmol.js", new File(issueSummaryDir+"/"+"Jmol.js"));

		httpClient.writeResourceToFile("http://wwmm.ch.cam.ac.uk/download/ned24/cifsummary/eprints.css", new File(displayDirPath+"/"+"eprints.css"));
		httpClient.writeResourceToFile("http://wwmm.ch.cam.ac.uk/download/ned24/cifsummary/summary.css", new File(displayDirPath+"/"+"summary.css"));
		httpClient.writeResourceToFile("http://wwmm.ch.cam.ac.uk/download/ned24/cifsummary/top.gif", new File(displayDirPath+"/"+"top.gif"));
		httpClient.writeResourceToFile("http://wwmm.ch.cam.ac.uk/download/ned24/cifsummary/bonds.css", new File(displayDirPath+"/"+"bonds.css"));
		httpClient.writeResourceToFile("http://wwmm.ch.cam.ac.uk/download/ned24/cifsummary/fragsummary.css", new File(displayDirPath+"/"+"fragsummary.css"));
		httpClient.writeResourceToFile("http://wwmm.ch.cam.ac.uk/download/ned24/cifsummary/placeholder.bmp", new File(displayDirPath+"/"+"placeholder.bmp"));

		/*
		File display1 = new File("./src/main/resources/display1");
		File display2 = new File("./src/main/resources/display2");
		try {
			FileUtils.copyDirectory(display1, new File(issueSummaryDir));
			FileUtils.copyDirectory(display2, displayDir);
		} catch (IOException e1) {
			LOG.warn("Problem copying CrystalEye display files: "+e1.getMessage());
		}
		 */


	}

	public static void main(String[] args) {
		File propsFile = new File("c:/workspace/crystaleye-trunk-data/docs/cif-flow-props.txt");
		WebpageManager web = new WebpageManager(propsFile);
		web.execute();
	}

}
