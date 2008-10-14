package wwmm.crystaleye.fetch;

import static wwmm.crystaleye.CrystalEyeConstants.X_XHTML;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;
import wwmm.crystaleye.IOUtils;

public class PolyhedronBacklog {

	String writeDir;
	String logPath;
	Document logDoc;

	private static final String publisherAbbreviation = "elsevier";
	private static final String journalAbbreviation = "polyhedron";
	private static final String siteUrl = "http://www.sciencedirect.com";
	private static final String currentIssueUrl = siteUrl+"/science/journal/02775387";

	private int sleepMax = 5000;

	public PolyhedronBacklog(String writeDir, String logPath) {
		this.writeDir = writeDir;
		this.logPath = logPath;
		this.logDoc = IOUtils.parseXmlFile(logPath);
	}

	public void fetch() {
		Document currentIssueDoc = IOUtils.parseWebPageMinusComments(currentIssueUrl);
		sleep();
		Element volumesTable = getVolumesTable(currentIssueDoc);
		List<String> volumeUrls = getVolumeUrls(volumesTable);
		volumeUrls.add(0,currentIssueUrl);
		for (String volumeUrl : volumeUrls) {
			Document volumeToc = IOUtils.parseWebPageMinusComments(volumeUrl);
			sleep();
			List<String> issueUrls = getIssueUrls(volumeToc);
			issueUrls.add(0,volumeUrl);
			for (String issueUrl : issueUrls) {
				Document issueDoc = IOUtils.parseWebPageMinusComments(issueUrl);
				sleep();
				YearAndIssue yi = getYearAndIssue(issueDoc);
				if (alreadyFinishedIssue(yi)) {
					System.out.println("already finished: "+yi.getYear()+", "+yi.getIssue());
					continue;
				}
				System.out.println("Downloading from year "+yi.getYear()+", issue "+yi.getIssue());
				List<String> fullTextUrls = getFullTextUrls(issueDoc);
				for (String fullTextUrl : fullTextUrls) {
					Document articleDoc = IOUtils.parseWebPage(fullTextUrl);					
					sleep();
					String doi = getDoi(articleDoc);				
					File doiFile = new File(doi);
					String doiName = doiFile.getName().replaceAll("\\.", "-");
					Nodes nodes = articleDoc.query(".//x:p[following-sibling::x:p[contains(.,'Crystal structure. Crystallographic data.')]]//x:a[contains(@href,'.zip') and contains(.,'.zip')]", X_XHTML);
					if (nodes.size() > 0) {
						for (int i = 0; i < nodes.size(); i++) {
							String zipUrl = ((Element)nodes.get(i)).getAttributeValue("href");

							String outFolder = writeDir+File.separator+publisherAbbreviation+
							File.separator+journalAbbreviation+File.separator+yi.getYear()+File.separator+
							yi.getIssue()+File.separator+doiName;
							File outFile = new File(outFolder);
							if (!outFile.exists()) {
								outFile.mkdirs();
							}
							String filename = outFolder+File.separator+doiName+"-"+String.valueOf(i+1)+".zip";
							IOUtils.saveFileFromUrl(zipUrl, filename);
							System.out.println("Writing zip file with DOI: "+doi);
						}
					}
				}
				updateLog(yi);
			}
		}
	}

	public void updateLog(YearAndIssue yi) {
		Nodes issueNodes = logDoc.query("./log/publisher[@id='"+publisherAbbreviation+
				"']/journal[@id='"+journalAbbreviation+"']/year[@id='"+yi.getYear()+"']/issue[@id='"+yi.getIssue()+"']");
		if (issueNodes.size() == 1) {
			Element issueNode = (Element)issueNodes.get(0);
			issueNode.getAttribute("finished").setValue("true");
		} else {
			throw new RuntimeException("Should have found 1 issueNode, found "+issueNodes.size());
		}
		IOUtils.writePrettyXML(logDoc, logPath);
	}

	public boolean alreadyFinishedIssue(YearAndIssue yi) {
		Nodes journalNodes = logDoc.query("./log/publisher[@id='"+publisherAbbreviation+"']/journal[@id='"+journalAbbreviation+"']");
		if (journalNodes.size() == 1) {
			Element journalNode = (Element)journalNodes.get(0);
			Nodes yearNodes = journalNode.query("./year[@id='"+yi.getYear()+"']");
			if (yearNodes.size() == 0) {
				Element yearNode = new Element("year");
				yearNode.addAttribute(new Attribute("id", yi.getYear()));
				journalNode.appendChild(yearNode);
				Element issueNode = new Element("issue");
				yearNode.appendChild(issueNode);
				issueNode.addAttribute(new Attribute("id", yi.getIssue()));
				issueNode.addAttribute(new Attribute("finished", "false"));
				return false;
			} else if (yearNodes.size() == 1) {
				Element yearNode = (Element)yearNodes.get(0);
				Nodes issueNodes = yearNode.query("./issue[@id='"+yi.getIssue()+"']");
				if (issueNodes.size() == 0) {
					Element issueNode = new Element("issue");
					yearNode.appendChild(issueNode);
					issueNode.addAttribute(new Attribute("id", yi.getIssue()));
					issueNode.addAttribute(new Attribute("finished", "false"));
					return false;
				} else if (issueNodes.size() == 1) {
					Element issueNode = (Element)issueNodes.get(0);
					String value = issueNode.getAttributeValue("finished");
					if ("false".equals(value)) {
						return false;
					} else if ("true".equals(value)) {
						return true;
					} else {
						throw new RuntimeException("Invalid issue 'finished' value: "+value);
					}
				} else {
					throw new RuntimeException("Should have found 0 or 1 issue nodes, found "+issueNodes.size());
				}
			} else {
				throw new RuntimeException("Should have found 0 or 1 year nodes, found "+yearNodes.size());
			}
		} else {
			throw new RuntimeException("Should have found 0 or 1 journal nodes, found "+journalNodes.size());
		}
	}

