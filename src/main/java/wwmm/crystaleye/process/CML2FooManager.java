package wwmm.crystaleye.process;

import static org.xmlcml.cml.base.CMLConstants.CML_NS;
import static org.xmlcml.cml.base.CMLConstants.CML_XPATH;
import static org.xmlcml.euclid.EuclidConstants.S_UNDER;
import static wwmm.crystaleye.CrystalEyeConstants.CIF2CML;
import static wwmm.crystaleye.CrystalEyeConstants.CML2FOO;
import static wwmm.crystaleye.CrystalEyeConstants.COMPLETE_CML_MIME;
import static wwmm.crystaleye.CrystalEyeConstants.NED24_NS;
import static wwmm.crystaleye.CrystalEyeConstants.POLYMERIC_FLAG_DICTREF;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

import nu.xom.Attribute;
import nu.xom.Element;
import nu.xom.Node;
import nu.xom.Nodes;
import nu.xom.Text;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.layout.StructureDiagramGenerator;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.xmlcml.cml.base.CMLUtil;
import org.xmlcml.cml.element.CMLAngle;
import org.xmlcml.cml.element.CMLAtom;
import org.xmlcml.cml.element.CMLAtomParity;
import org.xmlcml.cml.element.CMLAtomSet;
import org.xmlcml.cml.element.CMLBond;
import org.xmlcml.cml.element.CMLBondSet;
import org.xmlcml.cml.element.CMLBondStereo;
import org.xmlcml.cml.element.CMLCml;
import org.xmlcml.cml.element.CMLCrystal;
import org.xmlcml.cml.element.CMLIdentifier;
import org.xmlcml.cml.element.CMLLength;
import org.xmlcml.cml.element.CMLMetadata;
import org.xmlcml.cml.element.CMLMolecule;
import org.xmlcml.cml.element.CMLTorsion;
import org.xmlcml.cml.tools.ConnectionTableTool;
import org.xmlcml.cml.tools.DisorderTool;
import org.xmlcml.cml.tools.GeometryTool;
import org.xmlcml.cml.tools.MoleculeTool;
import org.xmlcml.cml.tools.ValencyTool;
import org.xmlcml.molutil.ChemicalElement;
import org.xmlcml.molutil.ChemicalElement.Type;

import wwmm.crystaleye.AbstractManager;
import wwmm.crystaleye.CDKUtils;
import wwmm.crystaleye.CrystalEyeProperties;
import wwmm.crystaleye.CrystalEyeUtils;
import wwmm.crystaleye.IOUtils;
import wwmm.crystaleye.Utils;
import wwmm.crystaleye.CrystalEyeUtils.CompoundClass;
import wwmm.crystaleye.fetch.IssueDate;
import wwmm.crystaleye.tools.Cml2PngTool;
import wwmm.crystaleye.tools.InchiTool;

public class CML2FooManager extends AbstractManager {
	
	private static final Logger LOG = Logger.getLogger(CML2FooManager.class);

	private CrystalEyeProperties properties;

	private String doi;
	static final int MAX_RINGS = 15;

	public CML2FooManager() {
		;
	}

	public CML2FooManager(File propertiesFile) {
		this.setProperties(propertiesFile);
	}

	public CML2FooManager(String propertiesPath) {
		this(new File(propertiesPath));
	}

	private void setProperties(File propertiesFile) {
		properties = new CrystalEyeProperties(propertiesFile);
	}

	public void execute() {
		String[] publisherAbbreviations = properties.getPublisherAbbreviations();
		for (String publisherAbbreviation : publisherAbbreviations) {
			String[] journalAbbreviations = properties.getPublisherJournalAbbreviations(publisherAbbreviation);
			for (String journalAbbreviation : journalAbbreviations) {
				String downloadLogPath = properties.getDownloadLogPath();
				List<IssueDate> unprocessedDates = this.getUnprocessedDates(downloadLogPath, publisherAbbreviation, journalAbbreviation, CML2FOO, CIF2CML);
				if (unprocessedDates.size() != 0) {
					for (IssueDate date : unprocessedDates) {
						String writeDir = properties.getWriteDir();
						String year = date.getYear();
						String issueNum = date.getIssue();
						String issueWriteDir = FilenameUtils.separatorsToUnix(writeDir+"/"+
								publisherAbbreviation+"/"+journalAbbreviation+
								"/"+year+"/"+issueNum);
						process(issueWriteDir, publisherAbbreviation, journalAbbreviation, year, issueNum);
						updateProps(downloadLogPath, publisherAbbreviation, journalAbbreviation, year, issueNum, CML2FOO);
					}
				} else {
					LOG.info("No dates to process at this time for "+publisherAbbreviation+" journal "+journalAbbreviation);
				}
			}
		}
	}

