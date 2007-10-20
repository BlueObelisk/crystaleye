package ned24.sandbox.crystaleye.nmrshiftdb.etc;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.xmlcml.cml.element.CMLAtom;
import org.xmlcml.cml.element.CMLBond;
import org.xmlcml.cml.element.CMLMolecule;
import org.xmlcml.cml.element.CMLMolecule.HydrogenControl;
import org.xmlcml.cml.tools.ConnectionTableTool;

import uk.ac.cam.ch.crystaleye.IOUtils;

public class FirstProtocolSelector {

	public static void main(String[] args) {
		String path = "e:/nmrshiftdb/3d/mols";
		
		StringBuilder sb = new StringBuilder();
		for (File file : new File(path).listFiles()) {
			/*
			if (!file.getName().equals("nmrshiftdb234.cml.xml")) {
				continue;
			}
			*/
			CMLMolecule mol = (CMLMolecule)IOUtils.parseCmlFile(file).getRootElement();
			ConnectionTableTool ct = new ConnectionTableTool(mol);
			double mw = mol.getCalculatedMolecularMass(HydrogenControl.USE_EXPLICIT_HYDROGENS);
			if (mw > 300) {
				continue;
			}

			List<CMLAtom> cyclicAtoms = ct.getCyclicAtoms();
			if (cyclicAtoms.size() == 0) {
				continue;
			}
			List<CMLAtom> acyclicAtoms = new ArrayList<CMLAtom>();
			for (CMLAtom atom : mol.getAtoms()) {
				if (!"H".equals(atom.getElementType()) && !cyclicAtoms.contains(atom)) {
					acyclicAtoms.add(atom);
				}
			}

			
			boolean suitable = true;
			List<CMLBond> acyclicBonds = ct.getAcyclicBonds();
			for (CMLAtom atom : acyclicAtoms) {
				int acyclicCount = 0;
				for (CMLBond bond : atom.getLigandBonds()) {
					if (acyclicBonds.contains(bond)) {
						for (CMLAtom a : bond.getAtoms()) {
							if (!a.getId().equals(atom.getId())) {
								if (!"H".equals(a.getElementType())) {
									boolean skip = false;
									if ("O".equals(a.getElementType()) && a.getLigandAtoms().size() == 1) {
										skip = true;
									} /*else if ("O".equals(a.getElementType())) {
										for (CMLAtom at : a.getLigandAtoms()) {
											if ("H".equals(at.getElementType())) {
												skip = true;
												break;
											}
										}
									}*/
									if (!skip) {
										acyclicCount++;
									}
								}
							}
						}
					}
				}
				if (acyclicCount > 1) {
					suitable = false;
				}
			}

			if (suitable) {
				System.out.println(file.getAbsolutePath());
				sb.append(file.getAbsolutePath()+"\n");
			}
		}
		IOUtils.writeText(sb.toString(), "e:/nmrshiftdb/suitable-mols.txt");
	}
}
