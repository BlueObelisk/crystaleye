package ned24.sandbox.crystaleye.nmrshiftdb.results;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ned24.sandbox.crystaleye.nmrshiftdb.GaussianCmlTool;
import ned24.sandbox.crystaleye.nmrshiftdb.GaussianConstants;
import ned24.sandbox.crystaleye.nmrshiftdb.GaussianScatter;
import ned24.sandbox.crystaleye.nmrshiftdb.GaussianUtils;
import nu.xom.Document;

import org.graph.Point;
import org.xmlcml.cml.base.CMLElement.Hybridization;
import org.xmlcml.cml.element.CMLAtom;
import org.xmlcml.cml.element.CMLAtomSet;
import org.xmlcml.cml.element.CMLMolecule;
import org.xmlcml.cml.element.CMLPeak;
import org.xmlcml.cml.tools.ConnectionTableTool;
import org.xmlcml.cml.tools.MoleculeTool;
import org.xmlcml.cml.tools.RingNucleus;
import org.xmlcml.cml.tools.RingNucleusSet;

import uk.ac.cam.ch.crystaleye.IOUtils;
import uk.ac.cam.ch.crystaleye.Utils;

public class SubstitutedBenzenes implements GaussianConstants {

	public static void main(String[] args) {
		String protocolName = HSR0_HALOGEN_AND_MORGAN_NAME;
		String path = CML_DIR+protocolName;

		String htmlTitle = "Carbons of benzene rings";
		String folderName = "substituted-benzenes";

		List<Point> pointList = new ArrayList<Point>();
		for (File file : new File(path).listFiles()) {
			GaussianCmlTool g = new GaussianCmlTool(file);
			CMLMolecule molecule = g.getMolecule();
			int s = GaussianUtils.getSpectNum(file);
			List<CMLPeak> obsPeaks = g.getObservedPeaks(s);
			List<CMLPeak> calcPeaks = g.getListOfCalculatedPeaks();
			String solvent = g.getCalculatedSolvent();
			double tmsShift = GaussianUtils.getTmsShift(solvent);

			ConnectionTableTool ct = new ConnectionTableTool(molecule);
			RingNucleusSet rns = ct.getRingNucleusSet();
			MoleculeTool mt = new MoleculeTool(molecule);
			for (RingNucleus rn : rns.getRingNucleusList()) {
				CMLAtomSet as = rn.getAtomSet();

				List<String> ringIds = new ArrayList<String>();
				for (CMLAtom atom : as.getAtoms()) {
					ringIds.add(atom.getId());
				}

				Set<String> addIds = new HashSet<String>(); 
				if (as.getAtoms().size() == 6) {
					boolean allSp2 = true;
					for (CMLAtom atom : as.getAtoms()) {
						Hybridization h = mt.getGeometricHybridization(atom);
						if (!h.toString().equals("SP2") || !"C".equals(atom.getElementType())) {
							allSp2 = false;
							break;
						}
					}
					if (!allSp2) {
						continue;
					}
					for (CMLAtom atom : as.getAtoms()) {
						for (CMLAtom ligand : atom.getLigandAtoms()) {
							if (ringIds.contains(ligand.getId())) {
								continue;
							}
							if ("N".equals(ligand.getElementType())) {
								addIds.add(atom.getId());
							}
						}
					}
				}

				for (String atomId : addIds) {
					double obsShift = GaussianUtils.getPeakValue(obsPeaks, atomId);
					double calcShift = GaussianUtils.getPeakValue(calcPeaks, atomId);
					calcShift = tmsShift-calcShift;
					Point p = new Point();
					p.setX(obsShift);
					p.setY(calcShift);									
					int count = GaussianUtils.getAtomPosition(molecule, atomId);
					p.setLink("javascript:changeAtom('../../../cml/"+protocolName+"/"+file.getName()+"', "+count+");" +
							"changeCoordLabel("+Utils.round(obsShift, 1)+","+Utils.round(calcShift, 1)+");");

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
