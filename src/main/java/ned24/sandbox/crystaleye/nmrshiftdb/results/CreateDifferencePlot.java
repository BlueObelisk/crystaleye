package ned24.sandbox.crystaleye.nmrshiftdb.results;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ned24.sandbox.crystaleye.nmrshiftdb.GaussianCmlTool;
import ned24.sandbox.crystaleye.nmrshiftdb.GaussianConstants;
import ned24.sandbox.crystaleye.nmrshiftdb.GaussianScatter;
import ned24.sandbox.crystaleye.nmrshiftdb.GaussianUtils;
import nu.xom.Document;

import org.graph.Point;
import org.xmlcml.cml.base.CMLElements;
import org.xmlcml.cml.element.CMLMolecule;
import org.xmlcml.cml.element.CMLPeak;
import org.xmlcml.cml.element.CMLPeakList;
import org.xmlcml.cml.element.CMLSpectrum;

import uk.ac.cam.ch.crystaleye.IOUtils;

public class CreateDifferencePlot implements GaussianConstants {

	List<File> fileList1;
	List<File> fileList2;
	String outFolderName;
	String htmlTitle;

	String protocolUrl;
	String jmoljsPath;
	String summaryjsPath;

	String startFile = null;

	public CreateDifferencePlot(List<File> fileList1, List<File> fileList2, String outFolderName, String htmlTitle, String protocolUrl, String jmoljsPath, String summaryJsPath) {
		this.fileList1 = fileList1;
		this.fileList2 = fileList2;
		if (fileList1.size() != fileList2.size()) {
			throw new RuntimeException("File lists are of differing sizes.");
		}
		if (fileList1.size() == 1) {
			startFile = fileList1.get(0).getName();
		}
		this.outFolderName = outFolderName;
		this.htmlTitle = htmlTitle;
		this.protocolUrl = protocolUrl;
		this.jmoljsPath = jmoljsPath;
		this.summaryjsPath = summaryJsPath;
	}


	public Document getPlot() {
		List<Point> pointList = new ArrayList<Point>();
		double minx = Double.POSITIVE_INFINITY;
		double maxx = Double.NEGATIVE_INFINITY;
		double miny = Double.POSITIVE_INFINITY;
		double maxy = Double.NEGATIVE_INFINITY;

		for (int j = 0; j < fileList1.size(); j++) {
			File file1 = fileList1.get(j);
			System.out.println(file1.getAbsolutePath());
			File file2 = fileList2.get(j);

			GaussianCmlTool hsrC = new GaussianCmlTool(file1);
			CMLSpectrum hsrSpect = hsrC.getCalculatedSpectra();
			GaussianCmlTool defC = new GaussianCmlTool(file2);
			CMLSpectrum defSpect = defC.getCalculatedSpectra();

			CMLMolecule molecule = hsrC.getMolecule();

			CMLPeakList hsrPeakList = hsrSpect.getPeakListElements().get(0);
			CMLPeakList defPeakList = defSpect.getPeakListElements().get(0);	
			CMLElements<CMLPeak> hsrPeaks = hsrPeakList.getPeakElements();
			CMLElements<CMLPeak> defPeaks = defPeakList.getPeakElements();

			if (hsrPeaks.size() != defPeaks.size()) {
				throw new RuntimeException("Expected same number of peaks, found DEFAULT ("+defPeaks.size()+"), HSR ("+hsrPeaks.size()+")");
			}

			String solvent = hsrC.getCalculatedSolvent();
			List<CMLPeak> obsPeaks = hsrC.getObservedPeaks(solvent);
			double tmsShift = GaussianUtils.getTmsShift(solvent);
			for (int i = 0; i < hsrPeaks.size(); i++) {
				CMLPeak hsr = hsrPeaks.get(i);
				CMLPeak def = defPeaks.get(i);
				double h = tmsShift-hsr.getXValue();
				double d = tmsShift-def.getXValue();
				double hMinusD = h-d;

				if (!hsr.getAtomRefs()[0].equals(def.getAtomRefs()[0])) {
					throw new RuntimeException("Atom Ids for HSR ("+hsr.getAtomRefs()[0]+") and DEF ("+def.getAtomRefs()[0]+") peaks are not the same.");
				}

				String atomId = hsr.getAtomRefs()[0];
				double obs = GaussianUtils.getPeakValue(obsPeaks, atomId);
				if (Double.isNaN(obs)) {
					throw new RuntimeException("Could not find peak value for "+atomId);
				}
				Point p = new Point();
				p.setX(obs);
				p.setY(hMinusD);
				int count = GaussianUtils.getAtomPosition(molecule, atomId);
				if (startFile == null) {
					p.setLink("javascript:changeAtom('"+protocolUrl+"/"+CML_DIR_NAME+"/"+file1.getName()+"', "+count+");");
				} else {
					p.setLink("javascript:changeAtom('', "+count+");");
				}
				
				pointList.add(p);
				
				if (obs > maxx) {
					maxx = obs;
				}
				if (obs < minx) {
					minx = obs;
				}
				if (hMinusD > maxy) {
					maxy = hMinusD;
				}
				if (hMinusD < miny) {
					miny = hMinusD;
				}
			}
		}
		GaussianScatter gs = new GaussianScatter(pointList);
		gs.setXmin((int)minx-10);
		gs.setYmin((int)miny-5);
		gs.setXmax((int)maxx+10);
		gs.setYmax((int)maxy+5);
		gs.setXTickMarks(12);
		gs.setYTickMarks(12);
		gs.setYLab("Change in chemical shift (ppm)");
		Document doc = gs.getPlot();	
		return doc;
	}
	
