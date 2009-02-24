package uk.ac.cam.ch.crystaleye.fetch;

import static uk.ac.cam.ch.crystaleye.CrystalEyeConstants.XHTML_NS;
import static uk.ac.cam.ch.crystaleye.CrystalEyeConstants.X_XHTML;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Node;
import nu.xom.Nodes;
import uk.ac.cam.ch.crystaleye.IOUtils;

public class ActaOldLayout extends JournalFetcher {

	private static final String PUBLISHER_ABBREVIATION = "acta";

	String year;
	String issueNum;
	String issuePart;
	String journalAbbreviation;

	public ActaOldLayout(String journalAbbreviation, String year,
			String issueNum, String issuePart) {
		publisherAbbr = PUBLISHER_ABBREVIATION;
		setYear(year);
		setIssuePart(issuePart);
		setIssueNum(issueNum);
		setJournalAbbreviation(journalAbbreviation);
	}

	public void setYear(String year) {
		this.year = year;
	}

	public void setIssueNum(String issueNum) {
		this.issueNum = issueNum;
	}

	public void setIssuePart(String issuePart) {
		this.issuePart = issuePart;
	}

	public void setJournalAbbreviation(String journalAbbreviation) {
		if (journalAbbreviation.length() == 1) {
			this.journalAbbreviation = journalAbbreviation;
		} else {
			throw new IllegalArgumentException(
					"SectionLetter must be 1 character in length.");
		}
	}

	public void fetchAll() {
		String url = "http://journals.iucr.org/" + journalAbbreviation
				+ "/issues/" + year + "/" + issueNum + "/" + issuePart
				+ "/isscontsbdy.html";
		System.out.println("Fetching CIFs from " + url);
		Document doc = IOUtils.parseWebPage(url);
		List<Element> entries = getTocEntryList(doc);
		for (Element el : entries) {
			String cifLink = null;
			String doi = null;
			String cifId = null;
			boolean link = false;
			Nodes cifLinks = el
					.query(
							"//x:img[contains(@src,'graphics/cifborder.gif')]/parent::x:*",
							X_XHTML);
			if (cifLinks.size() != 0) {
				cifLink = ((Element) cifLinks.get(0)).getAttribute("href")
						.getValue().trim();
				if (cifLink != null) {
					Pattern p = Pattern
							.compile("http://scripts.iucr.org/cgi-bin/sendcif\\?(......)sup1");
					Matcher m = p.matcher(cifLink);
					if (m.find()) {
						cifId = m.group(1);
					}
					link = true;
				}
			}
			Nodes doiLinks = el.query("//x:font[contains(@size,'2')]", X_XHTML);
			if (doiLinks.size() != 0) {
				doi = ((Element) doiLinks.get(0)).getValue().trim();
			}
			if (link) {
				String cif = IOUtils.fetchWebPage(cifLink);
				String pathMinusMime = downloadDir + File.separator
						+ PUBLISHER_ABBREVIATION + File.separator
						+ journalAbbreviation + File.separator + year
						+ File.separator + issueNum + "-" + issuePart
						+ File.separator + cifId + File.separator + cifId;
				System.out.println(pathMinusMime);
				IOUtils.writeText(cif, pathMinusMime + ".cif");
				IOUtils.writeText(doi, pathMinusMime + ".doi");
			}
		}
		System.out.println("FINISHED FETCHING CIFS FROM " + url);
	}

	public List<Element> getTocEntryList(Document doc) {
		Element html = doc.getRootElement();
		Element body = html.getFirstChildElement("body", XHTML_NS);

		List<Element> tocEntryList = new ArrayList<Element>();
		Nodes entries = doc.query("//x:h3", X_XHTML);
		Integer[] h3s = new Integer[entries.size()];

		for (int i = 0; i < h3s.length; i++) {
			h3s[i] = body.indexOf(entries.get(i));
		}
		// get each entry - consisting of all sibling nodes between h3 elements
		for (int i = 0; i < h3s.length - 1; i++) {
			Element entry = new Element("entry");
			for (int j = h3s[i]; j < h3s[i + 1]; j++) {
				Node n = body.getChild(j).copy();
				entry.appendChild(n);
			}
			tocEntryList.add(entry);
		}
		// last entry consists of last h3 to end of body element
		Element entry = new Element("entry");
		for (int j = h3s[h3s.length - 1]; j < body.getChildCount(); j++) {
			Node n = body.getChild(j).copy();
			entry.appendChild(n);
		}
		tocEntryList.add(entry);
		return tocEntryList;
	}

	public static void main(String[] args) {
		try {
			String[] years = { "1995" };
			String[] parts = { "12" };
			for (int i = 0; i < years.length; i++) {
				for (int j = 0; j < parts.length; j++) {
					Properties props = IOUtils
							.loadProperties("e:/data-test/docs/cif-flow-props.txt");
					ActaOldLayout oae = new ActaOldLayout("c", years[i],
							parts[j], "00");
					oae
							.setDownloadDir(new File(props
									.getProperty("write.dir")));
					oae.fetchAll();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}