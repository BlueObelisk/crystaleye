package wwmm.crystaleye.fetch;

import static wwmm.crystaleye.CrystalEyeConstants.X_XHTML;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;

import org.apache.log4j.Logger;

import wwmm.crystaleye.IOUtils;
import wwmm.crystaleye.IssueDate;
import wwmm.crystaleye.Utils;
import wwmm.crystaleye.tools.Unzip;

public class ElsevierCurrent extends CurrentIssueFetcher {
	
	private static final Logger LOG = Logger.getLogger(ElsevierCurrent.class);

	private static final String SITE_PREFIX = "http://www.sciencedirect.com";
	private static final String publisherAbbreviation = "elsevier";
	
	private String currentIssueUrl;

	public ElsevierCurrent(File propertiesFile) {
		super(publisherAbbreviation, propertiesFile);
	}

	public ElsevierCurrent(String propertiesFile) {
		this(new File(propertiesFile));
	}

	protected IssueDate getCurrentIssueId(String journalAbbreviation) {
		currentIssueUrl = SITE_PREFIX+"/science/journal/";
		if ("polyhedron".equals(journalAbbreviation)) {
			currentIssueUrl += "02775387";
		} else {
			throw new RuntimeException("Unrecognised "+publisherAbbreviation+" journal: "+journalAbbreviation);
		}
		Document currentIssueDoc = IOUtils.parseWebPageMinusComments(currentIssueUrl);
		Nodes titleNodes = currentIssueDoc.query(".//x:title", X_XHTML);
		if (titleNodes.size() == 1) {
			String title = titleNodes.get(0).getValue();
			Pattern p = Pattern.compile("[^,]+,\\s+Volume\\s+(\\d+)\\s*,\\s+Issue[s]?\\s+([\\d-]+).*");
			Matcher m = p.matcher(title);
			if (m.find()) {
				if (m.groupCount() == 2) {
					String year = m.group(1);
					String issue = m.group(2);
					return new IssueDate(year, issue);
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

	protected void fetch(String issueWriteDir, String journalAbbreviation, String year, String issue) {
		Document currentIssueDoc = IOUtils.parseWebPageMinusComments(currentIssueUrl);
		List<String> fullTextUrls = getFullTextUrls(currentIssueDoc);
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
					String outFolder = issueWriteDir+File.separator+doiName;
					File outFile = new File(outFolder);
					if (!outFile.exists()) {
						outFile.mkdirs();
					}
					String filename = outFolder+File.separator+doiName+"-"+String.valueOf(i+1)+".zip";
					LOG.info("Writing zip file with DOI: "+doi);
					IOUtils.saveFileFromUrl(zipUrl, filename);
					
					String[] args = new String[1];
					args[0] = filename;
					Unzip.main(args);
					File parent = new File(filename).getParentFile();
					int cifCount = 0;
					for (File file : parent.listFiles()) {
						if (file.getAbsolutePath().endsWith(".cif") || file.getAbsolutePath().endsWith(".CIF")) {
							cifCount++;
							String cif = Utils.file2String(file.getAbsolutePath());
							writeFiles(issueWriteDir, parent.getName(), cifCount, cif, doi);							
						}
					}
				}
			}
		}
		
		LOG.info("FINISHED FETCHING CIFS FROM "+currentIssueUrl);
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

	public static void main(String[] args) {
		ElsevierCurrent els = new ElsevierCurrent("e:/data-test/docs/cif-flow-props.txt");
		els.execute();
	}
}
