package ned24.sandbox.crystaleye.nmrshiftdb.results;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ned24.sandbox.crystaleye.nmrshiftdb.GaussianCmlTool;
import ned24.sandbox.crystaleye.nmrshiftdb.GaussianConstants;
import ned24.sandbox.crystaleye.nmrshiftdb.GaussianScatter;
import ned24.sandbox.crystaleye.nmrshiftdb.GaussianUtils;
import nu.xom.Document;

import org.graph.Point;
import org.xmlcml.cml.element.CMLMolecule;
import org.xmlcml.cml.element.CMLPeak;

import uk.ac.cam.ch.crystaleye.IOUtils;

public class CvsRMSD implements GaussianConstants {

	public static void main(String[] args) {
		//String protocolName = SECOND_PROTOCOL_NAME;
		//String protocolName = SECOND_PROTOCOL_MOD1_NAME;
		//String protocolName = SECOND_PROTOCOL_MANUALMOD_NAME;
		String protocolName = SECOND_PROTOCOL_MANUAL_AND_MORGAN_NAME;
		
		String path = CML_DIR+protocolName;
		String folderName = "RMSD-vs-C";
		
		double min = Double.POSITIVE_INFINITY;
		double max = Double.NEGATIVE_INFINITY;
		
		List<Point> pointList = new ArrayList<Point>();
		for (File file : new File(path).listFiles()) {
			GaussianCmlTool g = new GaussianCmlTool(file);
			CMLMolecule molecule = g.getMolecule();
			String solvent = g.getCalculatedSolvent();
			int s = GaussianUtils.getSpectNum(file);
			List<CMLPeak> obsPeaks = g.getObservedPeaks(s);
			List<CMLPeak> calcPeaks = g.getListOfCalculatedPeaks();

			double c = PlotUtils.getC(calcPeaks, obsPeaks, solvent);
			double rmsd = PlotUtils.getRMSDAboutC(c, calcPeaks, obsPeaks, solvent);
			
			//System.out.println(rmsd);
			Point p = new Point();
			p.setX(rmsd);
			p.setY(c);
			int count = GaussianUtils.getAtomPosition(molecule, "99999");
			p.setLink("javascript:changeAtom('../../../cml/"+protocolName+"/"+file.getName()+"', "+count+");");
			pointList.add(p);
					
			if (rmsd > max) {
				max = rmsd;
			}
			if (rmsd < min) {
				min = rmsd;
			}
		}

		GaussianScatter gs = new GaussianScatter(pointList);
		gs.setXmin(0);
		gs.setYmin(-12);
		gs.setXmax(12);
		gs.setYmax(12);
		gs.setXTickMarks(12);
		gs.setYTickMarks(12);
		gs.setXLab("RMSD");
		gs.setYLab("C");
		Document doc = gs.getPlot();
		
		String outFolderPath = HTML_DIR+File.separator+protocolName+File.separator+folderName;
		String svgPath = outFolderPath+"/index.svg";
		IOUtils.writePrettyXML(doc, svgPath);
		String htmlContent = PlotUtils.getHtmlContent("RMSD vs. C", protocolName, null);
		String htmlPath = outFolderPath+"/index.html";
		IOUtils.writeText(htmlContent, htmlPath);
	}
}
