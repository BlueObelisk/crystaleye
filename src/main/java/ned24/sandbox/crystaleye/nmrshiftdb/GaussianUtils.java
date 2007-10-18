package ned24.sandbox.crystaleye.nmrshiftdb;


public class GaussianUtils {
	
	public static final double TMS_ACETONE = 196.6971;
	public static final double TMS_ACETONITRILE = 196.7066;
	public static final double TMS_BENZENE = 196.5173;
	public static final double TMS_CCL4 = 196.5157;
	public static final double TMS_CHLOROFORM = 196.6263;
	public static final double TMS_DICHLOROMETHANE = 196.6681;
	public static final double TMS_DMSO = 196.7093;
	public static final double TMS_ETHER = 196.6142;
	public static final double TMS_METHANOL = 196.7051;
	public static final double TMS_THF = 196.6590;
	public static final double TMS_TOLUENE = 196.5285;
	public static final double TMS_WATER = 196.7116;
	
	public enum TmsShifts {
		
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
}
