package ned24.sandbox.crystaleye.nmrshiftdb.results;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import ned24.sandbox.crystaleye.nmrshiftdb.GaussianCmlTool;
import ned24.sandbox.crystaleye.nmrshiftdb.GaussianConstants;
import ned24.sandbox.crystaleye.nmrshiftdb.GaussianUtils;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;

import org.xmlcml.cml.element.CMLSpectrum;

import uk.ac.cam.ch.crystaleye.CrystalEyeConstants;
import uk.ac.cam.ch.crystaleye.IOUtils;

public class FieldComparison implements GaussianConstants, CrystalEyeConstants {

	public static void main(String[] args) {
		//String protocolName = SECOND_PROTOCOL_NAME;
		//String protocolName = SECOND_PROTOCOL_MOD1_NAME;
		String protocolName = HSR0_MANUAL_AND_MORGAN_NAME;

		String path = HTML_DIR+protocolName;
		String cmlPath = CML_DIR+protocolName;

		Set<String> set = new HashSet<String>();
		for (File file : new File(cmlPath).listFiles()) {
			GaussianCmlTool g = new GaussianCmlTool(file);

			int s = GaussianUtils.getSpectNum(file);
			CMLSpectrum spect = g.getObservedSpectrum(s);
			Nodes tempNodes = spect.query(".//cml:scalar[@dictRef='cml:field']", X_CML);
			for (int i = 0; i < tempNodes.size(); i++) {
				Element tempNode = (Element)tempNodes.get(i);
				set.add(tempNode.getValue()+"MHz");
			}
		}

		int count = 0;
		Document mainDoc = null;
		Set<String> outSet = new HashSet<String>();
		for (File folder : new File(path).listFiles()) {
			if (!set.contains(folder.getName())) {
				continue;
			}
			File svgFile = new File(folder+File.separator+"index.svg");
			if (!svgFile.exists()) {
				throw new RuntimeException("Can't find svg file: "+svgFile.getAbsolutePath());
			}
			if (count == 0) {
				mainDoc = IOUtils.parseXmlFile(svgFile);
			} else {
				Document doc = IOUtils.parseXmlFile(svgFile);
				Nodes links = doc.query(".//svg:a", X_SVG);
				for (int i = 0; i < links.size(); i++) {
					Element link = (Element)links.get(i);
					link.detach();
					mainDoc.getRootElement().insertChild(link, 0);

					String colour = colours[count-1];
					outSet.add(colour+" "+folder.getName());
					
					link.getChildElements().get(0).getAttribute("fill").setValue(colour);
				}
			}
			count++;
		}
		
		for (String out : outSet) {
			System.out.println(out);
		}

		String html = PlotUtils.getHtmlContent("Comparison of fields", protocolName, null, false);
		String root = path+File.separator+"fields";
		IOUtils.writeText(html, root+File.separator+"index.html");
		IOUtils.writePrettyXML(mainDoc, root+File.separator+"index.svg");
	}
}
