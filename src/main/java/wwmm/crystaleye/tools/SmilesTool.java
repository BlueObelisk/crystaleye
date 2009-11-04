package wwmm.crystaleye.tools;

import java.io.IOException;
import java.io.StringReader;

import nu.xom.ParsingException;
import nu.xom.ValidityException;

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
	
	CMLMolecule molecule;

	private static final Logger LOG = Logger.getLogger(SmilesTool.class);

	public SmilesTool(CMLMolecule molecule) {
		this.molecule = molecule;
	}

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
	public String generateSmiles() {
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
	
	public static void main(String[] args) throws ValidityException, ParsingException, IOException {
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
		SmilesTool tool = new SmilesTool(molecule);
		String smiles = tool.generateSmiles();
		System.out.println(smiles);
	}

}
