package ned24.sandbox.crystaleye.nmrshiftdb.results;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ned24.sandbox.crystaleye.nmrshiftdb.GaussianCmlTool;
import ned24.sandbox.crystaleye.nmrshiftdb.GaussianConstants;
import ned24.sandbox.crystaleye.nmrshiftdb.plottypes.CreateMisassignmentPlot;
import ned24.sandbox.crystaleye.nmrshiftdb.plottypes.CreateShiftPlot;

public class AllWithIndividualsAndMisassignments implements GaussianConstants {

	public static void main(String[] args) {
		//String protocolName = HSR0_NAME;
		//String protocolName = HSR1_NAME;	
		String protocolName = HSR0_MANUAL_AND_MORGAN_NAME;
		//String protocolName = HSR1_MANUAL_AND_MORGAN_NAME;
		
		System.out.println(protocolName);
		String cmlFolder = CML_DIR+protocolName;			
		List<File> fileList = new ArrayList<File>();
		for (File file : new File(cmlFolder).listFiles()) {
			if (file.getAbsolutePath().endsWith(".cml.xml")) {
				fileList.add(file);
			}
		}
		String htmlTitle = "Selection of structures from NMRShiftDB with MW < 300";
		
		String folderName = "all";
		CreateShiftPlot c = new CreateShiftPlot(fileList, protocolName, folderName, htmlTitle);
		c.run();
		
		
		String allMisassignmentFolderName = "all-misassignments";
		CreateMisassignmentPlot cm = new CreateMisassignmentPlot(fileList, protocolName, allMisassignmentFolderName, htmlTitle);
		cm.run();
		
		String urlPrefix = "http://nmrshiftdb.ice.mpg.de/portal/js_pane/P-Results;jsessionid=FA2A776224CDA757D4B710F5FC12A899.tomcat2?nmrshiftdbaction=showDetailsFromHome&molNumber=";
		
		for (File file : new File(cmlFolder).listFiles()) {
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
			String title = nameAndSolventNum;
			
			String number = nameAndSolventNum.substring(10);
			number = number.substring(0,number.indexOf("-"));
			
			String htmlTitle2 = "<a href='"+urlPrefix+number+"'>"+name+" (solvent: "+solvent+")</a>";
			CreateShiftPlot c2 = new CreateShiftPlot(fileList2, protocolName, title, htmlTitle2);
			c2.run();
		}
		
		for (File file : new File(cmlFolder).listFiles()) {
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
			CreateMisassignmentPlot c2 = new CreateMisassignmentPlot(fileList2, protocolName, title, htmlTitle2);
			c2.run();
		}
		
	}
}