	public String getHtmlContent() {
		String startStruct = "";
		String button = "";
		if (startFile != null) {
			startStruct = "load "+protocolUrl+"/"+CML_DIR_NAME+"/"+startFile;
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

	public void run() {
		Document doc = getPlot();
		String outFolderPath = protocolUrl.substring(8)+File.separator+outFolderName;
		String svgPath = outFolderPath+"/index.svg";
		IOUtils.writePrettyXML(doc, svgPath);
		String htmlContent = getHtmlContent();
		String htmlPath = outFolderPath+"/index.html";
		IOUtils.writeText(htmlContent, htmlPath);
	}

	public static void main(String[] args) {
		String defaultPath = "e:/gaussian/html/second-protocol/cml/";
		String hsrPath = "e:/gaussian/html/second-protocol_mod1/cml/";
		String outFolderName = "all";
		
		List<File> fileList1 = Arrays.asList(new File(hsrPath).listFiles());
		List<File> fileList2 = new ArrayList<File>();
		for (File file : new File(defaultPath).listFiles()) {
			String name1 = file.getName();
			for (File f : fileList1) {
				String name2 = f.getName();
				if (name1.equals(name2)) {
					fileList2.add(file);
					break;
				}
			}
		}
		
		String htmlTitle = "Experimentally observed shift VS. difference in Gaussian methods (HSR1 - DEFAULT)";
		CreateDifferencePlot c = new CreateDifferencePlot(fileList1, fileList2, outFolderName, htmlTitle, FIRST_DIFF_URL, FIRST_DIFF_JMOL_JS, FIRST_DIFF_SUMMARY_JS);
		c.run();
		
		String urlPrefix = "http://nmrshiftdb.ice.mpg.de/portal/js_pane/P-Results;jsessionid=FA2A776224CDA757D4B710F5FC12A899.tomcat2?nmrshiftdbaction=showDetailsFromHome&molNumber=";
		for (int i = 0; i < fileList1.size(); i++) {
			List<File> list1 = new ArrayList<File>();
			list1.add(fileList1.get(i));
			List<File> list2 = new ArrayList<File>();
			list2.add(fileList2.get(i));
			File file = fileList1.get(i);
			GaussianCmlTool c13 = new GaussianCmlTool(file);
			String solvent = c13.getCalculatedSolvent().toLowerCase();
			String name = file.getName();
			name = name.substring(0,name.indexOf("."));
			
			String number = name.substring(10);
			number = number.substring(0,number.indexOf("-"));
			
			String htmlTitle2 = "<a href='"+urlPrefix+number+"'>"+name+" (solvent: "+solvent+")</a>";
			outFolderName = name;
			CreateDifferencePlot c2 = new CreateDifferencePlot(list1, list2, outFolderName, htmlTitle2, FIRST_DIFF_URL, FIRST_DIFF_JMOL_JS, FIRST_DIFF_SUMMARY_JS);
			c2.run();
		}
	}
}
