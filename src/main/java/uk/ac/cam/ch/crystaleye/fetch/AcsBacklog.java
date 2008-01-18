package uk.ac.cam.ch.crystaleye.fetch;

import static uk.ac.cam.ch.crystaleye.CrystalEyeConstants.X_XHTML;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;
import uk.ac.cam.ch.crystaleye.CrystalEyeRuntimeException;
import uk.ac.cam.ch.crystaleye.IOUtils;

public class AcsBacklog extends JournalFetcher {

	private static final String PUBLISHER_ABBREVIATION = "acs";

	private String journalAbbreviation;
	private String year;
	private String issue;
	private String decade;
	private String volume = "0";

	public AcsBacklog(String journalAbbreviation, String year, String issue) {
		this.publisherAbbr = PUBLISHER_ABBREVIATION;
		setYear(year);
		if (year.length() != 4) {
			throw new CrystalEyeRuntimeException(
					"Year supplied must be of form YYYY (e.g. 2007)");
		}
		setJournalAbbreviation(journalAbbreviation);
		setIssue(issue);
		setDecade(year.substring(2, 3));
	}

	public void setYear(String year) {
		this.year = year;
	}

	public void setDecade(String decade) {
		this.decade = decade;
	}

	public void setIssue(String issue) {
		this.issue = issue;
	}

	public void setJournalAbbreviation(String journalAbbreviation) {
		this.journalAbbreviation = journalAbbreviation;
		if ("cgdefu".equalsIgnoreCase(journalAbbreviation)) {
			volume = String.valueOf((Integer.parseInt(year) - 2000));
		} else if ("inocaj".equalsIgnoreCase(journalAbbreviation)) {
			volume = String.valueOf((Integer.parseInt(year) - 1961));
		} else if ("jacsat".equalsIgnoreCase(journalAbbreviation)) {
			volume = String.valueOf((Integer.parseInt(year) - 1878));
		} else if ("jnprdf".equalsIgnoreCase(journalAbbreviation)) {
			volume = String.valueOf((Integer.parseInt(year) - 1937));
		} else if ("joceah".equalsIgnoreCase(journalAbbreviation)) {
			volume = String.valueOf((Integer.parseInt(year) - 1935));
		} else if ("orlef7".equalsIgnoreCase(journalAbbreviation)) {
			volume = String.valueOf((Integer.parseInt(year) - 1998));
		} else if ("orgnd7".equalsIgnoreCase(journalAbbreviation)) {
			volume = String.valueOf((Integer.parseInt(year) - 1981));
		}
	}

	public void fetchAll() throws IOException {
		String issueWriteDir = downloadDir.getCanonicalPath() + File.separator
				+ PUBLISHER_ABBREVIATION + File.separator + journalAbbreviation
				+ File.separator + year + File.separator + issue;
		String url = "http://pubs3.acs.org/acs/journals/toc.page?incoden="
				+ journalAbbreviation.toLowerCase() + "&indecade=" + decade
				+ "&involume=" + volume + "&inissue=" + issue;
		System.out.println("fetching url: " + url);
		Document doc = IOUtils.parseWebPage(url);
		Nodes suppLinks = doc.query("//x:a[contains(text(),'Supporting')]",
				X_XHTML);
		sleep();

		if (suppLinks.size() > 0) {
			for (int j = 0; j < suppLinks.size(); j++) {
				String suppUrl = ((Element) suppLinks.get(j))
						.getAttributeValue("href");
				doc = IOUtils.parseWebPage(suppUrl);
				System.out.println("fetching: " + suppUrl);
				sleep();
				Nodes cifLinks = doc.query(".//x:a[contains(@href,'.cif')]",
						X_XHTML);
				System.out.println("cifs: " + cifLinks.size());
				if (cifLinks.size() > 0) {
					String cifId = "";
					for (int k = 0; k < cifLinks.size(); k++) {
						String cifUrl = ((Element) cifLinks.get(k))
								.getAttributeValue("href");
						int idx = cifUrl.lastIndexOf("/");
						cifId = cifUrl.substring(0, idx);
						idx = cifId.lastIndexOf("/");
						cifId = cifId.substring(idx + 1);
						int suppNum = k + 1;
						cifUrl = cifUrl.replaceAll("pubs\\.acs\\.org/",
								"pubs\\.acs\\.org//");
						String response = IOUtils.fetchWebPage(cifUrl);
						IOUtils.writeText(response, issueWriteDir
								+ File.separator + cifId + File.separator
								+ cifId + "sup" + suppNum + ".cif");
						sleep();
					}
					Nodes doiAnchors = doc.query(
							"//x:a[contains(@href,'dx.doi.org')]", X_XHTML);
					if (doiAnchors.size() > 0) {
						String doi = ((Element) doiAnchors.get(0)).getValue();
						IOUtils.writeText(doi, issueWriteDir + File.separator
								+ cifId + File.separator + cifId + ".doi");
					}
				}
			}
		}
		System.out.println("FINISHED FETCHING CIFS FROM " + url);
	}

	public static void main(String[] args) {
		try {
			// this line just to initialise
			AcsBacklog ab = new AcsBacklog("cgdefu", "2006", "12");
			Properties props = new Properties();
			props.load(new FileInputStream(
					"e:/data-test2/docs/cif-flow-props.txt"));
			for (int i = 12; i < 13; i++) {
				ab = new AcsBacklog("orlef7", "2007", String.valueOf(i));
				ab.setDownloadDir(new File(props.getProperty("write.dir")));
				ab.fetchAll();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
