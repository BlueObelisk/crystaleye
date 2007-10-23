package ned24.sandbox.crystaleye.nmrshiftdb;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nu.xom.Element;
import nu.xom.Nodes;

import org.xmlcml.cml.base.CMLElement.FormalChargeControl;
import org.xmlcml.cml.element.CMLAtom;
import org.xmlcml.cml.element.CMLBond;
import org.xmlcml.cml.element.CMLMolecule;
import org.xmlcml.cml.element.CMLSpectrum;
import org.xmlcml.cml.element.CMLMolecule.HydrogenControl;
import org.xmlcml.cml.tools.ConnectionTableTool;

import uk.ac.cam.ch.crystaleye.IOUtils;

public class CreateInputs implements GaussianConstants {

	static String outFolder = "e:/gaussian/inputs/second-protocol_freq";
	static String[] allowedElements = {"C", "N", "O", "P", "I", "F", "N", "S", "B", "Cl", "H", "Si", "Br"};
	static List<String> elList;

	static {
		elList = Arrays.asList(allowedElements);
	}

	private static boolean passesProtocol2(CMLMolecule molecule) {

		// MW < 300
		// no charges 
		// must have cyclic bonds
		// must contain C
		// no two acyclic atoms bonded (H, O, F, Cl, Br, I)

		if (molecule.getCalculatedMolecularMass(HydrogenControl.USE_EXPLICIT_HYDROGENS) > 300) {
			return false;
		}

		if (molecule.getCalculatedFormalCharge(FormalChargeControl.DEFAULT) != 0) {
			return false;
		}

		ConnectionTableTool ct = new ConnectionTableTool(molecule);
		List<CMLAtom> cyclicAtoms = ct.getCyclicAtoms();
		if (cyclicAtoms.size() == 0) {
			return false;
		}	

		boolean containsC = false;
		for (CMLAtom atom : molecule.getAtoms()) {
			if ("C".equals(atom.getElementType())) {
				containsC = true;
			}
			if (!elList.contains(atom.getElementType())) {
				return false;
			}
		}

		if (!containsC) {
			return false;
		}

		List<CMLAtom> acyclicAtoms = new ArrayList<CMLAtom>();
		for (CMLAtom atom : molecule.getAtoms()) {
			if (!"H".equals(atom.getElementType()) && !cyclicAtoms.contains(atom)) {
				acyclicAtoms.add(atom);
			}
		}
		List<CMLBond> acyclicBonds = ct.getAcyclicBonds();
		for (CMLAtom atom : acyclicAtoms) {
			int acyclicCount = 0;
			for (CMLBond bond : atom.getLigandBonds()) {
				if (acyclicBonds.contains(bond)) {
					for (CMLAtom a : bond.getAtoms()) {
						if (!a.getId().equals(atom.getId())) {
							if (!"H".equals(a.getElementType())) {
								boolean skip = false;
								if (("O".equals(a.getElementType()) && a.getLigandAtoms().size() == 1) ||
										"F".equals(a.getElementType()) ||
										"Cl".equals(a.getElementType()) ||
										"Br".equals(a.getElementType()) ||
										"I".equals(a.getElementType())) {
									skip = true;
								}
								if (!skip) {
									acyclicCount++;
								}
							}
						}
					}
				}
			}
			if (acyclicCount > 1) {
				return false;
			}
		}

		return true;
	}

	private static String getFileName(File file) {
		String name = file.getName();
		int idx = name.indexOf(".");
		return name.substring(0,idx);
	}

	public static void main(String[] args) {
		String path = "E:\\gaussian\\all-mols";

		File startFile = new File(path);
		File outFile = new File(outFolder);

		int count = 0;
		int numberFileCount = 1;

		for (File file : startFile.listFiles()) {	
			try {
				CMLMolecule molecule = (CMLMolecule)IOUtils.parseCmlFile(file).getRootElement();

				if (!passesProtocol2(molecule)) {
					continue;
				}

				Nodes specNodes = molecule.query("./cml:spectrum[cml:metadataList/cml:metadata[@content='13C']]", X_CML);
				if (specNodes.size() == 0) {
					continue;
				}
				for (int i = 0; i < specNodes.size(); i++) {
					CMLSpectrum spect = (CMLSpectrum)specNodes.get(i);
					Nodes solventNodes = spect.query("./cml:substanceList/cml:substance[@role='"+SOLVENT_ROLE+"']", X_CML);
					String cmlSolvent = "";
					if (solventNodes.size() > 0) {
						cmlSolvent = ((Element)solventNodes.get(0)).getAttributeValue("title").trim();
					}
					if (cmlSolvent.contains("+")) {
						// the solvent will contain more than one component, don't know how to 
						// deal with this so just skip this molecule
						continue;
					}
					String inputFileSolvent = "";
					if ("".equals(cmlSolvent)) {
						continue;
					} else {
						inputFileSolvent = GaussianUtils.nmrShiftDbSolvent2GaussianSolvent(cmlSolvent);

						if (inputFileSolvent == null) {
							System.err.println("Can't match solvent "+cmlSolvent+" in "+file.getAbsolutePath());
							continue;
						}

						String connTable = GaussianUtils.getConnectionTable(molecule);

						String name = getFileName(file);
						int solventNumber = i+1;
						name += "-"+solventNumber;

						/*
						boolean hasC = false;
						boolean hasO = false;
						for (CMLAtom atom : molecule.getAtoms()) {
							if ("C".equals(atom.getElementType())) {
								hasC = true;
							}
							if ("O".equals(atom.getElementType())) {
								hasO = true;
							}
						}
						if (!hasC && !hasO) {
							continue;
						}
						*/

						boolean freq = true;
						GaussianTemplate g = new GaussianTemplate(name, connTable, inputFileSolvent, freq);
						//g.setExtraBasis(true);
						//g.setHasC(hasC);
						//g.setHasO(hasO);
						String input = g.getThreeStepWorkflowInput();

						String folderName = outFile.getName()+"/"+numberFileCount;
						String outPath = outFolder+File.separator+numberFileCount+File.separator+name+FLOW_MIME;


						IOUtils.writeText(input, outPath);
						String outFol = outFolder+File.separator+numberFileCount;
						GaussianUtils.writeCondorSubmitFile(outFol, folderName, name, numberFileCount);
						GaussianUtils.writeShFile(outFol, name, numberFileCount);
						count++;
						if (count == 500) {
							numberFileCount++;
							count = 0;
						}
					}
				}
			} catch (Exception e) {
				System.err.println("Exception processing : "+file.getAbsolutePath());
			}
		}
	}
}