	public void process(String issueWriteDir, String publisherAbbreviation, String journalAbbreviation, String year, String issueNum) {
		// go through to the article directories in the issue dir and process all found raw CML files
		if (new File(issueWriteDir).exists()) {
			File[] parentList = new File(issueWriteDir).listFiles();
			if (parentList.length > 0) {
				for (File parent : parentList) {
					File[] fileList = parent.listFiles();
					for (File file : fileList) {
						if (file.isDirectory()) {
							File[] structureFiles = file.listFiles();
							for (File structureFile : structureFiles) {
								if (structureFile.getName().endsWith(COMPLETE_CML_MIME)) {
									String pathMinusMime = Utils.getPathMinusMimeSet(structureFile);
									String suppId = pathMinusMime.substring(pathMinusMime.lastIndexOf(File.separator)+1);
									String articleId = suppId.substring(0,suppId.indexOf("_"));
									articleId = articleId.replaceAll("sup[\\d]*", "");
									CMLCml cml = null;
									try {
										cml = (CMLCml)(IOUtils.parseCml(structureFile)).getRootElement();
									} catch (Exception e) {
										System.err.println("Error parsing CML file: "+e.getMessage());
									}
									if (cml == null) {
										continue;
									}
									Nodes classNodes = cml.query(".//cml:scalar[@dictRef='iucr:compoundClass']", CML_XPATH);
									String compClass = "";
									if (classNodes.size() > 0) {
										compClass = ((Element)classNodes.get(0)).getValue();
									} else {
										throw new RuntimeException("Molecule should have a compoundClass scalar set.");
									}
									boolean isPolymeric = false;
									Nodes polymericNodes = cml.query(".//"+CMLMetadata.NS+"[@dictRef='"+
											POLYMERIC_FLAG_DICTREF+"']", CML_XPATH);
									if (polymericNodes.size() > 0) {
										isPolymeric = true;
									}

									if (!compClass.equals(CompoundClass.INORGANIC.toString()) && !isPolymeric) {
										CMLMolecule molecule = (CMLMolecule)cml.getFirstCMLChild(CMLMolecule.TAG);

										if (molecule.getAtomCount() < 1000) {
											List<Node> bondLengthNodes = CMLUtil.getQueryNodes(molecule, ".//cml:length", CML_XPATH);
											for (Node bondLengthNode : bondLengthNodes) {
												bondLengthNode.detach();
											}

											//don't generate image if the molecule is disordered
											List<CMLMolecule> uniqueMolList = CrystalEyeUtils.getUniqueSubMolecules(molecule);	
											int count = 1;
											for (CMLMolecule subMol : uniqueMolList) {	
												Nodes nonUnitOccNodes = subMol.query(".//"+CMLAtom.NS+"[@occupancy[. < 1]]", CML_XPATH);
												if (!DisorderTool.isDisordered(subMol) && !subMol.hasCloseContacts() && nonUnitOccNodes.size() == 0
														&& Cif2CmlManager.hasBondOrdersAndCharges(subMol)) {
													if (CrystalEyeUtils.isBoringMolecule(subMol)) {
														continue;
													}
													write2dImages(subMol, pathMinusMime+"_"+count);
													count++;
												}
											}

											// set doi
											Nodes doiNodes = cml.query("//cml:scalar[@dictRef='idf:doi']", CML_XPATH);
											this.doi = "";
											if (doiNodes.size() > 0) {
												this.doi = ((Element)doiNodes.get(0)).getValue();
											}

											File structureParent = structureFile.getParentFile();
											String structureId = structureParent.getName();					
											File moietyDir = new File(structureParent, "moieties");
											try {
												outputMoieties(moietyDir, structureId, molecule, compClass);
											} catch(Exception e) {
												System.err
														.println("Error while outputting moieties: "+e.getMessage());
											}
										}
									}
								}
							}
						}
					}
				}		
			}
		}
	}

	private void write2dImages(CMLMolecule molecule, String pathMinusMime) {
		write2dImage(pathMinusMime+".small.png", molecule, 358, 278, false);
		write2dImage(pathMinusMime+".png", molecule, 600, 600, false);
	}

	private void writeXML(String filename, CMLMolecule molecule, String message) {
		try {
			File file = new File(filename);
			File parent = file.getParentFile();
			if (!parent.exists()) {
				parent.mkdirs();
			}
			molecule.serialize(new FileOutputStream(file), 1);
		} catch (IOException e) {
			throw new RuntimeException("ERROR "+e);
		}
	}

	private void addSmiles2Molecule(String smiles, CMLMolecule mol) {
		if (smiles != null && !"".equals(smiles)) {
			Element identifier = new Element("identifier", CML_NS);
			identifier.addAttribute(new Attribute("convention", "daylight:smiles"));
			identifier.appendChild(new Text(smiles));
			mol.appendChild(identifier);
		}
	}

