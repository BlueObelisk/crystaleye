package wwmm.crystaleye.fetch;

import static wwmm.crystaleye.CrystalEyeConstants.X_XHTML;

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
import wwmm.crystaleye.IssueDate;
import wwmm.crystaleye.util.HttpUtils;
import wwmm.crystaleye.util.XmlIOUtils;

public class ActaCurrent extends CurrentIssueFetcher {

	private static final String SITE_PREFIX = "http://journals.iucr.org";
	private static final String PUBLISHER_ABBR = "acta";

	public ActaCurrent() {
		publisherAbbr = PUBLISHER_ABBR;
	}

	protected IssueDate getCurrentIssueId() {
		String url = "http://journals.iucr.org/" + journalAbbr
				+ "/contents/backissuesbdy.html";
		// get current issue page as a DOM
		Document doc = HttpUtils.getWebpageAsXML(url);
		Nodes currentIssueLink = doc.query(
				"//x:a[contains(@target,'_parent')]", X_XHTML);
		if (currentIssueLink.size() != 0) {
			Node current = currentIssueLink.get(0);
			if (((Element) current).getValue().contains("preparation")) {
				current = currentIssueLink.get(1);
			}
			String info = ((Element) current).getAttributeValue("href");
			Pattern pattern = Pattern
					.compile("\\.\\./issues/(\\d\\d\\d\\d)/(\\d\\d/\\d\\d)/issconts.html");
			Matcher matcher = pattern.matcher(info);
			if (!matcher.find()) {
				throw new CrystalEyeRuntimeException(
						"Could not extract the year/issue information from the 'current-issue' page "
								+ url);
			} else {
				String year = matcher.group(1);
				String issueNum = matcher.group(2).replaceAll("/", "-");
				return new IssueDate(year, issueNum);
			}
		} else {
			throw new CrystalEyeRuntimeException(
					"Could not find the year/issue information from the 'current-issue' page "
							+ url);
		}
	}

	/**
	 * @throws IOException 
	 * @todo me to build a list of urls to be downloaded (along with
	 *           relevant data structure) so the web page XOM can be released
	 *           before downloading CIFs.
	 */
	protected void fetch(File issueWriteDir, String year, String issueNum) throws IOException {
		Pattern pattern = Pattern
				.compile("http://scripts.iucr.org/cgi-bin/sendcif\\?(.*)");
		String url = "http://journals.iucr.org/" + journalAbbr + "/issues/"
				+ year + "/" + issueNum.replaceAll("-", "/")
				+ "/isscontsbdy.html";
		Document doc = HttpUtils.getWebpageAsXML(url);
		Nodes tocEntries = doc.query("//x:div[@class='toc entry']", X_XHTML);
		sleep();
		if (tocEntries.size() > 0) {
			for (int i = 0; i < tocEntries.size(); i++) {
				Node tocEntry = tocEntries.get(i);
				Nodes cifLinks = tocEntry.query(".//x:img[contains(@src,'"
						+ journalAbbr
						+ "/graphics/cifborder.gif')]/parent::x:*", X_XHTML);
				if (cifLinks.size() > 0) {
					String cifId = "";
					for (int j = 0; j < cifLinks.size(); j++) {
						Node cifLink = cifLinks.get(j);
						String cifUrl = ((Element) cifLink)
								.getAttributeValue("href");
						Matcher matcher = pattern.matcher(cifUrl);
						if (matcher.find()) {
							cifId = matcher.group(1);
							cifId = cifId.replaceAll("sup[\\d]*", "");
						} else {
							throw new CrystalEyeRuntimeException(
									"Could not find the CIF ID.");
						}
						String doi = null;
						String title = null;
						if (j == 0) {
							Nodes doiNodes = tocEntry.query(
									".//x:p/x:font[@size='2']", X_XHTML);
							if (doiNodes.size() > 0) {
								doi = ((Element) doiNodes.get(0)).getValue()
										.substring(4);
							} else {
								System.err
										.println("Could not find the DOI for this toc entry.");
							}
							Nodes checkCifNodes = tocEntry
									.query(
											".//x:img[contains(@src,'/"
													+ journalAbbr
													+ "/graphics/checkcifborder.gif')]/parent::x:*",
											X_XHTML);
							if (checkCifNodes.size() > 0) {
								for (int k = 0; k < checkCifNodes.size(); k++) {
									Node checkCifLink = checkCifNodes.get(k);
									String checkCifUrl = ((Element) checkCifLink)
											.getAttributeValue("href");
									String checkcif = getWebPage(SITE_PREFIX
											+ checkCifUrl);
									XmlIOUtils
											.writeText(
													checkcif,
													issueWriteDir
															+ File.separator
															+ cifId
															+ File.separator
															+ cifId
															+ "sup"
															+ (+1)
															+ ".deposited.checkcif.html");
									sleep();
								}
							}
							Nodes titleNodes = tocEntry.query("./x:h3[1]", X_XHTML);
							if (titleNodes.size() > 0) {
								title = ((Element)titleNodes.get(0)).getValue().trim();
							} else {
								System.err.println("Could not find the TITLE for this toc entry.");
							}
						}
						URL cifURL = new URL(cifUrl);
						writeFiles(issueWriteDir, cifId, j + 1, cifURL, doi, title);
						sleep();
					}
				}
			}
		}
		System.out.println("FINISHED FETCHING CIFS FROM " + url);
	}
}
