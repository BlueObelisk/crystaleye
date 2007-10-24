package ned24.sandbox.crystaleye.nmrshiftdb.results;

import java.util.List;

import ned24.sandbox.crystaleye.nmrshiftdb.GaussianConstants;
import ned24.sandbox.crystaleye.nmrshiftdb.GaussianUtils;

import org.xmlcml.cml.element.CMLPeak;

public class PlotUtils implements GaussianConstants {
	
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

	public static String getHtmlContent(String protocolUrl, String htmlTitle, String startFile, String jmoljsPath, String summaryjsPath) {
		String startStruct = "";
		String button = "";
		if (startFile != null) {
			startStruct = "load "+protocolUrl+"/"+CML_DIR_NAME+"/"+startFile+"/";
		} else {
			button = "<button onclick=\"showPlot(currentStructure);\">Show plot for this structure</button>";
		}

		return "<html><head>"+
		"<script src=\""+jmoljsPath+"\" type=\"text/ecmascript\">"+
		"</script>"+
		"<script src=\""+summaryjsPath+"\" type=\"text/ecmascript\">"+
		"</script>"+
		"</head>"+
		"<body>"+
		"<div style=\"position: absolute; text-align: center; width: 100%; z-index: 100;\"><h2>"+htmlTitle+"</h2></div>"+
		"<div style=\"position: absolute; top: -50px;\">"+
		"<embed id='svgPlot' src=\"./index.svg\" width=\"715\" height=\"675\" style=\"position:absolute;\" />"+
		"<div style=\"position: absolute; left: 675px; top: 200px;\">"+
		"<script type=\"text/javascript\">jmolInitialize(\""+protocolUrl+"/\");"+
		"</script>"+
		"<script type=\"text/javascript\">jmolApplet(300, \""+startStruct+"\");</script>"+
		button+
		"</div>"+
		"</div>"+
		"</body>"+
		"</html>";
	}
}
