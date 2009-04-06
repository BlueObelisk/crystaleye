package wwmm.crystaleye.tools;

import static org.junit.Assert.assertEquals;
import static wwmm.crystaleye.tools.CheckCifParser.X_CC;

import java.io.File;
import java.io.IOException;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;

import org.junit.Test;

public class CheckCifParserTest {
	
	@Test
	public void testParseServiceCifWithSingleDatablock() throws IOException {
		File ccFile = new File("./src/test/resources/tools/checkcif/av3132sup1_I.checkcif.html");
		CheckCifParser ccp = new CheckCifParser(ccFile);
		Document parsedDoc = ccp.parseService();
		// run through checking various elements of the parsed doc
		Nodes alerts = parsedDoc.query(".//c:alert", X_CC);
		assertEquals(5, alerts.size());
		Element secondAlert = (Element)alerts.get(2);
		String secondAlertText = secondAlert.getValue();
		assertEquals("Hirshfeld Test Diff for C5 -- C6 .. 6.87 su", secondAlertText);
		Nodes properties = parsedDoc.query(".//c:property", X_CC);
		assertEquals(15, properties.size());
		Nodes platonLink = parsedDoc.query(".//c:platon/c:link", X_CC);
		assertEquals(1, platonLink.size());
		String link = platonLink.get(0).getValue();
		assertEquals("http://dynhost1.iucr.org/tmp/032209174812/platon_Ite.gif", link);	
	}

}
