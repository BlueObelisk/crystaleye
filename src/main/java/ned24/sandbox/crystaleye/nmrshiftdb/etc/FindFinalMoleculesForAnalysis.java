package ned24.sandbox.crystaleye.nmrshiftdb.etc;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.xmlcml.cml.base.CMLElement.FormalChargeControl;
import org.xmlcml.cml.element.CMLAtom;
import org.xmlcml.cml.element.CMLBond;
import org.xmlcml.cml.element.CMLMolecule;
import org.xmlcml.cml.element.CMLMolecule.HydrogenControl;
import org.xmlcml.cml.tools.ConnectionTableTool;

import uk.ac.cam.ch.crystaleye.IOUtils;

public class FindFinalMoleculesForAnalysis {
	
	static String[] allowedElements = {"C", "N", "O", "P", "I", "F", "N", "S", "B", "Cl", "H", "Si", "Br"};
	static List<String> elList;

	static {
		elList = Arrays.asList(allowedElements);
	}

	public static void main(String[] args) {
		String path = "e:/gaussian/cml/second-protocol_mod1";
		
		List<File> noList = new ArrayList<File>();
		
		for (File file : new File(path).listFiles()) {
			CMLMolecule molecule = (CMLMolecule)IOUtils.parseCmlFile(file).getRootElement();
			// MW < 300
			// no charges 
			// must have cyclic bonds
			// must contain C
			// no two acyclic atoms bonded (H, O, F, Cl, Br, I)

			if (molecule.getCalculatedMolecularMass(HydrogenControl.USE_EXPLICIT_HYDROGENS) > 300) {
				noList.add(file);
				continue;
			}

			if (molecule.getCalculatedFormalCharge(FormalChargeControl.DEFAULT) != 0) {
				noList.add(file);
				continue;
			}

			ConnectionTableTool ct = new ConnectionTableTool(molecule);
			List<CMLAtom> cyclicAtoms = ct.getCyclicAtoms();
			if (cyclicAtoms.size() == 0) {
				noList.add(file);
				continue;
			}	

			boolean containsC = false;
			for (CMLAtom atom : molecule.getAtoms()) {
				if ("C".equals(atom.getElementType())) {
					containsC = true;
				}
				if (!elList.contains(atom.getElementType())) {
					noList.add(file);
					continue;
				}
			}

			if (!containsC) {
				noList.add(file);
				continue;
			}

			List<CMLAtom> acyclicAtoms = new ArrayList<CMLAtom>();
			for (CMLAtom atom : molecule.getAtoms()) {
				if (!"H".equals(atom.getElementType()) && !cyclicAtoms.contains(atom)) {
					acyclicAtoms.add(atom);
				}
			}
			List<CMLBond> acyclicBonds = ct.getAcyclicBonds();
			for (CMLAtom atom : acyclicAtoms) {
				int acyclicCount = 0;
				for (CMLBond bond : atom.getLigandBonds()) {
					if (acyclicBonds.contains(bond)) {
						for (CMLAtom a : bond.getAtoms()) {
							if (!a.getId().equals(atom.getId())) {
								if (!"H".equals(a.getElementType())) {
									boolean skip = false;
									if (("O".equals(a.getElementType()) && a.getLigandAtoms().size() == 1) ||
											"F".equals(a.getElementType()) ||
											"Cl".equals(a.getElementType()) ||
											"Br".equals(a.getElementType()) ||
											"I".equals(a.getElementType())) {
										skip = true;
									}
									if (!skip) {
										acyclicCount++;
									}
								}
							}
						}
					}
				}
				if (acyclicCount > 1) {
					noList.add(file);
					continue;
				}
			}
			
		}
		
		for (File file : noList) {
			System.out.println(file.getAbsolutePath());
		}
	}
}
