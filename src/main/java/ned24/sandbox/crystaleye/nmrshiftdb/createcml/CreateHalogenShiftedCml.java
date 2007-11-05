package ned24.sandbox.crystaleye.nmrshiftdb.createcml;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ned24.sandbox.crystaleye.nmrshiftdb.GaussianCmlTool;
import ned24.sandbox.crystaleye.nmrshiftdb.GaussianConstants;

import org.xmlcml.cml.element.CMLAtom;
import org.xmlcml.cml.element.CMLMolecule;
import org.xmlcml.cml.element.CMLPeak;

import uk.ac.cam.ch.crystaleye.IOUtils;

public class CreateHalogenShiftedCml implements GaussianConstants {

	public static void main(String[] args) {
		String protocolName = HSR0_NAME;

		String inCmlDir = CML_DIR+protocolName;

		String outCmlDir = inCmlDir+"_hal/";

		List<File> fileList = new ArrayList<File>();
		for (File file : new File(inCmlDir).listFiles()) {
			if (file.getAbsolutePath().endsWith(".cml.xml")) {
				fileList.add(file);
			}
		}


		for (File file : fileList) {
			System.out.println(file.getAbsolutePath());
			GaussianCmlTool g = new GaussianCmlTool(file);
			CMLMolecule molecule = g.getMolecule();
			List<CMLPeak> calcPeaks = g.getListOfCalculatedPeaks();
			for (CMLPeak peak : calcPeaks) {
				String id = peak.getAtomRefs()[0];
				CMLAtom atom = molecule.getAtomById(id);
				int deduct = 0;
				for (CMLAtom lig : atom.getLigandAtoms()) {
					String type = lig.getElementType();
					if ("Cl".equals(type)) {
						deduct += 3;
					} else if ("Br".equals(type)) {
						deduct += 12;
					} else if ("S".equals(type)) {
						deduct += 2;
					} else if ("I".equals(type)) {
						deduct += 28;
					}
				}
				double shift = peak.getXValue();
				double newShift = shift+deduct;
				peak.setXValue(newShift);
			}

			String name = file.getName();
			String outPath = outCmlDir+name;
			IOUtils.writePrettyXML(molecule.getDocument(), outPath);
		}
	}

}
