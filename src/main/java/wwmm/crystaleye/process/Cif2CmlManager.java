package wwmm.crystaleye.process;

import static wwmm.crystaleye.CrystalEyeConstants.CIF2CML;
import static wwmm.crystaleye.CrystalEyeConstants.CIF_MIME;
import static wwmm.crystaleye.CrystalEyeConstants.COMPLETE_CML_MIME;
import static wwmm.crystaleye.CrystalEyeConstants.DATE_MIME;
import static wwmm.crystaleye.CrystalEyeConstants.MAX_CIF_SIZE_IN_BYTES;
import static wwmm.crystaleye.CrystalEyeConstants.NED24_NS;
import static wwmm.crystaleye.CrystalEyeConstants.NO_BONDS_OR_CHARGES_FLAG_DICTREF;
import static wwmm.crystaleye.CrystalEyeConstants.POLYMERIC_FLAG_DICTREF;
import static wwmm.crystaleye.CrystalEyeConstants.RAW_CML_MIME;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Nodes;
import nu.xom.Text;
import nu.xom.XPathContext;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.layout.StructureDiagramGenerator;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.xmlcml.cif.CIF;
import org.xmlcml.cif.CIFDataBlock;
import org.xmlcml.cif.CIFException;
import org.xmlcml.cif.CIFParser;
import org.xmlcml.cml.base.CMLConstants;
import org.xmlcml.cml.base.CMLElement;
import org.xmlcml.cml.element.CMLAtom;
import org.xmlcml.cml.element.CMLBond;
import org.xmlcml.cml.element.CMLBondArray;
import org.xmlcml.cml.element.CMLCml;
import org.xmlcml.cml.element.CMLCrystal;
import org.xmlcml.cml.element.CMLFormula;
import org.xmlcml.cml.element.CMLLength;
import org.xmlcml.cml.element.CMLMetadata;
import org.xmlcml.cml.element.CMLMetadataList;
import org.xmlcml.cml.element.CMLMolecule;
import org.xmlcml.cml.element.CMLMolecule.HydrogenControl;
import org.xmlcml.cml.inchi.InChIGeneratorTool;
import org.xmlcml.cml.legacy2cml.cif.CIFConverter;
import org.xmlcml.cml.tools.ConnectionTableTool;
import org.xmlcml.cml.tools.CrystalTool;
import org.xmlcml.cml.tools.DisorderTool;
import org.xmlcml.cml.tools.DisorderToolControls;
import org.xmlcml.cml.tools.MoleculeTool;
import org.xmlcml.cml.tools.StereochemistryTool;
import org.xmlcml.cml.tools.ValencyTool;
import org.xmlcml.cml.tools.DisorderToolControls.ProcessControl;
import org.xmlcml.euclid.RealRange;
import org.xmlcml.molutil.ChemicalElement.Type;

import wwmm.crystaleye.AbstractManager;
import wwmm.crystaleye.properties.ProcessProperties;
import wwmm.crystaleye.util.CDKUtils;
import wwmm.crystaleye.util.CrystalEyeUtils;
import wwmm.crystaleye.util.Utils;
import wwmm.crystaleye.util.CrystalEyeUtils.CompoundClass;

public class Cif2CmlManager extends AbstractManager implements CMLConstants {

	/**
	 * 
	 */
	private ProcessProperties properties;

	/**
	 * 
	 * @param propertiesFile
	 */
	public Cif2CmlManager(File propertiesFile) {
		this.setProperties(propertiesFile);
	}

	/**
	 * 
	 * @param propertiesPath
	 */
	public Cif2CmlManager(String propertiesPath) {
		this(new File(propertiesPath));
	}

	/**
	 * 
	 * @param propertiesFile
	 */
	private void setProperties(File propertiesFile) {
		properties = new ProcessProperties(propertiesFile);
	}

	/**
	 * Iterates over all publishers, journals, years and issues in the download log
	 * Looks for a value of false in the cif2cml tag and runs the process on each
	 */
	public void execute() {
		
		//For all publishers
		String[] publisherAbbreviations = properties.getPublisherAbbreviations();
		for (String pubAbbrev : publisherAbbreviations) {
			
			//For all journals
			String[] journalAbbreviations = properties.getPublisherJournalAbbreviations(pubAbbrev);
			for (String journAbbrev : journalAbbreviations) {
								
				//Get path to download log file
				String downloadLogPath = properties.getDownloadLogPath();
				
				//Get unprocessed date from download log
				List<IssueDate> unprocessedDates = this.getUnprocessedDates(downloadLogPath, pubAbbrev, journAbbrev, CIF2CML, null);
				if (unprocessedDates.size() != 0) {
					for (IssueDate date : unprocessedDates) {
						String writeDir = properties.getWriteDir();
						String year = date.getYear();
						String issueNum = date.getIssue();
						String issueWriteDir = Utils.convertFileSeparators(writeDir+File.separator+
								pubAbbrev+File.separator+journAbbrev+File.separator+
								year+File.separator+issueNum);
						this.process(issueWriteDir, pubAbbrev, journAbbrev, year, issueNum);
						updateProps(downloadLogPath, pubAbbrev, journAbbrev, year, issueNum, CIF2CML);
					}
				} else {
					System.out.println("["+CIF2CML+"] No dates to process at this time for "+pubAbbrev+" journal "+journAbbrev);
				}
			}
		}
	}
	
