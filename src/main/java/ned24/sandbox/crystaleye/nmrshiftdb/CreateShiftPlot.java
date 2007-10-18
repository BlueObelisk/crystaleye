package ned24.sandbox.crystaleye.nmrshiftdb;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import nu.xom.Document;

import org.graph.Point;
import org.xmlcml.cml.base.CMLElements;
import org.xmlcml.cml.element.CMLAtom;
import org.xmlcml.cml.element.CMLMolecule;
import org.xmlcml.cml.element.CMLPeak;

import uk.ac.cam.ch.crystaleye.IOUtils;

public class CreateShiftPlot implements GaussianConstants {

	List<File> fileList;
	String outFolderName;
	String htmlTitle;
	
	String startFile = null;

	public CreateShiftPlot(List<File> fileList, String outFolderName, String htmlTitle) {
		this.fileList = fileList;
		if (fileList.size() == 1) {
			startFile = fileList.get(0).getName();
		}
		this.outFolderName = outFolderName;
		this.htmlTitle = htmlTitle;
	}

	public Document getPlot() {
		List<Point> pointList = new ArrayList<Point>();
		double min = Double.POSITIVE_INFINITY;
		double max = Double.NEGATIVE_INFINITY;

		for (File file : fileList) {
			System.out.println(file.getAbsolutePath());
			C13SpectraTool c = new C13SpectraTool(file);
			CMLMolecule molecule = c.getMolecule();
			String solvent = c.getCalculatedSolvent();
			boolean b = c.testSpectraConcordant(solvent);
			if (!b) {
				continue;
			}
			double tmsShift = GaussianUtils.getTmsShift(solvent);

			CMLElements<CMLPeak> calcPeaks = c.getCalculatedPeaks();
			List<CMLPeak> obsPeaks = c.getObservedPeaks(solvent);

			for (int i = 0; i < calcPeaks.size(); i++) {
				CMLPeak calcPeak = (CMLPeak)calcPeaks.get(i);
				double calcShift = calcPeak.getXValue();
				calcShift = tmsShift-calcShift;
				String calcId = calcPeak.getAtomRefs()[0];
				for (int j = 0; j < obsPeaks.size(); j++) {
					CMLPeak obsPeak = (CMLPeak)obsPeaks.get(j);
					String[] ids = obsPeak.getAtomRefs();
					for (String id : ids) {
						if (id.equals(calcId)) {
							if (!isAtomSuitable(molecule, id)) {
								continue;
							}
							double obsShift = obsPeak.getXValue();
							Point p = new Point();
							p.setX(obsShift);
							p.setY(calcShift);									
							int count = getAtomPosition(molecule, id);
							p.setLink("javascript:changeAtom('"+JMOL_APPLET_FOLDER+CML_DIR_NAME+"/"+file.getName()+"', "+count+");");
							pointList.add(p);
							if (calcShift > max) {
								max = calcShift;
							}
							if (calcShift < min) {
								min = calcShift;
							}
						}
					}
				}			
			}
		}

		GaussianScatter gs = new GaussianScatter(pointList);
		gs.setXmin(0);
		gs.setYmin(0);
		gs.setXmax(240);
		gs.setYmax(240);
		gs.setXTickMarks(12);
		gs.setYTickMarks(12);
		Document doc = gs.getPlot();	
		return doc;
	}

	private boolean isAtomSuitable(CMLMolecule molecule, String id) {
		/*
		CMLAtom atom = molecule.getAtomById(id);
		for (CMLAtom ligand : atom.getLigandAtoms()) {
			if ("O".equals(ligand.getElementType()) && ligand.getLigandAtoms().size() == 1) {
				return true;
			}
		}
		return false;
		 */
		return true;
	}

	private int getAtomPosition(CMLMolecule molecule, String atomId) {
		int count = 1;
		for (CMLAtom atom : molecule.getAtoms()) {
			if (atomId.equals(atom.getId())) {
				break;
			}
			count++;
		}
		return count;
	}

	public String getHtmlContent() {
		String startStruct = "";
		if (startFile != null) {
			startStruct = "load "+JMOL_APPLET_FOLDER+CML_DIR_NAME+"/"+startFile;
		}
		
		return "<html><head>"+
		"<script src=\""+JMOL_JS_PATH+"\" type=\"text/ecmascript\">"+
		"</script>"+
		"<script src=\""+SUMMARY_JS_PATH+"\" type=\"text/ecmascript\">"+
		"</script>"+
		"</head>"+
		"<body>"+
		"<div style=\"position: absolute; text-align: center; width: 100%; z-index: 100;\"><h2>"+htmlTitle+"</h2></div>"+
		"<div style=\"position: absolute; top: -50px;\">"+
		"<embed src=\"./index.svg\" width=\"715\" height=\"675\" style=\"position:absolute;\" />"+
		"<div style=\"position: absolute; left: 675px; top: 200px;\">"+
		"<script type=\"text/javascript\">jmolInitialize(\""+JMOL_APPLET_FOLDER+"\");"+
		"</script>		<script type=\"text/javascript\">jmolApplet(300, \""+startStruct+"\");</script>"+
		"</div>"+
		"</div>"+
		"</body>"+
		"</html>";
	}

	public void run() {
		Document doc = getPlot();
		String outFolderPath = JMOL_ROOT_DIR+outFolderName;
		String svgPath = outFolderPath+"/index.svg";
		IOUtils.writePrettyXML(doc, svgPath);
		String htmlContent = getHtmlContent();
		String htmlPath = outFolderPath+"/index.html";
		IOUtils.writeText(htmlContent, htmlPath);
	}

	public static void main(String[] args) {
		String path = JMOL_ROOT_DIR+CML_DIR_NAME;
		String outFolderName = "first";
		
		String urlPrefix = "http://nmrshiftdb.ice.mpg.de/portal/js_pane/P-Results;jsessionid=FA2A776224CDA757D4B710F5FC12A899.tomcat2?nmrshiftdbaction=showDetailsFromHome&molNumber=";
		//List<File> fileList = Arrays.asList(new File(path).listFiles());
		//String htmlTitle = "Selection of structures from NMRShiftDB with MW < 300";
		
		for (File file : new File(path).listFiles()) {
			List<File> fileList = new ArrayList<File>();
			fileList.add(file);
			C13SpectraTool c13 = new C13SpectraTool(file);
			String solvent = c13.getCalculatedSolvent().toLowerCase();
			String name = file.getName();
			name = name.substring(0,name.indexOf("-"));
			String number = name.substring(10);
			String htmlTitle = "<a href='"+urlPrefix+number+"'>"+name+" (solvent: "+solvent+")</a>";
			outFolderName = name;
			CreateShiftPlot c = new CreateShiftPlot(fileList, outFolderName, htmlTitle);
			c.run();
		}
	}
}
