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
import org.xmlcml.euclid.Point3;

import uk.ac.cam.ch.crystaleye.IOUtils;

public class CreateInputs implements GaussianConstants {

	static String outFolder = "e:/gaussian/second-protocol";
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

	private static void writeCondorSubmitFile(String folderName, String name, int numberFileCount) {
		String submitFile = "universe=vanilla\n"+
		"getenv=True\n"+
		"requirements = Arch == \"X86_64\" && OpSys == \"LINUX\" && Machine != \"gridlock20--ch.grid.private.cam.ac.uk\" && Machine != \"gridlock26--ch.grid.private.cam.ac.uk\" && Machine != \"gridlock27--ch.grid.private.cam.ac.uk\" && HAS_GAUSSIAN == TRUE\n"+
		"executable = /home/ned24/gaussian/"+folderName+"/"+name+".sh\n"+
		"input = /home/ned24/gaussian/"+folderName+"/"+name+FLOW_MIME+"\n"+
		"output = "+name+".out\n"+
		"error = "+name+".err\n"+
		"log = "+name+".log\n"+
		"\n"+
		"should_transfer_files=YES\n"+
		"transfer_executable=True\n"+
		"when_to_transfer_output=ON_EXIT_OR_EVICT\n"+
		"\n"+
		"Queue\n";

		IOUtils.writeText(submitFile, outFolder+File.separator+numberFileCount+File.separator+name+SUBMIT_FILE_MIME);
	}

	private static void writeShFile(String folderName, String name, int numberFileCount) {
		String content = "#!/bin/sh\n"+
		"\n"+
		"# Run g03 job\n"+
		"/usr/local/g03/g03 < "+name+FLOW_MIME+" > "+name+".out \n";
		IOUtils.writeText(content, outFolder+File.separator+numberFileCount+File.separator+name+".sh");
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

					StringBuilder sb = new StringBuilder();
					for (CMLAtom atom : molecule.getAtoms()) {
						sb.append(" "+atom.getElementType()+" ");
						Point3 p3 = atom.getXYZ3();
						double[] a = p3.getArray();
						sb.append(a[0]+" ");
						sb.append(a[1]+" ");
						sb.append(a[2]+"\n");
					}
					String name = getFileName(file);
					int solventNumber = i+1;
					name += "-"+solventNumber;
					
					/*
					boolean hasCDoubleBondC = false;
					boolean hasCarbonyl = false;
					for (CMLBond bond : molecule.getBonds()) {
						String order = bond.getOrder();
						if (order.equals(CMLBond.DOUBLE_D) ||
								order.equals(CMLBond.DOUBLE)) {
							int c = 0;
							int o = 0;
							for (CMLAtom atom : bond.getAtoms()) {
								if ("C".equals(atom.getElementType())) {
									c++;
								} else if ("O".equals(atom.getElementType())) {
									o++;
								}
							}
							if (c == 2) {
								hasCDoubleBondC = true;
							}
							if (c == 1 && o == 1) {
								hasCarbonyl = true;
							}
						}
					}
					
					if (!hasCDoubleBondC && !hasCarbonyl) {
						continue;
					}
					*/
					
					GaussianTemplate g = new GaussianTemplate(name, sb.toString(), inputFileSolvent);
					//g.setExtraBasis(true);
					//g.setHasCDoubleBondC(hasCDoubleBondC);
					//g.setHasCarbonyl(hasCarbonyl);
					String input = g.getInput();
					
					String folderName = outFile.getName()+"/"+numberFileCount;
					String outPath = outFolder+File.separator+numberFileCount+File.separator+name+FLOW_MIME;
					
					
					IOUtils.writeText(input, outPath);
					writeCondorSubmitFile(folderName, name, numberFileCount);
					writeShFile(folderName, name, numberFileCount);
					count++;
					if (count == 500) {
						numberFileCount++;
						count = 0;
					}
				}
			}
		}
	}
}
