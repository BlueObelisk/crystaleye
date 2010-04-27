package ned24.sandbox;

import static wwmm.crystaleye.CrystalEyeConstants.ATOMPUB;
import static wwmm.crystaleye.CrystalEyeConstants.BONDLENGTHS;
import static wwmm.crystaleye.CrystalEyeConstants.CELLPARAMS;
import static wwmm.crystaleye.CrystalEyeConstants.CIF2CML;
import static wwmm.crystaleye.CrystalEyeConstants.CML2FOO;
import static wwmm.crystaleye.CrystalEyeConstants.CML2RDF;
import static wwmm.crystaleye.CrystalEyeConstants.DOILIST;
import static wwmm.crystaleye.CrystalEyeConstants.RSS;
import static wwmm.crystaleye.CrystalEyeConstants.SMILESLIST;
import static wwmm.crystaleye.CrystalEyeConstants.WEBPAGE;

import java.io.File;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import wwmm.crystaleye.util.Utils;

public class RecreateDownloadLog {

	public static void main(String[] args) throws Exception {
		Document doc = new Document(new Element("log"));
		Element root = (Element)doc.getRootElement();

		String dataPath = "/scratch/crystaleye-work/";
		for (File pubDir : new File(dataPath).listFiles()) {
			Element publisher = new Element("publisher");
			root.appendChild(publisher);
			publisher.addAttribute(new Attribute("abbreviation", pubDir.getName()));
			for (File journalDir : pubDir.listFiles()) {
				Element journal = new Element("journal");
				publisher.appendChild(journal);
				journal.addAttribute(new Attribute("abbreviation", journalDir.getName()));
				for (File yearDir : journalDir.listFiles()) {
					Element year = new Element("year");
					journal.appendChild(year);
					year.addAttribute(new Attribute("id", yearDir.getName()));
					for (File issueDir : yearDir.listFiles()) {
						Element issue = new Element("issue");
						year.appendChild(issue);
						issue.addAttribute(new Attribute("id", issueDir.getName()));
						Element cif2Cml = new Element(CIF2CML);
						issue.appendChild(cif2Cml);
						cif2Cml.addAttribute(new Attribute("value", "true"));			
						Element cml2Foo = new Element(CML2FOO);
						cml2Foo.addAttribute(new Attribute("value", "true"));
						issue.appendChild(cml2Foo);	
						Element cml2rdfElement = new Element(CML2RDF);
						cml2rdfElement.addAttribute(new Attribute("value", "true"));
						issue.appendChild(cml2rdfElement);	
						Element webpage = new Element(WEBPAGE);
						webpage.addAttribute(new Attribute("value", "true"));
						issue.appendChild(webpage);
						Element doilist = new Element(DOILIST);
						doilist.addAttribute(new Attribute("value", "true"));
						issue.appendChild(doilist);
						Element bondLengths = new Element(BONDLENGTHS);
						bondLengths.addAttribute(new Attribute("value", "true"));
						issue.appendChild(bondLengths);
						Element cellParams = new Element(CELLPARAMS);
						cellParams.addAttribute(new Attribute("value", "true"));
						issue.appendChild(cellParams);
						Element smiles = new Element(SMILESLIST);
						smiles.addAttribute(new Attribute("value", "true"));
						issue.appendChild(smiles);
						Element rss = new Element(RSS);
						rss.addAttribute(new Attribute("value", "true"));
						issue.appendChild(rss);
						Element atompub = new Element(ATOMPUB);
						atompub.addAttribute(new Attribute("value", "true"));
						issue.appendChild(atompub);
					}
				}
			}
		}
		
		String logPath = "/usr/local/crystaleye/log/download-log.xml.new";
		File logFile = new File(logPath);
		Utils.writeXML(logFile, doc);
	}

}
