package ned24.sandbox.crystaleye.nmrshiftdb.etc;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ned24.sandbox.crystaleye.nmrshiftdb.GaussianCmlTool;

import org.openscience.cdk.graph.invariant.MorganNumbersTools;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IMolecule;
import org.xmlcml.cml.element.CMLAtom;
import org.xmlcml.cml.element.CMLMolecule;
import org.xmlcml.cml.element.CMLPeak;

import uk.ac.cam.ch.crystaleye.CDKUtils;
import uk.ac.cam.ch.crystaleye.IOUtils;

public class CreateMorganAveragedCmls {

	public static void main(String[] args) {
		String path = "e:/gaussian/cml/second-protocol_mod1";
		String outFolder = "e:/gaussian/cml/second-protocol_manualAndMorgan/";
		for (File file : new File(path).listFiles()) {
			if (!file.getAbsolutePath().contains("nmrshiftdb10005691")) {
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
			
			Map<Long, List<String>> morganMap = getMorganMap(molecule);
			
			for (CMLPeak peak : calcPeaks) {
				String id = peak.getAtomRefs()[0];
				long morganNum = getMorganNumber(molecule, id);
				List<String> atomIdList = morganMap.get(morganNum);
				if (atomIdList.size() > 1) {
					double totalShift = 0;
					for (String atomId : atomIdList) {
						totalShift += getShift(calcPeaks, atomId);
					}
					double modShift = totalShift/atomIdList.size();
					peak.setXValue(modShift);
				}
			}
			String name = file.getName();
			String outPath = outFolder+name;
			IOUtils.writePrettyXML(molecule.getDocument(), outPath);
			
		}
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
	
	public static long getMorganNumber(CMLMolecule molecule, String atomId) {
		IMolecule cdkMol = CDKUtils.cmlMol2CdkMol(molecule);
		int num = -1;
		for (int i = 0; i < cdkMol.getAtomCount(); i++) {
			IAtom atom = cdkMol.getAtom(i);
			if (atom.getID().equals(atomId)) {
				num = i;
			}
		}
		long[] morganNums = MorganNumbersTools.getLongMorganNumbers(cdkMol);
		return morganNums[num];
	}
	
	public static Map<Long, List<String>> getMorganMap(CMLMolecule molecule) {		
		Map<Long, List<String>> map = new HashMap<Long, List<String>>();
		IMolecule cdkMol = CDKUtils.getCdkMol(molecule);
		String[] a = new String[cdkMol.getAtomCount()];
		for (int i = 0; i < cdkMol.getAtomCount(); i++) {
			IAtom atom = cdkMol.getAtom(i);
			a[i] = atom.getID();
		}
		long[] morganNums = MorganNumbersTools.getLongMorganNumbers(cdkMol);
		int count = 0;
		for (long i : morganNums) {
			List<String> list = map.get(i); 
			if (list == null) {
				list = new ArrayList<String>();
				list.add(a[count]);
			} else {
				list.add(a[count]);
			}
			map.put(i, list);
			count++;
		}
		
		int c = 0;
		for (String s : a) {
			System.out.println(s+" "+morganNums[c]);
			c++;
		}
		return map;
	}
}
