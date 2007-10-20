package ned24.sandbox.crystaleye.nmrshiftdb;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xmlcml.cml.element.CMLAtom;
import org.xmlcml.cml.element.CMLCml;
import org.xmlcml.cml.element.CMLMolecule;
import org.xmlcml.cml.legacy2cml.molecule.GaussianArchiveConverter;
import org.xmlcml.euclid.Point3;

import uk.ac.cam.ch.crystaleye.IOUtils;


public class GaussianUtils implements GaussianConstants {
	
	public enum Solvent {
		ACETONE, ACETONITRILE, BENZENE, CCL4, CHLOROFORM, DICHLOROMETHANE,
		DMSO, ETHER, METHANOL, THF, TOLUENE, WATER;
	}
	
	public static double getTmsShift(Solvent solvent) {
		if (solvent.equals(Solvent.ACETONE)) {
			return 196.6971;
		} else if (solvent.equals(Solvent.ACETONITRILE)) {
			return 196.7066;
		} else if (solvent.equals(Solvent.BENZENE)) {
			return 196.5173;
		} else if (solvent.equals(Solvent.CCL4)) {
			return 196.5157;
		} else if (solvent.equals(Solvent.CHLOROFORM)) {
			return 196.6263;
		} else if (solvent.equals(Solvent.DICHLOROMETHANE)) {
			return 196.6681;
		} else if (solvent.equals(Solvent.DMSO)) {
			return 196.7093;
		} else if (solvent.equals(Solvent.ETHER)) {
			return 196.6142;
		} else if (solvent.equals(Solvent.METHANOL)) {
			return 196.7051;
		} else if (solvent.equals(Solvent.THF)) {
			return 196.6590;
		} else if (solvent.equals(Solvent.TOLUENE)) {
			return 196.5285;
		} else if (solvent.equals(Solvent.WATER)) {
			return 196.7116;
		} else {
			throw new RuntimeException("Unknown solvent.");
		}
	}
	
	public static double getTmsShift(String solvent) {
		String solv = nmrShiftDbSolvent2GaussianSolvent(solvent);
		Solvent s = getSolvent(solv);
		return getTmsShift(s);
	}
	
	public static Solvent getSolvent(String solv) {
		Solvent s = null;
		if (solv.equals("Acetone")) {
			s = Solvent.ACETONE;
		} else if (solv.equals("DiChloroMethane")) {
			s = Solvent.DICHLOROMETHANE;
		} else if (solv.equals("Benzene")) {
			s = Solvent.BENZENE;
		} else if (solv.equals("Water")) {
			s = Solvent.WATER;
		} else if (solv.equals("Ether")) {
			s = Solvent.ETHER;
		} else if (solv.equals("Methanol")) {
			s = Solvent.METHANOL;
		} else if (solv.equals("Chloroform")) {
			s = Solvent.CHLOROFORM;
		} else if (solv.equals("DMSO")) {
			s = Solvent.DMSO;
		} else if (solv.equals("Acetonitrile")) {
			s = Solvent.ACETONITRILE;
		} else if (solv.equals("Toluene")) {
			s = Solvent.TOLUENE;
		} else if (solv.equals("THF")) {
			s = Solvent.THF;
		} else if (solv.equals("CCl4")) {
			s = Solvent.CCL4;
		}
		return s;
	}
	
