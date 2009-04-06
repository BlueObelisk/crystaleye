package wwmm.crystaleye.tools;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.StringReader;

import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.junit.Test;
import org.xmlcml.cml.base.CMLBuilder;
import org.xmlcml.cml.element.CMLMolecule;

public class SmilesToolTest {

	@Test
	public void testGenerateSmiles() throws ValidityException, ParsingException, IOException {
		String molStr = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
		"<molecule xmlns=\"http://www.xml-cml.org/schema\">\n" +
		"<atomArray>\n" +
		"<atom id=\"a1\" elementType=\"C\" x3=\"0.0\" y3=\"0.0\" z3=\"0.0\" />\n" +
		"<atom id=\"a2\" elementType=\"H\" x3=\"0.0\" y3=\"1.0\" z3=\"0.0\" />\n" +
		"<atom id=\"a3\" elementType=\"Cl\" x3=\"-1.0\" y3=\"-1.0\" z3=\"0.0\" />\n" +
		"<atom id=\"a4\" elementType=\"Br\" x3=\"0.0\" y3=\"-1.0\" z3=\"1.0\" />\n" +
		"<atom id=\"a5\" elementType=\"I\" x3=\"1.0\" y3=\"-1.0\" z3=\"-1.0\" />\n" +
		"</atomArray>\n" +
		"<bondArray>\n" +
		"<bond id=\"a1_a2\" atomRefs2=\"a1 a2\" order=\"1\" />\n" +
		"<bond id=\"a1_a3\" atomRefs2=\"a1 a3\" order=\"1\" />\n" +
		"<bond id=\"a1_a4\" atomRefs2=\"a1 a4\" order=\"1\" />\n" +
		"<bond id=\"a1_a5\" atomRefs2=\"a1 a5\" order=\"1\" />\n" +
		"</bondArray>\n" +
		"</molecule>";
		CMLMolecule molecule = (CMLMolecule)new CMLBuilder().build(new StringReader(molStr)).getRootElement();
		SmilesTool tool = new SmilesTool(molecule);
		String smiles = tool.generateSmiles();
		String expectedSmiles = "Cl[C@@H](Br)I";
		assertEquals(expectedSmiles, smiles);
	}

}
