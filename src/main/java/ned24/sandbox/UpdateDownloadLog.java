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
import nu.xom.Nodes;
import wwmm.crystaleye.util.Utils;

public class UpdateDownloadLog {

	public static void main(String[] args) {
		String logPath = "C:\\Documents and Settings\\ned24.UCC\\Desktop\\download-log.xml";
		File logFile = new File(logPath);
		Document doc = Utils.parseXml(logFile);
		Element root = (Element)doc.getRootElement();

		String dirPath = "E:\\crystaleye-2010\\";
		for (File pubDir : new File(dirPath).listFiles()) {
			for (File journalDir : pubDir.listFiles()) {
				for (File yearDir : journalDir.listFiles()) {
					for (File issueDir : yearDir.listFiles()) {
						String pub = pubDir.getName();
						String journal = journalDir.getName();
						String year = yearDir.getName();
						String issue = issueDir.getName();
						
						if (issue.equals("null")) {
							continue;
						}
						if (pub.equals("acta") && !issue.contains("-")) {
							issue = createActaIssue(issue);
						}
						
						String xpath = ".//publisher[@abbreviation='"+pub+"']/journal[@abbreviation='"+journal+"']/year[@id='"+year+"']/issue[@id='"+issue+"']";
						Nodes nds = root.query(xpath);
						if (nds.size() == 0) {
							String xpath2 = ".//publisher[@abbreviation='"+pub+"']/journal[@abbreviation='"+journal+"']/year[@id='"+year+"']";
							Nodes nds2 = root.query(xpath2);
							if (nds2.size() != 1) {
								String xpath3 = ".//publisher[@abbreviation='"+pub+"']/journal[@abbreviation='"+journal+"']";
								Nodes nds3 = root.query(xpath3);
								if (nds3.size() != 1) {
									throw new RuntimeException("basll");
								} else {
									System.out.println("no year");
									System.out.println(pub+" "+journal+" "+year+" "+issue);
									Element journalEl = (Element)nds3.get(0);
									Element yearEl = new Element("year");
									journalEl.appendChild(yearEl);
									yearEl.addAttribute(new Attribute("id", year));
									Element issueEl = new Element("issue");
									yearEl.appendChild(issueEl);
									issueEl.addAttribute(new Attribute("id", issue));
									Element cif2Cml = new Element(CIF2CML);
									issueEl.appendChild(cif2Cml);
									cif2Cml.addAttribute(new Attribute("value", "false"));			
									Element cml2Foo = new Element(CML2FOO);
									cml2Foo.addAttribute(new Attribute("value", "false"));
									issueEl.appendChild(cml2Foo);	
									Element cml2rdfElement = new Element(CML2RDF);
									cml2rdfElement.addAttribute(new Attribute("value", "false"));
									issueEl.appendChild(cml2rdfElement);	
									Element webpage = new Element(WEBPAGE);
									webpage.addAttribute(new Attribute("value", "false"));
									issueEl.appendChild(webpage);
									Element doilist = new Element(DOILIST);
									doilist.addAttribute(new Attribute("value", "false"));
									issueEl.appendChild(doilist);
									Element bondLengths = new Element(BONDLENGTHS);
									bondLengths.addAttribute(new Attribute("value", "false"));
									issueEl.appendChild(bondLengths);
									Element cellParams = new Element(CELLPARAMS);
									cellParams.addAttribute(new Attribute("value", "false"));
									issueEl.appendChild(cellParams);
									Element smiles = new Element(SMILESLIST);
									smiles.addAttribute(new Attribute("value", "false"));
									issueEl.appendChild(smiles);
									Element rss = new Element(RSS);
									rss.addAttribute(new Attribute("value", "false"));
									issueEl.appendChild(rss);
									Element atompub = new Element(ATOMPUB);
									atompub.addAttribute(new Attribute("value", "false"));
									issueEl.appendChild(atompub);
								}
							} else {
								System.out.println("no issue");
								System.out.println(pub+" "+journal+" "+year+" "+issue);
								Element yearEl = (Element)nds2.get(0);
								Element issueEl = new Element("issue");
								yearEl.appendChild(issueEl);
								issueEl.addAttribute(new Attribute("id", issue));
								Element cif2Cml = new Element(CIF2CML);
								issueEl.appendChild(cif2Cml);
								cif2Cml.addAttribute(new Attribute("value", "false"));			
								Element cml2Foo = new Element(CML2FOO);
								cml2Foo.addAttribute(new Attribute("value", "false"));
								issueEl.appendChild(cml2Foo);	
								Element cml2rdfElement = new Element(CML2RDF);
								cml2rdfElement.addAttribute(new Attribute("value", "false"));
								issueEl.appendChild(cml2rdfElement);	
								Element webpage = new Element(WEBPAGE);
								webpage.addAttribute(new Attribute("value", "false"));
								issueEl.appendChild(webpage);
								Element doilist = new Element(DOILIST);
								doilist.addAttribute(new Attribute("value", "false"));
								issueEl.appendChild(doilist);
								Element bondLengths = new Element(BONDLENGTHS);
								bondLengths.addAttribute(new Attribute("value", "false"));
								issueEl.appendChild(bondLengths);
								Element cellParams = new Element(CELLPARAMS);
								cellParams.addAttribute(new Attribute("value", "false"));
								issueEl.appendChild(cellParams);
								Element smiles = new Element(SMILESLIST);
								smiles.addAttribute(new Attribute("value", "false"));
								issueEl.appendChild(smiles);
								Element rss = new Element(RSS);
								rss.addAttribute(new Attribute("value", "false"));
								issueEl.appendChild(rss);
								Element atompub = new Element(ATOMPUB);
								atompub.addAttribute(new Attribute("value", "false"));
								issueEl.appendChild(atompub);
							}
						}
					}
				}
			}
		}
		
		Utils.writeXML(new File(logPath+".2"), doc);
	}
	
	private static String createActaIssue(String ss) {
		String issue = ss+"-00";
		if (issue.length() < 5) {
			issue = "0"+issue;
		}
		return issue;
	}

}
