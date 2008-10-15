package ned24.sandbox.crystaleye;

import java.io.File;
import java.io.FileReader;

import nu.xom.Attribute;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;
import wwmm.crystaleye.util.XmlIOUtils;

public class AddElementToDownloadLog {

	public static void main(String[] args) {
		String path = "E:\\download-log.xml";
		File file = new File(path);

		Document doc = null;
		try {
			doc = new Builder().build(new FileReader(file));
			Nodes issueNodes = doc.query(".//issue");
			if (issueNodes.size() > 0) {
				for (int i = 0; i < issueNodes.size(); i++) {
					Element issueNode = (Element)issueNodes.get(i);
					Nodes doiListNodes = issueNode.query("./atompub");
					if (doiListNodes.size() == 0) {
						Element doiListNode = new Element("atompub");
						issueNode.appendChild(doiListNode);
						doiListNode.addAttribute(new Attribute("value", "false"));
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		XmlIOUtils.writeXML(doc, path);
	}
}
