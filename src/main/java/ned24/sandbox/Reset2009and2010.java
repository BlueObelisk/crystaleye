package ned24.sandbox;

import java.io.File;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Node;
import nu.xom.Nodes;
import wwmm.crystaleye.util.Utils;

public class Reset2009and2010 {
	
	public static void main(String[] args) {
		File logFile = new File("C:\\Documents and Settings\\ned24.UCC\\Desktop\\download-log.xml");
		Document doc = Utils.parseXml(logFile);
		Nodes yearNds = doc.query(".//year");
		for (int i = 0; i < yearNds.size(); i++) {
			Element yearEl = (Element)yearNds.get(i);
			String yearId = yearEl.getAttributeValue("id");
			if (yearId.equals("2010") || yearId.equals("2009")) {
				Nodes childs = yearEl.query(".//*");
				for (int j = 0; j < childs.size(); j++) {
					Node n = childs.get(j);
					if (n instanceof Element) {
						Element e= (Element)n;
						if (e.getLocalName().equals("rss")) {
							continue;
						}
						if ("true".equals(e.getAttributeValue("value"))) {
							Attribute a = e.getAttribute("value");
							a.setValue("false");
						}
					}
				}
			}
		}
		
		File outFile = new File("C:\\Documents and Settings\\ned24.UCC\\Desktop\\download-log.xml.2");
		Utils.writeXML(outFile, doc);
		System.out.println("ere");
	}

}
