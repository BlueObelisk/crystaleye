package ned24.sandbox.crystaleye.nmrshiftdb;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nu.xom.Document;

import org.xmlcml.cml.element.CMLAtom;
import org.xmlcml.cml.element.CMLCml;
import org.xmlcml.cml.element.CMLMetadata;
import org.xmlcml.cml.element.CMLMetadataList;
import org.xmlcml.cml.element.CMLMolecule;
import org.xmlcml.cml.element.CMLPeak;
import org.xmlcml.cml.element.CMLPeakList;
import org.xmlcml.cml.element.CMLSpectrum;
import org.xmlcml.cml.element.CMLSubstance;
import org.xmlcml.cml.element.CMLSubstanceList;
import org.xmlcml.euclid.Point3;

import uk.ac.cam.ch.crystaleye.IOUtils;

public class MergeCalculation2Cml implements GaussianConstants {

	File gaussianFile;

	File cmlFile;

	CMLCml gaussianCml;
	File tmpFolder;

	public MergeCalculation2Cml(File gaussianFile, File cmlFile, File tmpFolder) {
		this.gaussianFile = gaussianFile;
		this.cmlFile = cmlFile;
		this.tmpFolder = tmpFolder;
	}

	public Document merge() {
		Pattern e = Pattern.compile("\\s+Error termination.*");
		
		Pattern p = Pattern
				.compile("\\s*\\d+\\s+\\w+\\s+Isotropic\\s+=\\s+([^\\s]*)\\s+Anisotropy\\s+=\\s+[^\\s]*\\s*");
		Pattern p2 = Pattern
				.compile("\\s*Solvent\\s+:\\s+(\\w+),\\s+\\w+\\s+=\\s+\\w+\\s*");

		String solvent = null;
		List<String> values = new ArrayList<String>();
		BufferedReader input = null;
		try {
			input = new BufferedReader(new FileReader(gaussianFile));
			String line = null;
			while ((line = input.readLine()) != null) {
				if (line != null && !"".equals(line)) {
					Matcher me = e.matcher(line);
					if (me.find()) {
						return null;
					}
					Matcher m = p.matcher(line);
					if (m.find()) {
						values.add(m.group(1));
					}
					Matcher m2 = p2.matcher(line);
					if (m2.find()) {
						solvent = m2.group(1);
					}
				}
			}
			input.close();
		} catch (FileNotFoundException ex) {
			throw new RuntimeException("Could not find file: " + gaussianFile);
		} catch (IOException ex) {
			throw new RuntimeException("Error reading file: " + gaussianFile);
		} finally {
			try {
				if (input != null) {
					input.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

		if (solvent == null) {
			System.err.println("Could not find solvent.");
			return null;
		}

		CMLMolecule molecule = (CMLMolecule) IOUtils.parseCmlFile(cmlFile)
				.getRootElement();
		List<CMLAtom> atoms = molecule.getAtoms();

		if (atoms.size() != values.size()) {
			throw new RuntimeException("Number of CMLAtoms " + atoms.size()
					+ " not the same as number of shifts" + values.size() + ".");
		}

		CMLCml finalCml = GaussianUtils.getFinalCml(gaussianFile, tmpFolder);
		if (finalCml == null) {
			return null;
		}
		CMLMolecule finalMol = (CMLMolecule) finalCml
				.getFirstCMLChild(CMLMolecule.TAG);

		if (molecule.getAtomCount() != finalMol.getAtomCount()) {
			throw new RuntimeException("Input molecule atom count ("
					+ molecule.getAtomCount()
					+ ") is different to output molecule ("
					+ finalMol.getAtomCount() + ").");
		}

		copyAtomIds(molecule, finalMol);
		copyCartesians(finalMol, molecule);
		addSpectrumToMolecule(molecule, values, solvent);
		addSpectrumToMolecule(finalMol, values, solvent);
		gaussianCml = finalCml;

		return molecule.getDocument();
	}
	
	private void copyAtomIds(CMLMolecule from, CMLMolecule to) {
		int count = 0;
		for (CMLAtom atom : from.getAtoms()) {
			String id = atom.getId();
			CMLAtom other = to.getAtom(count);
			other.resetId(id);
			count++;
		}
	}

	private void copyCartesians(CMLMolecule from, CMLMolecule to) {
		for (CMLAtom atom : from.getAtoms()) {
			String id = atom.getId();
			Point3 p = atom.getXYZ3();
			CMLAtom other = to.getAtomById(id);
			other.setXYZ3(p);
		}
	}

	public CMLCml getGaussianCml() {
		return gaussianCml;
	}

	public void addSpectrumToMolecule(CMLMolecule molecule,
			List<String> values, String solvent) {
		CMLSpectrum spectrum = new CMLSpectrum();
		molecule.appendChild(spectrum);
		spectrum.setType(NMR_TYPE);

		String name = gaussianFile.getName();
		int idx = name.indexOf(".");
		name = name.substring(0, idx);
		spectrum.setMoleculeRef(name);

		CMLMetadataList metList = new CMLMetadataList();
		spectrum.appendChild(metList);
		CMLMetadata met = new CMLMetadata();
		metList.appendChild(met);
		met.setName(GAUSSIAN_NMR);
		met.setContent("13C");

		CMLSubstanceList subList = new CMLSubstanceList();
		spectrum.appendChild(subList);
		CMLSubstance sub = new CMLSubstance();
		subList.appendChild(sub);
		sub.setDictRef(FIELD_DICTREF);
		sub.setRole(SOLVENT_ROLE);
		sub.setTitle(solvent);

		CMLPeakList peakList = new CMLPeakList();
		spectrum.appendChild(peakList);
		List<CMLAtom> atoms = molecule.getAtoms();
		int i = 0;
		for (String value : values) {
			CMLAtom atom = atoms.get(i);
			i++;
			if (!"C".equals(atom.getElementType())) {
				continue;
			}
			CMLPeak peak = new CMLPeak();
			peakList.appendChild(peak);
			peak.setXUnits(UNITS_PPM);
			peak.setXValue(value);
			peak.setAtomRefs(atom.getId());
			peak.setId("p" + i);
		}
	}

	public static void main(String[] args) {
		String cmlFolderPath = "e:/gaussian/all-mols";
		String calcOutputPath = "e:/gaussian/outputs/second-protocol/1";

		String outFolder = "e:/gaussian/merged/second-protocol";

		for (File file : new File(calcOutputPath).listFiles()) {
			String path = file.getAbsolutePath();
			if (!path.endsWith(".out")) {
				continue;
			}
			System.out.println(path);
			String name = file.getName();
			int idx = name.indexOf("-");
			name = name.substring(0, idx);
			File cmlFile = null;
			for (File f : new File(cmlFolderPath).listFiles()) {
				if (f.getAbsolutePath().endsWith(name + ".cml.xml")) {
					cmlFile = f;
					break;
				}
			}
			if (cmlFile == null) {
				throw new RuntimeException("Can't find CML file.");
			}
			MergeCalculation2Cml m = new MergeCalculation2Cml(file, cmlFile, new File("e:/temp"));
			Document doc = m.merge();
			if (doc == null) {
				System.err.println("Problem reading doc");
				continue;
			}
			String outPath = outFolder + File.separator
					+ file.getName().replaceAll(".out", ".cml.xml");
			IOUtils.writePrettyXML(doc, outPath);

			CMLCml gauCml = m.getGaussianCml();
			String gauCmlPath = outFolder + File.separator
					+ file.getName().replaceAll(".out", ".gau.cml.xml");
			IOUtils.writePrettyXML(gauCml.getDocument(), gauCmlPath);
		}
	}
}
