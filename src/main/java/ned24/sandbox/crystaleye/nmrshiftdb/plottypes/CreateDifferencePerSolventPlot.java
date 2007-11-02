package ned24.sandbox.crystaleye.nmrshiftdb.plottypes;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ned24.sandbox.crystaleye.nmrshiftdb.GaussianCmlTool;
import ned24.sandbox.crystaleye.nmrshiftdb.GaussianConstants;
import ned24.sandbox.crystaleye.nmrshiftdb.GaussianScatter;
import ned24.sandbox.crystaleye.nmrshiftdb.GaussianUtils;
import ned24.sandbox.crystaleye.nmrshiftdb.GaussianUtils.Solvent;
import ned24.sandbox.crystaleye.nmrshiftdb.results.PlotUtils;
import nu.xom.Document;

import org.graph.Point;
import org.xmlcml.cml.base.CMLElements;
import org.xmlcml.cml.element.CMLMolecule;
import org.xmlcml.cml.element.CMLPeak;

import uk.ac.cam.ch.crystaleye.IOUtils;

public class CreateDifferencePerSolventPlot implements GaussianConstants {

	List<File> fileList;
	String htmlTitle;

	String protocolName;
	String folderName;

	String startFile = null;

	public CreateDifferencePerSolventPlot(List<File> fileList, String protocolName, String folderName, String htmlTitle) {
		this.fileList = fileList;
		if (fileList.size() == 1) {
			startFile = fileList.get(0).getName();
		}
		this.htmlTitle = htmlTitle;
		this.protocolName = protocolName;
		this.folderName = folderName;
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

			String colour = getSolventColour(solvent);
			
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
				p.setY(calcShift-obsShift);
				
				sb.append(obsShift+","+calcShift+"\n");
				
				int count = GaussianUtils.getAtomPosition(molecule, calcId);
				if (startFile == null) {
					p.setLink("javascript:changeAtom('../../../cml/"+protocolName+"/"+file.getName()+"', "+count+");");
				} else {
					p.setLink("javascript:changeAtom('', "+count+");");
				}
				p.setColour(colour);
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
		gs.setYmin(-20);
		gs.setXmax(240);
		gs.setYmax(20);
		gs.setXTickMarks(12);
		gs.setYTickMarks(12);
		gs.setXLab("observed shift");
		gs.setYLab("difference (calc- obs)");
		Document doc = gs.getPlot();	
		return doc;
	}
	
	private String getSolventColour(String solvent) {	
		List<String> solvents = new ArrayList<String>();
		for (Solvent s : GaussianUtils.Solvent.values()) {
			solvents.add(GaussianUtils.getSolventString(s));
		}
		int i = 0;
		solvent = GaussianUtils.nmrShiftDbSolvent2GaussianSolvent(solvent);
		for (String s : solvents) {
			if (s.equals(solvent)) {
				System.out.println(i+" "+colours[i]);
				return colours[i];
			}
			i++;
		}
		throw new RuntimeException("Could not find solvent: "+solvent);
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

	public void run() {
		Document doc = getPlot();
		String outFolderPath = HTML_DIR+File.separator+protocolName+File.separator+folderName;
		String svgPath = outFolderPath+"/index.svg";
		IOUtils.writePrettyXML(doc, svgPath);
		String htmlContent = PlotUtils.getHtmlContent(htmlTitle, protocolName, startFile, false);
		String htmlPath = outFolderPath+"/index.html";
		IOUtils.writeText(htmlContent, htmlPath);
	}

	public static void main(String[] args) {
		//String protocolName = HSR0_NAME;
		//String protocolName = HSR1_NAME;	
		String protocolName = HSR0_MANUAL_AND_MORGAN_NAME;
		//String protocolName = HSR1_MANUAL_AND_MORGAN_NAME;
		
		System.out.println(protocolName);
		String cmlFolder = CML_DIR+protocolName;			
		List<File> fileList = new ArrayList<File>();
		for (File file : new File(cmlFolder).listFiles()) {
			if (file.getAbsolutePath().endsWith(".cml.xml")) {
				fileList.add(file);
			}
		}
		String htmlTitle = "Selection of structures from NMRShiftDB with MW < 300";
		
		String folderName = "differencebysolvent";
		CreateDifferencePerSolventPlot c = new CreateDifferencePerSolventPlot(fileList, protocolName, folderName, htmlTitle);
		c.run();
	}
}