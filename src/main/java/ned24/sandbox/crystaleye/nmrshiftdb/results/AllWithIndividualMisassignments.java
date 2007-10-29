package ned24.sandbox.crystaleye.nmrshiftdb.results;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ned24.sandbox.crystaleye.nmrshiftdb.GaussianCmlTool;
import ned24.sandbox.crystaleye.nmrshiftdb.GaussianConstants;

public class AllWithIndividualMisassignments implements GaussianConstants {

	public static void main(String[] args) {
		//String protocolName = SECOND_PROTOCOL_NAME;
		//String protocolName = SECOND_PROTOCOL_MOD1_NAME;
		
		//String protocolName = SECOND_PROTOCOL_MANUALMOD_NAME;
		String protocolName = SECOND_PROTOCOL_MANUAL_AND_MORGAN_NAME;
		
		
		System.out.println(protocolName);
		String folderName = "all-misassignments";
		String cmlFolder = CML_DIR+protocolName;
				
		List<File> fileList = Arrays.asList(new File(cmlFolder).listFiles());
		String htmlTitle = "Selection of structures from NMRShiftDB with MW < 300";
		CreateMisassignmentPlot c = new CreateMisassignmentPlot(fileList, protocolName, folderName, htmlTitle);
		c.run();
		
		String urlPrefix = "http://nmrshiftdb.ice.mpg.de/portal/js_pane/P-Results;jsessionid=FA2A776224CDA757D4B710F5FC12A899.tomcat2?nmrshiftdbaction=showDetailsFromHome&molNumber=";
		for (File file : new File(cmlFolder).listFiles()) {
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
			CreateMisassignmentPlot c2 = new CreateMisassignmentPlot(fileList2, protocolName, title, htmlTitle2);
			c2.run();
		}
		
	}
}
