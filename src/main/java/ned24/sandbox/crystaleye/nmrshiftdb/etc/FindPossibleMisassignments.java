package ned24.sandbox.crystaleye.nmrshiftdb.etc;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ned24.sandbox.crystaleye.nmrshiftdb.GaussianCmlTool;
import ned24.sandbox.crystaleye.nmrshiftdb.GaussianConstants;
import ned24.sandbox.crystaleye.nmrshiftdb.GaussianUtils;

import org.graph.Point;
import org.xmlcml.cml.base.CMLElements;
import org.xmlcml.cml.element.CMLPeak;

public class FindPossibleMisassignments implements GaussianConstants {

	static int PPM_EPS = 2;
	
	String protocolName;
	
	public FindPossibleMisassignments(String protocolName) {
		this.protocolName = protocolName;
	}
	
	public Set<File> getFileList() {
		String path = CML_DIR+protocolName;
		Set<File> fileList = new HashSet<File>();
		for (File file : new File(path).listFiles()) {
			if (!file.getAbsolutePath().endsWith(".cml.xml")) {
				continue;
			}
			List<Point> pointList = new ArrayList<Point>();
			GaussianCmlTool c = new GaussianCmlTool(file);
			String solvent = c.getCalculatedSolvent();
			boolean b = c.testSpectraConcordant(solvent);
			if (!b) {
				continue;
			}
			double tmsShift = GaussianUtils.getTmsShift(solvent);

			int s = GaussianUtils.getSpectNum(file);
			CMLElements<CMLPeak> calcPeaks = c.getCalculatedPeaks();
			List<CMLPeak> obsPeaks = c.getObservedPeaks(s);

			for (int i = 0; i < calcPeaks.size(); i++) {
				CMLPeak calcPeak = (CMLPeak)calcPeaks.get(i);
				double calcShift = calcPeak.getXValue();
				calcShift = tmsShift-calcShift;
				String calcId = calcPeak.getAtomRefs()[0];

				double obsShift = GaussianUtils.getPeakValue(obsPeaks, calcId);

				double y = getYValue(obsShift, calcShift);
				double x = getXValue(obsShift, calcShift);
				Point p = new Point();
				p.setX(x);
				p.setY(y);
				pointList.add(p);
			}

			for (Point p1 : pointList) {
				for (Point p2 : pointList) {
					Double x1 = p1.getX();
					Double x2 = p2.getX();
					if (!x1.equals(x2)) {
						if (Math.abs(x1-x2) < PPM_EPS) {
							double y1 = p1.getY();
							double y2 = p2.getY();
							if (Math.abs(y1-y2) > PPM_EPS) {
								fileList.add(file);
							}
						}
					}
				}
			}
		}

		for (File file : fileList) {
			//System.out.println(file.getAbsolutePath());
		}
		//System.out.println(fileList.size());
		return fileList;
	}

	public static void main(String[] args) {		
		//String protocolName = HSR0_NAME;
		//String protocolName = HSR1_NAME;
		String protocolName = HSR0_HALOGEN_AND_MORGAN_NAME;
	
	}

	public static double getXValue(double calc, double obs) {
		double ret = (obs+calc)/2;

		return ret;
	}

	public static double getYValue(double calc, double obs) {
		double ret = (calc-obs)/2;

		return ret;
	}
}
