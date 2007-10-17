package uk.ac.cam.ch.crystaleye.fetch;

import static uk.ac.cam.ch.crystaleye.CrystalEyeConstants.X_XHTML;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;
import uk.ac.cam.ch.crystaleye.CrystalEyeRuntimeException;
import uk.ac.cam.ch.crystaleye.IOUtils;
import uk.ac.cam.ch.crystaleye.IssueDate;

public class AcsCurrent extends CurrentIssueFetcher {
	
	private static final String publisherAbbreviation = "acs";
	
	public AcsCurrent(File propertiesFile) {
		super(publisherAbbreviation, propertiesFile);
	}
	
	public AcsCurrent(String propertiesFile) {
		this(new File(propertiesFile));
	}

	protected IssueDate getCurrentIssueId(String journalAbbreviation) {
		// old url
		//String url = "http://pubs.acs.org/journals/"+this.journalAbbreviation+"/index.html";
		String url = "http://pubs3.acs.org/acs/journals/toc.page?incoden="+journalAbbreviation;
		// get current issue page as a DOM
		Document doc = IOUtils.parseWebPage(url);
		// query that went with first and second patterns
		//Nodes journalInfo = doc.query("//x:p/x:img[contains(@src,'current_issue.gif')]/parent::x:*/text()[3]", XHTML);
		Nodes journalInfo = doc.query(".//x:div[@id='issueinfo']", X_XHTML);
		if (journalInfo.size() != 0) {
			String info = journalInfo.get(0).getValue().trim();
			// first pattern
			// Pattern pattern = Pattern.compile("[^,]*,[^,\\d]*(\\d+),[\\s]+(\\d*)$");
			// second pattern
			// Pattern pattern = Pattern.compile("Issue\\s*(\\d+)[^,]*,\\s*(\\d+)");
			// third pattern
			Pattern pattern = Pattern.compile("\\s*Vol\\.\\s+\\d+,\\s+No\\.\\s+(\\d+):.*(\\d\\d\\d\\d)");
			Matcher matcher = pattern.matcher(info);
			if (!matcher.find()) {
				throw new CrystalEyeRuntimeException("Could not extract the year/issue information from the 'current-issue' page "+url);
			} else {
				String year = matcher.group(2);
				String issueNum = matcher.group(1);
				return new IssueDate(year, issueNum);
			}
		} else {
			throw new CrystalEyeRuntimeException("Could not find the year/issue information from the 'current-issue' page "+url);
		}
	}

	protected void fetch(String issueWriteDir, String journalAbbreviation, String year, String issueNum) {
		expectedNoCifs = 0;
		String url = "http://pubs.acs.org/journals/"+journalAbbreviation+"/index.html";
		Document doc = IOUtils.parseWebPage(url);
		Nodes suppLinks = doc.query("//x:a[contains(text(),'Supporting')]", X_XHTML);
		sleep();

		if (suppLinks.size() > 0) {
			for (int j = 0; j < suppLinks.size(); j++) {
				String suppUrl = ((Element)suppLinks.get(j)).getAttributeValue("href");
				System.out.println("fetching: "+suppUrl);
				doc = IOUtils.parseWebPage(suppUrl);
				sleep();

				Nodes cifLinks = doc.query(".//x:a[contains(@href,'.cif')]", X_XHTML);
				if (cifLinks.size() > 0) {
					for (int k = 0; k < cifLinks.size(); k++) {
						String cifUrl = ((Element)cifLinks.get(k)).getAttributeValue("href");
						int idx = cifUrl.lastIndexOf("/");
						String cifId = cifUrl.substring(0,idx);
						idx = cifId.lastIndexOf("/");
						cifId = cifId.substring(idx+1);
						int suppNum = k+1;
						cifUrl = cifUrl.replaceAll("pubs\\.acs\\.org/", "pubs\\.acs\\.org//");
						String cif = getWebPage(cifUrl);
						expectedNoCifs++;
						Nodes doiAnchors = doc.query("//x:a[contains(@href,'dx.doi.org')]", X_XHTML);
						String doi = null;
						if (doiAnchors.size() > 0) {
							doi = ((Element)doiAnchors.get(0)).getValue();
						}
						writeFiles(issueWriteDir, cifId, suppNum, cif, doi);
						sleep();
					}
				}
			}
		}
		System.out.println("FINISHED FETCHING CIFS FROM "+url);
	}

	public static void main(String[] args) {
		AcsCurrent acs = new AcsCurrent("E:\\data-test\\docs\\cif-flow-props.txt");
		acs.execute();

	}
}
