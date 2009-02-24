package wwmm.crystaleye;

import static wwmm.crystaleye.CrystalEyeConstants.CRYSTALEYE_DATE_FORMAT;
import static wwmm.crystaleye.CrystalEyeConstants.TITLE_MIME;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nu.xom.Node;
import nu.xom.Nodes;

import org.apache.commons.io.FileUtils;
import org.xmlcml.cif.CIFUtil;
import org.xmlcml.cml.base.CMLConstants;
import org.xmlcml.cml.base.CMLUtil;
import org.xmlcml.cml.element.CMLAtom;
import org.xmlcml.cml.element.CMLCml;
import org.xmlcml.cml.element.CMLFormula;
import org.xmlcml.cml.element.CMLMolecule;
import org.xmlcml.molutil.ChemicalElement.Type;



public class CrystalEyeUtils implements CMLConstants {

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
				List<Node> inchiNodes = CMLUtil.getQueryNodes(subMol, ".//cml:identifier[@convention='iupac:inchi']", CML_XPATH);
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
				List<Node> molNodes = CMLUtil.getQueryNodes(molecule, ".//cml:molecule[cml:identifier[text()='"+inchi+"']]", CML_XPATH);
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
		Utils.writeText(dNow, path);
	}
	
	public static String getStructureTitleFromCml(CMLCml cml) {
		Nodes titleNodes = cml.query(".//cml:scalar[@dictRef=\"iucr:_publ_section_title\"]", CML_XPATH);
		String title = "";
		try {
			if (titleNodes.size() != 0) {
				title = titleNodes.get(0).getValue();
				title = CIFUtil.translateCIF2ISO(title);
				title = title.replaceAll("\\\\", "");

				String patternStr = "\\^(\\d+)\\^";
				String replaceStr = "<sup>$1</sup>";
				Pattern pattern = Pattern.compile(patternStr);
				Matcher matcher = pattern.matcher(title);
				title = matcher.replaceAll(replaceStr);

				patternStr = "~(\\d+)~";
				replaceStr = "<sub>$1</sub>";
				pattern = Pattern.compile(patternStr);
				matcher = pattern.matcher(title);
				title = matcher.replaceAll(replaceStr);
			}
		} catch (Exception e) {
			System.err.println("Could not translate CIF string to ISO: "+title);
			title = "";
		}
		return title;
	}
	
	public static String getStructureTitleFromTOC(File cmlFile) {
		File titleParent = cmlFile.getParentFile().getParentFile();
		String name = titleParent.getName();
		File titleFile = new File(titleParent, name+TITLE_MIME);
		String title = "";
		if (titleFile.exists()) {
			try {
				title = FileUtils.readFileToString(titleFile).trim();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return title;
	}
}
