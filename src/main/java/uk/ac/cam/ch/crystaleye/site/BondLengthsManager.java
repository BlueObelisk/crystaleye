package uk.ac.cam.ch.crystaleye.site;

import static uk.ac.cam.ch.crystaleye.CrystalEyeConstants.BONDLENGTHS;
import static uk.ac.cam.ch.crystaleye.CrystalEyeConstants.COMPLETE_CML_MIME;
import static uk.ac.cam.ch.crystaleye.CrystalEyeConstants.COMPLETE_CML_MIME_REGEX;
import static uk.ac.cam.ch.crystaleye.CrystalEyeConstants.CSV_MIME;
import static uk.ac.cam.ch.crystaleye.CrystalEyeConstants.HTML_MIME;
import static uk.ac.cam.ch.crystaleye.CrystalEyeConstants.POLYMERIC_FLAG_DICTREF;
import static uk.ac.cam.ch.crystaleye.CrystalEyeConstants.SVG_MIME;
import static uk.ac.cam.ch.crystaleye.CrystalEyeConstants.WEBPAGE;
import static uk.ac.cam.ch.crystaleye.CrystalEyeConstants.X_SVG;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Node;
import nu.xom.Nodes;
import nu.xom.Text;

import org.graph.GraphException;
import org.graph.Point;
import org.graph.SVGElement;
import org.hist.Histogram;
import org.interpret.SVGInterpretter;
import org.layout.GraphLayout;
import org.xmlcml.cml.base.CMLConstants;
import org.xmlcml.cml.base.CMLUtil;
import org.xmlcml.cml.base.CMLElement.CoordinateType;
import org.xmlcml.cml.element.CMLAtom;
import org.xmlcml.cml.element.CMLBond;
import org.xmlcml.cml.element.CMLCml;
import org.xmlcml.cml.element.CMLFormula;
import org.xmlcml.cml.element.CMLMetadata;
import org.xmlcml.cml.element.CMLMolecule;
import org.xmlcml.cml.element.CMLScalar;
import org.xmlcml.cml.tools.CrystalTool;
import org.xmlcml.cml.tools.DisorderTool;
import org.xmlcml.cml.tools.MoleculeTool;
import org.xmlcml.euclid.Point3;

import uk.ac.cam.ch.crystaleye.AbstractManager;
import uk.ac.cam.ch.crystaleye.CrystalEyeRuntimeException;
import uk.ac.cam.ch.crystaleye.CrystalEyeUtils;
import uk.ac.cam.ch.crystaleye.IOUtils;
import uk.ac.cam.ch.crystaleye.IssueDate;
import uk.ac.cam.ch.crystaleye.Utils;
import uk.ac.cam.ch.crystaleye.CrystalEyeUtils.CompoundClass;
import uk.ac.cam.ch.crystaleye.process.Cif2CmlManager;
import uk.ac.cam.ch.crystaleye.properties.SiteProperties;
import uk.ac.cam.ch.crystaleye.templates.webpages.BondLengthElementIndex;
import uk.ac.cam.ch.crystaleye.templates.webpages.BondLengthIndex;
import uk.ac.cam.ch.crystaleye.templates.webpages.CifSummaryToc;

public class BondLengthsManager extends AbstractManager implements CMLConstants {

	private String temp = "";
	private String rf = "";
	private String doiStr = "";
	private String moietyS = "";
	private String compoundClass = "";
	private boolean isPolymeric = false;

	private final int LENGTH_COL = 0;
	private final int ID_COL = 1;
	private final int ATOMNOS_COL = 2;
	private final int TEMP_COL = 3;
	private final int RF_COL = 4;
	private final int DOI_COL = 5;
	private final int COMPCLASS_COL = 6;
	private final int FORMULA_COL = 7;
	private final int POLY_COL = 8;
	private final int UNIQUEMOL_COL = 9;

	public static final String AFTER_PROTOCOL = "-after-protocol";
	private final double PROTOCOL_MAX_TEMP = 200.0;
	private final double PROTOCOL_MAX_RF = 0.05;

	private SiteProperties properties;

	private Set<String> changedBonds;

	private String jmolLoadForSummary;
	private String imageLoadForSummary;
	private int summaryRowCount;
	private int structureCount;
	private int maxImageForSummary;

	String dNow;
	{
		Date date = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		dNow = formatter.format(date);
	}

	public BondLengthsManager(File propertiesFile) {
		this.setProperties(propertiesFile);
	}

