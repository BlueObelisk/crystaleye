package uk.ac.cam.ch.crystaleye.fetch;

import static uk.ac.cam.ch.crystaleye.CrystalEyeConstants.RSC_DOI_PREFIX;
import static uk.ac.cam.ch.crystaleye.CrystalEyeConstants.X_XHTML;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;
import uk.ac.cam.ch.crystaleye.CrystalEyeRuntimeException;
import uk.ac.cam.ch.crystaleye.IOUtils;
import uk.ac.cam.ch.crystaleye.IssueDate;

public class RscCurrent extends CurrentIssueFetcher {

	private static final String SITE_PREFIX = "http://www.rsc.org";
	private static final String PUBLISHER_ABBREVIATION = "rsc";

	public RscCurrent() {
		publisherAbbr = PUBLISHER_ABBREVIATION;
	}

	protected IssueDate getCurrentIssueId() {
		String url = "http://rsc.org/Publishing/Journals/"
				+ journalAbbr.toUpperCase() + "/Article.asp?Type=CurrentIssue";
		Document doc = IOUtils.parseWebPageMinusComments(url);
		// old version of current version xpath
		// Nodes journalInfo =
		// doc.query("//x:img[contains(@src,'current_issue.gif')]/parent::x:p/text()[3]",
		// XHTML);
		Nodes journalInfo = doc.query("//x:h3[contains(text(),'Contents')]",
				X_XHTML);
		if (journalInfo.size() != 0) {
			String info = journalInfo.get(0).getValue().trim();
			Pattern pattern = Pattern.compile("[^\\d]*(\\d+),[^\\d]*(\\d+)$");
			Matcher matcher = pattern.matcher(info);
			if (!matcher.find()) {
				throw new CrystalEyeRuntimeException(
						"Could not extract the year/issue information from the 'current-issue' page "
								+ url);
			} else {
				String issueNum = matcher.group(1);
				String year = matcher.group(2);
				return new IssueDate(year, issueNum);
			}
		} else {
			throw new CrystalEyeRuntimeException(
					"Could not find the year/issue information from the 'current-issue' page "
							+ url);
		}
	}

	protected void fetch(File issueWriteDir, String year, String issue)
			throws IOException {
		String url = "http://rsc.org/Publishing/Journals/" + journalAbbr
				+ "/Article.asp?Type=CurrentIssue";
		Document doc = IOUtils.parseWebPageMinusComments(url);
		Nodes articleLinks = doc
				.query(
						"//x:a[contains(@href,'/Publishing/Journals/"
								+ journalAbbr.toUpperCase()
								+ "/article.asp?doi=') and preceding-sibling::x:strong[contains(text(),'DOI:')]]",
						X_XHTML);
		sleep();
		if (articleLinks.size() > 0) {
			for (int i = 0; i < articleLinks.size(); i++) {
				String articleUrl = SITE_PREFIX
						+ ((Element) articleLinks.get(i))
								.getAttributeValue("href");
				doc = IOUtils.parseWebPageMinusComments(articleUrl);
				
				String title = null;
				Nodes titleNodes = doc.query(".//x:span[@style='font-size:150%;']", X_XHTML);
				if (titleNodes.size() > 0) {
					title = titleNodes.get(0).getValue();
				}
				
				sleep();
				Nodes suppLinks = doc
						.query(
								"//x:a[contains(text(),'Electronic supplementary information')]",
								X_XHTML);
				System.out.println(articleUrl);
				System.out.println("supplinks: " + suppLinks.size());
				if (suppLinks.size() > 0) {
					for (int j = 0; j < suppLinks.size(); j++) {
						String link = ((Element) suppLinks.get(j))
								.getAttributeValue("href");
						String suppUrl = SITE_PREFIX + link;
						doc = IOUtils.parseWebPageMinusComments(suppUrl);
						sleep();
						Nodes cifLinks = doc
								.query(
										"//x:a[text()='Crystal structure data'] | //x:a[text()='Crystal Structure Data'] | //x:a[text()='Crystal Structure data'] | //x:a[text()='Crystal data'] | //x:a[text()='Crystal Data'] | //x:a[text()='Crystallographic Data'] | //x:a[text()='Crystallographic data']",
										X_XHTML);
						if (cifLinks.size() > 0) {
							for (int k = 0; k < cifLinks.size(); k++) {
								String cifFileName = ((Element) cifLinks.get(k))
										.getAttributeValue("href");
								String urlMiddle = link.substring(0, link
										.lastIndexOf("/"));
								String cifUrl = SITE_PREFIX + urlMiddle + "/"
										+ cifFileName;
								String cifId = cifFileName.substring(0,
										cifFileName.lastIndexOf("."));
								int suppNum = k + 1;
								writeFiles(downloadDir, cifId, suppNum,
										new URL(cifUrl), RSC_DOI_PREFIX + "/"
												+ cifId, title);
								sleep();
							}
						}
					}
				}
			}
		}
		System.out.println("FINISHED FETCHING CIFS FROM " + url);
	}

	public static void main(String[] args) {
		try {
			RscCurrent rsc = new RscCurrent();
			Properties props = IOUtils
					.loadProperties("E:\\data-test\\docs\\cif-flow-props.txt");
			rsc.setDownloadDir(new File(props.getProperty("write.dir")));
			rsc.fetchAll();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
