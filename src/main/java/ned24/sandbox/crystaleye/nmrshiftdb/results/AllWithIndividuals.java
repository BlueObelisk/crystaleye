package ned24.sandbox.crystaleye.nmrshiftdb.results;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ned24.sandbox.crystaleye.nmrshiftdb.C13SpectraTool;
import ned24.sandbox.crystaleye.nmrshiftdb.GaussianConstants;

public class AllWithIndividuals implements GaussianConstants {

	public static void main(String[] args) {
		String path = JMOL_ROOT_DIR+CML_DIR_NAME;
		String outFolderName = "all";
				
		List<File> fileList = Arrays.asList(new File(path).listFiles());
		String htmlTitle = "Selection of structures from NMRShiftDB with MW < 300";
		CreateShiftPlot c = new CreateShiftPlot(fileList, outFolderName, htmlTitle);
		c.run();
		
		String urlPrefix = "http://nmrshiftdb.ice.mpg.de/portal/js_pane/P-Results;jsessionid=FA2A776224CDA757D4B710F5FC12A899.tomcat2?nmrshiftdbaction=showDetailsFromHome&molNumber=";
		for (File file : new File(path).listFiles()) {
			List<File> fileList2 = new ArrayList<File>();
			fileList2.add(file);
			C13SpectraTool c13 = new C13SpectraTool(file);
			String solvent = c13.getCalculatedSolvent().toLowerCase();
			String name = file.getName();
			name = name.substring(0,name.indexOf("-"));
			String number = name.substring(10);
			String htmlTitle2 = "<a href='"+urlPrefix+number+"'>"+name+" (solvent: "+solvent+")</a>";
			outFolderName = name;
			CreateShiftPlot c2 = new CreateShiftPlot(fileList2, outFolderName, htmlTitle2);
			c2.run();
		}
		
	}
}