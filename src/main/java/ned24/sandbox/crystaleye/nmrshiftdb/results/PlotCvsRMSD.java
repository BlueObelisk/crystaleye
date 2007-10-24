package ned24.sandbox.crystaleye.nmrshiftdb.results;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ned24.sandbox.crystaleye.nmrshiftdb.GaussianCmlTool;
import nu.xom.Document;

import org.graph.GraphException;
import org.graph.Point;
import org.hist.Histogram;
import org.interpret.SVGInterpretter;
import org.layout.GraphLayout;
import org.xmlcml.cml.element.CMLPeak;

import uk.ac.cam.ch.crystaleye.IOUtils;

public class PlotCvsRMSD {

	public static void main(String[] args) {
		String path = "e:/gaussian/html/second-protocol_mod1/cml";
		
		double min = Double.POSITIVE_INFINITY;
		double max = Double.NEGATIVE_INFINITY;
		
		StringBuilder sb = new StringBuilder();
		
		List<Point> pointList = new ArrayList<Point>();
		for (File file : new File(path).listFiles()) {
			GaussianCmlTool g = new GaussianCmlTool(file);
			String solvent = g.getCalculatedSolvent();
			
			List<CMLPeak> obsPeaks = g.getObservedPeaks(solvent);
			List<CMLPeak> calcPeaks = g.getListOfCalculatedPeaks();

			double c = PlotUtils.getC(calcPeaks, obsPeaks, solvent);
			double rmsd = PlotUtils.getRMSDAboutC(c, calcPeaks, obsPeaks, solvent);
			
			sb.append(c+","+rmsd+","+file.getName()+"\n");
			
			//System.out.println(rmsd);
			Point p = new Point();
			p.setX(rmsd);
			p.setLink(file.getName());
			pointList.add(p);
					
			if (rmsd > max) {
				max = rmsd;
			}
			if (rmsd < min) {
				min = rmsd;
			}
		}

		double binWidth = 0.5;
		int numBins = (int)(28/binWidth);

		GraphLayout layout = new GraphLayout();
		layout.setXmin(0);
		layout.setXmax(28);
		layout.setYmin(0);
		layout.setYmax(60);
		layout.setPlotXGridLines(false);
		layout.setPlotYGridLines(false);
		try {
			layout.setNXTickMarks(14);
			layout.setNYTickMarks(15);
		} catch (GraphException e1) {
			System.err.println("Problem setting NXTickMarks");
		}

		Histogram hist1 = new Histogram(layout);
		Document doc = null;
		try {
			//hist1.setPlotfrequency(false);
			hist1.setNBins(numBins);
			hist1.addDataToPlot(pointList);
			hist1.setXlab("RMSD");
			hist1.setYlab("No. occurences");
			hist1.setGraphTitle("RMSD plot");

			hist1.plot();
			doc = new Document(hist1.getSVG());
			SVGInterpretter svgi = new SVGInterpretter (hist1);
		} catch (GraphException e) {
			System.err.println(e.getMessage());
		}
		
		IOUtils.writeText(sb.toString(), "e:/gaussian/html/hsr1-c_rmsd_name.csv");
		IOUtils.writePrettyXML(doc, "e:/gaussian/html/hsr1-rmsd.svg");
	}
}