	public String getDoi(Document articleDoc) {
		Nodes doiNodes = articleDoc.query(".//x:a[contains(@href,'http://dx.doi.org')]", X_XHTML);
		if (doiNodes.size() == 1) {
			Element doiNode = (Element)doiNodes.get(0);
			return doiNode.getAttributeValue("href");
		} else {
			throw new RuntimeException("Expected to find 1 doi node, found "+doiNodes.size()) ;
		}
	}

	public List<String> getFullTextUrls(Document issueDoc) {
		List<String> fullTextUrls = new ArrayList<String>();
		Nodes fullTextLinkNodes = issueDoc.query(".//x:a[contains(.,'Full Text + Links')]", X_XHTML);
		for (int i = 0; i < fullTextLinkNodes.size(); i++) {
			Element fullTextLinkNode = (Element)fullTextLinkNodes.get(i);
			String link = fullTextLinkNode.getAttributeValue("href");
			fullTextUrls.add(link);
		}
		return fullTextUrls;
	}

	public YearAndIssue getYearAndIssue(Document issueDoc) {
		Nodes titleNodes = issueDoc.query(".//x:title", X_XHTML);
		if (titleNodes.size() == 1) {
			String title = titleNodes.get(0).getValue();
			System.out.println(title);
			Pattern p = Pattern.compile("[^,]+,\\s+Volume\\s+(\\d+)\\s*,\\s+Issue[s]?\\s+([\\d-]+).*");
			Matcher m = p.matcher(title);
			if (m.find()) {
				if (m.groupCount() == 2) {
					String year = m.group(1);
					String issue = m.group(2);
					return new YearAndIssue(year, issue);
				} else {
					throw new RuntimeException("Could not find year and issue information in page HTML.");
				}
			} else {
				throw new RuntimeException("Could not find year and issue information in page HTML.");
			}
		} else {
			throw new RuntimeException("Should have found one title node in issue HTML, found "+titleNodes.size());
		}
	}

	public List<String> getIssueUrls(Document volumeToc) {
		List<String> issueUrls = new ArrayList<String>();
		Nodes issueUrlNodes = volumeToc.query(".//x:a[contains(.,'Volume') and contains(.,'Issue')]", X_XHTML);
		for (int i = 0; i < issueUrlNodes.size(); i++)  {
			Element issueUrlNode = (Element)issueUrlNodes.get(i);
			String suffix = issueUrlNode.getAttributeValue("href");
			issueUrls.add(siteUrl+suffix);
		}
		return issueUrls;
	}

	public List<String> getVolumeUrls(Element volumesTable) {
		Nodes linkNodes = volumesTable.query(".//x:a[not(contains(@href,'ctockey'))]", X_XHTML);
		List<String> urls = new ArrayList<String>();
		for (int i = 0; i < linkNodes.size(); i++) {
			Element linkNode = (Element)linkNodes.get(i);
			String text = linkNode.getValue();
			Pattern p = Pattern.compile("\\s*Volume\\s+\\d+\\s+\\([^\\)]*\\)\\s*");
			Matcher m = p.matcher(text);
			if (m.find()) {
				String href = linkNode.getAttributeValue("href");
				urls.add(siteUrl+href);
			}
		}
		return urls;
	}

	public Element getVolumesTable(Document doc) {
		Nodes volumesTableNodes = doc.query(".//x:table[@class='pubBody']", X_XHTML);
		if (volumesTableNodes.size() == 5) {
			return (Element)volumesTableNodes.get(2);
		} else {
			throw new RuntimeException("Current issue page HTML has changed.  Fetcher no longer works.");
		}
	}

	protected void sleep() {
		int maxTime = Integer.valueOf(sleepMax);
		try {
			Thread.sleep(((int)(maxTime*Math.random())));
		} catch (InterruptedException e) {
			System.err.println("Sleep interrupted.");
		}
	}

	public class YearAndIssue {

		private String year;
		private String issue;

		private YearAndIssue(String year, String issue) {
			this.year = year;
			this.issue = issue;
		}

		public String getYear() {
			return year;
		}

		public String getIssue() {
			return issue;
		}
	}

	public static void main(String[] args) {
		String writeDir = "e:/tet-scraping";
		String logPath = "e:/tet-scraping/docs/poly-download-log.xml";
		PolyhedronBacklog tet = new PolyhedronBacklog(writeDir, logPath);
		tet.fetch();
	}
}
