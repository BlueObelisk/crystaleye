package ned24.sandbox.crystaleye.nmrshiftdb.results;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ned24.sandbox.crystaleye.nmrshiftdb.GaussianConstants;
import ned24.sandbox.crystaleye.nmrshiftdb.GaussianUtils;
import ned24.sandbox.crystaleye.nmrshiftdb.GaussianUtils.Solvent;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;
import uk.ac.cam.ch.crystaleye.CrystalEyeConstants;
import uk.ac.cam.ch.crystaleye.IOUtils;

public class CreateSolventComparisonScatter implements GaussianConstants, CrystalEyeConstants {

	public static void main(String[] args) {
		String protocolName = SECOND_PROTOCOL_NAME;
		//String protocolName = SECOND_PROTOCOL_MOD1_NAME;

		String rootFolder = HTML_DIR+protocolName;

		String[] colours = {"blue", "green", "olive", "purple", "orange", "gray"};

		List<String> solvents = new ArrayList<String>();
		for (Solvent solvent : GaussianUtils.Solvent.values()) {
			String s = solvent.toString();
			solvents.add(s);
		}

		int count = 0;
		Document mainDoc = null;
		for (File folder : new File(rootFolder).listFiles()) {
			if (!solvents.contains(folder.getName())) {
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
					link.getChildElements().get(0).getAttribute("fill").setValue(colour);
				}
			}
			count++;
		}

		String html = PlotUtils.getHtmlContent("Comparison of solvents", protocolName, null);
		String root = rootFolder+File.separator+"solvents";
		IOUtils.writeText(html, root+File.separator+"index.html");
		IOUtils.writePrettyXML(mainDoc, root+File.separator+"index.svg");
	}
}
