package ned24.sandbox.crystaleye.nmrshiftdb;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nu.xom.Attribute;

import org.xmlcml.cml.element.CMLAtom;
import org.xmlcml.cml.element.CMLMolecule;
import org.xmlcml.cml.element.CMLMolecule.HydrogenControl;
import org.xmlcml.cml.tools.MoleculeTool;
import org.xmlcml.molutil.Molutils;

import uk.ac.cam.ch.crystaleye.IOUtils;

public class AddHydrogens {

	public static void main(String[] args) {
		String path = "e:/gaussian/TMS";
		int i = 0;
		List<File> deleteList = new ArrayList<File>();
		for (File file : new File(path).listFiles()) {
			System.out.println(file.getAbsolutePath());
			i++;
			CMLMolecule molecule = (CMLMolecule)IOUtils.parseCmlFile(file).getRootElement();
			for (CMLAtom atom : molecule.getAtoms()) {
				Attribute att = atom.getAttribute("hydrogenCount");
				if (att != null) {
					att.detach();
				}
			}
			MoleculeTool mt = new MoleculeTool(molecule);
			mt.adjustHydrogenCountsToValency(HydrogenControl.ADD_TO_HYDROGEN_COUNT);
			Set<CMLAtom> hLigands = new HashSet<CMLAtom>();
			for (CMLAtom atom : molecule.getAtoms()) {
				if ("H".equals(atom.getElementType())) {
					if (atom.getXYZ3() ==  null) {
						hLigands.add(atom.getLigandAtoms().get(0));
					}
				}
			}
			for (CMLAtom atom : hLigands) {
				System.out.println(atom.getId());
				try {
					mt.calculate3DCoordinatesForLigands(atom, Molutils.DEFAULT, 1.0, 108);
				} catch (Exception e) {
					System.err.println("problem: "+file.getAbsolutePath());
					deleteList.add(file);
				}
			}
			//molecule.debug();
			IOUtils.writePrettyXML(molecule.getDocument(), file.getAbsolutePath());
		}
		
		for (File file : deleteList) {
			file.delete();
		}
	}
}
