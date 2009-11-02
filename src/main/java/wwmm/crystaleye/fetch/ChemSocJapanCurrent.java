package wwmm.crystaleye.fetch;

import static wwmm.crystaleye.CrystalEyeConstants.X_XHTML;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Node;
import nu.xom.Nodes;

import org.apache.log4j.Logger;

import wwmm.crystaleye.IssueDate;
import wwmm.crystaleye.WebUtils;

public class ChemSocJapanCurrent extends CurrentIssueFetcher {
	
	private static final Logger LOG = Logger.getLogger(ChemSocJapanCurrent.class);

	private static final String SITE_PREFIX = "http://www.jstage.jst.go.jp";
	private static final String publisherAbbreviation = "chemSocJapan";

	public ChemSocJapanCurrent(File propertiesFile) {
		super(publisherAbbreviation, propertiesFile);
	}

	public ChemSocJapanCurrent(String propertiesFile) {
		this(new File(propertiesFile));
	}

	protected IssueDate getCurrentIssueId(String journalAbbreviation) {
		String url = "http://www.csj.jp/journals/"+journalAbbreviation+"/cl-cont/newissue.html";
		// get current issue page as a DOM
		Document doc = WebUtils.parseWebPageAndRemoveComments(url);
		Nodes journalInfo = doc.query("//x:span[@class='augr']", X_XHTML);
		if (journalInfo.size() != 0) {
			String info = journalInfo.get(0).getValue();
			Pattern pattern = Pattern.compile("[^,]*,\\s+\\w+\\.\\s+(\\d+)\\s+\\([^,]*,\\s+(\\d\\d\\d\\d)\\)");
			Matcher matcher = pattern.matcher(info);
			if (!matcher.find()) {
				throw new RuntimeException("Could not extract the year/issue information from the 'current-issue' page "+url);
			} else {
				String year = matcher.group(2);
				String issueNum = matcher.group(1);
				return new IssueDate(year, issueNum);
			}
		} else {
			throw new RuntimeException("Could not find the year/issue information from the 'current-issue' page "+url);
		}
	}

	protected void fetch(String issueWriteDir, String journalAbbreviation, String year, String issue) {
		String url = "http://www.csj.jp/journals/"+journalAbbreviation+"/cl-cont/newissue.html";
		Document doc = WebUtils.parseWebPageAndRemoveComments(url);
		Nodes abstractPageLinks = doc.query("//x:a[contains(text(),'Supporting Information')]", X_XHTML);
		sleep();
		if (abstractPageLinks.size() > 0) {
			for (int i = 0; i < abstractPageLinks.size(); i++) {
				String abstractPageLink = ((Element)abstractPageLinks.get(i)).getAttributeValue("href");
				Document abstractPage = WebUtils.parseWebPage(abstractPageLink);
				Nodes suppPageLinks = abstractPage.query("//x:a[contains(text(),'Supplementary Materials')]", X_XHTML);
				sleep();
				if (suppPageLinks.size() > 0) {
					String suppPageUrl = SITE_PREFIX+((Element)suppPageLinks.get(0)).getAttributeValue("href");
					Document suppPage = WebUtils.parseWebPage(suppPageUrl);
					Nodes crystRows = suppPage.query("//x:tr[x:td[contains(text(),'cif')]] | //x:tr[x:td[contains(text(),'CIF')]]", X_XHTML);
					sleep();
					if (crystRows.size() > 0) {
						for (int j = 0; j < crystRows.size(); j++) {
							Node crystRow = crystRows.get(j);
							Nodes cifLinks = crystRow.query(".//x:a[contains(@href,'appendix')]", X_XHTML);
							if (cifLinks.size() > 0) {
								String cifLink = SITE_PREFIX+((Element)cifLinks.get(0)).getAttributeValue("href");
								BufferedReader reader = null;
								try {
									String cif = getWebPage(cifLink);
									String cifId = new File(suppPageUrl).getParentFile().getName().replaceAll("_", "-");
									Nodes doiElements = abstractPage.query("//*[contains(text(),'doi:10.1246')]", X_XHTML);
									int suppNum = j+1;
									String doi = null;
									if (doiElements.size() > 0) {
										doi = ((Element)doiElements.get(0)).getValue().substring(4).trim();
									}
									writeFiles(issueWriteDir, cifId, suppNum, cif, doi);
									sleep();
								} finally {
									try {
										if (reader != null) reader.close();
									}
									catch (IOException ex){
										LOG.warn("Cannot close reader: " + reader);
									}
								}
							}
						}
					}
				}
			}
		}
		LOG.info("FINISHED FETCHING CIFS FROM "+url);
	}

	public static void main(String[] args) {
		ChemSocJapanCurrent jap = new ChemSocJapanCurrent("e:/crystaleye-new/docs/cif-flow-props.txt");
		jap.execute();
	}
}
