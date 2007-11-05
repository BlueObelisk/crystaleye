package ned24.sandbox.crystaleye.nmrshiftdb.plottools;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ned24.sandbox.crystaleye.nmrshiftdb.GaussianCmlTool;
import ned24.sandbox.crystaleye.nmrshiftdb.GaussianConstants;
import ned24.sandbox.crystaleye.nmrshiftdb.GaussianScatter;
import ned24.sandbox.crystaleye.nmrshiftdb.GaussianUtils;
import ned24.sandbox.crystaleye.nmrshiftdb.results.PlotUtils;
import ned24.sandbox.crystaleye.nmrshiftdb.results.PlotUtils.PlotType;
import nu.xom.Document;

import org.graph.Point;
import org.xmlcml.cml.base.CMLElements;
import org.xmlcml.cml.element.CMLMolecule;
import org.xmlcml.cml.element.CMLPeak;

import uk.ac.cam.ch.crystaleye.IOUtils;
import uk.ac.cam.ch.crystaleye.Utils;

public class ShiftPlot implements GaussianConstants {

	List<File> fileList;
	String htmlTitle;

	String protocolName;
	String folderName;

	String startFile = null;
	
	String pointColour;
	
	protected ShiftPlot() {
		;
	}

	public ShiftPlot(List<File> fileList, String protocolName, String folderName, String htmlTitle) {
		this.fileList = fileList;
		if (fileList.size() == 1) {
			startFile = fileList.get(0).getName();
		}
		this.htmlTitle = htmlTitle;
		this.protocolName = protocolName;
		this.folderName = folderName;
	}
	
	public void setPointColour(String colour) {
		pointColour = colour;
	}

	public Document getPlot() {
		List<Point> pointList = new ArrayList<Point>();
		double min = Double.POSITIVE_INFINITY;
		double max = Double.NEGATIVE_INFINITY;

		StringBuilder sb = new StringBuilder();
		for (File file : fileList) {
			System.out.println(file.getAbsolutePath());
			GaussianCmlTool c = new GaussianCmlTool(file);
			CMLMolecule molecule = c.getMolecule();
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

				if (!isAtomSuitable(molecule, calcId)) {
					continue;
				}
				
				double obsShift = GaussianUtils.getPeakValue(obsPeaks, calcId);
				Point p = new Point();
				p.setX(obsShift);
				p.setY(calcShift);
				if (pointColour != null) {
					p.setColour(pointColour);
				}
				
				sb.append(obsShift+","+calcShift+"\n");
				
				int count = GaussianUtils.getAtomPosition(molecule, calcId);
				if (startFile == null) {
					p.setLink("javascript:changeAtom('../../../cml/"+protocolName+"/"+file.getName()+"', "+count+");" +
							"changeCoordLabel("+Utils.round(obsShift, 1)+","+Utils.round(calcShift, 1)+");");
				} else {
					p.setLink("javascript:changeAtom('', "+count+");changeCoordLabel("+Utils.round(obsShift, 1)+","+Utils.round(calcShift, 1)+");");
				}
				pointList.add(p);
				if (calcShift > max) {
					max = calcShift;
				}
				if (calcShift < min) {
					min = calcShift;
				}

			}
		}
		
		IOUtils.writeText(sb.toString(), "e:/gaussian/hsr1.csv");

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

	protected boolean isAtomSuitable(CMLMolecule molecule, String atomId) {
		return true;
	}

	public void run() {
		Document doc = getPlot();
		String outFolderPath = HTML_DIR+File.separator+protocolName+File.separator+folderName;
		String svgPath = outFolderPath+"/index.svg";
		IOUtils.writePrettyXML(doc, svgPath);
		String htmlContent = PlotUtils.getHtmlContent(htmlTitle, protocolName, startFile, PlotType.OBS_VS_CALC, "");
		String htmlPath = outFolderPath+"/index.html";
		IOUtils.writeText(htmlContent, htmlPath);
	}

	public static void main(String[] args) {
		//
	}
}
