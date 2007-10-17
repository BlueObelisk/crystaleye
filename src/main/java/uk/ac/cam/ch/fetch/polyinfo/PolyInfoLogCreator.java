package uk.ac.cam.ch.fetch.polyinfo;

import java.io.File;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import uk.ac.cam.ch.crystaleye.IOUtils;

public class PolyInfoLogCreator {
	public static void main(String[] args) {
		String path = "E:\\data\\polyinfo-new";
		File dir = new File(path);
		File[] classes = dir.listFiles();

		Element root = new Element("polyinfo");
		Document doc = new Document(root);

		for (File classe : classes) {
			String classePath = classe.getAbsolutePath();
			if (classePath.endsWith(".xml")) {
				continue;
			}
			String className = classe.getName();
			Element classEl = new Element("class");
			classEl.addAttribute(new Attribute("name", className));
			classEl.addAttribute(new Attribute("completed", "true"));
			root.appendChild(classEl);

			File[] carbons = classe.listFiles();
			for (File carbon : carbons) {
				String carbonName = carbon.getName();
				Element carbonEl = new Element("carbons");
				carbonEl.addAttribute(new Attribute("number", carbonName));
				carbonEl.addAttribute(new Attribute("completed", "true"));
				classEl.appendChild(carbonEl);

				File[] polymers = carbon.listFiles();
				for (File polymer : polymers) {
					String polyName = polymer.getName();
					Element polyEl = new Element("polymer");
					polyEl.addAttribute(new Attribute("name", polyName));
					carbonEl.appendChild(polyEl);
				}
			}
		}
		IOUtils.writeXML(doc, "e:/data/polyinfo-new/polyinfo-index.xml");
	}
}
