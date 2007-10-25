package ned24.sandbox.crystaleye.nmrshiftdb.results;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ned24.sandbox.crystaleye.nmrshiftdb.GaussianCmlTool;
import ned24.sandbox.crystaleye.nmrshiftdb.GaussianConstants;
import ned24.sandbox.crystaleye.nmrshiftdb.GaussianUtils;
import nu.xom.Element;
import nu.xom.Nodes;

import org.xmlcml.cml.base.CMLConstants;
import org.xmlcml.cml.element.CMLSpectrum;

public class Fields implements CMLConstants, GaussianConstants {

	public static void main(String[] args) {
		String protocolName = SECOND_PROTOCOL_NAME;
		//String protocolName = SECOND_PROTOCOL_MOD1_NAME;

		String path = CML_DIR+protocolName;
		String dictRef = "cml:field";

		Set<String> set = new HashSet<String>();
		for (File file : new File(path).listFiles()) {
			GaussianCmlTool g = new GaussianCmlTool(file);

			int s = GaussianUtils.getSpectNum(file);
			CMLSpectrum spect = g.getObservedSpectrum(s);
			Nodes fieldNodes = spect.query(".//cml:scalar[@dictRef='"+dictRef+"']", X_CML);
			for (int i = 0; i < fieldNodes.size(); i++) {
				Element fieldNode = (Element)fieldNodes.get(i);
				String field = fieldNode.getValue();
				int t = -1;
				try {
					t = Integer.valueOf(field);
				} catch (NumberFormatException e) {
					System.err.println("Not a valid field.");
					continue;
				}
				set.add(t+"MHz");
			}
		}

		for (String field : set) {
			List<File> fileList = new ArrayList<File>();
			String htmlTitle = "Taken at field of "+field+" MHz";
			for (File file : new File(path).listFiles()) {
				GaussianCmlTool g = new GaussianCmlTool(file);
				int s = GaussianUtils.getSpectNum(file);
				CMLSpectrum spect = g.getObservedSpectrum(s);
				Nodes fieldNodes = spect.query(".//cml:scalar[@dictRef='"+dictRef+"']", X_CML);
				if (fieldNodes.size() != 1) {
					continue;
				} 
				if ((fieldNodes.get(0).getValue()+"MHz").equals(field)) {
					fileList.add(file);
				}
			}
			if (fileList.size() > 0) {
				CreateShiftPlot c2 = new CreateShiftPlot(fileList, protocolName, field, htmlTitle);
				c2.run();
			}
		}	
	}
}
