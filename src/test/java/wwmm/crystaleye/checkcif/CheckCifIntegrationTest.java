package wwmm.crystaleye.checkcif;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static wwmm.crystaleye.checkcif.CheckCifParser.X_CC;

import java.io.File;
import java.io.IOException;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;

import org.junit.BeforeClass;
import org.junit.Test;

public class CheckCifIntegrationTest {
	
	static String checkCifStr;
	
	@BeforeClass
	public static void before() {
		File cifFile = new File("./src/test/resources/checkcif/av3132sup1_I.cif");
		CheckCifTool cct = new CheckCifTool();
		checkCifStr = cct.getCheckcifString(cifFile);
	}

	/**
	 * Simple integration test that asserts that by providing a CIF
	 * the tool successfully gets the CheckCIF.
	 */
	@Test
	public void testGetCheckCIF() throws IOException {
		assertTrue(checkCifStr.contains("checkCIF/PLATON report (publication check)"));
	}
	
	/**
	 * 
	 */
	@Test
	public void testParseCheckCIF() {
		CheckCifParser parser = new CheckCifParser(checkCifStr);
		Document parsedDoc = parser.parseService();
		Nodes alerts = parsedDoc.query(".//c:alert", X_CC);
		assertEquals(5, alerts.size());
		Element secondAlert = (Element)alerts.get(2);
		String secondAlertText = secondAlert.getValue();
		assertEquals("Hirshfeld Test Diff for    C5     --  C6      ..       6.87 su", secondAlertText);
		Nodes properties = parsedDoc.query(".//c:property", X_CC);
		assertEquals(15, properties.size());
		Nodes platonLink = parsedDoc.query(".//c:platon/c:link", X_CC);
		assertEquals(1, platonLink.size());
		String link = platonLink.get(0).getValue();
		assertTrue(link.contains("http://dynhost1.iucr.org/tmp/"));
		assertTrue(link.contains("platon_Ite.gif"));
	}
	
}
