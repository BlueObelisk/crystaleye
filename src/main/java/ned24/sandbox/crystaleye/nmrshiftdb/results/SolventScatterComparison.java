package ned24.sandbox.crystaleye.nmrshiftdb.results;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ned24.sandbox.crystaleye.nmrshiftdb.GaussianConstants;
import ned24.sandbox.crystaleye.nmrshiftdb.GaussianUtils;
import ned24.sandbox.crystaleye.nmrshiftdb.GaussianUtils.Solvent;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;
import uk.ac.cam.ch.crystaleye.CrystalEyeConstants;
import uk.ac.cam.ch.crystaleye.IOUtils;

public class SolventScatterComparison implements GaussianConstants, CrystalEyeConstants {

	public static void main(String[] args) {
		//String protocolName = SECOND_PROTOCOL_NAME;
		//String protocolName = SECOND_PROTOCOL_MOD1_NAME;
		//String protocolName = HSR0_HALOGEN_AND_MORGAN_NAME;
		String protocolName = HSR1_HALOGEN_AND_MORGAN_NAME;

		String rootFolder = HTML_DIR+protocolName;

		List<String> solvents = new ArrayList<String>();
		for (Solvent solvent : GaussianUtils.Solvent.values()) {
			String s = solvent.toString();
			solvents.add(s+"-scatter");
		}

		int count = 0;
		Document mainDoc = null;
		
		Set<String> outSet = new HashSet<String>();
		for (File folder : new File(rootFolder).listFiles()) {
			if (!solvents.contains(folder.getName())) {
				continue;
			}
			String solvent = folder.getName().substring(0,folder.getName().length()-8);
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

					String colour = GaussianUtils.getSolventPointColour(solvent.toLowerCase());
					outSet.add(colour+" "+folder.getName());
					Element el = link.getChildElements().get(0);
					el.getAttribute("fill").setValue(colour);
				}
			}
			count++;
		}
		
		for (String out : outSet) {
			System.out.println(out);
		}
		
		String extraHtml = "<div style='text-align: left; padding-left: 20px;'>" +
		"<span>Key:</span><br />" +
		"<span style='border: 1px solid; background-color: red; margin-bottom: 2px;'>...</span><span> = Chloroform</span><br />" +
		"<span style='border: 1px solid; background-color: GoldenRod; margin-bottom: 2px;'>...</span><span> = DMSO</span><br />" +
		"<span style='border: 1px solid; background-color: Blue; margin-bottom: 2px;'>...</span><span> = Water</span><br />" +
		"<span style='border: 1px solid; background-color: AntiqueWhite; margin-bottom: 2px;'>...</span><span> = Methanol</span><br />" +
		"<span style='border: 1px solid; background-color: Aqua; margin-bottom: 2px;'>...</span><span> = Carbon Tetrachloride</span><br />" +
		"<span style='border: 1px solid; background-color: DarkCyan; margin-bottom: 2px;'>...</span><span> = Benzene</span><br />" +
		"<span style='border: 1px solid; background-color: YellowGreen; margin-bottom: 2px;'>...</span><span> = Acetone</span><br />" +
		"</div>";

		String html = PlotUtils.getHtmlContent("Comparison of solvents", protocolName, null, null, extraHtml);
		String root = rootFolder+File.separator+"solvents-scatter";
		IOUtils.writeText(html, root+File.separator+"index.html");
		IOUtils.writePrettyXML(mainDoc, root+File.separator+"index.svg");
	}
}
