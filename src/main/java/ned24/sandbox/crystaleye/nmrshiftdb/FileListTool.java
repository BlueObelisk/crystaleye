package ned24.sandbox.crystaleye.nmrshiftdb;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import nu.xom.Nodes;

import org.xmlcml.cml.element.CMLMolecule;

import uk.ac.cam.ch.crystaleye.IOUtils;

public class FileListTool implements GaussianConstants {

	String path;

	boolean includeNotRemoved = true;
	boolean includeHumanEdited = false;
	boolean includeMisassigned = false;
	boolean includePossMisassigned = false;
	boolean includeTooLargeRing = false;
	boolean includeTautomers = false;
	boolean includePoorStructures = false;
	
	private static enum FileType {
		NOT_REMOVED, HUMAN_EDITED, MISASSIGNED, POSS_MISASSIGNED,
		TOO_LARGE_RING, TAUTOMER, POOR_STRUCTURE;
	}

	public FileListTool(String path) {
		this.path = path;
	}

	public List<File> getFileList() {
		List<File> fileList = new ArrayList<File>();
		for (File file : new File(path).listFiles()) {
			if (!file.getAbsolutePath().endsWith(".cml.xml")) {
				continue;
			}
			CMLMolecule molecule = (CMLMolecule)IOUtils.parseCmlFile(file).getRootElement();
			if (includeFile(molecule)) {
				fileList.add(file);
			}
		}

		return fileList;
	}
	
	protected boolean includeFile(CMLMolecule molecule) {
		FileType type = getFileType(molecule);
		if (type.equals(FileType.HUMAN_EDITED)) {
			if (includeHumanEdited) {
				return true;
			}
		} else if (type.equals(FileType.MISASSIGNED)) {
			if (includeMisassigned) {
				return true;
			}
		} else if (type.equals(FileType.POOR_STRUCTURE)) {
			if (includePoorStructures) {
				return true;
			}
		} else if (type.equals(FileType.NOT_REMOVED)) {
			if (includeNotRemoved) {
				return true;
			}
		} else if (type.equals(FileType.POSS_MISASSIGNED)) {
			if (includePossMisassigned) {
				return true;
			}
		} else if (type.equals(FileType.TAUTOMER)) {
			if (includeTautomers) {
				return true;
			}
		} else if (type.equals(FileType.TOO_LARGE_RING)) {
			if (includeTooLargeRing) {
				return true;
			}
		} else {
			throw new RuntimeException("Unknow file type.");
		}
		return false;
	}
	
	protected FileType getFileType(CMLMolecule molecule) {
		if (isNotRemovedFile(molecule)) {
			return FileType.NOT_REMOVED;
		} else if (isHumanEditedFile(molecule)) {
			return FileType.HUMAN_EDITED;
		} else if (isMisassignedFile(molecule)) {
			return FileType.MISASSIGNED;
		} else if (isPoorStructureFile(molecule)) {
			return FileType.POOR_STRUCTURE;
		} else if (isPossMisassignedFile(molecule)) {
			return FileType.POSS_MISASSIGNED;
		} else if (isTautomerFile(molecule)) {
			return FileType.TAUTOMER;
		} else if (isTooLargeRingFile(molecule)) {
			return FileType.TOO_LARGE_RING;
		} else {
			throw new RuntimeException("Unknown filetype.");
		}
	}
	
	protected boolean isNotRemovedFile(CMLMolecule molecule) {
		return isFileOfType(molecule, NOT_REMOVED_CML_DICTREF);
	}
	
	protected boolean isPoorStructureFile(CMLMolecule molecule) {
		return isFileOfType(molecule, POOR_STRUCT_CML_DICTREF);
	}
	
	protected boolean isTautomerFile(CMLMolecule molecule) {
		return isFileOfType(molecule, TAUTOMERS_CML_DICTREF);
	}
	
	protected boolean isTooLargeRingFile(CMLMolecule molecule) {
		return isFileOfType(molecule, LARGE_RING_CML_DICTREF);
	}
	
	protected boolean isPossMisassignedFile(CMLMolecule molecule) {
		return isFileOfType(molecule, POSS_MISASSIGNED_CML_DICTREF);
	}
	
	protected boolean isMisassignedFile(CMLMolecule molecule) {
		return isFileOfType(molecule, MISASSIGNED_CML_DICTREF);
	}
	
	protected boolean isHumanEditedFile(CMLMolecule molecule) {
		return isFileOfType(molecule, HUMANEDIT_CML_DICTREF);
	}

	protected boolean isRemovedFile(CMLMolecule molecule) {
		return isFileOfType(molecule, REMOVED_CML_DICTREF);
	}
	
	protected boolean isFileOfType(CMLMolecule molecule, String dictRef) {
		boolean b = false;
		Nodes removedNodes = molecule.query(".//cml:scalar[@dictRef='"+dictRef+"']", X_CML);
		if (removedNodes.size() > 0) {
			return true;
		} else {
			return b;
		}
	}

	public void setIncludeHumanEdited(boolean includeHumanEdited) {
		this.includeHumanEdited = includeHumanEdited;
	}

	public void setIncludeMisassigned(boolean includeMisassigned) {
		this.includeMisassigned = includeMisassigned;
	}

	public void setIncludePoorStructures(boolean includePoorStructures) {
		this.includePoorStructures = includePoorStructures;
	}

	public void setIncludePossMisassigned(boolean includePossMisassigned) {
		this.includePossMisassigned = includePossMisassigned;
	}

	public void setIncludeTautomers(boolean includeTautomers) {
		this.includeTautomers = includeTautomers;
	}

	public void setIncludeTooLargeRing(boolean includeTooLargeRing) {
		this.includeTooLargeRing = includeTooLargeRing;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public void setIncludeNotRemoved(boolean includeNotRemoved) {
		this.includeNotRemoved = includeNotRemoved;
	}
}
