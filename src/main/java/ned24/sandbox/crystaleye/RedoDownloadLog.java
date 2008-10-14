package ned24.sandbox.crystaleye;

import java.io.File;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import wwmm.crystaleye.IOUtils;

public class RedoDownloadLog {
	public static void main(String[] args) {
		String cifPath = "/data_soft/crystaleye-work/";
		File cifDir = new File(cifPath);
		Document doc = new Document(new Element("log"));
		Element root = doc.getRootElement();
		File[] publisherFiles = cifDir.listFiles();
		for (File publisherFile : publisherFiles) {
			if (publisherFile.getAbsolutePath().contains("doc")) continue;
			String title = publisherFile.getName();
			Element element = new Element("publisher");
			element.addAttribute(new Attribute("abbreviation", title));
			root.appendChild(element);

			File[] journalFiles = publisherFile.listFiles();
			for (File journalFile : journalFiles) {
				System.out.println(journalFile.getAbsolutePath());
				title = journalFile.getName();
				Element journalElement = new Element("journal");
				journalElement.addAttribute(new Attribute("abbreviation", title));
				element.appendChild(journalElement);

				File[] yearFiles = journalFile.listFiles();
				for (File yearFile : yearFiles) {
					title = yearFile.getName();
					Element yearElement = new Element("year");
					yearElement.addAttribute(new Attribute("id", title));
					journalElement.appendChild(yearElement);

					File[] issueFiles = yearFile.listFiles();
					for (File issueFile : issueFiles) {
						title = issueFile.getName();
						Element issueElement = new Element("issue");
						issueElement.addAttribute(new Attribute("id", title));
						yearElement.appendChild(issueElement);

						File[] articleFiles = issueFile.listFiles();
						int numArticles = articleFiles.length;
						Element cifs = new Element("cifs");
						cifs.addAttribute(new Attribute("number", String.valueOf(numArticles)));
						issueElement.appendChild(cifs);

						Element cif2Cml = new Element("cif2Cml");
						cif2Cml.addAttribute(new Attribute("value", "false"));
						issueElement.appendChild(cif2Cml);

						Element cml2Foo = new Element("cml2Foo");
						cml2Foo.addAttribute(new Attribute("value", "false"));
						issueElement.appendChild(cml2Foo);

						Element webpage = new Element("webpage");
						webpage.addAttribute(new Attribute("value", "false"));
						issueElement.appendChild(webpage);

						Element rss = new Element("rss");
						rss.addAttribute(new Attribute("value", "false"));
						issueElement.appendChild(rss);
						
						Element smi = new Element("smileslist");
						smi.addAttribute(new Attribute("value", "false"));
						issueElement.appendChild(smi);
						
						Element doi = new Element("doilist");
						doi.addAttribute(new Attribute("value", "false"));
						issueElement.appendChild(doi);
						
						Element bl = new Element("bondlengths");
						bl.addAttribute(new Attribute("value", "false"));
						issueElement.appendChild(bl);
						
						Element cp = new Element("cellParams");
						cp.addAttribute(new Attribute("value", "false"));
						issueElement.appendChild(cp);
					}
				}
			}
		}
		IOUtils.writePrettyXML(doc, "/usr/local/crystaleye/regenerate/download-log-all.xml");
	}
}
