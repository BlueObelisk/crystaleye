package ned24.sandbox.crystaleye.nmrshiftdb.results;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ned24.sandbox.crystaleye.nmrshiftdb.GaussianConstants;
import ned24.sandbox.crystaleye.nmrshiftdb.plottools.DifferencePerSolventPlot;

public class DifferencePerSolvent implements GaussianConstants {

	public static void main(String[] args) {
		//String protocolName = HSR0_NAME;
		//String protocolName = HSR1_NAME;	
		String protocolName = HSR0_HALOGEN_AND_MORGAN_NAME;
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
		
		String folderName = "differencebysolvent";
		DifferencePerSolventPlot c = new DifferencePerSolventPlot(fileList, protocolName, folderName, htmlTitle);
		c.run();
	}
}