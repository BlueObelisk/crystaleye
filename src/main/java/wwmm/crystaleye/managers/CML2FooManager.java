package wwmm.crystaleye.managers;

import static org.xmlcml.cml.base.CMLConstants.CML_NS;
import static org.xmlcml.cml.base.CMLConstants.CML_XPATH;
import static org.xmlcml.euclid.EuclidConstants.S_UNDER;
import static wwmm.crystaleye.CrystalEyeConstants.CIF2CML;
import static wwmm.crystaleye.CrystalEyeConstants.CML2FOO;
import static wwmm.crystaleye.CrystalEyeConstants.COMPLETE_CML_MIME;
import static wwmm.crystaleye.CrystalEyeConstants.MAX_RINGS_FOR_SMILES_CALCULATION;
import static wwmm.crystaleye.CrystalEyeConstants.NED24_NS;
import static wwmm.crystaleye.CrystalEyeConstants.POLYMERIC_FLAG_DICTREF;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Node;
import nu.xom.Nodes;
import nu.xom.Text;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.xmlcml.cml.base.CMLUtil;
import org.xmlcml.cml.element.CMLAtom;
import org.xmlcml.cml.element.CMLAtomParity;
import org.xmlcml.cml.element.CMLAtomSet;
import org.xmlcml.cml.element.CMLBond;
import org.xmlcml.cml.element.CMLBondSet;
import org.xmlcml.cml.element.CMLBondStereo;
import org.xmlcml.cml.element.CMLCml;
import org.xmlcml.cml.element.CMLCrystal;
import org.xmlcml.cml.element.CMLIdentifier;
import org.xmlcml.cml.element.CMLMetadata;
import org.xmlcml.cml.element.CMLMolecule;
import org.xmlcml.cml.tools.ConnectionTableTool;
import org.xmlcml.cml.tools.DisorderTool;
import org.xmlcml.cml.tools.MoleculeTool;
import org.xmlcml.cml.tools.ValencyTool;
import org.xmlcml.molutil.ChemicalElement;
import org.xmlcml.molutil.ChemicalElement.Type;

import wwmm.crystaleye.AbstractManager;
import wwmm.crystaleye.CrystalEyeJournals;
import wwmm.crystaleye.IssueDate;
import wwmm.crystaleye.JournalDetails;
import wwmm.crystaleye.tools.Cml2PngTool;
import wwmm.crystaleye.tools.InchiTool;
import wwmm.crystaleye.tools.SiteCreation;
import wwmm.crystaleye.tools.SmilesTool;
import wwmm.crystaleye.util.CDKUtils;
import wwmm.crystaleye.util.CMLUtils;
import wwmm.crystaleye.util.ChemistryUtils;
import wwmm.crystaleye.util.ChemistryUtils.CompoundClass;
import wwmm.crystaleye.util.CrystalEyeUtils;
import wwmm.crystaleye.util.Utils;

public class CML2FooManager extends AbstractManager {

	private static final Logger LOG = Logger.getLogger(CML2FooManager.class);

	private String doi;

	private CML2FooManager() {
		;
	}

	public CML2FooManager(File propertiesFile) {
		this.setProperties(propertiesFile);
	}

	public void execute() {
		String processLogPath = properties.getProcessLogPath();
		for (JournalDetails journalDetails : new CrystalEyeJournals().getDetails()) {
			String publisherAbbreviation = journalDetails.getPublisherAbbreviation();
			String journalAbbreviation = journalDetails.getJournalAbbreviation();
			List<IssueDate> unprocessedDates = this.getUnprocessedDates(processLogPath, publisherAbbreviation, journalAbbreviation, CML2FOO, CIF2CML);
			if (unprocessedDates.size() != 0) {
				for (IssueDate date : unprocessedDates) {
					String writeDir = properties.getCifDir();
					String year = date.getYear();
					String issueNum = date.getIssue();
					String issueWriteDir = FilenameUtils.separatorsToUnix(writeDir+"/"+
							publisherAbbreviation+"/"+journalAbbreviation+
							"/"+year+"/"+issueNum);
					process(issueWriteDir, publisherAbbreviation, journalAbbreviation, year, issueNum);
					updateProcessLog(processLogPath, publisherAbbreviation, journalAbbreviation, year, issueNum, CML2FOO);
				}
			} else {
				LOG.info("No dates to process at this time for "+publisherAbbreviation+" journal "+journalAbbreviation);
			}
		}
	}

