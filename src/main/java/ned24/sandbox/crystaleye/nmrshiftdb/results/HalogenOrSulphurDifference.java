package ned24.sandbox.crystaleye.nmrshiftdb.results;

import java.io.File;
import java.util.List;

import ned24.sandbox.crystaleye.nmrshiftdb.FileListTool;
import ned24.sandbox.crystaleye.nmrshiftdb.plottools.DifferencePlot;

import org.xmlcml.cml.element.CMLAtom;
import org.xmlcml.cml.element.CMLMolecule;
import org.xmlcml.molutil.ChemicalElement.Type;

public class HalogenOrSulphurDifference extends DifferencePlot {
	
	public HalogenOrSulphurDifference(List<File> fileList, String protocolName, String folderName, String htmlTitle) {
		super(fileList, protocolName, folderName, htmlTitle);
	}

	protected boolean isAtomSuitable(CMLMolecule molecule, String atomId) {
		boolean suitable = false;
		CMLAtom atom = molecule.getAtomById(atomId);
		for (CMLAtom ligand : atom.getLigandAtoms()) {
			if (ligand.getChemicalElement().isChemicalElementType(Type.HALOGEN) 
					|| "S".equals(ligand.getElementType())) {
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
		
		String cmlDir = CML_DIR+protocolName;
		String folderName = "halogensAndS-difference";
		
		FileListTool ft = new FileListTool(cmlDir);
		//ft.setIncludeNotRemoved(false);folderName+="_nr";
		ft.setIncludeHumanEdited(true);folderName+="_he";
		ft.setIncludeMisassigned(true);folderName+="_m";
		ft.setIncludePoorStructures(true);folderName+="_ps";
		ft.setIncludePossMisassigned(true);folderName+="_pm";
		ft.setIncludeTautomers(true);folderName+="_ta";
		ft.setIncludeTooLargeRing(true);folderName+="_lr";
		List<File> fileList = ft.getFileList();
		
		String htmlTitle = "Carbons bonded to Halogens";
		
		HalogenOrSulphurDifference c = new HalogenOrSulphurDifference(fileList, protocolName, folderName, htmlTitle);
		c.run();
	}
}
