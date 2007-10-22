package ned24.sandbox.crystaleye.nmrshiftdb.results;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ned24.sandbox.crystaleye.nmrshiftdb.GaussianCmlTool;
import ned24.sandbox.crystaleye.nmrshiftdb.GaussianUtils;
import nu.xom.Document;

import org.graph.GraphException;
import org.graph.Point;
import org.hist.Histogram;
import org.interpret.SVGInterpretter;
import org.layout.GraphLayout;
import org.xmlcml.cml.element.CMLPeak;

public class CreateCDistribution {

	public static void main(String[] args) {
		String path = "e:/gaussian/html/second-protocol/cml";
		
		double min = Double.POSITIVE_INFINITY;
		double max = Double.NEGATIVE_INFINITY;
		
		List<Point> pointList = new ArrayList<Point>();
		for (File file : new File(path).listFiles()) {
			GaussianCmlTool g = new GaussianCmlTool(file);
			String solvent = g.getCalculatedSolvent();
			
			List<CMLPeak> obsPeaks = g.getObservedPeaks(solvent);
			List<CMLPeak> calcPeaks = g.getListOfCalculatedPeaks();
			
			double total = 0;
			for (CMLPeak calcPeak : calcPeaks) {
				String atomId = calcPeak.getAtomRefs()[0];
				double obsShift = GaussianUtils.getPeakValue(obsPeaks, atomId);
				double calcShift = GaussianUtils.getTmsShift(solvent)-calcPeak.getXValue();
				double diff = calcShift-obsShift;
				total += diff;
			}
			int numPeaks = calcPeaks.size();
			double c = total / numPeaks;
			
			if (Double.isNaN(c)) {
				throw new RuntimeException("c is NaN: "+file.getAbsolutePath());
			}
			
			if (c > max) {
				max = c;
			}
			if (c < min) {
				min = c;
			}
			
			Point p = new Point();
			p.setX(c);
			p.setLink(file.getName());
			pointList.add(p);
		}
		
		double minR = -12;
		double maxR = 16;

		double binWidth = 0.5;
		int numBins = (int)(28/binWidth);

		GraphLayout layout = new GraphLayout();
		layout.setXmin(minR);
		layout.setXmax(maxR);
		layout.setPlotXGridLines(false);
		layout.setPlotYGridLines(false);
		try {
			layout.setNXTickMarks(14);
			layout.setNYTickMarks(16);
		} catch (GraphException e1) {
			System.err.println("Problem setting NXTickMarks");
		}

		Histogram hist1 = new Histogram(layout);
		Document doc = null;
		try {
			//hist1.setPlotfrequency(false);
			hist1.setNBins(numBins);
			hist1.addDataToPlot(pointList);
			hist1.setXlab("Bond Length (angstroms)");
			hist1.setYlab("No. occurences");
			hist1.setGraphTitle("C plot");

			hist1.plot();
			doc = new Document(hist1.getSVG());
			SVGInterpretter svgi = new SVGInterpretter (hist1);
		} catch (GraphException e) {
			System.err.println(e.getMessage());
		}
		
		System.out.println(doc.toXML());
	}
}