	private String calculateSmiles(CMLMolecule mol) {
		SmilesGenerator generator = new SmilesGenerator();
		String smiles = "";
		StringBuffer sb = new StringBuffer();
		int count = 0;
		for (CMLMolecule subMol : mol.getDescendantsOrMolecule()) {
			if (count > 0) {
				sb.append(".");
			}
			IMolecule molecule = CDKUtils.cmlMol2CdkMol(subMol);
			StructureDiagramGenerator sdg = new StructureDiagramGenerator();
			sdg.setMolecule(molecule);
			if (molecule.getAtomCount() == 1) {
				molecule.getAtom(0).setPoint2d(new Point2d(0, 0));
			} else {
				try {
					sdg.generateCoordinates(new Vector2d(0, 1));
					molecule = sdg.getMolecule();
				} catch (Exception e) {
					System.err.println("Error generating molecule coordinates: "+e.getMessage());
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
	}

	private void write2dImage(String path, CMLMolecule molecule, int width, int height, boolean showH) {
		try {
			if (molecule.isMoleculeContainer()) {
				throw new RuntimeException("Molecule should not contain molecule children.");
			}
			File file = new File(path).getParentFile();
			if (!file.exists()) {
				file.mkdirs();
			}
			CDKUtils.add2DCoords(molecule);
			Cml2PngTool cp = new Cml2PngTool(molecule);
			cp.setWidthAndHeight(width, height);
			cp.renderMolecule(path);
		} catch (Exception e) {
			System.err.println("Could not produce 2D image for molecule.");
		}
	}

	private void writeGeometryHtml(List list, String filename, CMLMolecule molecule, int folderDepth) {
		String displayPathPrefix = "";
		for (int i = 0; i < folderDepth; i++) {
			displayPathPrefix += "../";
		}

		String minusMime = Utils.getPathMinusMimeSet(filename);
		String minCmlPath = minusMime.substring(minusMime.lastIndexOf(File.separator)+1)+COMPLETE_CML_MIME;
		String content = "";
		String title = "";
		StringWriter sw = null;
		try {
			sw = new StringWriter();
			if (list.size() == 0) {
				sw.write("<b>NO GEOMETRY</b>\n");
			} else if (list.get(0) instanceof CMLLength) {
				outputCMLLengthHTML(sw, list, molecule); 
				title = "Bond Length Summary";
			} else if (list.get(0) instanceof CMLAngle) {
				outputCMLAngleHTML(sw, list, molecule);
				title = "Bond Angle Summary";
			} else if (list.get(0) instanceof CMLTorsion) {
				outputCMLTorsionHTML(sw, list, molecule);
				title = "Bond Torsion Summary";
			}
			content = sw.toString();
			sw.close();
		} catch (IOException e) {
			throw new RuntimeException("ERROR "+e);
		} finally {
			if (sw != null)
				try {
					sw.close();
				} catch (IOException e) {
					System.err.println("Cannot close writer: " + sw);
				}
		}

		String page = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">"+
		"<html>"+
		"<head>"+
		"<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">"+
		"<title>"+title+"</title>"+
		"<link rel=\"stylesheet\" type=\"text/css\" href=\""+displayPathPrefix+"display/bonds.css\" title=\"screen stylesheet\" media=\"screen\" />"+
		"<link rel=\"Top\" href=\"http://wwmm.ch.cam.ac.uk\" />"+
		"<script src=\""+displayPathPrefix+"Jmol.js\" type=\"text/javascript\"></script>"+
		"</head>"+
		"<body topmargin=\"0\" rightmargin=\"0\" leftmargin=\"0\" height=\"100%\" id=\"page_abstract\" bgcolor=\"#ffffff\" marginheight=\"0\" marginwidth=\"0\" text=\"#000000\">"+
		"<div id=\"header\">"+	
		"<h1>"+
		title+
		"</h1>"+
		"</div>"+
		"<script type=\"text/javascript\">jmolInitialize(\""+displayPathPrefix+"\");</script>"+
		"<div>"+
		"<div id=\"content\">"+
		"<div id=\"table\">"+
		content+
		"</div>"+
		"</div>"+
		"<div id=\"render\">"+
		"<div id=\"jmol\">"+
		"<script type=\"text/javascript\">"+
		"jmolApplet(370, \"load ./"+minCmlPath+";\");"+
		"</script>"+
		"</div>"+
		"</div>"+
		"</div>"+
		"</body>"+
		"</html>";

		IOUtils.writeText(new File(filename), page);					
	}

	private String getOutfile(File writeDir, String id, 
			String fragType, String typePrefix, int subMol, int serial, String mime) {
		String s = writeDir.getAbsolutePath();
		File dir = new File(s + "/" + fragType);
		if (!dir.exists()) {
			dir.mkdir();
		}
		if (!"".equalsIgnoreCase(typePrefix)) {
			fragType = fragType+"-"+typePrefix;
		}
		s = dir + "/" + id+"_"+fragType;
		if (subMol > 0) {
			s+= S_UNDER + subMol;
		}
		s += S_UNDER+serial+mime;

		return s;
	}

	private void addAtomSequenceNumbers(CMLMolecule mol) {
		if (mol.isMoleculeContainer()) throw new RuntimeException("Molecule should be a molecule container.");
		int i = 0;
		for (CMLAtom atom : mol.getAtoms()) {
			Attribute sequenceNumber = new Attribute("sequenceNumber", String.valueOf(i+1));
			sequenceNumber.setNamespace("n", NED24_NS);
			atom.addAttribute(sequenceNumber);
			i++;
		}
	}

	private CMLMolecule addAndMarkFragmentRAtoms(CMLMolecule mol, CMLAtomSet atomSet) {
		CMLAtomSet newAS = new CMLAtomSet();
		List<CMLAtom> atoms = atomSet.getAtoms();
		List<String> rGroupIds = new ArrayList<String>();
		for (CMLAtom atom : atoms) {
			newAS.addAtom(atom);
			List<CMLAtom> ligandList = atom.getLigandAtoms();
			for (CMLAtom ligand : ligandList) {
				if (!atomSet.contains(ligand)) {
					newAS.addAtom(ligand);	
					rGroupIds.add(ligand.getId());
					mol.getBond(atom, ligand).setOrder(CMLBond.SINGLE);
				}
			}
		}
		CMLBondSet newBS = new MoleculeTool(mol).getBondSet(newAS);
		CMLMolecule newMol = MoleculeTool.createMolecule(newAS, newBS);
		List<CMLBond> removeList = new ArrayList<CMLBond>();
		for (String id : rGroupIds) {
			CMLAtom at = newMol.getAtomById(id);
			at.setElementType("R");
			at.setFormalCharge(0);
			// set single bonds to all R groups
			List<CMLBond> bonds = at.getLigandBonds();
			for (CMLBond bond : bonds) {
				// remove bonds between two R groups
				List<CMLAtom> ats = bond.getAtoms();
				if ("R".equalsIgnoreCase(ats.get(0).getElementType()) && "R".equalsIgnoreCase(ats.get(1).getElementType())) {
					if (!removeList.contains(bond)) {
						removeList.add(bond);
					}
				}
				bond.setAttribute("order", CMLBond.SINGLE);
			}
		}
		for (CMLBond bond : removeList) {
			bond.detach();
		}
		return newMol;
	}


	private void outputFragments(File dir, String id, CMLMolecule molecule, String compoundClass,
			String fragType, int depth) {
		removeStereoInformation(molecule);
		List<CMLMolecule> subMoleculeList = molecule.getDescendantsOrMolecule();
		int subMolCount = 0;
		boolean sprout = false;
		for (CMLMolecule subMolecule : subMoleculeList) {
			MoleculeTool mt = new MoleculeTool(subMolecule);
			List<CMLMolecule> fragmentList = new ArrayList<CMLMolecule>();
			if ("chain-nuc".equals(fragType)) {
				molecule = new CMLMolecule(molecule);
				subMolecule = new CMLMolecule(subMolecule);
				ValencyTool.removeMetalAtomsAndBonds(subMolecule);
				new ConnectionTableTool(subMolecule).partitionIntoMolecules();
				for (CMLMolecule subSubMol : subMolecule.getDescendantsOrMolecule()) {
					fragmentList.addAll(new MoleculeTool(subSubMol).getChainMolecules());
				}
			} else if ("ring-nuc".equals(fragType)) {
				molecule = new CMLMolecule(molecule);
				subMolecule = new CMLMolecule(subMolecule);
				ValencyTool.removeMetalAtomsAndBonds(subMolecule);
				new ConnectionTableTool(subMolecule).partitionIntoMolecules();
				for (CMLMolecule subSubMol : subMolecule.getDescendantsOrMolecule()) {
					fragmentList.addAll(new MoleculeTool(subSubMol).getRingNucleiMolecules());
				}
				sprout = true;
			} else if ("cluster-nuc".equals(fragType)) {
				List<Type> typeList = new ArrayList<Type>();
				typeList.add(ChemicalElement.Type.METAL);
				fragmentList = mt.createClusters(typeList);
				sprout = true;
			} else if ("ligand".equals(fragType)) {
				List<Type> typeList = new ArrayList<Type>();
				typeList.add(ChemicalElement.Type.METAL);
				fragmentList = mt.createLigands(typeList);
			} else {
				throw new IllegalArgumentException("Illegal type of fragment.");
			}
			subMolCount++;
			int count = 0;
			for (CMLMolecule fragment : fragmentList) {
				count++;
				CMLAtomSet atomSet = new CMLAtomSet(subMolecule, new MoleculeTool(fragment).getAtomSet().getAtomIDs());
				writeFragmentFiles(molecule, subMolecule, compoundClass, fragment, fragType, subMolCount, count, 
						dir, id, depth);
				if (sprout) {
					CMLMolecule sproutMol = mt.sprout(atomSet);
					int nucCount = fragment.getAtomCount();
					int spCount = sproutMol.getAtomCount();
					if (nucCount < spCount) {
						writeFragmentFiles(molecule, subMolecule, compoundClass, sproutMol, fragType+"-sprout-1", subMolCount, count, 
								dir, id, depth);
						CMLAtomSet spAtomSet = new CMLAtomSet(subMolecule, new MoleculeTool(sproutMol).getAtomSet().getAtomIDs());
						CMLMolecule sprout2Mol = mt.sprout(spAtomSet);
						int sp2Count = sprout2Mol.getAtomCount();
						if (spCount < sp2Count && sp2Count < molecule.getAtomCount()) {
							writeFragmentFiles(molecule, subMolecule, compoundClass, sprout2Mol, fragType+"-sprout-2", subMolCount, count, 
									dir, id, depth);
						}
					}	
				}
			}
		}
	}

	private void removeStereoInformation(CMLMolecule molecule) {
		Nodes atomParityNodes = molecule.query(".//"+CMLAtomParity.NS, CML_XPATH);
		for (int i = 0; i < atomParityNodes.size(); i++) {
			atomParityNodes.get(i).detach();
		}
		Nodes bondStereoNodes = molecule.query(".//"+CMLBondStereo.NS, CML_XPATH);
		for (int i = 0; i < bondStereoNodes.size(); i++) {
			bondStereoNodes.get(i).detach();
		}
	}

	private void writeFragmentFiles(CMLMolecule originalMolecule, CMLMolecule subMolecule, 
			String compoundClass, CMLMolecule fragment, String fragType, int subMolCount, int count,
			File dir, String id, int depth) {
		CMLMolecule subMolCopy = new CMLMolecule(subMolecule);
		CMLMolecule fragCopy = new CMLMolecule(fragment);
		CMLAtomSet atomSet = new CMLAtomSet(subMolCopy, new MoleculeTool(fragCopy).getAtomSet().getAtomIDs());
		CMLMolecule molR = addAndMarkFragmentRAtoms(subMolCopy, atomSet);

		addInChIToRGroupMolecule(molR);
		if (!compoundClass.equals(CompoundClass.INORGANIC.toString())) {
			if (getNumberOfRings(fragCopy) < MAX_RINGS) {
				String smiles = calculateSmiles(fragCopy);
				if (smiles != null) {
					addSmiles2Molecule(smiles, molR);
				}
			}
		}

		addAtomSequenceNumbers(molR);
		addDoi(molR);
		molR.setId(subMolecule.getId()+"_"+fragType+"_"+subMolCount+"_"+count);
		String outfile = getOutfile(dir, id, fragType, "", subMolCount, count, COMPLETE_CML_MIME);
		writeXML(outfile, molR, fragType);
		String pathMinusMime = Utils.getPathMinusMimeSet(outfile);

		writeGeometryHtmlFiles(molR, pathMinusMime, depth);
		for (CMLAtom atom : molR.getAtoms()) {
			if ("R".equals(atom.getChemicalElement().getSymbol())) {
				atom.setElementType("Xx");
			}
		}
		write2dImages(molR, pathMinusMime);
	}

	private void outputMoieties(File dir, String id, CMLMolecule mergedMolecule, String compoundClass) {
		int moiCount = 0;
		for (CMLMolecule mol : mergedMolecule.getDescendantsOrMolecule()) {
			Nodes nonUnitOccNodes = mol.query(".//"+CMLAtom.NS+"[@occupancy[. < 1]]", CML_XPATH);
			if (!DisorderTool.isDisordered(mol) && !mol.hasCloseContacts() && nonUnitOccNodes.size() == 0
					&& Cif2CmlManager.hasBondOrdersAndCharges(mol)) {
				moiCount++;
				if (CrystalEyeUtils.isBoringMolecule(mol)) continue;
				// remove crystal nodes from molecule if they exist
				Nodes crystNodes = mol.query(".//"+CMLCrystal.NS, CML_XPATH);
				for (int i = 0; i < crystNodes.size(); i++) {
					crystNodes.get(i).detach();
				}

				String moiName = id+"_moiety_"+moiCount;
				String moiDir = dir+"/"+moiName;
				String outPath = moiDir+"/"+moiName+COMPLETE_CML_MIME;
				String pathMinusMime = outPath.substring(0,outPath.indexOf(COMPLETE_CML_MIME));
				addDoi(mol);
				writeXML(outPath, mol, "moiety");
				String smallPngPath = pathMinusMime+".small.png";
				String pngPath = pathMinusMime+".png";
				write2dImage(pngPath, mol, 600, 600, true);
				write2dImage(smallPngPath, mol, 358, 278, true);
				addAtomSequenceNumbers(mol);
				writeGeometryHtmlFiles(mol, pathMinusMime, 5);	
				// remove atom sequence numbers from mol now it has been written so they
				// don't interfere with the fragments that are written
				for (CMLAtom atom : mol.getAtoms()) {
					Attribute att = atom.getAttribute("sequenceNumber", NED24_NS);
					if (att != null) {
						att.detach();
					}
				}

				File fragmentDir = new File(moiDir, "fragments");
				outputFragments(fragmentDir, id, mol, compoundClass, "chain-nuc", 7);
				outputFragments(fragmentDir, id, mol, compoundClass, "ring-nuc", 7);
				outputFragments(fragmentDir, id, mol, compoundClass, "cluster-nuc", 7);
				outputFragments(fragmentDir, id, mol, compoundClass, "ligand", 7);
				outputAtomCenteredSpecies(fragmentDir, id, mol, compoundClass);
			}
		}
	}

	private void outputAtomCenteredSpecies(File dir, String id, CMLMolecule mergedMolecule, String compoundClass) {
		List<CMLMolecule> subMoleculeList = mergedMolecule.getDescendantsOrMolecule();
		int subMol = 0;
		for (CMLMolecule subMolecule : subMoleculeList) {
			CMLMolecule subMolCopy = new CMLMolecule(subMolecule);
			MoleculeTool subMoleculeTool = new MoleculeTool(subMolecule);
			subMol++;
			List<CMLAtom> atoms = subMolecule.getAtoms();
			int atomCount = 0;
			for (CMLAtom atom : atoms) {
				ChemicalElement element = atom.getChemicalElement();
				if (element.isChemicalElementType(Type.METAL)) {
					atomCount++;
					Set<CMLAtom> atomSet = new HashSet<CMLAtom>();
					atomSet.add(atom);
					CMLAtomSet singleAtomSet = new CMLAtomSet(atomSet);
					CMLMolecule atomR = addAndMarkFragmentRAtoms(subMolCopy, singleAtomSet);
					CMLMolecule atomMol = new CMLMolecule();
					atomMol.addAtom((CMLAtom)atom.copy());

					String smiles = "";
					if (!compoundClass.equals(CompoundClass.INORGANIC.toString())) {
						// need to calculate inchi and smiles before R groups added
						addInChIToRGroupMolecule(atomR);
						smiles = calculateSmiles(atomMol);
						addSmiles2Molecule(smiles, atomR);
					}

					addAtomSequenceNumbers(atomR);
					addDoi(atomR);
					atomR.setId(subMolecule.getId()+"_atom-nuc_"+subMol+"_"+atomCount);
					String outfile = getOutfile(dir, id, "atom-nuc", "", subMol, atomCount, COMPLETE_CML_MIME);
					writeXML(outfile, atomR, "atom-nuc");

					String pathMinusMime = Utils.getPathMinusMimeSet(outfile);
					for (CMLAtom at : atomR.getAtoms()) {
						if ("R".equals(at.getChemicalElement().getSymbol())) {
							at.setElementType("Xx");
						}
					}
					write2dImages(atomR, pathMinusMime);

					// there will be no geometry so don't try to calculate
					writeGeometryHtmlFiles(atomR, pathMinusMime, 7);

					//sprout once
					CMLMolecule sprout = subMoleculeTool.sprout(singleAtomSet);
					CMLAtomSet spAtomSet = new CMLAtomSet(subMolecule, new MoleculeTool(sprout).getAtomSet().getAtomIDs());

					CMLMolecule sproutR = addAndMarkFragmentRAtoms(subMolCopy, spAtomSet);
					if (sproutR.getAtomCount() == sprout.getAtomCount()) {
						continue;
					}
					// need to calculated inchi and smiles before R groups added
					addInChIToRGroupMolecule(sproutR);
					if (!compoundClass.equals(CompoundClass.INORGANIC.toString())) {
						if (getNumberOfRings(sprout) < MAX_RINGS) {
							smiles = calculateSmiles(sprout);
							addSmiles2Molecule(smiles, sproutR);
						}
					}

					addAtomSequenceNumbers(sproutR);
					addDoi(sproutR);
					sproutR.setId(subMolecule.getId()+"_atom-nuc-sprout-1_"+subMol+"_"+atomCount);
					outfile = getOutfile(dir, id, "atom-nuc-sprout-1", "", subMol, atomCount, COMPLETE_CML_MIME);
					pathMinusMime = Utils.getPathMinusMimeSet(outfile);	          

					writeXML(outfile, sproutR, "atom-nuc-sprout-1");
					for (CMLAtom at : sproutR.getAtoms()) {
						if ("R".equals(at.getChemicalElement().getSymbol())) {
							at.setElementType("Xx");
						}
					}
					write2dImages(sproutR, pathMinusMime);

					writeGeometryHtmlFiles(sproutR, pathMinusMime, 7);

					// sprout2
					CMLMolecule sprout2 = subMoleculeTool.sprout(spAtomSet);
					int sp2Count = sprout2.getAtomCount();
					int spCount = sprout.getAtomCount();
					if (spCount < sp2Count) {
						CMLAtomSet sp2AtomSet = new CMLAtomSet(subMolecule, new MoleculeTool(sprout2).getAtomSet().getAtomIDs());
						CMLMolecule sprout2R = addAndMarkFragmentRAtoms(subMolCopy, sp2AtomSet);
						if (sprout2R.getAtomCount() == sprout2.getAtomCount()) {
							continue;
						}

						addInChIToRGroupMolecule(sprout2R);
						if (!compoundClass.equals(CompoundClass.INORGANIC.toString())) {
							// need to calculated inchi and smiles before R groups added
							if (getNumberOfRings(sprout2) < MAX_RINGS) {
								smiles = calculateSmiles(sprout2);
								addSmiles2Molecule(smiles, sprout2R);
							}
						}

						addAtomSequenceNumbers(sprout2R);
						addDoi(sprout2R);
						sprout2R.setId(subMolecule.getId()+"_atom-nuc-sprout-2_"+subMol+"_"+atomCount);

						outfile = getOutfile(dir, id, "atom-nuc-sprout-2", "", subMol, atomCount, COMPLETE_CML_MIME);
						pathMinusMime = Utils.getPathMinusMimeSet(outfile);
						writeXML(outfile, sprout2R, "atom-nuc-sprout-2");
						for (CMLAtom at : sprout2R.getAtoms()) {
							if ("R".equals(at.getChemicalElement().getSymbol())) {
								at.setElementType("Xx");
							}
						}
						write2dImages(sprout2R, pathMinusMime);

						writeGeometryHtmlFiles(sprout2R, pathMinusMime, 7);
					}
				}
			}
		}
	}

	public static int getNumberOfRings(CMLMolecule mol) {
		int atomCount = mol.getAtomCount();
		int bondCount = mol.getBondCount();
		return (bondCount-atomCount)+1;
	}

	private void addDoi(Element element) {
		Element doi = new Element("scalar", CML_NS);
		doi.addAttribute(new Attribute("dictRef", "idf:doi"));
		doi.addAttribute(new Attribute("dataType", "xsd:string"));
		doi.appendChild(new Text(this.doi));
		element.appendChild(doi);
	}

	public static void outputCMLLengthHTML(
			Writer w, List<CMLLength> lengthList,
			CMLMolecule molecule) throws IOException {
		if (lengthList.size() > 0) {
			w.write("<table border='1'>\n");
			w.write("<tr>");
			w.write("<th>");
			w.write("atom1");
			w.write("</th>");
			w.write("<th>");
			w.write("atom2");
			w.write("</th>");
			w.write("<th>");
			w.write("length");
			w.write("</th>");
			w.write("<th>");
			w.write("highlight");
			w.write("</th>");
			w.write("</tr>\n");
			for (CMLLength length : lengthList) {
				List<CMLAtom> atoms = length.getAtoms(molecule);
				w.write("<tr>");
				List<String> atomSeqNos = new ArrayList<String>(2);
				for (int i = 0; i < 2; i++) {
					w.write("<td>");
					CMLAtom atom = atoms.get(i);
					String label = atom.getId();
					atomSeqNos.add(atom.getAttribute("sequenceNumber", NED24_NS).getValue());
					w.write( (label == null) ? atom.getId() : label);
					w.write("</td>");
				}
				String s = ""+length.getXMLContent();
				w.write("<td>"+s.substring(0, Math.min(6, s.length()))+"</td>");
				if (atomSeqNos.size() == 2) {
					w.write("<td><form><script language=\"JavaScript\" type=\"text/javascript\">jmolLink(\"select atomno="+atomSeqNos.get(0)+" or atomno="+atomSeqNos.get(1)+"; set display SELECTED;\", \"view\")</script></form></td>");
				}
				w.write("</tr>\n");
			}
			w.write("</table>\n");
		}
	}

	public static void outputCMLAngleHTML(
			Writer w, List<CMLAngle> angleList,
			CMLMolecule molecule) throws IOException {
		if (angleList.size() > 0) {
			w.write("<table border='1'>\n");
			w.write("<tr>");
			w.write("<th>");
			w.write("atom1");
			w.write("</th>");
			w.write("<th>");
			w.write("atom2");
			w.write("</th>");
			w.write("<th>");
			w.write("atom3");
			w.write("</th>");
			w.write("<th>");
			w.write("angle");
			w.write("</th>");
			w.write("<th>");
			w.write("highlight");
			w.write("</th>");
			w.write("</tr>\n");
			for (CMLAngle angle : angleList) {
				List<CMLAtom> atoms = angle.getAtoms(molecule);
				w.write("<tr>");
				List<String> atomSeqNos = new ArrayList<String>(3);
				for (int i = 0; i < 3; i++) {
					w.write("<td>");
					CMLAtom atom = atoms.get(i);
					String label = atom.getId();
					atomSeqNos.add(atom.getAttribute("sequenceNumber", NED24_NS).getValue());
					w.write( (label == null) ? atom.getId() : label);
					w.write("</td>");
				}
				String s = ""+angle.getXMLContent();
				w.write("<td>"+s.substring(0, Math.min(6, s.length()))+"</td>");
				if (atomSeqNos.size() == 3) {
					w.write("<td><form><script language=\"JavaScript\" type=\"text/javascript\">jmolLink(\"select atomno="+atomSeqNos.get(0)+" or atomno="+atomSeqNos.get(1)+" or atomno="+atomSeqNos.get(2)+"; set display SELECTED;\", \"view\")</script></form></td>");
				}
				w.write("</tr>\n");
			}
			w.write("</table>\n");
		}
	}

	public static void outputCMLTorsionHTML(
			Writer w, List<CMLTorsion> torsionList,
			CMLMolecule molecule) throws IOException {
		if (torsionList.size() > 0) {
			w.write("<table border='1'>\n");
			w.write("<tr>");
			w.write("<th>");
			w.write("atom1");
			w.write("</th>");
			w.write("<th>");
			w.write("atom2");
			w.write("</th>");
			w.write("<th>");
			w.write("atom3");
			w.write("</th>");
			w.write("<th>");
			w.write("atom4");
			w.write("</th>");
			w.write("<th>");
			w.write("torsion");
			w.write("</th>");
			w.write("<th>");
			w.write("highlight");
			w.write("</th>");
			w.write("</tr>\n");
			for (CMLTorsion torsion : torsionList) {
				List<CMLAtom> atoms = torsion.getAtoms(molecule);
				w.write("<tr>");
				List<String> atomSeqNos = new ArrayList<String>(4);
				for (int i = 0; i < 4; i++) {
					w.write("<td>");
					CMLAtom atom = atoms.get(i);
					String label = atom.getId();
					atomSeqNos.add(atom.getAttribute("sequenceNumber", NED24_NS).getValue());
					w.write( (label == null) ? atom.getId() : label);
					w.write("</td>");
				}
				String s = "UNSET";
				try {
					Double content = torsion.getXMLContent();
					if (content != null) {
						s = ""+content;
					}
				} catch (Exception e) {
					System.err.println("Could not get torsion XML content.");
				}
				w.write("<td>"+s.substring(0, Math.min(6, s.length()))+"</td>");
				if (atomSeqNos.size() == 4) {
					w.write("<td><form><script language=\"JavaScript\" type=\"text/javascript\">jmolLink(\"select atomno="+atomSeqNos.get(0)+" or atomno="+atomSeqNos.get(1)+" or atomno="+atomSeqNos.get(2)+" or atomno="+atomSeqNos.get(3)+"; set display SELECTED;\", \"view\")</script></form></td>");
				}
				w.write("</tr>\n");
			}
			w.write("</table>\n");
		}
	}

	private void addInChIToRGroupMolecule(CMLMolecule molecule) {
		// need to calculated inchi and smiles before R groups added
		CMLMolecule copy = new CMLMolecule(molecule);
		List<CMLAtom> detachAtomList = new ArrayList<CMLAtom>();
		List<CMLBond> detachBondList = new ArrayList<CMLBond>();
		for (CMLAtom atom : copy.getAtoms()) {
			if ("R".equals(atom.getElementType())) {
				for (CMLBond bond : atom.getLigandBonds()) {
					if (!detachBondList.contains(bond)) {
						detachBondList.add(bond);
					}
				}
				detachAtomList.add(atom);
			}
		}
		for (CMLAtom atom : detachAtomList) {
			atom.detach();
		}
		for (CMLBond bond : detachBondList) {
			bond.detach();
		}
		InchiTool tool = new InchiTool(molecule);
		String inchi = tool.generateInchi("");
		CMLIdentifier identifier = new CMLIdentifier();
		identifier.setConvention("iupac:inchi");
		identifier.appendChild(new Text(inchi));
		molecule.appendChild(identifier);
	}

	private void writeGeometryHtmlFiles(CMLMolecule molecule, String pathMinusMime, int depth) {
		GeometryTool geometryTool = new GeometryTool(molecule);
		boolean calculate = true;
		boolean add = false;
		List<CMLLength> lengthList = 
			geometryTool.createValenceLengths(calculate, add);
		writeGeometryHtml(lengthList, pathMinusMime+".lengths.html", molecule, depth);
		// angles
		List<CMLAngle> angleList = 
			geometryTool.createValenceAngles(calculate, add);
		writeGeometryHtml(angleList, pathMinusMime+".angles.html", molecule, depth);
		// torsion
		List<CMLTorsion> torsionList = 
			geometryTool.createValenceTorsions(calculate, add);
		writeGeometryHtml(torsionList, pathMinusMime+".torsions.html", molecule, depth);
	}

	public static void main(String[] args) {
		CML2FooManager acta = new CML2FooManager("c:/workspace/crystaleye-trunk-data/docs/cif-flow-props.txt");
		acta.execute();
	}
}
