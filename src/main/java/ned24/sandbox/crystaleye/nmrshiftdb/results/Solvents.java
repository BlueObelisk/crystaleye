package ned24.sandbox.crystaleye.nmrshiftdb.results;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ned24.sandbox.crystaleye.nmrshiftdb.C13SpectraTool;
import ned24.sandbox.crystaleye.nmrshiftdb.GaussianConstants;
import ned24.sandbox.crystaleye.nmrshiftdb.GaussianUtils;
import ned24.sandbox.crystaleye.nmrshiftdb.GaussianUtils.Solvent;

public class Solvents implements GaussianConstants {

	public static void main(String[] args) {
		String path = JMOL_ROOT_DIR+CML_DIR_NAME;

		for (Solvent s : GaussianUtils.Solvent.values()) {
			List<File> fileList = new ArrayList<File>();
			String outFolderName = s.toString();
			String htmlTitle = s.toString();
			for (File file : new File(path).listFiles()) {
				C13SpectraTool c13 = new C13SpectraTool(file);
				String solvent = c13.getCalculatedSolvent();
				solvent = GaussianUtils.nmrShiftDbSolvent2GaussianSolvent(solvent);
				Solvent thisSolvent = GaussianUtils.getSolvent(solvent);
				if (thisSolvent.equals(s)) {
					fileList.add(file);
				}
			}
			if (fileList.size() > 0) {
				CreateShiftPlot c2 = new CreateShiftPlot(fileList, outFolderName, htmlTitle);
				c2.run();
			}
		}		
	}
}
