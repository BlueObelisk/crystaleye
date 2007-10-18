package ned24.sandbox.crystaleye.nmrshiftdb;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import nu.xom.Nodes;

import org.xmlcml.cml.base.CMLElements;
import org.xmlcml.cml.element.CMLMolecule;
import org.xmlcml.cml.element.CMLPeak;
import org.xmlcml.cml.element.CMLPeakList;
import org.xmlcml.cml.element.CMLSpectrum;

import uk.ac.cam.ch.crystaleye.IOUtils;

public class C13SpectraTool implements GaussianConstants {

	CMLMolecule molecule;

	public C13SpectraTool(File file) {
		this.molecule = (CMLMolecule)IOUtils.parseCmlFile(file).getRootElement();
	}

	public String getCalculatedSolvent() {
		CMLSpectrum calculated = getCalculatedSpectra();
		Nodes solvent = calculated.query("./cml:substanceList/cml:substance/@title", X_CML);
		if (solvent.size() != 1) {
			throw new RuntimeException("Expected one substance title, but found "+solvent.size());
		}
		return solvent.get(0).getValue();
	}

	public List<CMLSpectrum> getObservedSpectra(String solvent) {
		solvent = GaussianUtils.nmrShiftDbSolvent2GaussianSolvent(solvent);
		List<CMLSpectrum> spectra = getObservedSpectra();
		List<CMLSpectrum> list = matchSpectraToSolvent(spectra, solvent);
		return list;
	}

	private List<CMLSpectrum> matchSpectraToSolvent(List<CMLSpectrum> spectra, String solvent) {
		List<CMLSpectrum> list = new ArrayList<CMLSpectrum>();
		for (int i = 0; i < spectra.size(); i++) {
			CMLSpectrum spectrum = (CMLSpectrum)spectra.get(i);
			if (spectrumUsedSolvent(spectrum, solvent)) {
				list.add(spectrum);
			}
		}
		return list;
	}

	private boolean spectrumUsedSolvent(CMLSpectrum spectrum, String solvent) {
		Nodes nodes = spectrum.query("./cml:substanceList/cml:substance/@title", X_CML);
		if (nodes.size() > 1) {
			throw new RuntimeException("Only expected one solvent to be found: "+molecule.getId());
		} else if (nodes.size() == 0) {
			return false;
		}
		String foundSolvent = nodes.get(0).getValue().trim();
		foundSolvent = GaussianUtils.nmrShiftDbSolvent2GaussianSolvent(foundSolvent);
		//System.out.println("solvent comparison: -"+solvent+"-/-"+foundSolvent+"-");
		if (foundSolvent == null) {
			return false;
		}
		if (foundSolvent.equals(solvent)) {
			return true;
		} else {
			return false;
		}
	}

	public List<CMLSpectrum> getObservedSpectra() {
		List<CMLSpectrum> list = new ArrayList<CMLSpectrum>();
		Nodes nodes =  molecule.query(".//cml:spectrum[.//cml:metadata[@name='"+OBSERVED_NMR+"' and @content='13C']]", X_CML);
		for (int i = 0; i < nodes.size(); i++) {
			CMLSpectrum s = (CMLSpectrum)nodes.get(i);
			list.add(s);
		}
		return list;
	}

	public CMLSpectrum getCalculatedSpectra() {
		Nodes nodes = molecule.query(".//cml:spectrum[.//cml:metadata[@name='"+GAUSSIAN_NMR+"' and @content='13C']]", X_CML);
		if (nodes.size() > 1) {
			throw new RuntimeException("Expected one calculated spectra per molecule, but found "+nodes.size());
		}
		return (CMLSpectrum)nodes.get(0);
	}

	public CMLMolecule getMolecule() {
		return molecule;
	}

	public List<CMLPeak> getObservedPeaks(String solvent) {
		List<CMLSpectrum> observedNodes = getObservedSpectra(solvent);
		List<CMLPeak> peaks = new ArrayList<CMLPeak>();
		for (int i = 0; i < observedNodes.size(); i++) {
			CMLSpectrum observed = observedNodes.get(i);
			CMLPeakList obsPL = (CMLPeakList)observed.getFirstCMLChild(CMLPeakList.TAG);
			CMLElements<CMLPeak> ps = obsPL.getPeakElements();
			for (int j = 0; j < ps.size(); j++) {
				peaks.add(ps.get(j));
			}
		}
		return peaks;
	}

	public int getObservedSpectraCount(String solvent) {
		return getObservedSpectra(solvent).size();
	}

	public CMLElements<CMLPeak> getCalculatedPeaks() {
		CMLSpectrum calculated = getCalculatedSpectra();
		CMLPeakList calcPL = (CMLPeakList)calculated.getFirstCMLChild(CMLPeakList.TAG);
		return calcPL.getPeakElements();
	}

	public boolean testSpectraConcordant(String solvent) {
		List<CMLSpectrum> obsSp = getObservedSpectra(solvent);
		List<Integer> intList = new ArrayList<Integer>();
		int obsAtoms = 0;
		for (CMLSpectrum sp : obsSp) {
			int count = C13SpectraTool.getSpectrumAtomCount(sp);
			intList.add(count);
			obsAtoms += count;
		}

		int start = intList.get(0);
		boolean fine = true;
		for (int i : intList) {
			if (start != i) {
				fine = false;
				break;
			}
		}
		if (!fine) {
			System.err.println("Differing number of atoms for peaks in observed spectra.");
			return false;
		}
		int expectedObsAtoms = getCalculatedPeaks().size()*obsSp.size();
		//System.out.println(expectedObsAtoms+" "+obsAtoms);
		if (expectedObsAtoms != obsAtoms) {
			System.err.println("Differing number of atoms for peaks in observed spectra to those in calculated spectra: "+molecule.getId());
			return false;
		}
		return true;
	}

	public static int getSpectrumAtomCount(CMLSpectrum spectrum) {
		int count = 0;
		CMLElements<CMLPeakList> peakLists = spectrum.getPeakListElements();
		for (int i = 0; i < peakLists.size(); i++) {
			CMLPeakList peakList = (CMLPeakList)peakLists.get(i);
			CMLElements<CMLPeak> peaks = peakList.getPeakElements();
			for (int j = 0; j < peaks.size(); j++) {
				CMLPeak peak = (CMLPeak)peaks.get(j);
				String[] as = peak.getAtomRefs();
				if (as != null) {
					count += as.length;
				}
			}
		}
		return count;
	}
}