	public static String nmrShiftDbSolvent2GaussianSolvent(String solvent) {
		String gauSolvent = null;
		//System.out.println(solvent);
		if (solvent.equals("Acetone-d6") ||
				solvent.equals("acetone") ||
				solvent.equals("Acetone-D6 ((CD3)2CO)") ||
				solvent.equals("(CD3)2CO") ||
				solvent.equals("Acetone")) {
			gauSolvent = "Acetone";
		} else if (solvent.equals("Methylenchloride-D2 (CD2Cl2)") ||
				solvent.equals("CH2Cl2") ||
				solvent.equals("dichloromethane") ||
				solvent.equals("DiChloroMethane")) {
			gauSolvent = "DiChloroMethane";
		} else if (solvent.equals("C6D6") ||
				solvent.equals("benzene") ||
				solvent.equals("Benzene-D6 (C6D6)") ||
				solvent.equals("Benzene")) {
			gauSolvent = "Benzene";
		} else if (solvent.equals("D2O")||
				solvent.equals("H2O") ||
				solvent.equalsIgnoreCase("water") ||
				solvent.equalsIgnoreCase("Water (H2O)") ||
				solvent.equalsIgnoreCase("Water (D2O)") ||
				solvent.equalsIgnoreCase("Deuteriumoxide (D2O)") ||
				solvent.equals("Water")) {
			gauSolvent = "Water";
		} else if (solvent.equals("Diethylether ((CH3CH2)2O)") ||
				solvent.equalsIgnoreCase("diethylether") ||
				solvent.equalsIgnoreCase("Et2O") ||
				solvent.equals("Ether")) {
			gauSolvent = "Ether";
		} else if (solvent.equals("Methanol-D4 (CD3OD)") ||
				solvent.equalsIgnoreCase("CD3OD") ||
				solvent.equalsIgnoreCase("METHANOL-D1 (CH3OD)") ||
				solvent.equalsIgnoreCase("MeOH") ||
				solvent.equalsIgnoreCase("Methanol-D3(CD3OH)") ||
				solvent.equalsIgnoreCase("MeOH-d4") ||
				solvent.equals("Methanol")) {	
			gauSolvent = "Methanol";
		} else if (solvent.equals("Chloroform-D1 (CDCl3)") ||
				solvent.equalsIgnoreCase("CHCl3") ||
				solvent.equalsIgnoreCase("CDCL3") ||
				solvent.equalsIgnoreCase("CDCl3") ||
				solvent.equalsIgnoreCase("chloroform") ||
				solvent.equals("Chloroform")) {
			gauSolvent = "Chloroform";
		} else if (solvent.equals("DMSO") ||
				solvent.equalsIgnoreCase("d6-DMSO") ||
				solvent.equalsIgnoreCase("DMSO-d6") ||
				solvent.equalsIgnoreCase("Dimethylsulphoxide-D6 (DMSO-D6, C2D6SO))") ||
				solvent.equals("DMSO")) {
			gauSolvent = "DMSO";
		} else if (solvent.equals("MeCN") ||
				solvent.equalsIgnoreCase("CD3CN") ||
				solvent.equalsIgnoreCase("Acetonitrile-D3(CD3CN)") ||
				solvent.equals("Acetonitrile")) {
			gauSolvent = "Acetonitrile";
		} else if (solvent.equals("toluene") ||
				solvent.equals("Toluene")) {
			gauSolvent = "Toluene";
		} else if (solvent.equals("THF") ||
				solvent.equalsIgnoreCase("Tetrahydrofuran-D8 (THF-D8, C4D4O)") ||
				solvent.equals("THF")) {
			gauSolvent = "THF";
		} else if (solvent.equals("TETRACHLORO-METHANE (CCl4)") ||
				solvent.equalsIgnoreCase("CCl4") ||
				solvent.equalsIgnoreCase("Carbontetrachloride")) {
			gauSolvent = "CCl4";
		}
		return gauSolvent;
	}
	
	public static CMLCml getFinalCml(File gaussianFile) {
		String gaussianPath = gaussianFile.getAbsolutePath();
		String gaussianName = gaussianFile.getName();
		gaussianName = gaussianName.substring(0, gaussianName.length() - 4);
		runGaussianConverter(gaussianPath, gaussianPath);
		File parent = gaussianFile.getParentFile();

		List<Integer> intList = new ArrayList<Integer>();
		List<File> gauCmlFiles = new ArrayList<File>();
		for (File file : parent.listFiles()) {
			String filename = file.getName();
			Pattern p = Pattern.compile(gaussianName + "_(\\d+)"
					+ GAUSSIAN_CONVERTER_OUT_MIME);
			Matcher m = p.matcher(filename);
			if (m.find()) {
				int i = Integer.valueOf(m.group(1));
				intList.add(i);
				gauCmlFiles.add(file);
			}
		}
		Collections.sort(intList);
		Collections.reverse(intList);
		String finalname = parent.getAbsolutePath() + File.separator
				+ gaussianName + "_" + intList.get(0)
				+ GAUSSIAN_CONVERTER_OUT_MIME;
		CMLCml cml = (CMLCml) IOUtils.parseCmlFile(finalname).getRootElement();
		for (File f : gauCmlFiles) {
			f.delete();
		}
		return cml;
	}

	public static void runGaussianConverter(String infile, String outfile) {
		String[] args = { "-INFILE", infile, "-OUTFILE", outfile, "-DICT",
				GAUSSIAN_DICT };
		GaussianArchiveConverter converter = new GaussianArchiveConverter();
		try {
			converter.runCommands(args);
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("GaussianArchiveConverter EXCEPTION... " + e);
		}
	}
	
	public static String getConnectionTable(CMLMolecule molecule) {
		StringBuilder sb = new StringBuilder();
		for (CMLAtom atom : molecule.getAtoms()) {
			sb.append(" "+atom.getElementType()+" ");
			Point3 p3 = atom.getXYZ3();
			double[] a = p3.getArray();
			sb.append(a[0]+" ");
			sb.append(a[1]+" ");
			sb.append(a[2]+"\n");
		}
		return sb.toString();
	}
}
