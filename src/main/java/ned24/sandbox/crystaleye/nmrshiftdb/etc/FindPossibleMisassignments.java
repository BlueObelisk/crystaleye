package ned24.sandbox.crystaleye.nmrshiftdb.etc;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ned24.sandbox.crystaleye.nmrshiftdb.GaussianCmlTool;
import ned24.sandbox.crystaleye.nmrshiftdb.GaussianConstants;
import ned24.sandbox.crystaleye.nmrshiftdb.GaussianUtils;

import org.xmlcml.cml.base.CMLElements;
import org.xmlcml.cml.element.CMLPeak;

public class FindPossibleMisassignments implements GaussianConstants {

	static int PPM_EPS = 2;

	public static void main(String[] args) {		
		//String protocolName = HSR0_NAME;
		//String protocolName = HSR1_NAME;
		String protocolName = HSR0_MANUAL_AND_MORGAN_NAME;
		String path = CML_DIR+protocolName;

		Set<File> fileList = new HashSet<File>();
		for (File file : new File(path).listFiles()) {
			if (!file.getAbsolutePath().endsWith(".cml.xml")) {
				continue;
			}
			List<Double> xList = new ArrayList<Double>();
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
				xList.add(x);
			}

			for (Double d : xList) {
				for (Double o : xList) {
					if (!d.equals(o)) {
						if (Math.abs(d-o) < PPM_EPS) {
							fileList.add(file);
						}
					}
				}
			}
		}

		for (File file : fileList) {
			System.out.println(file.getAbsolutePath());
		}
		System.out.println(fileList.size());	
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
