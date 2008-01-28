package uk.ac.cam.ch.crystaleye.fetch;

import static uk.ac.cam.ch.crystaleye.CrystalEyeConstants.MAX_CIF_SIZE_IN_BYTES;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Nodes;

import org.xmlcml.cif.CIF;
import org.xmlcml.cif.CIFDataBlock;
import org.xmlcml.cif.CIFException;
import org.xmlcml.cif.CIFParser;
import org.xmlcml.cml.base.CMLConstants;
import org.xmlcml.cml.element.CMLCml;
import org.xmlcml.cml.element.CMLCrystal;
import org.xmlcml.cml.element.CMLMolecule;
import org.xmlcml.cml.legacy2cml.cif.CIFConverter;

import uk.ac.cam.ch.crystaleye.CrystalEyeRuntimeException;
import uk.ac.cam.ch.crystaleye.IOUtils;
import uk.ac.cam.ch.crystaleye.Unzip;
import uk.ac.cam.ch.crystaleye.Utils;
import uk.ac.cam.ch.crystaleye.properties.CODProperties;

public class CODCurrent implements CMLConstants {

	private CODProperties properties;

	private static final String PUBLISHER_ABBREVIATION = "crystallographynet";
	private static final String COD = "cod";
	private static final String COD_ZIP_URL = "http://sdpd.univ-lemans.fr/cod/cod.zip";
	private static final String CIF_ZIP_NAME = "Cif.zip";
	
	private static final double CELL_PARAM_EPSILON = 0.01;
	
	private int expectedNoCifs;

	private CODCurrent() {
		;
	}

	public CODCurrent(File propertiesFile) {
		this.setProperties(propertiesFile);
	}

	private void setProperties(File propertiesFile) {
		properties = new CODProperties(propertiesFile);
	}

	public void execute() {
		String codZipName = new File(COD_ZIP_URL).getName();
		String userHome = System.getProperty("user.home");
		String codDir = userHome+File.separator+"crystallographyopendatabase";
		File codFile = new File(codDir);
		if (!codFile.exists()) {
			codFile.mkdirs();
		}
		String zipPath = codDir+File.separator+codZipName;
		// TODO - UNCOMMENT THIS
		//IOUtils.saveFileFromUrl(codZipUrl, zipPath);
		//unzipFile(zipPath, cifZipName);	
		//unzipAllFiles(codDir+File.separator+cifZipName);
		/*
		for (File file : codFile.listFiles()) {
			String filePath = file.getAbsolutePath();
			if (filePath.endsWith(".zip") && !filePath.endsWith(codZipName) &&
					!filePath.endsWith(cifZipName)) {
				unzipAllFiles(filePath);
			}
		}
		 */

		// do this next bit to compare the files in the COD with those in the 
		// list we maintain.  Saves us having to parse all of the COD cif files
		// each time we download it to check whether we already have the files (continued below) ....
		Set<String> alreadyGotSet = populateAlreadyGotSet();
		System.out.println(alreadyGotSet.size());
		Set<File> notGotSet = new HashSet<File>();

		for (File file : codFile.listFiles()) {
			String filePath = file.getAbsolutePath();
			if (filePath.endsWith(".cif") || filePath.endsWith(".CIF")) {
				String parent = file.getParentFile().getAbsolutePath();
				String name = file.getName();
				String lowerCaseName = name.toLowerCase();
				if (!name.equals(lowerCaseName)) {
					file.renameTo(new File(parent+File.separator+lowerCaseName));
				}
				if (!alreadyGotSet.contains(lowerCaseName)) {
					notGotSet.add(file);
				}
			}
		}		
		
		String writeDir = properties.getWriteDir();
		Date date = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		String[] arr = formatter.format(date).split("-");
		String year = arr[0];
		String month = arr[1];
		String day = arr[2];
		String outDir = writeDir+PUBLISHER_ABBREVIATION+File.separator+COD+
		File.separator+year+File.separator+month+"-"+day;
		Set<double[]> cellParamSet = populateCellParamSet();

		// ... however, if haven't already tried the file, we then need to parse into CML and look at the
		// crystal cell parameters to see if we already have the structure from a different source.
		expectedNoCifs = 0;
		for (File file : notGotSet) {
			if (fileTooLarge(file)) {
				continue;
			} else {
				List<File> splitCifList = null;
				try {
					splitCifList = this.createSplitCifs(file);
				} catch (Exception e) {
					System.err.println("Could not split cif file: "+file.getAbsolutePath());
					continue;
				}
				for (File splitCif : splitCifList) {
					String splitCifPath = splitCif.getAbsolutePath();
					int idx = splitCifPath.indexOf(".cif");
					String mm = splitCifPath.substring(0,idx);
					String rawCmlPath = mm+".raw.cml.xml";
					runCIFConverter(splitCifPath, rawCmlPath);
					File rawCmlFile = new File(rawCmlPath);
					if (!rawCmlFile.exists()) {
						continue;
					}
					CMLCml cml = null;
					try { 
						cml = (CMLCml)IOUtils.parseCmlFile(rawCmlFile).getRootElement();
					} catch (Exception e) {
						System.err.println("Error reading CML in "+rawCmlFile);
						continue;
					}
					
					boolean alreadyGot = false;
					try {
						double[] params = getCellParams(cml);
						for (double[] cps : cellParamSet) {
							int counter = 0;
							for (int i = 0; i < 6; i++) {
								if (Math.abs(params[i]-cps[i]) < CELL_PARAM_EPSILON) {
									counter++;
								} else {
									break;
								}
							}
							if (counter == 6) {
								alreadyGot = true;
								break;
							}
						}
					} catch(Exception e) {
						System.err.println("Could not get cell params for "+rawCmlFile);
					}
					if (!alreadyGot) {			
						String name = splitCif.getName();
						int i = name.indexOf(".cif");
						String mim = name.substring(0,i);
						String o =  outDir+File.separator+mim;
						String newPath = o+File.separator+name;
						File f = new File(newPath).getParentFile();
						if (!f.exists()) {
							f.mkdirs();
						}
						System.out.println("moving "+splitCif.getAbsolutePath()+" to "+newPath);
						splitCif.renameTo(new File(newPath));
						expectedNoCifs++;
					}
				}
			}
		}
		
		if (notGotSet.size() > 0) {
			updateLog(COD, year, month+"-"+day);
		}
		
		// append all files from notGotSet to the file containing the list of already checked ones
		appendToFileList(notGotSet);
		
		// delete the contents of the directory where the COD file was unzipped in user home
		Utils.delDir(codDir);
	}
	
