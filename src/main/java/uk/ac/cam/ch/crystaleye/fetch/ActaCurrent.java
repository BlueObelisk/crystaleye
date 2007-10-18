package uk.ac.cam.ch.crystaleye.fetch;

import static uk.ac.cam.ch.crystaleye.CrystalEyeConstants.X_XHTML;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Node;
import nu.xom.Nodes;
import uk.ac.cam.ch.crystaleye.CrystalEyeRuntimeException;
import uk.ac.cam.ch.crystaleye.IOUtils;
import uk.ac.cam.ch.crystaleye.IssueDate;

public class ActaCurrent extends CurrentIssueFetcher {

	private static final String SITE_PREFIX = "http://journals.iucr.org";
	private static final String publisherAbbreviation = "acta";

	public ActaCurrent(File propertiesFile) {
		super(publisherAbbreviation, propertiesFile);
	}

	public ActaCurrent(String propertiesFile) {
		this(new File(propertiesFile));
	}

	protected IssueDate getCurrentIssueId(String journalAbbreviation) {
		String url = "http://journals.iucr.org/"+journalAbbreviation+"/contents/backissuesbdy.html";
		// get current issue page as a DOM
		Document doc = IOUtils.parseWebPage(url);
		Nodes currentIssueLink = doc.query("//x:a[contains(@target,'_parent')]", X_XHTML);
		if (currentIssueLink.size() != 0) {
			Node current = currentIssueLink.get(0);
			if (((Element)current).getValue().contains("preparation")) {
				current = currentIssueLink.get(1);
			}
			String info = ((Element)current).getAttributeValue("href");
			Pattern pattern = Pattern.compile("\\.\\./issues/(\\d\\d\\d\\d)/(\\d\\d/\\d\\d)/issconts.html");
			Matcher matcher = pattern.matcher(info);
			if (!matcher.find()) {
				throw new CrystalEyeRuntimeException("Could not extract the year/issue information from the 'current-issue' page "+url);
			} else {
				String year = matcher.group(1);
				String issueNum = matcher.group(2).replaceAll("/", "-");
				return new IssueDate(year, issueNum);
			}
		} else {
			throw new CrystalEyeRuntimeException("Could not find the year/issue information from the 'current-issue' page "+url);
		}
	}

	protected void fetch(String issueWriteDir, String journalAbbreviation, String year, String issueNum) {
		Pattern pattern = Pattern.compile("http://scripts.iucr.org/cgi-bin/sendcif\\?(.*)");
		String url = "http://journals.iucr.org/"+journalAbbreviation+"/issues/"+year+"/"+issueNum.replaceAll("-", "/")+"/isscontsbdy.html";
		Document doc = IOUtils.parseWebPage(url);
		Nodes tocEntries = doc.query("//x:div[@class='toc entry']", X_XHTML);
		sleep();
		if (tocEntries.size() > 0) {
			for (int i = 0; i < tocEntries.size(); i++) {
				Node tocEntry = tocEntries.get(i);
				Nodes cifLinks = tocEntry.query(".//x:img[contains(@src,'"+journalAbbreviation+"/graphics/cifborder.gif')]/parent::x:*", X_XHTML);
				if (cifLinks.size() > 0) {
					String cifId = "";
					for (int j = 0; j < cifLinks.size(); j++) {
						Node cifLink = cifLinks.get(j);
						String cifUrl = ((Element)cifLink).getAttributeValue("href");
						Matcher matcher = pattern.matcher(cifUrl);
						if (matcher.find()) {
							cifId = matcher.group(1);
							cifId = cifId.replaceAll("sup[\\d]*", "");
						} else {
							throw new CrystalEyeRuntimeException("Could not find the CIF ID.");
						}
						String cif = getWebPage(cifUrl);
						String doi = null;

						if (j == 0) {
							Nodes doiNodes = tocEntry.query(".//x:p/x:font[@size='2']", X_XHTML);
							if (doiNodes.size() > 0) {
								doi = ((Element)doiNodes.get(0)).getValue().substring(4);
							} else {
								System.err.println("Could not find the DOI for this toc entry.");
							}
							Nodes checkCifNodes = tocEntry.query(".//x:img[contains(@src,'/"+journalAbbreviation+"/graphics/checkcifborder.gif')]/parent::x:*", X_XHTML);
							if (checkCifNodes.size() > 0) {
								for (int k = 0; k < checkCifNodes.size(); k++) {
									Node checkCifLink = checkCifNodes.get(k);
									String checkCifUrl = ((Element)checkCifLink).getAttributeValue("href");
									String checkcif = getWebPage(SITE_PREFIX+checkCifUrl);
									IOUtils.writeText(checkcif, issueWriteDir+File.separator+cifId+File.separator+cifId+"sup"+(+1)+".deposited.checkcif.html");
									sleep();
								}
							}
						}

						writeFiles(issueWriteDir, cifId, j+1, cif, doi);
						sleep();
					}
				}
			}
		}
		System.out.println("FINISHED FETCHING CIFS FROM "+url);
	}

	public static void main(String[] args) {
		ActaCurrent acta = new ActaCurrent("e:/data-test/docs/cif-flow-props.txt");
		acta.execute();
	}
}
