package wwmm.crystaleye.tools;

import static org.xmlcml.cml.base.CMLConstants.CML_NS;
import static org.xmlcml.cml.base.CMLConstants.CML_XPATH;
import static org.xmlcml.euclid.EuclidConstants.S_UNDER;
import static wwmm.crystaleye.CrystalEyeConstants.MAX_RINGS_FOR_SMILES_CALCULATION;
import static wwmm.crystaleye.CrystalEyeConstants.NED24_NS;
import static wwmm.crystaleye.CrystalEyeConstants.NO_BONDS_OR_CHARGES_FLAG_DICTREF;
import static wwmm.crystaleye.CrystalEyeConstants.POLYMERIC_FLAG_DICTREF;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nu.xom.Attribute;
import nu.xom.Element;
import nu.xom.Nodes;
import nu.xom.Text;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
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

import wwmm.crystaleye.managers.CML2FooManager;
import wwmm.crystaleye.util.CDKUtils;
import wwmm.crystaleye.util.CMLUtils;
import wwmm.crystaleye.util.ChemistryUtils;
import wwmm.crystaleye.util.Utils;
import wwmm.crystaleye.util.ChemistryUtils.CompoundClass;

public class RawCml2CompleteCmlTool {
	
	private static final Logger LOG = Logger.getLogger(RawCml2CompleteCmlTool.class);
	
	private static final int MAX_ATOMS_IN_CRYSTAL = 1000;
	
	public CMLCml convert(File rawCmlFile) {
		CMLCml cml = null;
		try { 
			cml = (CMLCml)Utils.parseCml(rawCmlFile).getRootElement();
		} catch (Exception e) {
			throw new RuntimeException("Error reading CML: "+e.getMessage(), e);
		}
		
		String pathMinusMime = Utils.getPathMinusMimeSet(rawCmlFile);

		CMLMolecule molecule = CMLUtils.getFirstParentMolecule(cml);
		if (molecule == null) {
			throw new RuntimeException("No molecule found in created CML.");
		}

		// don't want to do molecules that are too large, so if > 1000 atoms, then pass
		if (molecule.getAtomCount() > MAX_ATOMS_IN_CRYSTAL) {
			throw new RuntimeException("Crystal contains too many atoms (> "+ MAX_ATOMS_IN_CRYSTAL +"), stopping process.");
		}

		CompoundClass compoundClass = ChemistryUtils.getCompoundClass(molecule);
		addCompoundClass(cml, compoundClass);
		try {
			processDisorder(molecule, compoundClass);
			CMLMolecule mergedMolecule = null;
			try {
				mergedMolecule = createFinalStructure(molecule, compoundClass);
			} catch (Exception e) {
				throw new RuntimeException("Could not calculate final structure: "+e.getMessage(), e);
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
					CMLUtils.rearrangeChiralAtomsInCMLBondIds(mergedMolecule);
					add2DStereoSMILESAndInChI(mergedMolecule, compoundClass);
				}
			}

			addDoi(cml, pathMinusMime);
			// need to replace the molecule created from atoms explicit in the CIF with mergedMolecule.
			molecule.detach();
			cml.appendChild(mergedMolecule);
			repositionCMLCrystalElement(cml);
		} catch (RuntimeException e) {
			LOG.warn("Error creating complete CML: "+e.getMessage());
			e.printStackTrace();
		}
		
