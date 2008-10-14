package wwmm.crystaleye.fetch;

import static wwmm.crystaleye.CrystalEyeConstants.X_XHTML;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Node;
import nu.xom.Nodes;
import wwmm.crystaleye.CrystalEyeRuntimeException;
import wwmm.crystaleye.IOUtils;
import wwmm.crystaleye.IssueDate;

public class ChemSocJapanCurrent extends CurrentIssueFetcher {

	private static final String SITE_PREFIX = "http://www.jstage.jst.go.jp";
	private static final String PUBLISHER_ABBR = "chemSocJapan";

	public ChemSocJapanCurrent() {
		publisherAbbr = PUBLISHER_ABBR;
	}

	protected IssueDate getCurrentIssueId() {
		String url = "http://www.csj.jp/journals/"+journalAbbr+"/cl-cont/newissue.html";
		// get current issue page as a DOM
		Document doc = IOUtils.parseWebPageMinusComments(url);
		Nodes journalInfo = doc.query("//x:span[@class='augr']", X_XHTML);
		if (journalInfo.size() != 0) {
			String info = journalInfo.get(0).getValue();
			Pattern pattern = Pattern.compile("[^,]*,\\s+\\w+\\.\\s+(\\d+)\\s+\\([^,]*,\\s+(\\d\\d\\d\\d)\\)");
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

	protected void fetch(File issueWriteDir, String year, String issue) throws IOException {
		String url = "http://www.csj.jp/journals/"+journalAbbr+"/cl-cont/newissue.html";
		Document doc = IOUtils.parseWebPageMinusComments(url);
		Nodes abstractPageLinks = doc.query("//x:a[contains(text(),'Supporting Information')]", X_XHTML);
		sleep();
		if (abstractPageLinks.size() > 0) {
			for (int i = 0; i < abstractPageLinks.size(); i++) {
				String abstractPageLink = ((Element)abstractPageLinks.get(i)).getAttributeValue("href");
				Document abstractPage = IOUtils.parseWebPage(abstractPageLink);
				Nodes suppPageLinks = abstractPage.query("//x:a[contains(text(),'Supplementary Materials')]", X_XHTML);
				sleep();
				if (suppPageLinks.size() > 0) {
					String suppPageUrl = SITE_PREFIX+((Element)suppPageLinks.get(0)).getAttributeValue("href");
					System.out.println(suppPageUrl);
					Document suppPage = IOUtils.parseWebPage(suppPageUrl);
					Nodes crystRows = suppPage.query("//x:tr[x:td[contains(text(),'cif')]] | //x:tr[x:td[contains(text(),'CIF')]]", X_XHTML);
					System.out.println("crystrows: "+crystRows.size());
					sleep();
					if (crystRows.size() > 0) {
						for (int j = 0; j < crystRows.size(); j++) {
							Node crystRow = crystRows.get(j);
							Nodes cifLinks = crystRow.query(".//x:a[contains(@href,'appendix')]", X_XHTML);
							if (cifLinks.size() > 0) {
								String cifUrl = SITE_PREFIX+((Element)cifLinks.get(0)).getAttributeValue("href");
								BufferedReader reader = null;
								try {
									String cifId = new File(suppPageUrl).getParentFile().getName().replaceAll("_", "-");
									int suppNum = j+1;
									
									Nodes doiElements = abstractPage.query("//*[contains(text(),'doi:10.1246')]", X_XHTML);
									String doi = null;
									if (doiElements.size() > 0) {
										doi = ((Element)doiElements.get(0)).getValue().substring(4).trim();
									}
									Nodes titleNodes = abstractPage.query(".//x:font[@size='+1']", X_XHTML);
									String title = null;
									if (titleNodes.size() > 0) {
										title = titleNodes.get(0).getValue();
									}
									
									URL cifURL = new URL(cifUrl);
									writeFiles(issueWriteDir, cifId, suppNum, cifURL, doi, title);
									sleep();
								} finally {
									try {
										if (reader != null) reader.close();
									}
									catch (IOException ex){
										System.err.println("Cannot close reader: " + reader);
									}
								}
							}
						}
					}
				}
			}
		}
		System.out.println("FINISHED FETCHING CIFS FROM "+url);
	}

}
