package ned24.sandbox.crystaleye.nmrshiftdb.results;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ned24.sandbox.crystaleye.nmrshiftdb.GaussianCmlTool;
import ned24.sandbox.crystaleye.nmrshiftdb.GaussianConstants;
import ned24.sandbox.crystaleye.nmrshiftdb.GaussianUtils;
import ned24.sandbox.crystaleye.nmrshiftdb.GaussianUtils.Solvent;

public class Solvents implements GaussianConstants {

	public static void main(String[] args) {
		//String protocolName = SECOND_PROTOCOL_NAME;
		//|String protocolName = SECOND_PROTOCOL_MOD1_NAME;
		String protocolName = SECOND_PROTOCOL_MANUAL_AND_MORGAN_NAME;
		
		String path = CML_DIR+protocolName;

		for (Solvent s : GaussianUtils.Solvent.values()) {
			List<File> fileList = new ArrayList<File>();
			String htmlTitle = s.toString();
			for (File file : new File(path).listFiles()) {
				GaussianCmlTool c13 = new GaussianCmlTool(file);
				String solvent = c13.getCalculatedSolvent();
				solvent = GaussianUtils.nmrShiftDbSolvent2GaussianSolvent(solvent);
				Solvent thisSolvent = GaussianUtils.getSolvent(solvent);
				if (thisSolvent.equals(s)) {
					fileList.add(file);
				}
			}
			if (fileList.size() > 0) {
				CreateShiftPlot c2 = new CreateShiftPlot(fileList, protocolName, s.toString(), htmlTitle);
				c2.run();
			}
		}		
	}
}
