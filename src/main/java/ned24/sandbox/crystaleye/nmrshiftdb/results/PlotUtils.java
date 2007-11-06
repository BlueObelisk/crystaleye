package ned24.sandbox.crystaleye.nmrshiftdb.results;

import java.util.List;

import ned24.sandbox.crystaleye.nmrshiftdb.GaussianConstants;
import ned24.sandbox.crystaleye.nmrshiftdb.GaussianUtils;

import org.xmlcml.cml.element.CMLPeak;

public class PlotUtils implements GaussianConstants {

	public static enum PlotType {
		MISASSIGNMENT, OBS_VS_CALC, DIFFERENCE;
	}

	public static double getRMSDAboutC(List<CMLPeak> calcPeaks, List<CMLPeak> obsPeaks, String solvent) {
		double c = getC(calcPeaks, obsPeaks, solvent);
		if (Double.isNaN(c)) {
			throw new RuntimeException("c is NaN");
		}
		return getRMSDAboutC(c, calcPeaks, obsPeaks, solvent);
	}

	public static double getRMSDAboutC(double c, List<CMLPeak> calcPeaks, List<CMLPeak> obsPeaks, String solvent) {
		double totalSquared = 0;
		for (CMLPeak calcPeak : calcPeaks) {
			String atomId = calcPeak.getAtomRefs()[0];
			double obsShift = GaussianUtils.getPeakValue(obsPeaks, atomId);
			double calcShift = GaussianUtils.getTmsShift(solvent)-calcPeak.getXValue();
			double d = Math.pow(calcShift-(obsShift+c), 2);
			totalSquared += d;
		}
		int numPeaks = calcPeaks.size();
		double newC = totalSquared / numPeaks;
		return Math.sqrt(newC);
	}

	public static double getC(List<CMLPeak> calcPeaks, List<CMLPeak> obsPeaks, String solvent) {
		double total = 0;
		for (CMLPeak calcPeak : calcPeaks) {
			String atomId = calcPeak.getAtomRefs()[0];
			double obsShift = GaussianUtils.getPeakValue(obsPeaks, atomId);
			double calcShift = GaussianUtils.getTmsShift(solvent)-calcPeak.getXValue();
			double diff = calcShift-obsShift;
			total += diff;
		}
		int numPeaks = calcPeaks.size();
		return total / numPeaks;		
	}

	public static String getHtmlContent(String htmlTitle, String protocolName, String startFile, PlotType type, String extraHtml) {
		String startStruct = "";
		String button = "";
		String setCurrentStructure = "";
		if (startFile != null) {
			startStruct = "load ../../../cml/"+protocolName+"/"+startFile;
			int idx = startFile.indexOf(".");
			String c = startFile.substring(0,idx);
			setCurrentStructure = "<script type=\"text/ecmascript\">"+
			"setCurrentStructure('"+c+"');"+
			"</script>";
		} else {
			button = "<button onclick=\"showObsVsCalcPlot(currentStructure);\">Show obs vs calc plot</button>"
				+"<button onclick=\"showDifferencePlot(currentStructure);\">Show difference plot</button>"
				+"<button onclick=\"showMisassignmentPlot(currentStructure);\">Show misassignment plot</button>";
		}

		if (type != null && startFile != null) {
			if (type.equals(PlotType.DIFFERENCE)) {
				button = "<button onclick=\"showObsVsCalcPlot(currentStructure);\">Show obs vs calc plot</button>"
					+"<button onclick=\"showMisassignmentPlot(currentStructure);\">Show misassignment plot</button>";
			} else if (type.equals(PlotType.MISASSIGNMENT)) {
				button = "<button onclick=\"showObsVsCalcPlot(currentStructure);\">Show obs vs calc plot</button>"
					+"<button onclick=\"showDifferencePlot(currentStructure);\">Show difference plot</button>";
			} else if (type.equals(PlotType.OBS_VS_CALC)) {
				button = "<button onclick=\"showDifferencePlot(currentStructure);\">Show difference plot</button>"
					+"<button onclick=\"showMisassignmentPlot(currentStructure);\">Show misassignment plot</button>";
			}
		}

		return "<html><head>"+
		"<script src=\"../../../Jmol.js\" type=\"text/ecmascript\">"+
		"</script>"+
		"<script src=\"../../../summary.js\" type=\"text/ecmascript\">"+
		"</script>"+
		setCurrentStructure+
		"</head>"+
		"<body>"+
		"<div style=\"position: absolute; text-align: center; width: 100%; z-index: 100;\"><h2>"+htmlTitle+"</h2></div>"+
		"<div style=\"position: absolute; top: -50px;\">"+
		"<embed id='svgPlot' src=\"./index.svg\" width=\"715\" height=\"675\" style=\"position:absolute;\" />"+
		"<div style=\"position: absolute; left: 675px; top: 150px; text-align: center;\">"+
		"<script type=\"text/javascript\">jmolInitialize(\"../../../\");"+
		"</script>"+
		"<script type=\"text/javascript\">jmolApplet(300, \""+startStruct+"\");</script>"+
		button+
		"<p><span id='coords'>No point selected.</span></p>"+
		extraHtml+
		"</div>"+
		"</div>"+
		"</body>"+
		"</html>";
	}
}
