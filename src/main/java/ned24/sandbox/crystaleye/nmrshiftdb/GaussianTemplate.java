package ned24.sandbox.crystaleye.nmrshiftdb;

public class GaussianTemplate {

	String name;
	String connectionTable;
	String solvent;
	boolean freq;

	boolean hasC;
	boolean hasCarbonyl;
	boolean setExtraBasis;

	public GaussianTemplate(String name, String connectionTable, String solvent, boolean freq) {
		this.name = name;
		this.connectionTable = connectionTable;
		this.solvent = solvent;
		this.freq = freq;
	}

	public String getNmrStepInput() {
		StringBuilder sb = new StringBuilder();
		sb.append("--Link1--\n");
		sb.append("%chk="+name+".chk\n");

		if (!setExtraBasis) {
			sb.append("#P rmpw1pw91/6-31g(d,p)\n");
			sb.append("#P NMR scrf(cpcm,solvent="+solvent+")\n");
		} else {
			sb.append("# rmpw1pw91/6-31g(d,p) NMR scrf(cpcm,solvent="+solvent+") ExtraBasis\n");
		}

		sb.append("\n");
		sb.append("Calculating  GIAO-shifts.  \n");
		sb.append("\n");
		sb.append("0 1\n");
		sb.append(connectionTable);
		sb.append("\n");
		if (setExtraBasis) {
			if (hasC) {
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

	public String getThreeStepWorkflowInput() {
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
		if (freq) {
			sb.append("#N rmpw1pw91/6-31g(d,p) geom=checkpoint opt freq guess=read\n");
		} else {
			sb.append("#N rmpw1pw91/6-31g(d,p) geom=checkpoint opt guess=read\n");
		}
		sb.append("\n");
		sb.append("Optimisation using STO-3G coordinates and DFT\n");
		sb.append("\n");
		sb.append("0 1\n");
		sb.append("\n");
		sb.append("--Link1--\n");
		sb.append("%chk="+name+".chk\n");

		if (!setExtraBasis) {
			sb.append("#P rmpw1pw91/6-31g(d,p) geom=checkpoint guess=read\n");
			sb.append("#P NMR scrf(cpcm,solvent="+solvent+")\n");
		} else {
			sb.append("# rmpw1pw91/6-31g(d,p) NMR scrf(cpcm,solvent="+solvent+") ExtraBasis\n");
		}

		sb.append("\n");
		sb.append("Calculating  GIAO-shifts.  \n");
		sb.append("\n");
		sb.append("0 1\n");

		if (setExtraBasis) {
			if (hasC) {
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

	public void setHasO(boolean b) {
		this.hasCarbonyl = b;
	}

	public void setHasC(boolean b) {
		this.hasC = b;
	}

	public void setExtraBasis(boolean b) {
		this.setExtraBasis = b;
	}

	public boolean hasCarbonyl() {
		return hasCarbonyl;
	}

	public boolean hasC() {
		return hasC;
	}
}
