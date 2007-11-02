package ned24.sandbox.crystaleye.nmrshiftdb.results;

import java.io.File;
import java.util.List;

import ned24.sandbox.crystaleye.nmrshiftdb.GaussianCmlTool;
import ned24.sandbox.crystaleye.nmrshiftdb.GaussianConstants;
import ned24.sandbox.crystaleye.nmrshiftdb.GaussianUtils;
import nu.xom.Document;

import org.xmlcml.cml.element.CMLPeak;
import org.xmlcml.cml.element.CMLSpectrum;

import uk.ac.cam.ch.crystaleye.IOUtils;

public class TempVsC implements GaussianConstants {

	public static void main(String[] args) {
		String protocolName = HSR0_NAME;
		//String protocolName = HSR1_NAME
		
		String folder = CML_DIR+protocolName;
		
		for (File file : new File(folder).listFiles()) {
			GaussianCmlTool g = new GaussianCmlTool(file);
			String solvent = g.getCalculatedSolvent();
			
			List<CMLSpectrum> spectra = g.getObservedSpectra(solvent);
			if (spectra.size() > 1) {
				System.out.println(file.getAbsolutePath());
			}
			int s = GaussianUtils.getSpectNum(file);
			List<CMLPeak> obsPeaks = g.getObservedPeaks(s);
			List<CMLPeak> calcPeaks = g.getListOfCalculatedPeaks();

			double c = PlotUtils.getC(calcPeaks, obsPeaks, solvent);
			Document doc = IOUtils.parseXmlFile(file);
			
		}
	}
}
