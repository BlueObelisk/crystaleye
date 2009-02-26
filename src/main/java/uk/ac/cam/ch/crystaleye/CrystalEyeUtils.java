package uk.ac.cam.ch.crystaleye;

import static uk.ac.cam.ch.crystaleye.CrystalEyeConstants.CRYSTALEYE_DATE_FORMAT;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import nu.xom.Node;

import org.xmlcml.cml.base.CMLConstants;
import org.xmlcml.cml.base.CMLUtil;
import org.xmlcml.cml.element.CMLAtom;
import org.xmlcml.cml.element.CMLFormula;
import org.xmlcml.cml.element.CMLMolecule;
import org.xmlcml.molutil.ChemicalElement.Type;

public class CrystalEyeUtils implements CMLConstants {

	public static enum FragmentType {
		LIGAND ("ligand"),
		CHAIN_NUC ("chain-nuc"),
		RING_NUC ("ring-nuc"),
		RING_NUC_SPROUT_1 ("ring-nuc-sprout-1"),
		RING_NUC_SPROUT_2 ("ring-nuc-sprout-2"),
		CLUSTER_NUC ("cluster-nuc"),
		CLUSTER_NUC_SPROUT_1 ("cluster-nuc-sprout-1"),
		CLUSTER_NUC_SPROUT_2 ("cluster-nuc-sprout-2"),
		MOIETY ("moiety"),
		ATOM_NUC ("atom-nuc"),
		ATOM_NUC_SPROUT_1 ("atom-nuc-sprout-1"),
		ATOM_NUC_SPROUT_2 ("atom-nuc-sprout-2");

		private FragmentType(String name) {
			this.name = name;
		}

		private final String name;

		public String toString() {
			return name;
		}
	}

	public static enum DisorderType {
		UNPROCESSED,
		PROCESSED,
		NONE;
	}

	public static enum CompoundClass {
		ORGANIC("organic"),
		INORGANIC("inorganic"),
		ORGANOMETALLIC("organometallic");

		private CompoundClass(String name) {
			this.name = name;
		}

		private final String name;

		public String toString() {
			return name;
		}
	}

	public static CompoundClass getCompoundClass(CMLMolecule molecule) {
		boolean hasMetal = false;
		boolean hasC = false;
		boolean hasH = false;
		for (CMLAtom atom : molecule.getAtoms()) {
			if (atom.getChemicalElement().isChemicalElementType(Type.METAL)) {
				hasMetal = true;
			}
			String elType = atom.getElementType();
			if ("H".equals(elType)) {
				hasH = true;
			} else if ("C".equals(elType)) {
				hasC = true;
			}
		}
		if (!hasMetal) {
			return CompoundClass.ORGANIC;
		} else if (hasMetal) {
			if (hasH && hasC) {
				return CompoundClass.ORGANOMETALLIC;
			} else {
				return CompoundClass.INORGANIC;
			}
		}
		return null;
	}

	public static List<File> getSummaryDirFileList(String issueDir, String regex) {
		List<File> fileList = new ArrayList<File>();
		issueDir += File.separator+"data"+File.separator;
		File[] parents = new File(issueDir).listFiles();
		for (File articleParent : parents) {
			File[] articleFiles = articleParent.listFiles();
			for (File structureParent : articleFiles) {
				if (structureParent.isDirectory()) {
					File[] structureFiles = structureParent.listFiles();
					for (File structureFile : structureFiles) {
						String structurePath = structureFile.getName();
						if (structurePath.matches(regex)) {
							fileList.add(structureFile);
						}
					}
				}
			}
		}
		return fileList;
	}

	public static List<File> getDataDirFileList(String issueDir, String regex) {
		List<File> fileList = new ArrayList<File>();
		File[] parents = new File(issueDir).listFiles();
		for (File articleParent : parents) {
			File[] articleFiles = articleParent.listFiles();
			for (File structureParent : articleFiles) {
				if (structureParent.isDirectory()) {
					File[] structureFiles = structureParent.listFiles();
					for (File structureFile : structureFiles) {
						String structurePath = structureFile.getName();
						if (structurePath.matches(regex)) {
							fileList.add(structureFile);
						}
					}
				}
			}
		}
		return fileList;
	}

	public static boolean isBoringMolecule(CMLMolecule molecule) {
		// skip boring moieties
		CMLFormula formula = new CMLFormula(molecule);
		formula.normalize();
		String formulaS = formula.getConcise();
		formulaS = CMLFormula.removeChargeFromConcise(formulaS);
		if (formulaS.equals("H 2 O 1") || 
				formulaS.equals("H 3 O 1") ||
				formulaS.equals("H 4 O 1") ||
				molecule.getAtomCount() == 1) {
			return true;
		} else {
			return false;
		}
	}

	public static List<CMLMolecule> getUniqueSubMolecules(CMLMolecule molecule) {
		List<CMLMolecule> outputList = new ArrayList<CMLMolecule>();
		if (molecule.isMoleculeContainer()) {
			List<String> inchiList = new ArrayList<String>();
			for (CMLMolecule subMol : molecule.getDescendantsOrMolecule()) {
				List<Node> inchiNodes = CMLUtil.getQueryNodes(subMol, ".//cml:identifier[@convention='iupac:inchi']", X_CML);
				if (inchiNodes.size() > 0) {
					String inchi = inchiNodes.get(0).getValue();
					boolean got = false;
					for (String str : inchiList) {
						if (str.equals(inchi)) got = true;
					}
					if (!got) {
						inchiList.add(inchi);
					}
				}
			}
			for (String inchi : inchiList) {
				List<Node> molNodes = CMLUtil.getQueryNodes(molecule, ".//cml:molecule[cml:identifier[text()='"+inchi+"']]", X_CML);
				if (molNodes.size() > 0) {
					outputList.add((CMLMolecule)molNodes.get(0));
				}
			}
		} else {
			outputList.add(molecule);
		}
		return outputList;
	}
	
	public static Date parseString2Date(String dateString) {
		SimpleDateFormat formatter = new SimpleDateFormat(CRYSTALEYE_DATE_FORMAT);
		try {
			return formatter.parse(dateString);
		} catch (ParseException e) {
			throw new RuntimeException("Error parsing String into Date - "+dateString);
		}
	}

	public static String getDate() {
		Date date = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat(CRYSTALEYE_DATE_FORMAT);
		return formatter.format(date);
	}
	
	public static String date2String(Date date) {
		SimpleDateFormat formatter = new SimpleDateFormat(CRYSTALEYE_DATE_FORMAT);
		return formatter.format(date);
	}

	public static void writeDateStamp(String path) {
		String dNow = getDate();
		IOUtils.writeText(dNow, path);
	}
}