	/**
	 * Finds all cif files in the folder and converts each to cml 
	 * 
	 * @param issueWriteDir
	 * @param publisherAbbreviation
	 * @param journalAbbreviation
	 * @param year
	 * @param issueNum
	 */
	public void process(String issueWriteDir,
						String publisherAbbreviation,
						String journalAbbreviation,
						String year,
						String issueNum) {
		// go through to the article directories in the issue dir and process all found CIFs
		if (new File(issueWriteDir).exists()) {
			for (File parent : new File(issueWriteDir).listFiles()) {
				String fileName = parent.getAbsolutePath();
				// find CIF files and split them up so that each 'split' cif contains a 
				// data block and a copy of the global block 
				for (File file : parent.listFiles()) {
					fileName = file.getName();
					if (fileName.matches("[^\\._]*\\.cif")) {
						// calculate number of bytes in the file - if it is too large then do not try to parse
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
							
							for (File splitCifFile : splitCifList) {
								// parse split CIF to split cmls
								String pathMinusMime = Utils.getPathMinusMimeSet(splitCifFile);
								String suppId = pathMinusMime.substring(pathMinusMime.lastIndexOf(File.separator)+1);
								String articleId = suppId.substring(0,suppId.indexOf("_"));
								articleId = articleId.replaceAll("sup[\\d]*", "");

								String id = publisherAbbreviation+"_"+journalAbbreviation+"_"+year+"_"+issueNum+"_"+suppId;

								processCif(id, pathMinusMime) ;
							}
						}
					}
				}
			}		
		}
	}

	/**
	 * To ensure that the library that processes cif files doesn't complain. A hack!
	 * @param file
	 * @return
	 */
	private boolean fileTooLarge(File file) {
		boolean tooLarge = false;
		int total = Utils.getFileSizeInBytes(file);
		if (total > MAX_CIF_SIZE_IN_BYTES) {
			System.err.println("CIF file too large to parse.  Skipping: "+file.getAbsolutePath());
			tooLarge = true;
		}
		return tooLarge;
	}
	
	/**
	 * Splits the original cif in the one data block per cif format
	 * @param file
	 * @return
	 */
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

	/**
	 * Process a single non-splitable cif.
	 * @param id molecule id string
	 * @param pathMinusMime path to root for this cif file
	 */
	private void processCif(String id, String pathMinusMime) {

		//
		// set up and run CIFConverter
		String splitCifPath = pathMinusMime+CIF_MIME;
		String outfile = pathMinusMime+RAW_CML_MIME;
		runCIFConverter(splitCifPath, outfile);

		File rawCmlFile = new File(outfile);
		if (!rawCmlFile.exists()) {
			return;
		}

//		this.getCalculatedCheckCif(splitCifPath, pathMinusMime);

		// read raw CML back in and convert to 'complete' CML
		CMLCml cml = null;
		try { 
			cml = (CMLCml)Utils.parseCml(rawCmlFile).getRootElement();
		} catch (Exception e) {
			System.err.println("Error reading CML.");
			return;
		}

		// set molecule ID from issue information
		// These ids are specific to the journals
		// can be ignored for departmental repository
		cml.setId(id);
		CMLMolecule molecule = getMolecule(cml);
		molecule.setId(id);

		// don't want to do molecules that are too large, so if > 1000 atoms, then pass
		if (molecule.getAtomCount() > 1000) {
			return;
		}

		CompoundClass compoundClass = CrystalEyeUtils.getCompoundClass(molecule);
		addCompoundClass(cml, compoundClass);
		try {
			processDisorder(molecule, compoundClass);
			CMLMolecule mergedMolecule = null;
			try {
				mergedMolecule = createFinalStructure(molecule, compoundClass);
			} catch (Exception e) {
				System.err.println("Could not calculate final structure.");
				return;
			}
			boolean isPolymeric = false;
			if (!compoundClass.equals(CompoundClass.INORGANIC)) {
				// if the structure is a polymeric organometal then we want to add 
				// all atoms to the unit cell1
				isPolymeric = isPolymericOrganometal(molecule, mergedMolecule, compoundClass);
				if (!isPolymeric) {
					isPolymeric = isSiO4(mergedMolecule);
				}
				if (isPolymeric) {
					CrystalTool crystalTool = new CrystalTool(molecule);
					mergedMolecule = crystalTool.addAllAtomsToUnitCell(true);
					addPolymericFlag(mergedMolecule);
				}
				if (!isPolymeric) {
					calculateBondsAnd3DStereo(cml, mergedMolecule);
					rearrangeChiralAtomsInBonds(mergedMolecule);
					add2DStereoSMILESAndInChI(mergedMolecule, compoundClass);
				}
			}
			handleCheckcifs(cml, pathMinusMime);
			addDoi(cml, pathMinusMime);
			// need to replace the molecule created from atoms explicit in the CIF with mergedMolecule.
			molecule.detach();
			cml.appendChild(mergedMolecule);
			repositionCMLCrystalElement(cml);
			
			CrystalEyeUtils.writeDateStamp(pathMinusMime+DATE_MIME);
			Utils.writePrettyXML(cml.getDocument(), pathMinusMime+COMPLETE_CML_MIME);
		} catch (RuntimeException e) {
			System.err.println("Error creating complete CML: "+e.getMessage());
		}
	}

	/**
	 * Converts CIF to RawCML
	 * @param infile path to CIF
	 * @param outfile path to output file
	 */
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
	
	/**
	 * Calculate checkCif and writes it out to file
	 * @param cifPath
	 * @param pathMinusMime
	 */
	private void getCalculatedCheckCif(String cifPath, String pathMinusMime) {
		String calculatedCheckCif = calculateCheckcif(cifPath);
		String ccPath = pathMinusMime+".calculated.checkcif.html";
		Utils.writeText(calculatedCheckCif, ccPath);
	}

	/**
	 * Post CIF off to the checkCif service and grabs the returned HTML
	 * @param cifPath
	 * @return
	 */
	private String calculateCheckcif(String cifPath) {
		PostMethod filePost = null;
		InputStream in = null;
		String checkcif = "";

		int maxTries = 5;
		int count = 0;
		boolean finished = false;
		try {
			while(count < maxTries && !finished) {
				count++;
				File f = new File(cifPath);
				filePost = new PostMethod(
				"http://dynhost1.iucr.org/cgi-bin/checkcif.pl");
				Part[] parts = { new FilePart("file", f),
						new StringPart("runtype", "fullpublication"),
						new StringPart("UPLOAD", "Send CIF for checking") };
				filePost.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, 
						new DefaultHttpMethodRetryHandler(5, false));
				filePost.setRequestEntity(new MultipartRequestEntity(parts,
						filePost.getParams()));
				HttpClient client = new HttpClient();
				int statusCode = client.executeMethod(filePost);
				if (statusCode != HttpStatus.SC_OK) {
					System.err.println("Could not connect to the IUCr Checkcif service.");
					continue;
				}
				in = filePost.getResponseBodyAsStream();
				checkcif = XmlUtils.stream2String(in);
				in.close();
				if (checkcif.length() > 0) {
					finished = true;
				}
			}
		} catch (IOException e) {
			System.err.println("Error calculating checkcif.");
		} finally {
			if (filePost != null) {
				filePost.releaseConnection();
			}
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					System.err.println("Error closing InputStream: "+in);
				}
			}
		}
		return checkcif;
	}

	/**
	 * Getter for the molecule
	 * @param cml
	 * @return
	 */
	private CMLMolecule getMolecule(CMLElement cml) {
		Nodes moleculeNodes = cml.query(CMLMolecule.NS, CML_XPATH);
		if (moleculeNodes.size() != 1) {
			throw new RuntimeException("NO MOLECULE FOUND");
		}
		return (CMLMolecule) moleculeNodes.get(0);
	}	
	
	/**
	 * Appends to CML
	 * @param cml
	 * @param compoundClass
	 */
	private void addCompoundClass(CMLCml cml, CompoundClass compoundClass) {
		Element compClass = new Element("scalar", CML_NS);
		compClass.addAttribute(new Attribute("dataType", "xsd:string"));
		compClass.addAttribute(new Attribute("dictRef", "iucr:compoundClass"));
		compClass.appendChild(new Text(compoundClass.toString()));
		cml.appendChild(compClass);
	}
	
	/**
	 * Tries to remove disorder from the structure
	 * @param molecule
	 * @param compoundClass
	 */
	private void processDisorder(CMLMolecule molecule, CompoundClass compoundClass) {
		// sort disorder out per molecule rather than per crystal.  This way if the disorder is
		// invalid for one molecule, we may be able to resolve others within the crystal.
		MoleculeTool moleculeTool = MoleculeTool.getOrCreateTool(molecule);
		moleculeTool.createCartesiansFromFractionals();
		moleculeTool.calculateBondedAtoms();
		ConnectionTableTool ct = new ConnectionTableTool(molecule);
		// don't want to partition inorganics before resolving disorder as
		// chance is that atoms related by disorder won't be connected so partitioning
		// and doing molecule by molecule is a bad idea.
		if (!CompoundClass.INORGANIC.equals(compoundClass)) {
			ct.partitionIntoMolecules();
		}
		for (CMLMolecule mo : molecule.getDescendantsOrMolecule()) {
			try {
				DisorderToolControls dm = new DisorderToolControls(ProcessControl.LOOSE);
				DisorderTool dt = new DisorderTool(mo, dm);
				dt.resolveDisorder();
			} catch (RuntimeException e) {
				e.printStackTrace();
			}
		}
		ct.flattenMolecules();
	}

	/**
	 * Calculates the molecular skeleton from the RawCML data
	 * @param molecule
	 * @param compoundClass
	 * @return
	 * @throws Exception
	 */
	private CMLMolecule createFinalStructure(CMLMolecule molecule,
											CompoundClass compoundClass) throws Exception {
		CrystalTool crystalTool = new CrystalTool(molecule);
		CMLMolecule mergedMolecule = null;
		if (compoundClass.equals(CompoundClass.INORGANIC)) {
			mergedMolecule = crystalTool.addAllAtomsToUnitCell(true);
		} else {
			mergedMolecule = crystalTool.calculateCrystallochemicalUnit(new RealRange(0, 3.3 * 3.3));
		}
		return mergedMolecule;
	}
	
	/**
	 * Calculate whether organometallic structure is polymeric
	 * @param originalMolecule
	 * @param mergedMolecule
	 * @param compoundClass
	 * @return
	 */
	private boolean isPolymericOrganometal(CMLMolecule originalMolecule,
											CMLMolecule mergedMolecule,
											CompoundClass compoundClass) {
		boolean isPolymeric = false;
		if (compoundClass.equals(CompoundClass.ORGANOMETALLIC)) {
			/*
			 * checking for polymeric organometallic structures
			 * to be polymeric we test for the following things:
			 * 1. after symmetry molecule generation, a new metal position must have been generated.
			 * 2. if so, we check for either of the following:
			 *    a. one of the metals in the original molecule having a new bond to an atom with a new id
			 *    b. one of the 'new' bonds generated is also found elsewhere in the generated molecules where 
			 *       both atoms have new IDs.
			 *    If either of the last two points are true, then the structure is polymeric. 
			 * 
			 */	
			List<CMLAtom> originalMetalAtomList = new ArrayList<CMLAtom>();
			if (compoundClass.equals(CompoundClass.ORGANOMETALLIC)) {
				for (CMLAtom atom : originalMolecule.getAtoms()) {
					if (atom.getChemicalElement().isChemicalElementType(Type.METAL)) {
						originalMetalAtomList.add(atom);
					}
				}
			}
			List<String> newMetalAtomIdList = new ArrayList<String>();
			for (CMLAtom atom : mergedMolecule.getAtoms()) {
				if (atom.getChemicalElement().isChemicalElementType(Type.METAL)) {
					newMetalAtomIdList.add(atom.getId());
				}
			}
			if (newMetalAtomIdList.size() > originalMetalAtomList.size()) {
				// check old atoms for bonds to atoms with new IDs (as described in point 'a' above)
				for (CMLAtom atom : originalMolecule.getAtoms()) {
					if (isPolymeric) break;
					String atomId = atom.getId();
					Set<String> origSet = new HashSet<String>();
					for (CMLAtom ligand : atom.getLigandAtoms()) {
						origSet.add(ligand.getId());
					}
					List<CMLAtom> ligandAtoms = mergedMolecule.getAtomById(atomId).getLigandAtoms();
					if (ligandAtoms.size() > origSet.size()) {
						List<String> idList = new ArrayList<String>();
						for (CMLAtom ligand : ligandAtoms) {
							String idStart = getPreUnderscoreStringInAtomId(ligand.getId());
							if (idStart == null) {
								idList.add(ligand.getId());
							} else {
								idList.add(idStart);
							}
						}
						Collections.sort(idList);
						for (CMLAtom a : mergedMolecule.getAtoms()) {
							String aStart = getPreUnderscoreStringInAtomId(a.getId());
							if (aStart != null) {
								if (aStart.equals(atomId)) {
									List<String> ligandIdList = new ArrayList<String>();
									for (CMLAtom l : a.getLigandAtoms()) {
										String lStart = getPreUnderscoreStringInAtomId(l.getId());
										if (lStart == null) {
											ligandIdList.add(l.getId());
										} else {
											ligandIdList.add(lStart);
										}
									}
									Collections.sort(ligandIdList);
									if (!idList.equals(ligandIdList)) {
										isPolymeric = true;
										break;
									}
								}
							}
						}
					}
				}
			}
		}
		return isPolymeric;
	}

	/**
	 * get Pre Underscore String In Atom Id
	 * @param id
	 * @return
	 */
	private String getPreUnderscoreStringInAtomId(String id) {
		if (id.contains(S_UNDER)) {
			return id.substring(0,id.indexOf(S_UNDER));		
		} else {
			return null;
		}
	}
	
	/**
	 * Special case of SiO4 requires iterating twice
	 * @param molecule
	 * @return
	 */
	private boolean isSiO4(CMLMolecule molecule) {
		boolean is = false;
		int overall = 0;
		for (CMLAtom atom : molecule.getAtoms()) {
			if ("Si".equals(atom.getElementType()) && atom.getLigandAtoms().size() == 4) {
				int count = 0;
				for (CMLAtom lig : atom.getLigandAtoms()) {
					if ("O".equals(lig.getElementType())) {
						count++;
					}
				}
				if (count == 4) overall++;
			}
			if (overall >= 5) is = true;
		}
		return is;
	}

	/**
	 * Add flag to say whether the structure is polymeric or not
	 * @param molecule
	 */
	private void addPolymericFlag(CMLMolecule molecule) {
		CMLMetadataList ml = (CMLMetadataList)molecule.getFirstCMLChild(CMLMetadataList.TAG);
		if (ml == null) {
			ml = new CMLMetadataList();
			molecule.appendChild(ml);
		}
		CMLMetadata met = new CMLMetadata();
		ml.appendChild(met);
		met.setAttribute("dictRef", POLYMERIC_FLAG_DICTREF);
	}	

	/**
	 * Runs methods that bond orders and charges to molecules. Also adds stereo chemistry.
	 * @param cml
	 * @param mergedMolecule
	 */
	private void calculateBondsAnd3DStereo(CMLCml cml, CMLMolecule mergedMolecule) {
		for (CMLMolecule subMol : mergedMolecule.getDescendantsOrMolecule()) {
			boolean success = true;
			Nodes nonUnitOccNodes = subMol.query(".//"+CMLAtom.NS+"[@occupancy[. < 1]]", CML_XPATH);
			if (!DisorderTool.isDisordered(subMol) && !subMol.hasCloseContacts() && nonUnitOccNodes.size() == 0) {
				ValencyTool subMolTool = new ValencyTool(subMol);
				int molCharge = ValencyTool.UNKNOWN_CHARGE;
				if (mergedMolecule.getDescendantsOrMolecule().size() == 1) {
					// if there is only one moiety in the crystal, then must have a charge of 0.
					molCharge = 0;
				} else {
					// if more than one moiety, try and get the charge from the formula provided by the CIF.
					getMoietyChargeFromFormula(cml, subMol);
				}
				success = subMolTool.adjustBondOrdersAndChargesToValency(molCharge);
				if (!success) {
					// tag molecule to say we couldn't find a reasonable set of charges/bond orders for it
					addNoBondsOrChargesSetFlag(subMol);
				}
			} else {
				setAllBondOrders(subMol, CMLBond.SINGLE);
			}
			if (success) {
				// remove metals before adding stereochemistry - otherwise
				// bonds to metal confuse the tool
				Map<List<CMLAtom>, List<CMLBond>> metalMap = ValencyTool.removeMetalAtomsAndBonds(subMol);
				StereochemistryTool st = new StereochemistryTool(subMol);
				try {
					st.add3DStereo();
				} catch (RuntimeException e) {
					System.err.println("Error adding 3D stereochemistry.");
				}
				ValencyTool.addMetalAtomsAndBonds(subMol, metalMap);
			}
		}
	}	

	/**
	 * Puts chiral atom as the first atom in the bond
	 * @param molecule
	 */
	public void rearrangeChiralAtomsInBonds(CMLMolecule molecule) {
		for (CMLMolecule subMol : molecule.getDescendantsOrMolecule()) {
			StereochemistryTool st = new StereochemistryTool(subMol);
			List<CMLAtom> chiralAtoms = st.getChiralAtoms();
			List<CMLBond> toRemove = new ArrayList<CMLBond>();
			List<CMLBond> toAdd = new ArrayList<CMLBond>();
			for (CMLBond bond : subMol.getBonds()) {
				CMLAtom secondAtom = bond.getAtom(1);
				if (chiralAtoms.contains(secondAtom)) {
					CMLBond newBond = new CMLBond(bond);
					newBond.setAtomRefs2(bond.getAtom(1).getId()+" "+bond.getAtom(0).getId());
					newBond.resetId(bond.getAtom(1).getId()+"_"+bond.getAtom(0).getId());
					toAdd.add(newBond);
					toRemove.add(bond);
				}
			}
			for (CMLBond bond : toRemove) {
				bond.detach();
			}
			for (CMLBond bond : toAdd) {
				subMol.addBond(bond);
			}
		}
	}

	/**
	 * Calculate identifiers for molecule and add then in
	 * @param molecule
	 * @param compoundClass
	 */
	private void add2DStereoSMILESAndInChI(CMLMolecule molecule, CompoundClass compoundClass) {
		if (containsUnknownBondOrder(molecule)) {
			return;
		}

		List<CMLMolecule> molList = molecule.getDescendantsOrMolecule();
		if (molList.size() > 1) {
			Nodes nonUnitOccNodes = molecule.query(".//"+CMLAtom.NS+"[@occupancy[. < 1]]", CML_XPATH);
			if (!DisorderTool.isDisordered(molecule) && !molecule.hasCloseContacts() && nonUnitOccNodes.size() == 0
					&& hasBondOrdersAndCharges(molecule)) {
				// if mol contains submols (all of which are not disordered!)
				// then we need to generate InChI/SMILES for the containing mol too
				if (CML2FooManager.getNumberOfRings(molecule) < CML2FooManager.MAX_RINGS) {
					calculateAndAddSmilesToMoleculeContainer(molecule);
				}
				addInchiToMolecule(molecule);
			}
		}
		for (CMLMolecule cmlMol : molList) {
			// calculate formula and add in
			try {
				CMLFormula formula = new CMLFormula(cmlMol);
				formula.normalize();
				cmlMol.appendChild(formula);
			} catch (RuntimeException e) {
				System.err.println("Could not generate CMLFormula: "+e.getMessage());
			}

			//FIXME - remove this section and fix CDK instead!
			// calculate the inchi for each sub-molecule and append
			CMLCrystal cryst = (CMLCrystal)cmlMol.getFirstCMLChild(CMLCrystal.TAG);
			CMLCrystal crystCopy = null;
			if (cryst != null) {
				crystCopy = (CMLCrystal)cryst.copy();
				cryst.detach();
			}
			CMLFormula form = (CMLFormula)cmlMol.getFirstCMLChild(CMLFormula.TAG);
			CMLFormula formCopy = null;
			if (form != null) {
				formCopy = (CMLFormula)form.copy();
				form.detach();
			}
			/*-----end section to be removed-----*/

			Nodes nonUnitOccNodes = cmlMol.query(".//"+CMLAtom.NS+"[@occupancy[. < 1]]", CML_XPATH);
			if (!DisorderTool.isDisordered(cmlMol) && !cmlMol.hasCloseContacts() && nonUnitOccNodes.size() == 0
					&& hasBondOrdersAndCharges(cmlMol)) {
				try {
					CDKUtils.add2DCoords(cmlMol);
					new StereochemistryTool(cmlMol).addWedgeHatchBonds();
				} catch (Exception e) {
					System.err.println("Exception adding wedge/hatch bonds to molecule "+cmlMol.getId());;
				}
				if (!compoundClass.equals(CompoundClass.INORGANIC) && 
						(CML2FooManager.getNumberOfRings(cmlMol) < CML2FooManager.MAX_RINGS)) {
					calculateAndAddSmiles(cmlMol);
				}
				addInchiToMolecule(cmlMol);
			}

			//FIXME - remove this section and fix CDK instead!
			if (formCopy != null) cmlMol.appendChild(formCopy);
			if (crystCopy != null) cmlMol.appendChild(crystCopy);
			//------------------------------
			/*-----end section to be removed------*/

			Nodes bonds = cmlMol.query(".//"+CMLBondArray.NS+"/cml:bond", CML_XPATH);
			for (int l = 0; l < bonds.size(); l++) {
				this.addBondLength((CMLBond)bonds.get(l), cmlMol);	
			}	
		}	
	}

	/**
	 * Calculates the smiles for the supplied molecule and appends it
	 * @param mol
	 */
	private void calculateAndAddSmiles(CMLMolecule mol) {
		String smiles = calculateSmiles(mol);
		if (smiles != null) {
			Element scalar = new Element("identifier", CML_NS);
			scalar.addAttribute(new Attribute("convention", "daylight:smiles"));
			scalar.appendChild(new Text(smiles));
			mol.appendChild(scalar);
		}
	}

	/**
	 * If you want to generate the smiles for the unit cell you generate the individual
	 * ones and append them together
	 * @param mol
	 */
	private void calculateAndAddSmilesToMoleculeContainer(CMLMolecule mol) {
		List<CMLMolecule> subMolList = mol.getDescendantsOrMolecule();
		int molCount = subMolList.size();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < molCount; i++) {
			String smiles = calculateSmiles(subMolList.get(i));
			if (smiles != null) {
				sb.append(smiles);
				if (i < molCount-1) {
					sb.append(".");
				}
			} else {
				return;
			}
		}
		Element scalar = new Element("identifier", CML_NS);
		scalar.addAttribute(new Attribute("convention", "daylight:smiles"));
		scalar.appendChild(new Text(sb.toString()));
		mol.appendChild(scalar);
	}

	/**
	 * Use CDK to calculate the smiles from the molecule
	 * @param mol
	 * @return
	 */
	private String calculateSmiles(CMLMolecule mol) {
		if (mol.getAtoms().size() > 0) {
			SmilesGenerator generator = new SmilesGenerator();
			String smiles = "";
			StringBuffer sb = new StringBuffer();
			int count = 0;
			for (CMLMolecule subMol : mol.getDescendantsOrMolecule()) {
				if (count > 0) {
					sb.append(".");
				}
				IMolecule molecule = null;
				try {
					molecule = CDKUtils.cmlMol2CdkMol(subMol);
				} catch (Exception e) {
					System.err.println("Exception creating CDK molecule");
					continue;
				}
				StructureDiagramGenerator sdg = new StructureDiagramGenerator();
				sdg.setMolecule(molecule);
				if (molecule.getAtomCount() == 1) {
					molecule.getAtom(0).setPoint2d(new Point2d(0, 0));
				} else {
					try {
						sdg.generateCoordinates(new Vector2d(0, 1));
						molecule = sdg.getMolecule();
					} catch (Exception e) {
						System.err.println("Error generating molecule coordinates for SMILES generation: "+e.getMessage());
						continue;
					}
				}
				try {
					smiles = generator.createChiralSMILES(molecule, new boolean[20]);
				} catch (CDKException e) {
					System.err.println("Error calculating SMILES for mol "+mol.getId()+" : "+e.getMessage());
					return null;
				}
				sb.append(smiles);
				count++;
			}
			return sb.toString();
		} else {
			return null;
		}
	}

	/**
	 * Append an element to the CML bond to give the bond length
	 * @param bond
	 * @param cmlMol
	 */
	private void addBondLength(CMLBond bond, CMLMolecule cmlMol) {
		String[] atomRefs = bond.getAtomRefs2();
		CMLLength length = new CMLLength();
		length.setAtomRefs2(new String[] {
				atomRefs[0],
				atomRefs[1] 
		});

		double lengthVal = length.getCalculatedLength(cmlMol);
		length.setXMLContent(lengthVal);

		List<CMLAtom> atoms = bond.getAtoms();
		CMLAtom atom0 = atoms.get(0);
		CMLAtom atom1 = atoms.get(1);
		String id0 = atom0.getId();
		String id1 = atom1.getId();
		int a0 = atom0.getAtomicNumber();
		int a1 = atom1.getAtomicNumber();
		String type0 = atom0.getElementType();
		String type1 = atom1.getElementType();
		String atomIdStr = "";
		String elementTypes = "";
		if (a0 > a1) {
			atomIdStr = id1+" "+id0;
			elementTypes = type1+" "+type0;
		} else {
			atomIdStr = id0+" "+id1;
			elementTypes = type0+" "+type1;
		}
		length.getAttribute("atomRefs2").setValue(atomIdStr);
		Attribute eTypes2 = new Attribute("elementTypes2", elementTypes);
		eTypes2.setNamespace("n", NED24_NS);
		length.addAttribute(eTypes2);
		bond.appendChild(length);
	}
	
	/**
	 * Calculate the checkCif for this CML and converts the result of the checkCif
	 * into XML and appends this to the in process CML. Also downloads the ellipsoid plot which
	 * is linked to from the checkCif html 
	 * @param cml
	 * @param pathMinusMime
	 */
	private void handleCheckcifs(CMLCml cml, String pathMinusMime) {
		String depositedCheckcifPath = pathMinusMime.substring(0,pathMinusMime.lastIndexOf(File.separator));
		String depCCParent = new File(depositedCheckcifPath).getParent();
		depositedCheckcifPath = depCCParent+pathMinusMime.substring(pathMinusMime.lastIndexOf(File.separator),pathMinusMime.lastIndexOf("_"))+".deposited.checkcif.html";
		String calculatedCheckcifPath = pathMinusMime+".calculated.checkcif.html";
		File depositedCheckcif = new File(depositedCheckcifPath);
		File calculatedCheckcif = new File(calculatedCheckcifPath);
		if (depositedCheckcif.exists()) {
			String contents = Utils.file2String(depositedCheckcifPath);
			Document deposDoc = new CheckCifParser(contents).parseDeposited();
			cml.appendChild(deposDoc.getRootElement().copy());
		}
		if (calculatedCheckcif.exists()) {
			String contents = Utils.file2String(calculatedCheckcifPath);
			Document calcDoc = new CheckCifParser(contents).parseCalculated();
			cml.appendChild(calcDoc.getRootElement().copy());
			this.getPlatonImage(calcDoc, pathMinusMime);	
		}
	}
	
	/**
	 * Extracts the URL of the ellipsoid plot from the checkCif and downloads it
	 * @param doc
	 * @param pathMinusMime
	 */
	private void getPlatonImage(Document doc, String pathMinusMime) {
		// get platon from parsed checkcif/store
		Nodes platonLinks = doc.query("//x:checkCif/x:calculated/x:dataBlock/x:platon/x:link", new XPathContext("x", "http://journals.iucr.org/services/cif"));
		if (platonLinks.size() > 0) {
			URL url = null;
			try {
				String imageLink = platonLinks.get(0).getValue();
				String prefix = imageLink.substring(0, imageLink.lastIndexOf("/")+1);
				String file = imageLink.substring(imageLink.lastIndexOf("/")+1);
				url = new URL(prefix+URLEncoder.encode(file,"UTF-8")); 
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			BufferedImage image = null;
			try {
				image = ImageIO.read(url);
				image = image.getSubimage(14, 15, 590, 443);
				ImageIO.write(image, "jpeg", new File(pathMinusMime+".platon.jpeg"));
			} catch (IOException e) {
				System.err.println("ERROR: could not read PLATON image");
			}
		}	
	}
	
	/**
	 * Adds an element containing the article DOI
	 * @param cml
	 * @param pathMinusMime
	 */
	private void addDoi(CMLCml cml, String pathMinusMime) {
		String parent = pathMinusMime.substring(0,pathMinusMime.lastIndexOf(File.separator));
		parent = new File(parent).getParent();
		String doiPath = parent+pathMinusMime.substring(pathMinusMime.lastIndexOf(File.separator),pathMinusMime.lastIndexOf("_"));
		doiPath = doiPath.replaceAll("sup[\\d]*", "")+".doi";
		if (new File(doiPath).exists()) {
			String doiString = Utils.file2String(doiPath);
			Element doi = new Element("scalar", CML_NS);
			doi.addAttribute(new Attribute("dictRef", "idf:doi"));
			doi.appendChild(new Text(doiString));
			cml.appendChild(doi);
		}
	}

	/**
	 * Repositions the crystal element to be the first child of the molecule
	 * @param cml
	 */
	private void repositionCMLCrystalElement(CMLCml cml) {
		Nodes crystalNodes = cml.query(".//"+CMLCrystal.NS, CML_XPATH);
		if (crystalNodes.size() > 0) {
			CMLCrystal crystal = (CMLCrystal)crystalNodes.get(0);
			CMLCrystal crystalC = (CMLCrystal)crystal.copy();
			crystal.detach();
			CMLMolecule molecule = (CMLMolecule)cml.getFirstCMLChild(CMLMolecule.TAG);
			molecule.insertChild(crystalC, 0);
		} else {
			throw new RuntimeException("Should have found a CMLCrystal element as child of CMLCml.");
		}
	}

	
	
	




	/**
	 * Looks for flag that says if processing for bond orders and charges has been successful
	 * @param molecule
	 * @return
	 */
	public static boolean hasBondOrdersAndCharges(CMLMolecule molecule) {
		boolean hasBOAC = true;
		Nodes flagNodes = molecule.query(".//"+CMLMetadata.NS+"[@dictRef='"+NO_BONDS_OR_CHARGES_FLAG_DICTREF+"']", CML_XPATH);
		if (flagNodes.size() > 0) {
			hasBOAC = false;
		}
		return hasBOAC;
	}

	/**
	 * Add flag says if processing for bond orders and charges has been successful
	 * @param molecule
	 */
	private void addNoBondsOrChargesSetFlag(CMLMolecule molecule) {
		CMLMetadataList ml = (CMLMetadataList)molecule.getFirstCMLChild(CMLMetadataList.TAG);
		if (ml == null) {
			ml = new CMLMetadataList();
			molecule.appendChild(ml);
		}
		CMLMetadata met = new CMLMetadata();
		ml.appendChild(met);
		met.setAttribute("dictRef", NO_BONDS_OR_CHARGES_FLAG_DICTREF);
	}
	
	/**
	 * Does the molecule contain a bond order that hasn't been found
	 * @param molecule
	 * @return
	 */
	private boolean containsUnknownBondOrder(CMLMolecule molecule) {
		boolean b = false;
		for (CMLBond bond : molecule.getBonds()) {
			if (CMLBond.UNKNOWN_ORDER.equals(bond.getOrder())) {
				b = true;
				break;
			}
		}
		return b;
	}

	/**
	 * Compares a molecule against a formula to find what the charge is for the molecule
	 * @param cml
	 * @param molecule
	 * @return
	 */
	private int getMoietyChargeFromFormula(CMLCml cml, CMLMolecule molecule) {
		int molCharge = ValencyTool.UNKNOWN_CHARGE;
		Nodes moiFormNodes = cml.query(".//"+CMLFormula.NS+"[@dictRef='iucr:_chemical_formula_moiety']", CML_XPATH);
		CMLFormula moietyFormula = null;
		if (moiFormNodes.size() > 0) {
			moietyFormula = (CMLFormula)moiFormNodes.get(0);
			// get a list of formulas for the moieties. 
			List<CMLFormula> moietyFormulaList = new ArrayList<CMLFormula>();
			if (moietyFormula != null) {
				moietyFormulaList = moietyFormula.getFormulaElements().getList();
				if (moietyFormulaList.size() == 0) {
					moietyFormulaList.add(moietyFormula);
				}
				for (CMLFormula formula : moietyFormulaList) {
					CMLFormula molForm = molecule.calculateFormula(HydrogenControl.USE_EXPLICIT_HYDROGENS);
					if (molForm.getConciseNoCharge().equals(formula.getConciseNoCharge())) {
						molCharge = formula.getFormalCharge();
					}
				}
			}
		}
		return molCharge;
	}

	/**
	 * Set all bond orders in the molecule to the order provided
	 * @param molecule
	 * @param order
	 */
	private void setAllBondOrders(CMLMolecule molecule, String order) {
		for (CMLBond bond : molecule.getBonds()) {
			bond.setOrder(order);
		}
	}

	/**
	 * Calculates the Inchi for the supplied molecule and appends it
	 * @param molecule
	 */
	private void addInchiToMolecule(CMLMolecule molecule) {
		String inchi = new InChIGeneratorTool().generateInChI(molecule);
	}


}
