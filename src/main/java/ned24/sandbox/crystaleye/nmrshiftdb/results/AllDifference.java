package ned24.sandbox.crystaleye.nmrshiftdb.results;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ned24.sandbox.crystaleye.nmrshiftdb.FileListTool;
import ned24.sandbox.crystaleye.nmrshiftdb.GaussianCmlTool;
import ned24.sandbox.crystaleye.nmrshiftdb.GaussianConstants;
import ned24.sandbox.crystaleye.nmrshiftdb.GaussianUtils;
import ned24.sandbox.crystaleye.nmrshiftdb.plottools.DifferencePlot;

public class AllDifference implements GaussianConstants {

	public static void main(String[] args) {
		//String protocolName = HSR0_NAME;
		//String protocolName = HSR1_NAME;	
		//String protocolName = HSR0_MANUALMOD_NAME;
		//String protocolName = HSR0_HALOGEN_AND_MORGAN_NAME;
		String protocolName = HSR1_HALOGEN_AND_MORGAN_NAME;
		
		System.out.println(protocolName);
		String cmlDir = CML_DIR+protocolName;			
		FileListTool ft = new FileListTool(cmlDir);
		//ft.setIncludeNotRemoved(false);
		ft.setIncludeHumanEdited(true);
		ft.setIncludeMisassigned(true);
		ft.setIncludePoorStructures(true);
		ft.setIncludePossMisassigned(true);
		ft.setIncludeTautomers(true);
		ft.setIncludeTooLargeRing(true);
		List<File> fileList = ft.getFileList();
		String htmlTitle = "Selection of structures from NMRShiftDB with MW < 300";
		
		String folderName = "all-difference";
		DifferencePlot c = new DifferencePlot(fileList, protocolName, folderName, htmlTitle);
		c.run();

		String urlPrefix = "http://nmrshiftdb.ice.mpg.de/portal/js_pane/P-Results;jsessionid=FA2A776224CDA757D4B710F5FC12A899.tomcat2?nmrshiftdbaction=showDetailsFromHome&molNumber=";

		for (File file : fileList) {
			if (!file.getAbsolutePath().endsWith(".cml.xml")) {
				continue;
			}
			List<File> fileList2 = new ArrayList<File>();
			fileList2.add(file);
			GaussianCmlTool c13 = new GaussianCmlTool(file);
			String solvent = c13.getCalculatedSolvent().toLowerCase();
			String colour = GaussianUtils.getSolventPointColour(solvent);

			String filename = file.getName();
			String nameAndSolventNum = filename.substring(0,filename.indexOf("."));
			String name = nameAndSolventNum.substring(0,nameAndSolventNum.indexOf("-"));
			String title = nameAndSolventNum+"-difference";

			String number = nameAndSolventNum.substring(10);
			number = number.substring(0,number.indexOf("-"));

			String htmlTitle2 = "<a href='"+urlPrefix+number+"'>"+name+" (solvent: "+solvent+")</a>";
			DifferencePlot c2 = new DifferencePlot(fileList2, protocolName, title, htmlTitle2);
			c2.setPointColour(colour);
			c2.run();
		}
	}
}
