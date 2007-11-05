package ned24.sandbox.crystaleye.nmrshiftdb.results;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ned24.sandbox.crystaleye.nmrshiftdb.FileListTool;
import ned24.sandbox.crystaleye.nmrshiftdb.GaussianCmlTool;
import ned24.sandbox.crystaleye.nmrshiftdb.GaussianConstants;
import ned24.sandbox.crystaleye.nmrshiftdb.GaussianUtils;
import ned24.sandbox.crystaleye.nmrshiftdb.GaussianUtils.Solvent;
import ned24.sandbox.crystaleye.nmrshiftdb.plottools.ShiftPlot;

public class SolventScatters implements GaussianConstants {

	public static void main(String[] args) {
		//String protocolName = SECOND_PROTOCOL_NAME;
		//|String protocolName = SECOND_PROTOCOL_MOD1_NAME;
		//String protocolName = HSR0_HALOGEN_AND_MORGAN_NAME;
		String protocolName = HSR1_HALOGEN_AND_MORGAN_NAME;
		
		System.out.println(protocolName);
		String cmlDir = CML_DIR+protocolName;			
		FileListTool ft = new FileListTool(cmlDir);
		//ft.setIncludeNotRemoved(false);
		//ft.setIncludeHumanEdited(true);
		//ft.setIncludeMisassigned(true);
		//ft.setIncludePoorStructures(true);
		//ft.setIncludePossMisassigned(true);
		//ft.setIncludeTautomers(true);
		//ft.setIncludeTooLargeRing(true);
		List<File> fileList = ft.getFileList();

		for (Solvent s : GaussianUtils.Solvent.values()) {
			List<File> solventFileList = new ArrayList<File>();
			String htmlTitle = s.toString();
			for (File file : fileList) {
				GaussianCmlTool c13 = new GaussianCmlTool(file);
				String solvent = c13.getCalculatedSolvent();
				solvent = GaussianUtils.nmrShiftDbSolvent2GaussianSolvent(solvent);
				Solvent thisSolvent = GaussianUtils.getSolvent(solvent);
				if (thisSolvent.equals(s)) {
					solventFileList.add(file);
				}
			}
			if (solventFileList.size() > 0) {
				ShiftPlot c2 = new ShiftPlot(solventFileList, protocolName, s.toString()+"-scatter", htmlTitle);
				c2.run();
			}
		}		
	}
}