	private double[] getCellParams(CMLCml cml) {
		CMLMolecule molecule = (CMLMolecule)cml.getFirstCMLChild(CMLMolecule.TAG);
		CMLCrystal crystal = (CMLCrystal)molecule.getFirstCMLChild(CMLCrystal.TAG);
		
		double[] params = new double[6];
		Nodes lengthANodes = crystal.query(".//cml:scalar[@dictRef='iucr:_cell_length_a']", CML_XPATH);
		if (lengthANodes.size() == 1) {
			String lengthA  = lengthANodes.get(0).getValue();
			params[0] = Double.valueOf(lengthA);
		} else {
			throw new RuntimeException("Could not find lengthA node.");
		}
		
		Nodes lengthBNodes = crystal.query(".//cml:scalar[@dictRef='iucr:_cell_length_b']", CML_XPATH);
		if (lengthBNodes.size() == 1) {
			String lengthB  = lengthBNodes.get(0).getValue();
			params[1] = Double.valueOf(lengthB);
		} else {
			throw new RuntimeException("Could not find lengthB node.");
		}
		
		Nodes lengthCNodes = crystal.query(".//cml:scalar[@dictRef='iucr:_cell_length_c']", CML_XPATH);
		if (lengthCNodes.size() == 1) {
			String lengthC  = lengthCNodes.get(0).getValue();
			params[2] = Double.valueOf(lengthC);
		} else {
			throw new RuntimeException("Could not find lengthC node.");
		}
		
		Nodes angleANodes = crystal.query(".//cml:scalar[@dictRef='iucr:_cell_angle_alpha']", CML_XPATH);
		if (angleANodes.size() == 1) {
			String angleA  = angleANodes.get(0).getValue();
			params[3] = Double.valueOf(angleA);
		} else {
			throw new RuntimeException("Could not find angleA node.");
		}
		
		Nodes angleBNodes = crystal.query(".//cml:scalar[@dictRef='iucr:_cell_angle_beta']", CML_XPATH);
		if (angleBNodes.size() == 1) {
			String angleB  = angleBNodes.get(0).getValue();
			params[4] = Double.valueOf(angleB);
		} else {
			throw new RuntimeException("Could not find angleB node.");
		}
		
		Nodes angleGNodes = crystal.query(".//cml:scalar[@dictRef='iucr:_cell_angle_gamma']", CML_XPATH);
		if (angleGNodes.size() == 1) {
			String angleG  = angleGNodes.get(0).getValue();
			params[5] = Double.valueOf(angleG);
		} else {
			throw new RuntimeException("Could not find angleG node.");
		}
		return params;
	}
	