		return cml;
	}
	
	private void addCompoundClass(CMLCml cml, CompoundClass compoundClass) {
		Element compClass = new Element("scalar", CML_NS);
		compClass.addAttribute(new Attribute("dataType", "xsd:string"));
		compClass.addAttribute(new Attribute("dictRef", "iucr:compoundClass"));
		compClass.appendChild(new Text(compoundClass.toString()));
		cml.appendChild(compClass);
	}
	
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
	
	private void add2DStereoSMILESAndInChI(CMLMolecule molecule, CompoundClass compoundClass) {
		List<CMLMolecule> molList = molecule.getDescendantsOrMolecule();
		if (molList.size() > 1) {
			Nodes nonUnitOccNodes = molecule.query(".//"+CMLAtom.NS+"[@occupancy[. < 1]]", CML_XPATH);
			if (!DisorderTool.isDisordered(molecule) && !molecule.hasCloseContacts() && nonUnitOccNodes.size() == 0
					&& CMLUtils.hasBondOrdersAndCharges(molecule)) {
				CMLUtils.calculateAndAddInchi(molecule);
			}
		}
		for (CMLMolecule cmlMol : molList) {
			// calculate formula and add in
			try {
				CMLFormula formula = new CMLFormula(cmlMol);
				formula.normalize();
				cmlMol.appendChild(formula);
			} catch (RuntimeException e) {
				LOG.warn("Could not generate CMLFormula, due to: "+e.getMessage());
			}

			Nodes nonUnitOccNodes = cmlMol.query(".//"+CMLAtom.NS+"[@occupancy[. < 1]]", CML_XPATH);
			if (!DisorderTool.isDisordered(cmlMol) && !cmlMol.hasCloseContacts() && nonUnitOccNodes.size() == 0
					&& CMLUtils.hasBondOrdersAndCharges(cmlMol)) {
				try {
					CDKUtils.add2DCoords(cmlMol);
					new StereochemistryTool(cmlMol).addWedgeHatchBonds();
				} catch (Exception e) {
					LOG.warn("Exception adding wedge/hatch bonds to molecule ("+cmlMol.getId()+"), due to: "+e.getMessage());
				}
				if (!compoundClass.equals(CompoundClass.INORGANIC) && 
						(CML2FooManager.getNumberOfRings(cmlMol) < MAX_RINGS_FOR_SMILES_CALCULATION)) {
					CMLUtils.calculateAndAddSmiles(cmlMol);
				}
				CMLUtils.calculateAndAddInchi(cmlMol);
			}

			Nodes bonds = cmlMol.query(".//"+CMLBondArray.NS+"/cml:bond", CML_XPATH);
			for (int l = 0; l < bonds.size(); l++) {
				this.addBondLength((CMLBond)bonds.get(l), cmlMol);	
			}	
		}	
	}

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
				try {
					success = subMolTool.adjustBondOrdersAndChargesToValency(molCharge);
				} catch (OutOfMemoryError e) {
					success = false;
				}
				if (!success) {
					// tag molecule to say we couldn't find a reasonable set of charges/bond orders for it
					addNoBondsOrChargesSetFlag(subMol);
				}
			} else {
				CMLUtils.setAllBondOrders(subMol, CMLBond.SINGLE);
			}
			if (success) {
				// remove metals before adding stereochemistry - otherwise
				// bonds to metal confuse the tool
				Map<List<CMLAtom>, List<CMLBond>> metalMap = ValencyTool.removeMetalAtomsAndBonds(subMol);
				StereochemistryTool st = new StereochemistryTool(subMol);
				try {
					st.add3DStereo();
				} catch (RuntimeException e) {
					LOG.warn("Error adding 3D stereochemistry, due to: "+e.getMessage());
				}
				ValencyTool.addMetalAtomsAndBonds(subMol, metalMap);
			}
		}
	}
	
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
					CMLFormula molForm = MoleculeTool.getOrCreateTool(molecule).calculateFormula(HydrogenControl.USE_EXPLICIT_HYDROGENS);
					if (molForm.getConciseNoCharge().equals(formula.getConciseNoCharge())) {
						molCharge = formula.getFormalCharge();
					}
				}
			}
		}
		return molCharge;
	}

	private CMLMolecule createFinalStructure(CMLMolecule molecule, CompoundClass compoundClass) throws Exception {
		CrystalTool crystalTool = new CrystalTool(molecule);
		CMLMolecule mergedMolecule = null;
		if (compoundClass.equals(CompoundClass.INORGANIC)) {
			mergedMolecule = crystalTool.addAllAtomsToUnitCell(true);
		} else {
			mergedMolecule = crystalTool.calculateCrystallochemicalUnit(new RealRange(0, 3.3 * 3.3));
		}
		return mergedMolecule;
	}

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
	
	private void processDisorder(CMLMolecule molecule, CompoundClass compoundClass) {
		// sort disorder out per molecule rather than per crystal.  This way if the disorder is
		// invalid for one molecule, we may be able to resolve others within the crystal.
		MoleculeTool molTool = MoleculeTool.getOrCreateTool(molecule);
		molTool.createCartesiansFromFractionals();
		molTool.calculateBondedAtoms();
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
				LOG.info("Problem processing disorder, due to: "+e.getMessage());
			}
		}
		ct.flattenMolecules();
	}

	private void addDoi(CMLCml cml, String pathMinusMime) {
		String parent = pathMinusMime.substring(0,pathMinusMime.lastIndexOf(File.separator));
		parent = new File(parent).getParent();
		String doiPath = parent+pathMinusMime.substring(pathMinusMime.lastIndexOf(File.separator),pathMinusMime.lastIndexOf("_"));
		doiPath = doiPath.replaceAll("sup[\\d]*", "")+".doi";
		File doiFile = new File(doiPath);
		if (doiFile.exists()) {
			String doiString = null;
			try {
				doiString = FileUtils.readFileToString(doiFile);
			} catch (IOException e) {
				throw new RuntimeException("Exception while reading file ("+doiFile+"), due to: "+e.getMessage(), e);
			}
			Element doi = new Element("scalar", CML_NS);
			doi.addAttribute(new Attribute("dictRef", "idf:doi"));
			doi.appendChild(new Text(doiString));
			cml.appendChild(doi);
		}
	}

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

	private boolean isPolymericOrganometal(CMLMolecule originalMolecule, CMLMolecule mergedMolecule,
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
					if (isPolymeric) {
						break;
					}
					String atomId = atom.getId();
					Set<String> origSet = new HashSet<String>();
					for (CMLAtom ligand : atom.getLigandAtoms()) {
						origSet.add(ligand.getId());
					}
					List<CMLAtom> ligandAtoms = mergedMolecule.getAtomById(atomId).getLigandAtoms();
					if (ligandAtoms.size() > origSet.size()) {
						List<String> idList = new ArrayList<String>();
						for (CMLAtom ligand : ligandAtoms) {
							String idStart = getIdPrefix(ligand.getId());
							if (idStart == null) {
								idList.add(ligand.getId());
							} else {
								idList.add(idStart);
							}
						}
						Collections.sort(idList);
						for (CMLAtom a : mergedMolecule.getAtoms()) {
							String aStart = getIdPrefix(a.getId());
							if (aStart != null) {
								if (aStart.equals(atomId)) {
									List<String> ligandIdList = new ArrayList<String>();
									for (CMLAtom l : a.getLigandAtoms()) {
										String lStart = getIdPrefix(l.getId());
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
	
	private String getIdPrefix(String id) {
		if (id.contains(S_UNDER)) {
			return id.substring(0,id.indexOf(S_UNDER));		
		} else {
			return null;
		}
	}
	
	public static void main(String[] args) {
		File file = new File("C:\\Documents and Settings\\ned24.UCC\\Desktop\\test_2\\test_2.cml");
		RawCml2CompleteCmlTool tool = new RawCml2CompleteCmlTool();
		CMLCml cml = tool.convert(file);
		//System.out.println(cml.toXML());
	}

}
