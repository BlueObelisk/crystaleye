package ned24.sandbox;

import java.io.File;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Node;
import nu.xom.Nodes;
import wwmm.crystaleye.util.Utils;

public class ResetNewAcsJournalsInLog {
	
	public static void main(String[] args) {
		File logFile = new File("C:\\Documents and Settings\\ned24.UCC\\Desktop\\download-log.xml");
		Document doc = Utils.parseXml(logFile);
		
		Nodes journalNds = doc.query(".//publisher[@abbreviation='acs']/journal");
		for (int i = 0; i < journalNds.size(); i++) {
			Element e = (Element)journalNds.get(i);
			String jname = e.getAttributeValue("abbreviation");
			if (jname.equals("cgdefu") ||
					jname.equals("inocaj") ||
					jname.equals("jacsat") ||
					jname.equals("jnprdf") ||
					jname.equals("joceah") ||
					jname.equals("ordnd7") ||
					jname.equals("orlef7")) {
				continue;
			}
			Nodes issueNds = e.query(".//issue");
			for (int j = 0; j < issueNds.size(); j++) {
				Element issueNd = (Element)issueNds.get(j);
				Nodes childs = issueNd.query(".//*");
				for (int k = 0; k < childs.size(); k++) {
					Node n = childs.get(k);
					if (n instanceof Element) {
						Element ee= (Element)n;
						if (ee.getLocalName().equals("rss")) {
							continue;
						}
						if ("true".equals(ee.getAttributeValue("value"))) {
							Attribute a = ee.getAttribute("value");
							a.setValue("false");
						}
					}
				}
			}
		}
		
		File outFile = new File("C:\\Documents and Settings\\ned24.UCC\\Desktop\\download-log.xml.2");
		Utils.writeXML(outFile, doc);
	}

}
