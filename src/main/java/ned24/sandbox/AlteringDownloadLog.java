package ned24.sandbox;

import static wwmm.crystaleye.CrystalEyeConstants_Old.ATOMPUB;
import static wwmm.crystaleye.CrystalEyeConstants_Old.BONDLENGTHS;
import static wwmm.crystaleye.CrystalEyeConstants_Old.CELLPARAMS;
import static wwmm.crystaleye.CrystalEyeConstants_Old.CIF2CML;
import static wwmm.crystaleye.CrystalEyeConstants_Old.CML2FOO;
import static wwmm.crystaleye.CrystalEyeConstants_Old.CML2RDF;
import static wwmm.crystaleye.CrystalEyeConstants_Old.DOILIST;
import static wwmm.crystaleye.CrystalEyeConstants_Old.RSS;
import static wwmm.crystaleye.CrystalEyeConstants_Old.SMILESLIST;
import static wwmm.crystaleye.CrystalEyeConstants_Old.WEBPAGE;

import java.io.File;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import wwmm.crystaleye.util.Utils;

public class AlteringDownloadLog {

	public static void main(String[] args) throws Exception {
		String logPath = "e:/crystaleye-new/docs/download-log.xml";
		File logFile = new File(logPath);
		Document doc = Utils.parseXml(logFile);
		Element root = (Element)doc.getRootElement();

		String dataPath = "e:/crystaleye-new/new/";
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
						cif2Cml.addAttribute(new Attribute("value", "false"));			
						Element cml2Foo = new Element(CML2FOO);
						cml2Foo.addAttribute(new Attribute("value", "false"));
						issue.appendChild(cml2Foo);	
						Element cml2rdfElement = new Element(CML2RDF);
						cml2rdfElement.addAttribute(new Attribute("value", "false"));
						issue.appendChild(cml2rdfElement);	
						Element webpage = new Element(WEBPAGE);
						webpage.addAttribute(new Attribute("value", "false"));
						issue.appendChild(webpage);
						Element doilist = new Element(DOILIST);
						doilist.addAttribute(new Attribute("value", "false"));
						issue.appendChild(doilist);
						Element bondLengths = new Element(BONDLENGTHS);
						bondLengths.addAttribute(new Attribute("value", "false"));
						issue.appendChild(bondLengths);
						Element cellParams = new Element(CELLPARAMS);
						cellParams.addAttribute(new Attribute("value", "false"));
						issue.appendChild(cellParams);
						Element smiles = new Element(SMILESLIST);
						smiles.addAttribute(new Attribute("value", "false"));
						issue.appendChild(smiles);
						Element rss = new Element(RSS);
						rss.addAttribute(new Attribute("value", "false"));
						issue.appendChild(rss);
						Element atompub = new Element(ATOMPUB);
						atompub.addAttribute(new Attribute("value", "false"));
						issue.appendChild(atompub);
					}
				}
			}
		}
		
		Utils.writeXML(new File(logPath+".2"), doc);
	}

}
