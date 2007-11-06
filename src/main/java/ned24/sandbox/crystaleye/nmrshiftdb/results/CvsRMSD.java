package ned24.sandbox.crystaleye.nmrshiftdb.results;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ned24.sandbox.crystaleye.nmrshiftdb.FileListTool;
import ned24.sandbox.crystaleye.nmrshiftdb.GaussianCmlTool;
import ned24.sandbox.crystaleye.nmrshiftdb.GaussianConstants;
import ned24.sandbox.crystaleye.nmrshiftdb.GaussianScatter;
import ned24.sandbox.crystaleye.nmrshiftdb.GaussianUtils;
import nu.xom.Document;

import org.graph.Point;
import org.xmlcml.cml.element.CMLMolecule;
import org.xmlcml.cml.element.CMLPeak;

import uk.ac.cam.ch.crystaleye.IOUtils;
import uk.ac.cam.ch.crystaleye.Utils;

public class CvsRMSD implements GaussianConstants {

	public static void main(String[] args) {
		//String protocolName = HSR0_HALOGEN_NAME;
		//String protocolName = HSR1_HALOGEN_NAME;
		//String protocolName = HSR0_HALOGEN_AND_MORGAN_NAME;
		String protocolName = HSR1_HALOGEN_AND_MORGAN_NAME;
		
		String cmlDir = CML_DIR+protocolName;
		String folderName = "RMSD-vs-C";
		
		FileListTool ft = new FileListTool(cmlDir);
		//ft.setIncludeNotRemoved(false);folderName+="_nr";
		ft.setIncludeHumanEdited(true);folderName+="_he";
		ft.setIncludeMisassigned(true);folderName+="_m";
		ft.setIncludePoorStructures(true);folderName+="_ps";
		ft.setIncludePossMisassigned(true);folderName+="_pm";
		ft.setIncludeTautomers(true);folderName+="_ta";
		ft.setIncludeTooLargeRing(true);folderName+="_lr";
		List<File> fileList = ft.getFileList();
		
		double min = Double.POSITIVE_INFINITY;
		double max = Double.NEGATIVE_INFINITY;
		List<Point> pointList = new ArrayList<Point>();
		for (File file : fileList) {
			GaussianCmlTool g = new GaussianCmlTool(file);
			CMLMolecule molecule = g.getMolecule();
			String solvent = g.getCalculatedSolvent();
			int s = GaussianUtils.getSpectNum(file);
			List<CMLPeak> obsPeaks = g.getObservedPeaks(s);
			List<CMLPeak> calcPeaks = g.getListOfCalculatedPeaks();

			double c = PlotUtils.getC(calcPeaks, obsPeaks, solvent);
			double rmsd = PlotUtils.getRMSDAboutC(c, calcPeaks, obsPeaks, solvent);
			
			//System.out.println(rmsd);
			Point p = new Point();
			
			p.setX(rmsd);
			p.setY(c);
			p.setColour(GaussianUtils.getSolventPointColour(solvent));
			int count = GaussianUtils.getAtomPosition(molecule, "99999");
			p.setLink("javascript:changeAtom('../../../cml/"+protocolName+"/"+file.getName()+"', "+count+");" +
					"changeCoordLabel("+Utils.round(rmsd, 1)+","+Utils.round(c, 1)+");");
			pointList.add(p);
					
			if (rmsd > max) {
				max = rmsd;
			}
			if (rmsd < min) {
				min = rmsd;
			}
		}

		GaussianScatter gs = new GaussianScatter(pointList);
		gs.setXmin(0);
		gs.setYmin(-12);
		gs.setXmax(12);
		gs.setYmax(12);
		gs.setXTickMarks(12);
		gs.setYTickMarks(12);
		gs.setXLab("RMSD");
		gs.setYLab("C");
		Document doc = gs.getPlot();
		
		String extraHtml = "<div style='text-align: left; padding-left: 20px;'>" +
		"<span>Key:</span><br />" +
		"<span style='border: 1px solid; background-color: red; margin-bottom: 2px;'>...</span><span> = Chloroform</span><br />" +
		"<span style='border: 1px solid; background-color: GoldenRod; margin-bottom: 2px;'>...</span><span> = DMSO</span><br />" +
		"<span style='border: 1px solid; background-color: Blue; margin-bottom: 2px;'>...</span><span> = Water</span><br />" +
		"<span style='border: 1px solid; background-color: AntiqueWhite; margin-bottom: 2px;'>...</span><span> = Methanol</span><br />" +
		"<span style='border: 1px solid; background-color: MediumOrchid; margin-bottom: 2px;'>...</span><span> = Carbon Tetrachloride</span><br />" +
		"<span style='border: 1px solid; background-color: DarkCyan; margin-bottom: 2px;'>...</span><span> = Benzene</span><br />" +
		"<span style='border: 1px solid; background-color: YellowGreen; margin-bottom: 2px;'>...</span><span> = Acetone</span><br />" +
		"</div>";
		
		String outFolderPath = HTML_DIR+File.separator+protocolName+File.separator+folderName;
		String svgPath = outFolderPath+"/index.svg";
		IOUtils.writePrettyXML(doc, svgPath);
		String htmlContent = PlotUtils.getHtmlContent("RMSD vs. C", protocolName, null, null, extraHtml);
		String htmlPath = outFolderPath+"/index.html";
		IOUtils.writeText(htmlContent, htmlPath);
	}
}
