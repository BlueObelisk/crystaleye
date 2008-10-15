package ned24.sandbox.crystaleye;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import nu.xom.Attribute;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;
import nu.xom.XPathContext;

import org.xmlcml.cml.base.CMLConstants;

import wwmm.crystaleye.util.XmlIOUtils;

public class UpdateDownloadLogRecords implements CMLConstants {

	XPathContext x = new XPathContext("x", CML_NS);

	public static void main(String[] args) {
		String rscDir = "e:/data/cif/chemSocJapan";
		File rscFile = new File(rscDir);
		String logPath = "E:/data/cif/docs/download-log.txt";
		Document doc = null;
		try {
			doc = new Builder().build(new BufferedReader(new FileReader(logPath)));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Nodes rscPublisher = doc.query("//publisher[@abbreviation='chemSocJapan']");
		if (rscPublisher.size() > 0) {
			Element rsc = (Element)rscPublisher.get(0);
			File[] fileArray = rscFile.listFiles();
			for (File file : fileArray) {
				if (file.getName().startsWith(".")) continue;
				Element journal = new Element("journal");
				journal.addAttribute(new Attribute("abbreviation", file.getName()));
				rsc.appendChild(journal);
				File[] yearFiles = file.listFiles();
				for (File years : yearFiles) {
					if (years.getName().startsWith(".")) continue;
					Element year = new Element("year");
					year.addAttribute(new Attribute("id", years.getName()));
					journal.appendChild(year);
					File[] issueFiles = years.listFiles();
					for (File issues : issueFiles) {
						if (issues.getName().startsWith(".")) continue;
						Element issue = new Element("issue");
						issue.addAttribute(new Attribute("id", issues.getName()));
						Element processed = new Element("processed");
						processed.addAttribute(new Attribute("value", "false"));
						issue.appendChild(processed);
						Element disseminated = new Element("disseminated");
						disseminated.addAttribute(new Attribute("value", "false"));
						issue.appendChild(disseminated);

						File[] ids = issues.listFiles();
						int num = ids.length;
						Element cifs = new Element("cifs");
						cifs.addAttribute(new Attribute("number", String.valueOf(num)));
						issue.appendChild(cifs);
						year.appendChild(issue);
					}
				}
			}
			XmlIOUtils.writePrettyXML(doc, logPath);
		} else {
			System.err.println("ERROR: could not find rsc node in download log.");
		}
	}
}
