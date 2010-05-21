package wwmm.crystaleye.tools;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.xmlcml.cml.base.CMLBuilder;
import org.xmlcml.cml.element.CMLMolecule;

import sea36.jbabel.BabelException;
import sea36.jbabel.BabelFormat;
import sea36.jbabel.BabelResult;
import sea36.jbabel.BabelRunner;
import sea36.jbabel.OpenBabel;

/**
 * <p>
 * Tool for the generation of a SMILES string from a CML molecule.
 * </p>
 * 
 * @author ned24
 * @version 0.1
 */
public class SmilesTool {

	private static final Logger LOG = Logger.getLogger(SmilesTool.class);

	/**
	 * <p>
	 * Generate the SMILES for the objects molecule using the SMILES program 
	 * options provided as a parameter.
	 * </p>
	 * 
	 * @param options - the SMILES program options to be used for generation.
	 * 
	 * @return the SMILES string for the objects molecule. Returns null if there
	 * was a problem during generation.
	 */
	public static String generateSmiles(CMLMolecule molecule) {
		BabelRunner runner = null;
		try {
			runner = OpenBabel.getBabelRunner();
		} catch (BabelException e) {
			LOG.warn("Problem getting Babel runner: "+e.getMessage());
			return null;
		}
		BabelResult result = null;
		try {
			BabelFormat fcml = runner.getFormat("cml");
			BabelFormat fsmi = runner.getFormat("smi");
			String molStr = molecule.toXML();
	        result = runner.convert(molStr, fcml, fsmi);
		} catch (BabelException e) {
			LOG.warn("Problem generating SMILES from CML: "+e.getMessage());
			return null;
		}
		return result.getOutput().trim().split("\\s+")[0];
	}
	
	/**
	 * <p>
	 * Creates a SMILES index file from the list of SMILES in
	 * the input file.  Note that in the input file:
	 * 
	 * 1. there must be one SMILES string per line, with no
	 * preceding space.
	 * 2. following the SMILES on each line, there can be optional
	 * whitespace, followed by a text string to associate with that
	 * SMILES.
	 * 
	 * </p>
	 * 
	 * @param file - containing the SMILES strings to be indexed.
	 * @return a <code>String</code> containing the created SMILES 
	 * index.
	 */
	public static String createIndex(File file) {
		BabelRunner runner = null;
		try {
			runner = OpenBabel.getBabelRunner();
		} catch (BabelException e) {
			LOG.warn("Problem getting Babel runner: "+e.getMessage());
			return null;
		}
		BabelResult result = null;
		try {
			String fileContents = FileUtils.readFileToString(file);
			result = runner.runAdvanced(fileContents, "-ofs");
		} catch (Exception e) {
			LOG.warn("Problem creating SMILES index ("+file+"): "+e.getMessage(), e);
			return null;
		}
		return result.getOutput().trim();
	}
	
	public static void main(String[] args) throws ValidityException, ParsingException, IOException {
		testSmilesGeneration();
	}
	
	private static void testIndexGeneration() {
		String smilesPath = "C:/workspace/crystaleye-trunk-data/www/crystaleye/smiles/smiles.smi";
		File file = new File(smilesPath);
		SmilesTool.createIndex(file);
	}
	
	private static void testSmilesGeneration() throws ValidityException, ParsingException, IOException {
		String molStr = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
				"<molecule xmlns=\"http://www.xml-cml.org/schema\" id=\"testmol1\">\n" +
				"<atomArray>\n" +
					"<atom id=\"a1\" elementType=\"C\" />\n" +
					"<atom id=\"a2\" elementType=\"H\" />\n" +
					"<atom id=\"a3\" elementType=\"H\" />\n" +
					"<atom id=\"a4\" elementType=\"H\" />\n" +
					"<atom id=\"a5\" elementType=\"H\" />\n" +
				"</atomArray>\n" +
				"<bondArray>\n" +
					"<bond id=\"a1_a2\" atomRefs2=\"a1 a2\" />\n" +
					"<bond id=\"a1_a3\" atomRefs2=\"a1 a3\" />\n" +
					"<bond id=\"a1_a4\" atomRefs2=\"a1 a4\" />\n" +
					"<bond id=\"a1_a5\" atomRefs2=\"a1 a5\" />\n" +
				"</bondArray>\n" +
				"</molecule>";
		CMLMolecule molecule = (CMLMolecule)new CMLBuilder().build(new StringReader(molStr)).getRootElement();
		String smiles = SmilesTool.generateSmiles(molecule);
		System.out.println("generated smiles: "+smiles);
	}

}
