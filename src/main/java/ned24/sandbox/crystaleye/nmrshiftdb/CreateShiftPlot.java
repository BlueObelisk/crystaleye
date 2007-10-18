package ned24.sandbox.crystaleye.nmrshiftdb;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
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

	public CreateShiftPlot(List<File> fileList, String outFolderName) {
		this.fileList = fileList;
		this.outFolderName = outFolderName;
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

			CMLElements<CMLPeak> calcPeaks = c.getCalculatedPeaks();
			List<CMLPeak> obsPeaks = c.getObservedPeaks(solvent);

			for (int i = 0; i < calcPeaks.size(); i++) {
				CMLPeak calcPeak = (CMLPeak)calcPeaks.get(i);
				double calcShift = calcPeak.getXValue();
				calcShift = TMS_SHIFT-calcShift;

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
		gs.setXmin((int)min-10);
		gs.setYmin((int)min-10);
		gs.setXmax((int)max+10);
		gs.setYmax((int)max+10);
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
		return  "<!DOCTYPE html"+
				"      PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\""+
				"      \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">"+
				"<html"+
				"<head>"+
				"<script src=\""+JMOL_JS_PATH+"\" type=\"text/ecmascript\">"+
				"</script>"+
				"<script src=\""+SUMMARY_JS_PATH+"\" type=\"text/ecmascript\">"+
				"</script>"+
				"</head>"+
				"<body>"+
				"<embed src=\"./test.svg\" width=\"675\" height=\"675\" style=\"display: inline;\" />"+
				"    <div style=\"display: inline\">"+
				"		<script type=\"text/javascript\">jmolInitialize(\""+JMOL_APPLET_FOLDER+"\");</script>"+
				"		<script type=\"text/javascript\">jmolApplet(360, \"\");</script>"+
				"    </div>"+
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
		List<File> fileList = Arrays.asList(new File(path).listFiles());
		/*
		List<File> fileList = new ArrayList<File>();
		for (File file : new File(path).listFiles()) {
			fileList.add(file);
			break;
		}
		*/
		CreateShiftPlot c = new CreateShiftPlot(fileList, outFolderName);
		c.run();
	}
}
