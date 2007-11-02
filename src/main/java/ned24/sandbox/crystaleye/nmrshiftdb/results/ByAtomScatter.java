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
import org.xmlcml.cml.element.CMLAtom;
import org.xmlcml.cml.element.CMLMolecule;
import org.xmlcml.cml.element.CMLPeak;

import uk.ac.cam.ch.crystaleye.IOUtils;

public class ByAtomScatter implements GaussianConstants {

	public static void main(String[] args) {
		String protocolName = HSR0_MANUAL_AND_MORGAN_NAME;
		String path = CML_DIR+protocolName;
		String[] atoms = {"S"};

		String htmlTitle = "Carbons bonded to Henry atoms";
		String folderName = "henry-atoms";

		List<Point> pointList = new ArrayList<Point>();
		for (File file : new File(path).listFiles()) {
			GaussianCmlTool g = new GaussianCmlTool(file);
			CMLMolecule molecule = g.getMolecule();
			int s = GaussianUtils.getSpectNum(file);
			List<CMLPeak> obsPeaks = g.getObservedPeaks(s);
			List<CMLPeak> calcPeaks = g.getListOfCalculatedPeaks();
			String solvent = g.getCalculatedSolvent();
			double tmsShift = GaussianUtils.getTmsShift(solvent);

			for (CMLAtom atom : molecule.getAtoms()) {
				String atomId = atom.getId();
				boolean add = false;
				for (CMLAtom ligand : atom.getLigandAtoms()) {
					String type = ligand.getElementType();
					for (String at : atoms) {
						if (type.equals(at)) {
							add = true;
							break;
						}
					}
				}
				if (add) {
					double obsShift = GaussianUtils.getPeakValue(obsPeaks, atomId);
					double calcShift = GaussianUtils.getPeakValue(calcPeaks, atomId);
					if (Double.isNaN(obsShift) || Double.isNaN(calcShift)) {
						continue;
					}
					Point p = new Point();
					p.setX(obsShift);
					p.setY(tmsShift-calcShift);									
					int count = GaussianUtils.getAtomPosition(molecule, atomId);
					p.setLink("javascript:changeAtom('../../../cml/"+protocolName+"/"+file.getName()+"', "+count+");");
					pointList.add(p);
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

		String outFolderPath = HTML_DIR+File.separator+protocolName+File.separator+folderName;
		String svgPath = outFolderPath+"/index.svg";
		IOUtils.writePrettyXML(doc, svgPath);
		String htmlContent = PlotUtils.getHtmlContent(htmlTitle, protocolName, null, false);
		String htmlPath = outFolderPath+"/index.html";
		IOUtils.writeText(htmlContent, htmlPath);
	}
}
