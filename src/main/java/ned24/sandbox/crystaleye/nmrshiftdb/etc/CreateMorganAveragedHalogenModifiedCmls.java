package ned24.sandbox.crystaleye.nmrshiftdb.etc;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ned24.sandbox.crystaleye.nmrshiftdb.GaussianCmlTool;

import org.xmlcml.cml.element.CMLAtom;
import org.xmlcml.cml.element.CMLAtomSet;
import org.xmlcml.cml.element.CMLMolecule;
import org.xmlcml.cml.element.CMLPeak;
import org.xmlcml.cml.tools.Morgan;

import uk.ac.cam.ch.crystaleye.IOUtils;

public class CreateMorganAveragedHalogenModifiedCmls {

	public static void main(String[] args) {
		String path = "e:/gaussian/cml/second-protocol_mod1";
		String outFolder = "e:/gaussian/cml/second-protocol_manualAndMorgan/";
		for (File file : new File(path).listFiles()) {
			//if (!file.getAbsolutePath().contains("nmrshiftdb10005718")) {
			//	continue;
			//}
			if (!file.getAbsolutePath().endsWith(".cml.xml")) {
				continue;
			}
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
			
			Morgan mt = new Morgan(molecule);
			List<CMLAtomSet> atomSetList = mt.getAtomSetList();
			Map<String, Double> map = new HashMap<String, Double>();
			
			for (CMLPeak peak : calcPeaks) {
				String id = peak.getAtomRefs()[0];
				CMLAtomSet atomSet = getEquivalentAtoms(atomSetList, id);
				if (atomSet.size() > 1) {
					double totalShift = 0;
					for (CMLAtom atom : atomSet.getAtoms()) {
						totalShift += getShift(calcPeaks, atom.getId());
					}
					double modShift = totalShift/atomSet.size();
					map.put(id, modShift);
				}
			}
			for (Iterator it = map.entrySet().iterator(); it.hasNext();) {
				Map.Entry entry = (Map.Entry)it.next();
				CMLPeak peak = getPeak(calcPeaks, (String)entry.getKey());
				peak.setXValue((Double)entry.getValue());
			}
			String name = file.getName();
			String outPath = outFolder+name;
			IOUtils.writePrettyXML(molecule.getDocument(), outPath);
		}
	}
	
	public static CMLAtomSet getEquivalentAtoms(List<CMLAtomSet> atomSetList, String atomId) {
		for (CMLAtomSet atomSet : atomSetList) {
			for (CMLAtom atom : atomSet.getAtoms()) {
				if (atom.getId().equals(atomId)) {
					return atomSet;
				}
			}
		}
		throw new RuntimeException("Could not find atomset for: "+atomId);
	}
	
	public static CMLPeak getPeak(List<CMLPeak> peaks, String atomId) {
		for (CMLPeak peak : peaks) {
			String id = peak.getAtomRefs()[0];
			if (id.equals(atomId)) {
				return peak;
			}
		}
		throw new RuntimeException("Shouldn't reach here: "+atomId);
	}
	
	public static double getShift(List<CMLPeak> peaks, String atomId) {
		for (CMLPeak peak : peaks) {
			String id = peak.getAtomRefs()[0];
			if (id.equals(atomId)) {
				return peak.getXValue();
			}
		}
		throw new RuntimeException("Shouldn't reach here: "+atomId);
	}

}