	public BondLengthsManager(String propertiesPath) {
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
				List<IssueDate> unprocessedDates = this.getUnprocessedDates(downloadLogPath, publisherAbbreviation, journalAbbreviation, BONDLENGTHS, WEBPAGE);
				if (unprocessedDates.size() != 0) {
					for (IssueDate date : unprocessedDates) {
						String summaryWriteDir = properties.getSummaryWriteDir();
						String year = date.getYear();
						String issueNum = date.getIssue();
						String issueWriteDir = Utils.convertFileSeparators(summaryWriteDir+File.separator+
								publisherAbbreviation+File.separator+journalAbbreviation+File.separator+
								year+File.separator+issueNum);
						this.process(issueWriteDir);
						updateProps(downloadLogPath, publisherAbbreviation, journalAbbreviation, year, issueNum, BONDLENGTHS);
					}
				} else {
					System.out.println("No dates to process at this time for "+publisherAbbreviation+" journal "+journalAbbreviation);
				}
			}
		}

		generateProtocolBondLengthFiles(changedBonds);
		generateHistograms(changedBonds);
		generateHtmlLinkPages();
	}

	public void process(String issueWriteDir) {		
		List<File> fileList = new ArrayList<File>();
		changedBonds = new HashSet<String>();
		if (new File(issueWriteDir).exists()) {
			fileList = CrystalEyeUtils.getSummaryDirFileList(issueWriteDir, "[^\\._]*_[^\\.]*"+COMPLETE_CML_MIME_REGEX);
			if (fileList.size() > 0) {
				for (File cmlFile : fileList ) {
					try { 
						addLengthsFromCmlFile(cmlFile);
					} catch (OutOfMemoryError e) {
						System.err.println("Out of memory processing CML file: "+cmlFile.getAbsolutePath());
					}
				}
			}
		}
	}

	private void generateHtmlLinkPages() {
		String bondFolderPath = properties.getBondLengthsDir();
		List<File> csvList = new ArrayList<File>();
		for (File file : new File(bondFolderPath).listFiles()) {
			String filePath = file.getAbsolutePath();
			if (filePath.endsWith(CSV_MIME) && !filePath.contains(AFTER_PROTOCOL)) {
				csvList.add(file);
			}
		}

		Set<String> elements = new TreeSet<String>();
		Map<String, Set<String>> bondElementsMap = new HashMap<String, Set<String>>();
		for (File csvFile : csvList) {
			String csvName = csvFile.getName();
			int idx = csvName.indexOf(CSV_MIME);
			String symbols = csvName.substring(0,idx);
			String[] as = symbols.split("-");
			for (int i = 0; i < as.length; i++) {
				String element = as[i];
				elements.add(element);
				if (bondElementsMap.containsKey(element)) {
					Set<String> set = bondElementsMap.get(element);
					if (i == 0) {
						set.add(as[1]);
					} else if (i == 1) {
						set.add(as[0]);
					}
				} else {
					Set<String> set = new TreeSet<String>();
					if (i == 0) {
						set.add(as[1]);
					} else if (i == 1) {
						set.add(as[0]);
					}
					bondElementsMap.put(element, set);
				}
			}
		}

		BondLengthIndex bli = new BondLengthIndex(elements);
		String page = bli.getWebpage();
		String indexPath = bondFolderPath+File.separator+"index.html";
		IOUtils.writeText(page, indexPath);

		for (Iterator it = bondElementsMap.entrySet().iterator(); it.hasNext(); ) {
			Map.Entry entry = (Map.Entry)it.next();
			String element = (String)entry.getKey();
			Set<String> set = (Set<String>)entry.getValue();
			BondLengthElementIndex bei = new BondLengthElementIndex(bondFolderPath, element, set);
			String webpage = bei.getWebpage();
			String elementIndexPath = bondFolderPath+File.separator+element+"-index.html";
			IOUtils.writeText(webpage, elementIndexPath);
		}
	}

	private void generateProtocolBondLengthFiles(Set<String> changedBonds) {
		if (changedBonds == null) return;
		for (String bondType : changedBonds) {
			String bondLengthsDir = properties.getBondLengthsDir();
			String allBondsPath = bondLengthsDir+File.separator+bondType+CSV_MIME;
			String protocolBondsPath = bondLengthsDir+File.separator+bondType+AFTER_PROTOCOL+CSV_MIME;
			File protocolBondsFile = new File(protocolBondsPath);
			if (protocolBondsFile.exists()) {
				protocolBondsFile.delete();
			}
			BufferedReader input = null;
			int i = 0;
			StringBuilder sb = new StringBuilder();
			try {
				input = new BufferedReader(new FileReader(allBondsPath));
				String line = null;
				while (( line = input.readLine()) != null){
					if (line != null && !"".equals(line)) {
						String[] a = line.split(",");

						//FIXME - shouldn't need this\
						if (a.length != 10) {
							continue;
						}

						String tempStr = a[TEMP_COL].trim();
						String rfStr = a[RF_COL].trim();
						if ("".equals(tempStr) || "".equals(rfStr) ||
								" ".equals(tempStr) || " ".equals(rfStr) ) {
							continue;
						}
						double temp = Double.parseDouble(tempStr);
						double rf = Double.parseDouble(rfStr);
						if (temp <= PROTOCOL_MAX_TEMP && rf <= PROTOCOL_MAX_RF) {
							sb.append(line+"\n");
							i++;
						}
						if (i == 25000) {
							if (protocolBondsFile.exists()) {
								IOUtils.appendToFile(protocolBondsFile, sb.toString());
							} else {
								IOUtils.writeText(sb.toString(), protocolBondsPath);
							}
							sb = new StringBuilder();
							i = 0;
						}
					}
				}
				if (i > 0) {
					if (protocolBondsFile.exists()) {
						IOUtils.appendToFile(protocolBondsFile, sb.toString());
					} else {
						IOUtils.writeText(sb.toString(), protocolBondsPath);
					}
				}
				input.close();
			}
			catch (FileNotFoundException ex) {
				throw new RuntimeException("Could not find file: "+allBondsPath);
			}
			catch (IOException ex){
				throw new RuntimeException("Error reading file: "+allBondsPath);
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
		}
	}

	private void generateHistograms(Set<String> changedBonds) {
		// for those bond type lists that have been updated, regenerate the histogram
		if (changedBonds == null) return;
		for (String bondType : changedBonds) {	
			String bondLengthsDir = properties.getBondLengthsDir();
			String allBondsPath = bondLengthsDir+File.separator+bondType+CSV_MIME;
			Document allHist = getHistogram(allBondsPath, bondType, false);
			String allHistOutPath = bondLengthsDir+File.separator+bondType+SVG_MIME;
			IOUtils.writeXML(allHist, allHistOutPath);

			String protocolBondsPath = bondLengthsDir+File.separator+bondType+AFTER_PROTOCOL+CSV_MIME;
			if (new File(protocolBondsPath).exists()) {
				Document protocolHist = getHistogram(protocolBondsPath, bondType, true);
				String protocolHistOutPath = bondLengthsDir+File.separator+bondType+AFTER_PROTOCOL+SVG_MIME;
				IOUtils.writeXML(protocolHist, protocolHistOutPath);
			}
		}	
	}

	private Document getHistogram(String bondsPath, String bondType, boolean isAfterProtocol) {
		BufferedReader input = null;
		double min = 0.0;
		double max = 0.0;
		List<Point> points0 = new ArrayList<Point> ();
		try {
			input = new BufferedReader(new FileReader(bondsPath));
			String line = null;
			int i = 0;
			while (( line = input.readLine()) != null){
				if (line != null && !"".equals(line)) {
					String[] a = line.split(",");

					String length = a[LENGTH_COL].trim();
					if ("".equals(length) || " ".equals(length)) {
						continue;
					}
					double d = Double.parseDouble(length);
					if (i == 0) {
						min = d;
						max = d;
					} else {
						if (d < min) {
							min = d;
						}
						if (d > max) {
							max = d;
						}
					}

					Point p = new Point();
					p.setX(d);
					points0.add(p);	
					i++;
				}
			}
			input.close();
		}
		catch (FileNotFoundException ex) {
			throw new RuntimeException("Could not find file: "+bondsPath);
		}
		catch (IOException ex){
			throw new RuntimeException("Error reading file: "+bondsPath);
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

		// round the min and max values to 1 decimal place
		double minR = Utils.round((min)-0.1, 2);
		double maxR = Utils.round((max)+0.1, 2);

		double diff = Utils.round(maxR - minR, 2);
		double binWidth = 0.01;
		int numBins = (int)(diff/binWidth);

		GraphLayout layout = new GraphLayout();
		layout.setXmin(minR);
		layout.setXmax(maxR);
		layout.setPlotXGridLines(false);
		layout.setPlotYGridLines(false);
		try {
			layout.setNXTickMarks(10);
		} catch (GraphException e1) {
			System.err.println("Problem setting NXTickMarks");
		}

		Histogram hist1 = new Histogram(layout);
		Document doc = null;
		try {
			//hist1.setPlotfrequency(false);
			hist1.setNBins(numBins);
			hist1.addDataToPlot(points0);
			hist1.setXlab("Bond Length (angstroms)");
			hist1.setYlab("No. occurences");
			hist1.setGraphTitle(bondType+" Bond Lengths in CrystalEye (Last updated "+dNow+")");

			hist1.plot();
			doc = new Document(hist1.getSVG());
			SVGInterpretter svgi = new SVGInterpretter (hist1);
		} catch (GraphException e) {
			System.err.println(e.getMessage());
		}

		Element svg = doc.getRootElement();
		appendScriptElement(svg);
		improveSVGAndCreateHTMLSummaries(doc, minR, binWidth, bondsPath, bondType, isAfterProtocol);

		return doc;
	}

	private void addSubtitle(Document doc, boolean isAfterProtocol) {
		SVGElement text = new SVGElement("text");
		doc.getRootElement().appendChild(text);
		text.addAttribute(new Attribute("x", "400.0"));
		text.addAttribute(new Attribute("y", "42.5"));
		text.addAttribute(new Attribute("font-size", "12.0"));
		text.addAttribute(new Attribute("text-anchor", "middle"));
		if (isAfterProtocol) {
			text.appendChild(new Text("for non-disordered, unconstrained atoms in structures where temperature <= "+PROTOCOL_MAX_TEMP+" and r-factor <= "+PROTOCOL_MAX_RF));
		} else {
			text.appendChild(new Text("for non-disordered, unconstrained atoms"));
		}
	}

	private void improveSVGAndCreateHTMLSummaries(Document doc, double minR, double binWidth, String bondsPath, String bondType, boolean isAfterProtocol) {
		String bondTypeFolder = "";
		if (isAfterProtocol) {
			bondTypeFolder = properties.getBondLengthsDir()+File.separator+bondType+AFTER_PROTOCOL;
		} else {
			bondTypeFolder = properties.getBondLengthsDir()+File.separator+bondType;
		}
		addSubtitle(doc, isAfterProtocol);
		File bondTypeFile = new File(bondTypeFolder);
		if (bondTypeFile.exists()) {
			for (File f : bondTypeFile.listFiles()) {
				if (f.getAbsolutePath().endsWith(HTML_MIME)) {
					f.delete();
				}
			}
		}

		Nodes rectNodes = doc.query("./svg:svg/svg:rect", X_SVG);
		for (int i = 0; i < rectNodes.size(); i++) {
			double binMin = Utils.round(minR+(i*binWidth), 2);
			double binMax = Utils.round(minR+((i+1)*binWidth), 2);
			String id = String.valueOf(binMin+"-"+binMax); 

			BufferedReader input = null;
			List<String> lineList = new ArrayList<String>(); 
			try {
				input = new BufferedReader(new FileReader(bondsPath));
				String line = null;
				while (( line = input.readLine()) != null){
					if (line != null && !"".equals(line)) {
						String[] a = line.split(",");

						String length = a[LENGTH_COL].trim();
						if ("".equals(length) || " ".equals(length)) {
							continue;
						}
						double d = Double.parseDouble(length);
						if (d >= binMin && d < binMax) {
							lineList.add(line);
						}
					}
				}
				input.close();
			}
			catch (FileNotFoundException ex) {
				throw new RuntimeException("Could not find file: "+bondsPath);
			}
			catch (IOException ex){
				throw new RuntimeException("Error reading file: "+bondsPath);
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

			String href = bondType+"/"+id+HTML_MIME;
			String htmlPath = bondTypeFolder+File.separator+id+HTML_MIME;

			String table = createOverallCifSummaryTable(lineList);
			if (table == null) {
				continue;
			}
			String title = "CrystalEye: Structures containing "+bondType+" bonds<br />between "+id+" &Aring;";
			String header = "Structures containing "+bondType+" bonds<br />between "+id+" &Aring;";
			CifSummaryToc ocs = new CifSummaryToc(title, header, table, String.valueOf(structureCount), jmolLoadForSummary, imageLoadForSummary, String.valueOf(maxImageForSummary), 2);
			IOUtils.writeText(ocs.getWebpage(), htmlPath);

			Element rect = (Element)rectNodes.get(i);
			rect.addAttribute(new Attribute("onclick", "move('"+href+"')"));
		}
	}

	private void addOverallCifRowValues(BondLengthCmlDescription b, StringBuilder sb) {		

		String jmolSelectAtomString = createJmolAtomSelectString(b.getAtomIds());
		String[] a = b.getCmlId().split("_");
		String moietyS = b.getFormula();
		String compoundClass = b.getCompoundClass();
		boolean isPolymeric = b.isPolymeric();
		int uniqueMols = b.getUniqueSubMols();

		String articleId = a[4].replaceAll("sup[\\d]*", "");

		if (compoundClass.equals(CompoundClass.INORGANIC.toString()) || isPolymeric) {
			if (summaryRowCount == 0) {
				this.jmolLoadForSummary = "\"load ../../summary/"+a[0]+"/"+a[1]+"/"+a[2]+"/"+a[3]+"/data/"+articleId
				+"/"+a[4]+"_"+a[5]+"/"+a[4]+"_"+a[5]+COMPLETE_CML_MIME+"; set unitcell on;"+jmolSelectAtomString+"\"";
				this.imageLoadForSummary = "'../../display/placeholder.bmp'";
				this.maxImageForSummary = 1;
			}
			structureCount++;
			sb.append("<td>");
			sb.append("<a id=\""+structureCount+"\" href=\"javascript:showThisStructure"+
					"('../../display/placeholder.bmp',1,'load ../../summary/"+a[0]+"/"+a[1]+"/"+a[2]+"/"+a[3]+"/data/"+articleId
					+"/"+a[4]+"_"+a[5]+"/"+a[4]+"_"+a[5]+COMPLETE_CML_MIME+";"+jmolSelectAtomString+"',"+structureCount+");\">"+
					moietyS+"</a>");
			sb.append("</td>");

			sb.append("<td>");
			if (b.getDoi() != null) {
				sb.append("<a style=\"text-decoration: underline;\" href=\"http://dx.doi.org/"+b.getDoi()+"\">view</a>");
			} else {
				sb.append("N/A");
			}
			sb.append("</td>");

			sb.append("<td>");
			sb.append("<a style=\"text-decoration: underline;\" href=\"../../summary/"+a[0]+"/"+a[1]+"/"+a[2]+"/"+a[3]+"/data/"+articleId
					+"/"+a[4]+"_"+a[5]+"/"+a[4]+"_"+a[5]+".cif.summary.html\">view</a>");
			sb.append("</td>");
		} else {
			int maxImage = uniqueMols;
			structureCount++;
			sb.append("<td>");
			if (maxImage == 0) {
				sb.append("<a id=\""+structureCount+"\" href=\"javascript:showThisStructure"+
						"('../../display/placeholder.bmp',1,'load ../../summary/"+a[0]+"/"+a[1]+"/"+a[2]+"/"+a[3]+"/data/"+articleId
						+"/"+a[4]+"_"+a[5]+"/"+a[4]+"_"+a[5]+COMPLETE_CML_MIME+";"+jmolSelectAtomString+"',"+structureCount+");\">"+
						moietyS+"</a>");
			} else {
				sb.append("<a id=\""+structureCount+"\" href=\"javascript:showThisStructure"+
						"('../../summary/"+a[0]+"/"+a[1]+"/"+a[2]+"/"+a[3]+"/data/"+articleId
						+"/"+a[4]+"_"+a[5]+"/"+a[4]+"_"+a[5]+"_1.small.png',"+maxImage+",'load ../../summary/"+a[0]+"/"+a[1]+"/"+a[2]+"/"+a[3]+"/data/"+articleId
						+"/"+a[4]+"_"+a[5]+"/"+a[4]+"_"+a[5]+COMPLETE_CML_MIME+";"+jmolSelectAtomString+"',"+structureCount+");\">"+
						moietyS+"</a>");
			}
			sb.append("</td>");
			sb.append("<td>");
			if (b.getDoi() != null) {
				sb.append("<a style=\"text-decoration: underline;\" href=\"http://dx.doi.org/"+b.getDoi()+"\">view</a>");
			} else {
				sb.append("N/A");
			}
			sb.append("</td>");
			sb.append("<td>");
			sb.append("<a style=\"text-decoration: underline;\" href=\"../../summary/"+a[0]+"/"+a[1]+"/"+a[2]+"/"+a[3]+"/data/"+articleId
					+"/"+a[4]+"_"+a[5]+"/"+a[4]+"_"+a[5]+".cif.summary.html\">view</a>");
			sb.append("</td>");
			if (summaryRowCount == 0) {
				this.jmolLoadForSummary = "\"load ../../summary/"+a[0]+"/"+a[1]+"/"+a[2]+"/"+a[3]+"/data/"+articleId
				+"/"+a[4]+"_"+a[5]+"/"+a[4]+"_"+a[5]+COMPLETE_CML_MIME+"; set unitcell on;"+jmolSelectAtomString+"\"";
				if (maxImage == 0) {
					this.imageLoadForSummary = "'../../display/placeholder.bmp'";
				} else {
					this.imageLoadForSummary = "\"../../summary/"+a[0]+"/"+a[1]+"/"+a[2]+"/"+a[3]+"/data/"+articleId
					+"/"+a[4]+"_"+a[5]+"/"+a[4]+"_"+a[5]+"_1.small.png\"";
				}
				this.maxImageForSummary = maxImage;
			}
		}
		summaryRowCount++;
	}

	private String createJmolAtomSelectString(Set<String> atomNoSet) {
		StringBuilder sb = new StringBuilder();
		sb.append("select ");
		int size = atomNoSet.size();
		int j = 0;
		for (String atomNo : atomNoSet) {
			sb.append("atomno="+atomNo+" ");
			if (size-1 != j) {
				sb.append("or ");
			}
			j++;
		}
		sb.append("; set display SELECTED;");
		return sb.toString();
	}

	private String createOverallCifSummaryTable(List<String> lineList) {
		String summaryDir = properties.getSummaryWriteDir();
		Map<String, BondLengthCmlDescription> bMap = new HashMap<String, BondLengthCmlDescription>();
		for (String line : lineList) {
			String[] items = line.split(",");

			if (items.length != 10) {
				continue;
			}

			String cmlId = items[ID_COL];
			String[] a = cmlId.split("_");

			String completeCmlPath = summaryDir+File.separator+a[0]+File.separator+a[1]+File.separator+a[2]+File.separator+a[3]+
			File.separator+"data"+File.separator+a[4].replaceAll("sup[\\d]*", "")+File.separator+a[4]+"_"+a[5]+File.separator+a[4]+"_"+a[5]+COMPLETE_CML_MIME;

			String[] atomNos = items[ATOMNOS_COL].split("_");

			BondLengthCmlDescription b = bMap.get(completeCmlPath);
			if (b != null) {
				Set<String> set = b.getAtomIds();
				set.addAll(Arrays.asList(atomNos));
			} else {
				Set<String> set = new HashSet<String>();
				set.addAll(Arrays.asList(atomNos));
				String doi = items[DOI_COL].trim();
				String formula = items[FORMULA_COL].trim();
				String compClass = items[COMPCLASS_COL].trim();
				String isPolymeric = items[POLY_COL].trim();
				int uniqueSubMols = Integer.valueOf(items[UNIQUEMOL_COL].trim());
				b = new BondLengthCmlDescription(completeCmlPath, cmlId, set, doi, formula, compClass, Boolean.parseBoolean(isPolymeric), uniqueSubMols);
				bMap.put(completeCmlPath, b);
			}
		}

		int rows = 0;
		structureCount = 0;
		summaryRowCount = 0;
		StringBuilder table = new StringBuilder();
		table.append("<table border='1'><tr><th>Published Formula (clickable)</th><th>Article</th><th>Summary</th></tr>");	
		for (Iterator it = bMap.values().iterator(); it.hasNext(); ) {
			table.append("<tr>");
			addOverallCifRowValues((BondLengthCmlDescription)it.next(), table);
			table.append("</tr>");
			rows++;
		}
		table.append("</table>");

		if (rows == 0) {
			return null;
		}


		return table.toString();
	}

	private void appendScriptElement(Element element) {
		SVGElement script = new SVGElement("script");
		element.appendChild(script);
		script.addAttribute(new Attribute("type", "text/ecmascript"));
		Text content = new Text(""+	
				"function move(url) {\n"+
				"window.location = url\n"+
		"}\n");
		script.appendChild(content);
	}

	private void addLengthsFromCmlFile(File cmlFile) {
		System.out.println("Processing: "+cmlFile.getAbsolutePath());
		CMLCml c = null;
		try {
			c = (CMLCml)IOUtils.parseCmlFile(cmlFile).getRootElement();
		} catch (Exception e) {
			System.err.println("Error parsing CML: "+e.getMessage());
		}
		if (c == null) {
			return;
		}
		CMLCml cml = (CMLCml)c.copy();

		setCmlValues(cml);

		Nodes compoundClassNodes = cml.query(".//cml:scalar[@dictRef='iucr:compoundClass']", X_CML);
		if (compoundClassNodes.size() > 0) {
			String cc = compoundClassNodes.get(0).getValue();
			/*
			 * Minor difficulty processing bond lengths in the crystals in CrystalEye...
			 * If the crystal is organic or a non-polymeric organometallic, then Crystaleye will have produced a unit cell
			 * containing the unique moieties.  Hence getting all the bond lengths is easy as you just take the ones already there.
			 * However, if the crystal is inorganic or polymeric organometallic, then CrystalEye will have produced a unit cell 
			 * containing atoms in all positions.  Thus we can get all the bonds between the atoms in the unit cell, but this leads
			 * to two problems.  The first is the problem of bond duplication - we don't want to take equivalent bonds between
			 * symmetry related atoms.  The second is the problem of missing bonds between the explicit atoms and atoms in the adjacent 
			 * unit cell.  Hence we have two methods to deal with the two types of crystal that Crystaleye will have produced!
			 * 
			 */
			CMLMolecule molecule = (CMLMolecule)cml.getFirstCMLChild(CMLMolecule.TAG);
			if (CompoundClass.ORGANIC.toString().equals(cc) || molecule.getAtomCount() > 1000) {
				processDiscreteMoleculeCrystal(cml);
			} else if (CompoundClass.ORGANOMETALLIC.toString().equals(cc)) {
				Nodes polymericNodes = cml.query(".//"+CMLMetadata.NS+"[@dictRef='"+
						POLYMERIC_FLAG_DICTREF+"']", X_CML);
				if (polymericNodes.size() > 0) {
					try {
						processAllAtomCrystal(cml);
					} catch (OutOfMemoryError e) {
						System.err.println("Out of memory processing: "+cmlFile.getAbsolutePath());
						processDiscreteMoleculeCrystal(c);
					}
				} else {
					processDiscreteMoleculeCrystal(cml);
				}
			} else if (CompoundClass.INORGANIC.toString().equals(cc)) {
				try {
					processAllAtomCrystal(cml);
				} catch (OutOfMemoryError e) {
					System.err.println("Out of memory processing: "+cmlFile.getAbsolutePath());
					processDiscreteMoleculeCrystal(c);
				}
			} else {
				throw new CrystalEyeRuntimeException("Invalid compound class: "+cc);
			}
		}
	}

	private void setCmlValues(CMLCml cml) {
		Nodes tempNodes = cml.query(".//cml:scalar[@dictRef='iucr:_cell_measurement_temperature']", X_CML);
		if (tempNodes.size() == 1) {
			temp = tempNodes.get(0).getValue();
		} else {
			temp = "";
			System.err.println("Could not retrieve cell measurement temperature: "+cml.getId());
		}

		Nodes rFactorNodes = cml.query(".//cml:scalar[@dictRef='iucr:_refine_ls_r_factor_gt']", X_CML);
		if (rFactorNodes.size() == 1) {
			rf = rFactorNodes.get(0).getValue();
		} else {
			rf = "";
			System.err.println("Could not retrieve r factor gt: "+cml.getId());
		}

		Nodes doiNodes = cml.query(".//cml:scalar[@dictRef='idf:doi']", X_CML);
		if (doiNodes.size() > 0) {
			doiStr = doiNodes.get(0).getValue();
		} else {
			doiStr = "";
		}

		/*
		 * retrieve formula - AS PUBLISHED IN THE CIF - from the CML.
		 * if cannot find the chemical_formula_moiety then uses the chemical_formula_sum.
		 */ 
		Elements formulaElements = cml.getChildCMLElements(CMLFormula.TAG);
		StringWriter sw = new StringWriter();
		String moi = ".";
		String sum = ".";
		for (int i = 0; i < formulaElements.size(); i++) {
			try {
				CMLFormula formula = (CMLFormula) formulaElements.get(i);
				if ("iucr:_chemical_formula_moiety".equalsIgnoreCase(formula.getDictRef())) {
					formula.writeHTML(sw);
					moi = sw.toString();
				}
				if ("iucr:_chemical_formula_sum".equalsIgnoreCase(formula.getDictRef())) {
					formula.writeHTML(sw);
					sum = sw.toString();
				}
				sw.close();
			} catch (IOException e) {
				throw new CrystalEyeRuntimeException("Problem writing HTML of formula.", e);
			}
		}
		moietyS = (moi == ".") ? sum : moi;
		moietyS = moietyS.replaceAll(",", "_COMMA_");

		Nodes classNodes = cml.query(".//cml:scalar[contains(@dictRef,'iucr:compoundClass')]", X_CML);
		if (classNodes.size() > 0) {
			compoundClass = classNodes.get(0).getValue();
		} else {
			compoundClass = "";
		}
		Nodes polymericNodes = cml.query(".//"+CMLMetadata.NS+"[@dictRef='"+
				POLYMERIC_FLAG_DICTREF+"']", X_CML);
		if (polymericNodes.size() > 0) {
			isPolymeric = true;
		} else {
			isPolymeric = false;
		}
	}

	private void processAllAtomCrystal(CMLCml cml) throws OutOfMemoryError {
		CMLMolecule molecule = (CMLMolecule)cml.getFirstCMLChild(CMLMolecule.TAG).copy();
		CrystalTool ct = new CrystalTool(molecule);
		try {
			ct.createSupercell(3, 3, 3);
		} catch(OutOfMemoryError e) {
			throw new OutOfMemoryError();
		}

		List<CMLAtom> centralAtoms = new ArrayList<CMLAtom>();
		for (CMLAtom atom : molecule.getAtoms()) {
			Point3 p = atom.getXYZFract();
			int i = 0;
			for (double d : p.getArray()) {
				if (d >= 1.0 && d < 2.0) {
					i++;
				}
			}
			if (i == 3) {
				centralAtoms.add(atom);
			}
		}

		MoleculeTool mt = new MoleculeTool(molecule);
		mt.calculateBondedAtoms(centralAtoms);

		addLengthsToFiles(cml, molecule, centralAtoms, cml.getId());
	}

	private void processDiscreteMoleculeCrystal(CMLCml cml) {		
		CMLMolecule molecule = (CMLMolecule)cml.getFirstCMLChild(CMLMolecule.TAG);
		for (CMLMolecule subMol : molecule.getDescendantsOrMolecule()) {		
			addLengthsToFiles(cml, molecule, subMol.getAtoms(), cml.getId());
		}
	}

	private void addLengthsToFiles(CMLCml cml, CMLMolecule molecule, List<CMLAtom> atomList, String id) {
		Map<String, CMLAtom> originalAtomMap = new HashMap<String, CMLAtom>();
		for (CMLAtom atom : atomList) {
			String asl = getAtomSiteLabel(atom);
			if (asl != null && !originalAtomMap.containsKey(asl)) {
				originalAtomMap.put(asl, atom);
			}
		}

		List<String> doneBonds = new ArrayList<String>();
		List<CMLAtom> nonValidAtoms = new ArrayList<CMLAtom>(); 
		for (Iterator it = originalAtomMap.values().iterator(); it.hasNext(); ) {
			CMLAtom atom = (CMLAtom)it.next();
			for (CMLAtom ligand : atom.getLigandAtoms()) {
				if (!nonValidAtoms.contains(atom) && !nonValidAtoms.contains(ligand)) {
					if (!atomIsValid(atom)) {
						nonValidAtoms.add(atom);
						continue;
					} 
					if (!atomIsValid(ligand)) {
						nonValidAtoms.add(ligand);
						continue;
					}

					String atomLabel = getAtomSiteLabel(atom);
					String ligandLabel = getAtomSiteLabel(ligand);
					List<String> idList = new ArrayList<String>(2);
					idList.add(atomLabel);
					idList.add(ligandLabel);
					Collections.sort(idList);
					String bondAtomsId = idList.get(0)+"-"+idList.get(1);
					if (!doneBonds.contains(bondAtomsId)) {
						CMLBond bond = molecule.getBond(atom, ligand);
						addLengthToFile(cml, molecule, bond, id);
						doneBonds.add(bondAtomsId);
					}
				}
			}
		}
	}

	private String getAtomSiteLabel(CMLAtom atom) {
		Nodes aslNodes = atom.query(".//cml:scalar[@dictRef='iucr:_atom_site_label']", X_CML);
		if (aslNodes.size() == 1) {
			return aslNodes.get(0).getValue();
		} else {
			System.err.println("Could not find _atom_site_label for atom "+atom.getId());
			return null;
		}
	}

	private boolean atomIsValid(CMLAtom atom) {
		if (atomIsDisordered(atom) || atomHasCloseContacts(atom) || atomHasNonUnitOccupancy(atom) || atomHasRefinementFlag(atom)) {
			return false;
		} else {
			return true;
		}
	}

	private boolean atomHasRefinementFlag(CMLAtom atom) {
		Nodes refinementNodes = atom.query(".//cml:scalar[@dictRef='iucr:_atom_site_refinement_flags' and .!='.'] | " +
				".//cml:scalar[@dictRef='iucr:_atom_site_refinement_flags_adp' and .!='.'] | " +
				".//cml:scalar[@dictRef='iucr:_atom_site_refinement_flags_posn' and .!='.'] | " +
				".//cml:scalar[@dictRef='iucr:_atom_site_refinement_flags_occupancy' and .!='.']", X_CML);
		if (refinementNodes.size() > 0) {
			return true;
		} else {
			return false;
		}
	}

	private boolean atomIsDisordered(CMLAtom atom) {
		List<Node> nodes = CMLUtil.getQueryNodes(atom, ".//" + CMLScalar.NS
				+ "[@dictRef='" + CrystalTool.DISORDER_ASSEMBLY + "' and .!='.'] | "
				+ ".//" + CMLScalar.NS + "[@dictRef='"
				+ CrystalTool.DISORDER_GROUP + "' and .!='.']", X_CML);
		if (nodes.size() > 0) {
			return true;
		} else {
			return false;
		}
	}

	private boolean atomHasCloseContacts(CMLAtom atom) {
		boolean cc = false;
		for (CMLAtom ligand : atom.getLigandAtoms()) {
			double valenceDist = atom.getChemicalElement().getCovalentRadius()+ligand.getChemicalElement().getCovalentRadius();
			double dist = atom.getDistanceTo(ligand);
			if ((valenceDist/2) > dist) {
				cc = true;
				break;
			}
		}
		return cc;
	}

	private boolean atomHasNonUnitOccupancy(CMLAtom atom) {
		Nodes nonUnitOccNodes = atom.query(".//@occupancy[. < 1]", X_CML);
		if (nonUnitOccNodes.size() > 0) {
			return true;
		} else {
			return false;
		}
	}

	private void addLengthToFile(CMLCml cml, CMLMolecule mol, CMLBond bond, String id) {
		CMLMolecule originalMolecule = (CMLMolecule)cml.getFirstCMLChild(CMLMolecule.TAG);

		CoordinateType coordType = CoordinateType.CARTESIAN;
		String bondLengthsDir = properties.getBondLengthsDir();
		double length = bond.calculateBondLength(coordType);
		List<String> elList = new ArrayList<String>(2);
		for (String atomRef : bond.getAtomRefs2()) {
			elList.add(mol.getAtomById(atomRef).getElementType());
		}
		Collections.sort(elList);
		String bondTypeId = elList.get(0)+"-"+elList.get(1);
		String filename = bondTypeId+CSV_MIME;
		File lengthsFile = new File(bondLengthsDir+File.separator+filename);

		StringBuilder bondId = new StringBuilder();
		bondId.append(id+"_");
		List<String> atomIdList = new ArrayList<String>();
		int i = 0;
		for (CMLAtom atom : bond.getAtoms()) {
			String atomId = atom.getId().split("_")[0];
			atomIdList.add(atomId);
			bondId.append(atomId);
			if (i == 0) {
				bondId.append("/");
			}
			i++;
		}

		int k = 1;
		List<Integer> intList = new ArrayList<Integer>();
		for (CMLAtom atom : originalMolecule.getAtoms()) {
			String atomId = atom.getId();
			for (String aId : atomIdList) {
				if (atomId.contains(aId+"_") || atomId.equals(aId))	 {
					intList.add(k);
				}
			}
			k++;
		}

		String atomNos = "";
		for (int j = 0; j < intList.size(); j++) {
			atomNos += intList.get(j)+"_";
		}
		atomNos = atomNos.substring(0,atomNos.length()-1);

		List<CMLMolecule> uniqueMolList = CrystalEyeUtils.getUniqueSubMolecules(mol);	
		int uniqueSubMols = 0;
		for (CMLMolecule subMol : uniqueMolList) {	
			Nodes nonUnitOccNodes = subMol.query(".//"+CMLAtom.NS+"[@occupancy[. < 1]]", X_CML);
			if (!DisorderTool.isDisordered(subMol) && !subMol.hasCloseContacts() && nonUnitOccNodes.size() == 0
					&& Cif2CmlManager.hasBondOrdersAndCharges(subMol)) {
				if (CrystalEyeUtils.isBoringMolecule(subMol)) {
					continue;
				}
				uniqueSubMols++;
			}
		}

		String newContent = String.valueOf(length)+","+bondId.toString()+","+atomNos+","+temp+","+rf+","+doiStr+","+compoundClass+","+moietyS+","+isPolymeric+","+uniqueSubMols+"\n";

		changedBonds.add(bondTypeId);
		if (lengthsFile.exists()) {
			IOUtils.appendToFile(lengthsFile, newContent);
		} else {
			IOUtils.writeText(newContent, lengthsFile.getAbsolutePath());
		}
	}

	public static void main(String[] args) {
		//BondLengthsManager d = new BondLengthsManager("e:/crystaleye-test2/docs/cif-flow-props.txt");
		BondLengthsManager d = new BondLengthsManager("e:/data-test/docs/cif-flow-props.txt");
		d.execute();
	}
}