	private List<File> getCompleteCmlFiles(File issueDir) {
		List<File> completeCmls = new ArrayList<File>();
		File[] articleDirs = issueDir.listFiles();
		for (File articleDir : articleDirs) {
			File[] articleFiles = articleDir.listFiles();
			for (File articleFile : articleFiles) {
				if (!articleFile.isDirectory()) {
					continue;
				}
				File[] structureFiles = articleFile.listFiles();
				for (File structureFile : structureFiles) {
					if (structureFile.getName().endsWith(COMPLETE_CML_MIME)) {
						completeCmls.add(structureFile);
					}
				}
			}
		}
		return completeCmls;
	}

	public void process(String issueWriteDir, String publisherAbbreviation, String journalAbbreviation, String year, String issueNum) {
		// go through to the article directories in the issue dir and process all found raw CML files
		File issueDir = new File(issueWriteDir);
		if (!issueDir.exists()) {
			LOG.warn("Cannot find expected issue folder at: "+issueDir);
		}
		List<File> completeCmlFiles = getCompleteCmlFiles(issueDir);
		for (File cmlFile : completeCmlFiles) {
			LOG.info("Now processing: "+cmlFile);
			String pathMinusMime = Utils.getPathMinusMimeSet(cmlFile);
			String suppId = pathMinusMime.substring(pathMinusMime.lastIndexOf(File.separator)+1);
			String articleId = suppId.substring(0,suppId.indexOf("_"));
			articleId = articleId.replaceAll("sup[\\d]*", "");
			CMLCml cml = null;
			try {
				cml = (CMLCml)(Utils.parseCml(cmlFile)).getRootElement();
			} catch (Exception e) {
				LOG.warn("Error parsing CML file, due to: "+e.getMessage());
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

			if (compClass.equals(CompoundClass.INORGANIC.toString()) || isPolymeric) {
				continue;
			}
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
							&& CMLUtils.hasBondOrdersAndCharges(subMol)) {
						if (ChemistryUtils.isBoringMolecule(subMol)) {
							continue;
						}
						write2dImages(subMol, pathMinusMime+"_"+count);
						count++;
					}
				}

				Nodes doiNodes = cml.query("//cml:scalar[@dictRef='idf:doi']", CML_XPATH);
				this.doi = "";
				if (doiNodes.size() > 0) {
					this.doi = ((Element)doiNodes.get(0)).getValue();
				}

				File structureParent = cmlFile.getParentFile();
				String structureId = structureParent.getName();					
				File moietyDir = new File(structureParent, "moieties");
				try {
					outputMoieties(moietyDir, structureId, molecule, compClass);
				} catch(Exception e) {
					LOG.warn("Error while outputting moieties: "+e.getMessage());
				}
			}
		}
	}

	private void write2dImages(CMLMolecule molecule, String pathMinusMime) {
		write2dImage(pathMinusMime+".small.png", molecule, 358, 278, false);
		write2dImage(pathMinusMime+".png", molecule, 600, 600, false);
	}

	private void addSmiles2Molecule(String smiles, CMLMolecule mol) {
		if (smiles != null && !"".equals(smiles)) {
			Element identifier = new Element("identifier", CML_NS);
			identifier.addAttribute(new Attribute("convention", "daylight:smiles"));
			identifier.appendChild(new Text(smiles));
			mol.appendChild(identifier);
		}
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
			LOG.warn("Could not produce 2D image for molecule, due to: "+e.getMessage());
		}
	}

	private String getOutPath(File writeDir, String id, 
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
		CMLBondSet newBS = MoleculeTool.getOrCreateTool(mol).getBondSet(newAS);
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
			MoleculeTool mt = MoleculeTool.getOrCreateTool(subMolecule);
			List<CMLMolecule> fragmentList = new ArrayList<CMLMolecule>();
			if ("chain-nuc".equals(fragType)) {
				molecule = new CMLMolecule(molecule);
				subMolecule = new CMLMolecule(subMolecule);
				ValencyTool.removeMetalAtomsAndBonds(subMolecule);
				new ConnectionTableTool(subMolecule).partitionIntoMolecules();
				for (CMLMolecule subSubMol : subMolecule.getDescendantsOrMolecule()) {
					fragmentList.addAll(MoleculeTool.getOrCreateTool(subSubMol).getChainMolecules());
				}
			} else if ("ring-nuc".equals(fragType)) {
				molecule = new CMLMolecule(molecule);
				subMolecule = new CMLMolecule(subMolecule);
				ValencyTool.removeMetalAtomsAndBonds(subMolecule);
				new ConnectionTableTool(subMolecule).partitionIntoMolecules();
				for (CMLMolecule subSubMol : subMolecule.getDescendantsOrMolecule()) {
					fragmentList.addAll(MoleculeTool.getOrCreateTool(subSubMol).getRingNucleiMolecules());
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
				CMLAtomSet atomSet = new CMLAtomSet(subMolecule, MoleculeTool.getOrCreateTool(fragment).getAtomSet().getAtomIDs());
				writeFragmentFiles(molecule, subMolecule, compoundClass, fragment, fragType, subMolCount, count, 
						dir, id, depth);
				if (sprout) {
					CMLMolecule sproutMol = mt.sprout(atomSet);
					int nucCount = fragment.getAtomCount();
					int spCount = sproutMol.getAtomCount();
					if (nucCount < spCount) {
						writeFragmentFiles(molecule, subMolecule, compoundClass, sproutMol, fragType+"-sprout-1", subMolCount, count, 
								dir, id, depth);
						CMLAtomSet spAtomSet = new CMLAtomSet(subMolecule, MoleculeTool.getOrCreateTool(sproutMol).getAtomSet().getAtomIDs());
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
		CMLAtomSet atomSet = new CMLAtomSet(subMolCopy, MoleculeTool.getOrCreateTool(fragCopy).getAtomSet().getAtomIDs());
		CMLMolecule molR = addAndMarkFragmentRAtoms(subMolCopy, atomSet);

		addInChIToRGroupMolecule(molR);
		if (!compoundClass.equals(CompoundClass.INORGANIC.toString())) {
			if (getNumberOfRings(fragCopy) < MAX_RINGS_FOR_SMILES_CALCULATION) {
				String smiles = SmilesTool.generateSmiles(fragCopy);
				if (smiles != null) {
					addSmiles2Molecule(smiles, molR);
				}
			}
		}

		addAtomSequenceNumbers(molR);
		addDoi(molR);
		molR.setId(subMolecule.getId()+"_"+fragType+"_"+subMolCount+"_"+count);
		String outPath = getOutPath(dir, id, fragType, "", subMolCount, count, COMPLETE_CML_MIME);
		Utils.writeXML(new File(outPath), new Document(molR));
		String pathMinusMime = Utils.getPathMinusMimeSet(outPath);
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
					&& CMLUtils.hasBondOrdersAndCharges(mol)) {
				moiCount++;
				if (ChemistryUtils.isBoringMolecule(mol)) continue;
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
				Utils.writeXML(new File(outPath), new Document((Element)mol.copy()));
				String smallPngPath = pathMinusMime+".small.png";
				String pngPath = pathMinusMime+".png";
				write2dImage(pngPath, mol, 600, 600, true);
				write2dImage(smallPngPath, mol, 358, 278, true);
				addAtomSequenceNumbers(mol);
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
			MoleculeTool subMoleculeTool = MoleculeTool.getOrCreateTool(subMolecule);
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

					if (!compoundClass.equals(CompoundClass.INORGANIC.toString())) {
						// need to calculate inchi and smiles before R groups added
						addInChIToRGroupMolecule(atomR);
						String atomMolSmiles = SmilesTool.generateSmiles(atomMol);
						addSmiles2Molecule(atomMolSmiles, atomR);
					}

					addAtomSequenceNumbers(atomR);
					addDoi(atomR);
					atomR.setId(subMolecule.getId()+"_atom-nuc_"+subMol+"_"+atomCount);
					String outPath = getOutPath(dir, id, "atom-nuc", "", subMol, atomCount, COMPLETE_CML_MIME);
					Utils.writeXML(new File(outPath), new Document(atomR));
					String pathMinusMime = Utils.getPathMinusMimeSet(outPath);
					for (CMLAtom at : atomR.getAtoms()) {
						if ("R".equals(at.getChemicalElement().getSymbol())) {
							at.setElementType("Xx");
						}
					}
					write2dImages(atomR, pathMinusMime);

					//sprout once
					CMLMolecule sprout = subMoleculeTool.sprout(singleAtomSet);
					CMLAtomSet spAtomSet = new CMLAtomSet(subMolecule, MoleculeTool.getOrCreateTool(sprout).getAtomSet().getAtomIDs());

					CMLMolecule sproutR = addAndMarkFragmentRAtoms(subMolCopy, spAtomSet);
					if (sproutR.getAtomCount() == sprout.getAtomCount()) {
						continue;
					}
					// need to calculated inchi and smiles before R groups added
					addInChIToRGroupMolecule(sproutR);
					if (!compoundClass.equals(CompoundClass.INORGANIC.toString())) {
						if (getNumberOfRings(sprout) < MAX_RINGS_FOR_SMILES_CALCULATION) {
							String sproutSmiles= SmilesTool.generateSmiles(sprout);
							addSmiles2Molecule(sproutSmiles, sproutR);
						}
					}

					addAtomSequenceNumbers(sproutR);
					addDoi(sproutR);
					sproutR.setId(subMolecule.getId()+"_atom-nuc-sprout-1_"+subMol+"_"+atomCount);
					outPath = getOutPath(dir, id, "atom-nuc-sprout-1", "", subMol, atomCount, COMPLETE_CML_MIME);
					pathMinusMime = Utils.getPathMinusMimeSet(outPath);	          

					Utils.writeXML(new File(outPath), new Document(sproutR));
					for (CMLAtom at : sproutR.getAtoms()) {
						if ("R".equals(at.getChemicalElement().getSymbol())) {
							at.setElementType("Xx");
						}
					}
					write2dImages(sproutR, pathMinusMime);

					// sprout2
					CMLMolecule sprout2 = subMoleculeTool.sprout(spAtomSet);
					int sp2Count = sprout2.getAtomCount();
					int spCount = sprout.getAtomCount();
					if (spCount < sp2Count) {
						CMLAtomSet sp2AtomSet = new CMLAtomSet(subMolecule, MoleculeTool.getOrCreateTool(sprout2).getAtomSet().getAtomIDs());
						CMLMolecule sprout2R = addAndMarkFragmentRAtoms(subMolCopy, sp2AtomSet);
						if (sprout2R.getAtomCount() == sprout2.getAtomCount()) {
							continue;
						}

						addInChIToRGroupMolecule(sprout2R);
						if (!compoundClass.equals(CompoundClass.INORGANIC.toString())) {
							// need to calculated inchi and smiles before R groups added
							if (getNumberOfRings(sprout2) < MAX_RINGS_FOR_SMILES_CALCULATION) {
								String sprout2Smiles = SmilesTool.generateSmiles(sprout2);
								addSmiles2Molecule(sprout2Smiles, sprout2R);
							}
						}

						addAtomSequenceNumbers(sprout2R);
						addDoi(sprout2R);
						sprout2R.setId(subMolecule.getId()+"_atom-nuc-sprout-2_"+subMol+"_"+atomCount);

						outPath = getOutPath(dir, id, "atom-nuc-sprout-2", "", subMol, atomCount, COMPLETE_CML_MIME);
						pathMinusMime = Utils.getPathMinusMimeSet(outPath);
						Utils.writeXML(new File(outPath), new Document(sprout2R));
						for (CMLAtom at : sprout2R.getAtoms()) {
							if ("R".equals(at.getChemicalElement().getSymbol())) {
								at.setElementType("Xx");
							}
						}
						write2dImages(sprout2R, pathMinusMime);
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

	public static void main(String[] args) {
		File propsFile = new File("c:/workspace/crystaleye-trunk-data/docs/cif-flow-props.txt");
		CML2FooManager acta = new CML2FooManager(propsFile);
		acta.execute();
	}
}
