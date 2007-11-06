package ned24.sandbox.crystaleye.nmrshiftdb.results;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ned24.sandbox.crystaleye.nmrshiftdb.FileListTool;
import ned24.sandbox.crystaleye.nmrshiftdb.GaussianCmlTool;
import ned24.sandbox.crystaleye.nmrshiftdb.GaussianConstants;
import ned24.sandbox.crystaleye.nmrshiftdb.plottools.MisassignmentPlot;

public class AllMisassignments implements GaussianConstants {

	public static void main(String[] args) {
//		String protocolName = HSR0_NAME;
		//String protocolName = HSR1_NAME;
		//String protocolName = HSR0_HALOGEN_NAME;
		//String protocolName = HSR1_HALOGEN_NAME;
		//String protocolName = HSR0_HALOGEN_AND_MORGAN_NAME;
		String protocolName = HSR1_HALOGEN_AND_MORGAN_NAME;
		
		String cmlDir = CML_DIR+protocolName;
		String folderName = "all-misassignments";
		FileListTool ft = new FileListTool(cmlDir);
		//ft.setIncludeNotRemoved(false);folderName+="_nr";
		ft.setIncludeHumanEdited(true);folderName+="_he";
		ft.setIncludeMisassigned(true);folderName+="_m";
		ft.setIncludePoorStructures(true);folderName+="_ps";
		ft.setIncludePossMisassigned(true);folderName+="_pm";
		ft.setIncludeTautomers(true);folderName+="_ta";
		ft.setIncludeTooLargeRing(true);folderName+="_lr";
		List<File> fileList = ft.getFileList();

		String htmlTitle = "Selection of structures from NMRShiftDB with MW < 300";

		MisassignmentPlot cm = new MisassignmentPlot(fileList, protocolName, folderName, htmlTitle);
		cm.run();

		String urlPrefix = "http://nmrshiftdb.ice.mpg.de/portal/js_pane/P-Results;jsessionid=FA2A776224CDA757D4B710F5FC12A899.tomcat2?nmrshiftdbaction=showDetailsFromHome&molNumber=";

		for (File file : fileList) {
			if (!file.getAbsolutePath().endsWith(".cml.xml")) {
				continue;
			}
			List<File> fileList2 = new ArrayList<File>();
			fileList2.add(file);
			GaussianCmlTool c13 = new GaussianCmlTool(file);
			String solvent = c13.getCalculatedSolvent().toLowerCase();
			String filename = file.getName();

			String nameAndSolventNum = filename.substring(0,filename.indexOf("."));
			String name = nameAndSolventNum.substring(0,nameAndSolventNum.indexOf("-"));
			String title = nameAndSolventNum+"-misassignment";

			String number = nameAndSolventNum.substring(10);
			number = number.substring(0,number.indexOf("-"));

			String htmlTitle2 = "<a href='"+urlPrefix+number+"'>"+name+" (solvent: "+solvent+")</a>";
			MisassignmentPlot c2 = new MisassignmentPlot(fileList2, protocolName, title, htmlTitle2);
			c2.run();
		}

	}
}
