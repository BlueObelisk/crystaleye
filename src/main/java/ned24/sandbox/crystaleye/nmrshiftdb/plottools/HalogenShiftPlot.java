package ned24.sandbox.crystaleye.nmrshiftdb.plottools;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.xmlcml.cml.element.CMLAtom;
import org.xmlcml.cml.element.CMLMolecule;
import org.xmlcml.molutil.ChemicalElement.Type;

public class HalogenShiftPlot extends ShiftPlot {
	
	private HalogenShiftPlot() {
		;
	}
	
	public HalogenShiftPlot(List<File> fileList, String protocolName, String folderName, String htmlTitle) {
		super(fileList, protocolName, folderName, htmlTitle);
	}

	protected boolean isAtomSuitable(CMLMolecule molecule, String atomId) {
		boolean suitable = false;
		CMLAtom atom = molecule.getAtomById(atomId);
		for (CMLAtom ligand : atom.getLigandAtoms()) {
			if (ligand.getChemicalElement().isChemicalElementType(Type.HALOGEN)) {
				suitable = true;
				break;
			}
		}
		return suitable;
	}
	
	public static void main(String[] args) {
		//String protocolName = HSR0_NAME;
		//String protocolName = HSR1_NAME;
		//String protocolName = HSR0_HALOGEN_NAME;
		String protocolName = HSR1_HALOGEN_NAME;
		//String protocolName = HSR0_HALOGEN_AND_MORGAN_NAME;
		//String protocolName = HSR1_MANUAL_AND_MORGAN_NAME;
		
		System.out.println(protocolName);
		String cmlFolder = CML_DIR+protocolName;			
		List<File> fileList = new ArrayList<File>();
		for (File file : new File(cmlFolder).listFiles()) {
			if (file.getAbsolutePath().endsWith(".cml.xml")) {
				fileList.add(file);
			}
		}
		String htmlTitle = "Carbons bonded to Halogens";
		
		String folderName = "halogens";
		HalogenShiftPlot c = new HalogenShiftPlot(fileList, protocolName, folderName, htmlTitle);
		c.run();
	}
}
