package uk.ac.cam.ch.crystaleye.fetch;

import static uk.ac.cam.ch.crystaleye.CrystalEyeConstants.X_XHTML;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;
import uk.ac.cam.ch.crystaleye.CrystalEyeRuntimeException;
import uk.ac.cam.ch.crystaleye.IOUtils;
import uk.ac.cam.ch.crystaleye.IssueDate;

public class AcsCurrent extends CurrentIssueFetcher {
	
	private static final String PUBLISHER_ABBR = "acs";

	public AcsCurrent() {
		publisherAbbr = PUBLISHER_ABBR;
	}

	protected IssueDate getCurrentIssueId() {
		String url = "http://pubs3.acs.org/acs/journals/toc.page?incoden="+journalAbbr;
		// get current issue page as a DOM
		Document doc = IOUtils.parseWebPage(url);
		// query that went with first and second patterns
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

	protected void fetch(File issueWriteDir, String year,  String issueNum) throws IOException {
		String url = "http://pubs.acs.org/journals/"+journalAbbr+"/index.html";
		Document doc = IOUtils.parseWebPage(url);
		Nodes suppLinks = doc.query("//x:a[contains(text(),'Supporting')]", X_XHTML);
		sleep();

		if (suppLinks.size() > 0) {
			for (int j = 0; j < suppLinks.size(); j++) {
				String suppUrl = ((Element)suppLinks.get(j)).getAttributeValue("href");
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
						
						String doi = null;
						String title = null;
						Nodes doiAnchors = doc.query("//x:a[contains(@href,'dx.doi.org')]", X_XHTML);
						if (doiAnchors.size() > 0) {
							Element doiAnchor = (Element)doiAnchors.get(0);
							doi = doiAnchor.getValue();
							Element parent = (Element)doiAnchor.getParent();
							Nodes titleNodes = parent.query("./x:span[1]", X_XHTML);
							if (titleNodes.size() > 0) {
								title = ((Element)titleNodes.get(0)).getValue();
							}
						}
						
						URL cifURL = new URL(cifUrl);
						writeFiles(issueWriteDir, cifId, suppNum, cifURL, doi, title);
						sleep();
					}
				}
			}
		}
		System.out.println("FINISHED FETCHING CIFS FROM "+url);
	}
}