	private Set<double[]> populateCellParamSet() {
		Set<double[]> set = new HashSet<double[]>();
		String listPath = properties.getCellParamsFilePath();
		BufferedReader input = null;
		try {
			input = new BufferedReader(new FileReader(listPath));
			String line = null;
			while (( line = input.readLine()) != null){
				if (line == null || "".equals(line)) continue;
				String[] arr = line.split(",");
				double[] darr = new double[arr.length];
				for (int i = 0; i < 6; i++) {
					darr[i] = Double.valueOf(arr[i]);
				}
				set.add(darr);
			}
			input.close();
		}
		catch (FileNotFoundException ex) {
			throw new CrystalEyeRuntimeException("Could not find file: "+listPath);
		}
		catch (IOException ex){
			throw new CrystalEyeRuntimeException("Error reading file: "+listPath);
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
		return set;
	}

	private boolean fileTooLarge(File file) {
		boolean tooLarge = false;
		int total = Utils.getFileSizeInBytes(file);
		if (total > MAX_CIF_SIZE_IN_BYTES) {
			System.err.println("CIF file too large to parse.  Skipping: "+file.getAbsolutePath());
			tooLarge = true;
		}
		return tooLarge;
	}

	private List<File> createSplitCifs(File file) {
		String fileName = file.getAbsolutePath();
		List<File> splitCifList = new ArrayList<File>();
		// split the found CIF
		try {
			CIFParser parser = new CIFParser();
			parser.setSkipHeader(true);
			parser.setSkipErrors(true);
			parser.setCheckDuplicates(true);
			parser.setBlockIdsAsIntegers(false);

			CIF cif = (CIF) parser.parse(new BufferedReader(new FileReader(file))).getRootElement();

			List<CIFDataBlock> blockList = cif.getDataBlockList();
			CIFDataBlock global = null;
			String globalBlockId = "";
			for (CIFDataBlock block : blockList) {
				// check whether CIF is an mmCIF or not - we can't process mmCIFs so throw an exception if it is
				Elements loops = block.getChildElements("loop");
				for (int i = 0; i < loops.size(); i++) {
					Element loop = loops.get(i);
					Nodes mmCifNodes = loop.query("./@names[contains(.,'_atom_site.type_symbol') and " +
					"contains(.,'_atom_site.id')]");
					if (mmCifNodes.size() > 0) {
						System.err.println("CIF is an mmCIF, cannot process: "+file.getAbsolutePath());
						throw new CrystalEyeRuntimeException("CIF is an mmCIF, cannot process: "+file.getAbsolutePath());
					}
				}
			}
			for (CIFDataBlock block : blockList) {		
				Nodes crystalNodes = block.query(".//item[@name='_cell_length_a']");
				Nodes moleculeNodes = block.query(".//loop[contains(@names,'_atom_site_label')]");
				Nodes symmetryNodes = block.query(".//loop[contains(@names,'_symmetry_equiv_pos_as_xyz')]");
				if (crystalNodes.size() == 0 && moleculeNodes.size() == 0 && symmetryNodes.size() == 0) {
					global = block;
					globalBlockId = block.getId();
					break;
				}
			}
			for (CIFDataBlock block : blockList) {
				if (block.getId().equalsIgnoreCase(globalBlockId)) {
					continue;
				} else {
					CIF cifNew = new CIF();
					block.detach();
					if (global != null) {
						global.detach();
					}
					Writer writer = null; 
					try {
						if (global != null) {
							cifNew.add(global);
						}
						cifNew.add(block);
						String chemBlockId = block.getId();
						chemBlockId = chemBlockId.replaceAll("\\.", "-");
						chemBlockId = chemBlockId.replaceAll(":", "-");
						chemBlockId = chemBlockId.replaceAll("/", "-");
						chemBlockId = chemBlockId.replaceAll("\\\\", "-");
						chemBlockId = chemBlockId.replaceAll("_", "-");
						chemBlockId = chemBlockId.replaceAll("%", "-");
						chemBlockId = chemBlockId.replaceAll("\\*", "-");
						chemBlockId = chemBlockId.replaceAll("\\?", "-");
						chemBlockId = chemBlockId.replaceAll(">", "-");
						chemBlockId = chemBlockId.replaceAll("<", "-");
						chemBlockId = chemBlockId.replaceAll("'", "-");
						chemBlockId = chemBlockId.replaceAll("\"", "-");
						chemBlockId = chemBlockId.replaceAll(",", "-");
						String cifPathMinusMime = Utils.getPathMinusMimeSet(file);
						String cifId = cifPathMinusMime.substring(cifPathMinusMime.lastIndexOf(File.separator)+1);
						String cifParent = cifPathMinusMime.substring(0,cifPathMinusMime.lastIndexOf(File.separator));
						File splitCifParent = new File(cifParent+File.separator+cifId+"_"+chemBlockId);
						if (!splitCifParent.exists()) {
							splitCifParent.mkdirs();
						}
						File splitCifFile = new File(splitCifParent,File.separator+cifId+"_"+chemBlockId+".cif");
						writer = new FileWriter(splitCifFile);
						cifNew.writeCIF(writer);
						writer.close();
						splitCifList.add(splitCifFile);
					} catch (CIFException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					} finally {
						if (writer != null)
							writer.close();
					}
				}
			}
		} catch (FileNotFoundException e) {
			throw new CrystalEyeRuntimeException("Could not find file "+fileName, e);
		} catch (CIFException e) {
			throw new CrystalEyeRuntimeException("Could not parse CIF in file "+fileName, e);
		} catch (IOException e) {
			throw new CrystalEyeRuntimeException("Could not read file "+fileName, e);
		}
		return splitCifList;
	}

	private void appendToFileList(Set<File> set) {
		StringBuilder sb = new StringBuilder();
		for (File file : set) {
			sb.append(file.getName()+"\n");
		}
		String fileListPath = properties.getAlreadyGotCifFilePath();
		try {
			FileWriter fw = new FileWriter(fileListPath, true);
			fw.write(sb.toString());
			fw.close();
		} catch(IOException e) {
			throw new CrystalEyeRuntimeException("Error appending text to: "+fileListPath);
		}
	}

	private Set<String> populateAlreadyGotSet() {
		Set<String> set = new HashSet<String>();
		String listPath = properties.getAlreadyGotCifFilePath();

		BufferedReader input = null;
		try {
			input = new BufferedReader(new FileReader(listPath));
			String line = null;
			while (( line = input.readLine()) != null){
				if (line == null || "".equals(line)) continue;
				set.add(line.trim());
			}
			input.close();
		}
		catch (FileNotFoundException ex) {
			throw new CrystalEyeRuntimeException("Could not find file: "+listPath);
		}
		catch (IOException ex){
			throw new CrystalEyeRuntimeException("Error reading file: "+listPath);
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
		return set;
	}

	/**
	 * @todo REIMPLEMENT!
	 * @param filename
	 */
	private void unzipAllFiles(String filename) {
		String[] args = new String[1];
		args[0] = filename;
//		Unzip.main(args);
	}

	/**
	 * @todo REIMPLEMENT!
	 * @param filename
	 */

	private void unzipFile(String filename, String zipName) {
		String[] args = new String[2];
		args[0] = filename;
		args[1] = zipName;
//		Unzip.main(args);
	}

	private void runCIFConverter(String infile, String outfile) {
		String cifDict = properties.getCifDict();
		String spaceGroupXml = properties.getSpaceGroupXml();
		String[] args = {"-INFILE", infile, 
				"-OUTFILE", outfile, 
				"-SKIPERRORS", 
				"-SKIPHEADER", 
				"-NOGLOBAL", 
				"-SPACEGROUP", spaceGroupXml,
				"-DICT", cifDict
		};
		CIFConverter cifConverter = new CIFConverter();
		try {
			cifConverter.runCommands(args);
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("CIFConverter EXCEPTION... "+e);
		}
	}
	
	protected void updateLog(String journalAbbreviation, String year, String issueNum) {
		String downloadLogPath = properties.getDownloadLogPath();
		Document doc = IOUtils.parseXmlFile(downloadLogPath);
		Element logEl = doc.getRootElement();
		Nodes publishers = logEl.query("./publisher[@abbreviation='"+PUBLISHER_ABBREVIATION+"']");
		if (publishers.size() == 1) {
			Element publisherEl = (Element)publishers.get(0);
			Nodes journals = publisherEl.query("./journal[@abbreviation='"+journalAbbreviation+"']");
			if (journals.size() == 1) {
				Element journalEl = (Element)journals.get(0);
				Nodes years = journalEl.query("./year[@id='"+year+"']");
				if (years.size() == 1) {
					Element yearEl = (Element)years.get(0);
					yearEl.appendChild(getNewIssueElement(issueNum));
				} else if (years.size() > 1) {
					throw new CrystalEyeRuntimeException("Found more than one entry in the log for "+PUBLISHER_ABBREVIATION+"/"+journalAbbreviation+"/"+year+".  Cannot continue.");
				} else if (years.size() == 0) {
					Element yearEl = getNewYearElement(year);
					journalEl.appendChild(yearEl);
					yearEl.appendChild(getNewIssueElement(issueNum));
				}
			} else if (journals.size() > 1) {
				throw new CrystalEyeRuntimeException("Found more than one entry in the log for "+PUBLISHER_ABBREVIATION+"/"+journalAbbreviation+".  Cannot continue.");
			} else if (journals.size() == 0) {
				Element journalEl = getNewJournalElement(journalAbbreviation);
				publisherEl.appendChild(journalEl);
				Element yearEl = getNewYearElement(year);
				journalEl.appendChild(yearEl);
				yearEl.appendChild(getNewIssueElement(issueNum));
			}
		} else if (publishers.size() > 1) {
			throw new CrystalEyeRuntimeException("Found more than one entry in the log for "+PUBLISHER_ABBREVIATION+".  Cannot continue.");
		} else if (publishers.size() == 0) {
			Element publisherEl = getNewPublisherElement(PUBLISHER_ABBREVIATION);
			logEl.appendChild(publisherEl);
			Element journalEl = getNewJournalElement(journalAbbreviation);
			publisherEl.appendChild(journalEl);
			Element yearEl = getNewYearElement(year);
			journalEl.appendChild(yearEl);
			yearEl.appendChild(getNewIssueElement(issueNum));
		}
		
		IOUtils.writeXML(doc, downloadLogPath);
		System.out.println("Updated "+downloadLogPath+" by adding "+year+"-"+issueNum);
	}

	private Element getNewIssueElement(String issueNum) {
		Element issue = new Element("issue");
		Attribute issueId = new Attribute("id", issueNum);
		issue.addAttribute(issueId);
		Element cif2Cml = new Element("cif2Cml");
		issue.appendChild(cif2Cml);
		cif2Cml.addAttribute(new Attribute("value", "false"));			
		Element cml2Foo = new Element("cml2Foo");
		cml2Foo.addAttribute(new Attribute("value", "false"));
		issue.appendChild(cml2Foo);			
		Element webpage = new Element("webpage");
		webpage.addAttribute(new Attribute("value", "true"));
		issue.appendChild(webpage);
		Element doilist = new Element("doilist");
		doilist.addAttribute(new Attribute("value", "true"));
		issue.appendChild(doilist);
		Element bondLengths = new Element("bondLengths");
		bondLengths.addAttribute(new Attribute("value", "false"));
		issue.appendChild(bondLengths);
		Element cellParams = new Element("cellParams");
		cellParams.addAttribute(new Attribute("value", "false"));
		issue.appendChild(cellParams);
		Element rss = new Element("rss");
		rss.addAttribute(new Attribute("value", "true"));
		issue.appendChild(rss);
		Element cifArticles = new Element("cifs");
		cifArticles.addAttribute(new Attribute("number", String.valueOf(expectedNoCifs)));
		issue.appendChild(cifArticles);

		return issue;
	}
	
	private Element getNewYearElement(String year) {
		Element yearEl = new Element("year");
		yearEl.addAttribute(new Attribute("id", year));	
		return yearEl;
	}

	private Element getNewJournalElement(String journalAbbreviation) {
		Element journalEl = new Element("journal");
		journalEl.addAttribute(new Attribute("abbreviation", journalAbbreviation));
		return journalEl;
	}

	private Element getNewPublisherElement(String publisherAbbreviation) {
		Element publisherEl = new Element("publisher");
		publisherEl.addAttribute(new Attribute("id", publisherAbbreviation));
		return publisherEl;
	}

	public static void main(String[] args) {
		CODCurrent cod = new CODCurrent(new File("e:/data-test/docs/cif-flow-props.txt"));
		cod.execute();
	}
}
