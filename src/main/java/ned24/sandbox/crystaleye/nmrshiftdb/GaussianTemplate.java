package ned24.sandbox.crystaleye.nmrshiftdb;

public class GaussianTemplate {

	String name;
	String connectionTable;
	String solvent;

	boolean hasCDoubleBondC;
	boolean hasCarbonyl;
	boolean setExtraBasis;

	public GaussianTemplate(String name, String connectionTable, String solvent) {
		this.name = name;
		this.connectionTable = connectionTable;
		this.solvent = solvent;
	}

	public String getInput() {
		StringBuilder sb = new StringBuilder();
		sb.append("%chk="+name+".chk\n");
		sb.append("#N RHF/STO-3G opt(loose) \n");
		sb.append("\n");
		sb.append("Optimisation using  UFF coordinates and  STO-3G (more stable than  PM3)\n");
		sb.append("\n");
		sb.append("0 1\n");
		sb.append(connectionTable);
		sb.append("\n");
		sb.append("--Link1--\n");
		sb.append("%chk="+name+".chk\n");
		sb.append("#N rmpw1pw91/6-31g(d,p) geom=checkpoint opt guess=read\n");
		sb.append("\n");
		sb.append("Optimisation using STO-3G coordinates and DFT\n");
		sb.append("\n");
		sb.append("0 1\n");
		sb.append("\n");
		sb.append("--Link1--\n");
		sb.append("%chk="+name+".chk\n");
		sb.append("#P rmpw1pw91/6-31g(d,p) geom=checkpoint guess=read\n");
		
		if (!setExtraBasis) {
			sb.append("#P NMR scrf(cpcm,solvent="+solvent+")\n");
		} else {
			sb.append("#P NMR scrf(cpcm,solvent="+solvent+") ExtraBasis\n");
		}

		sb.append("\n");
		sb.append("Calculating  GIAO-shifts.  \n");
		sb.append("\n");
		sb.append("0 1\n");

		if (setExtraBasis) {
			if (hasCDoubleBondC) {
				sb.append("C     0\n");
				sb.append("SP     1     1.00\n");
				sb.append("             0.05             1.00000000             1.00000000\n");  
				sb.append("****\n");
			}
			if (hasCarbonyl) {
				sb.append("O     0\n");
				sb.append("SP     1     1.00\n");
				sb.append("             0.070000              1.0000000              1.0000000\n");     
				sb.append("****\n");
			}
		}

		sb.append("\n");

		return sb.toString();
	}

	public void setHasCarbonyl(boolean b) {
		this.hasCarbonyl = b;
	}

	public void setHasCDoubleBondC(boolean b) {
		this.hasCDoubleBondC = b;
	}

	public void setExtraBasis(boolean b) {
		this.setExtraBasis = b;
	}

	public boolean hasCarbonyl() {
		return hasCarbonyl;
	}

	public boolean hasCDoubleBondC() {
		return hasCDoubleBondC;
	}
}
