package ned24.sandbox.crystaleye.nmrshiftdb.results;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ned24.sandbox.crystaleye.nmrshiftdb.GaussianCmlTool;
import ned24.sandbox.crystaleye.nmrshiftdb.GaussianConstants;
import ned24.sandbox.crystaleye.nmrshiftdb.GaussianUtils;
import ned24.sandbox.crystaleye.nmrshiftdb.plottools.ShiftPlot;
import nu.xom.Element;
import nu.xom.Nodes;

import org.xmlcml.cml.base.CMLConstants;
import org.xmlcml.cml.element.CMLSpectrum;

public class Temperatures implements CMLConstants, GaussianConstants {

	public static void main(String[] args) {
		//String protocolName = SECOND_PROTOCOL_NAME;
		//String protocolName = SECOND_PROTOCOL_MOD1_NAME;
		String protocolName = HSR0_HALOGEN_AND_MORGAN_NAME;

		String path = CML_DIR+protocolName;

		Set<String> set = new HashSet<String>();
		for (File file : new File(path).listFiles()) {
			GaussianCmlTool g = new GaussianCmlTool(file);

			int s = GaussianUtils.getSpectNum(file);
			CMLSpectrum spect = g.getObservedSpectrum(s);
			Nodes tempNodes = spect.query(".//cml:scalar[@dictRef='cml:temp']", X_CML);
			for (int i = 0; i < tempNodes.size(); i++) {
				Element tempNode = (Element)tempNodes.get(i);
				String temp = tempNode.getValue();
				int t = -1;
				try {
					t = Integer.valueOf(temp);
				} catch (NumberFormatException e) {
					System.err.println("Not a valid temperature.");
					continue;
				}
				set.add(""+t);
			}
		}

		for (String temp : set) {
			List<File> fileList = new ArrayList<File>();
			String htmlTitle = "Taken at "+temp+" C";
			for (File file : new File(path).listFiles()) {
				GaussianCmlTool g = new GaussianCmlTool(file);
				int s = GaussianUtils.getSpectNum(file);
				CMLSpectrum spect = g.getObservedSpectrum(s);
				Nodes tempNodes = spect.query(".//cml:scalar[@dictRef='cml:temp']", X_CML);
				if (tempNodes.size() != 1) {
					continue;
				} 
				if (tempNodes.get(0).getValue().equals(temp)) {
					fileList.add(file);
				}
			}
			if (fileList.size() > 0) {
				ShiftPlot c2 = new ShiftPlot(fileList, protocolName, temp, htmlTitle);
				c2.run();
			}
		}	
	}
}
