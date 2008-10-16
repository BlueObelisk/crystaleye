package wwmm.crystaleye.find;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Node;

import org.apache.commons.httpclient.URI;
import org.apache.log4j.Logger;

import wwmm.crystaleye.util.HttpUtils;
import wwmm.crystaleye.util.Utils;

public class ActaCifFinder extends JournalCifFinder {

	public enum ActaJournal {
		SECTION_A("a", "Section A: Foundations of Crystallography"),
		SECTION_B("b", "Section B: Structural Science"),
		SECTION_C("c", "Section C: Crystal Structure Communications"),
		SECTION_D("d", "Section D: Biological Crystallography"),
		SECTION_E("e", "Section E: Structure Reports"),
		SECTION_J("j", "Section J: Applied Crystallography"),
		SECTION_S("s", "Section S: Synchrotron Radiation");

		private final String abbreviation;
		private final String fullTitle;

		ActaJournal(String abbreviation, String fullTitle) {
			this.abbreviation = abbreviation;
			this.fullTitle = fullTitle;
		}

		public String getFullTitle() {
			return this.fullTitle;
		}

		public String getAbbreviation() {
			return this.abbreviation;
		}
	}

	public ActaJournal journal;
	private static final Logger LOG = Logger.getLogger(ActaCifFinder.class);

	public ActaCifFinder(ActaJournal journal) {
		this.journal = journal;
	}

	public IssueDetails getCurrentIssueDetails() throws Exception {
		String url = "http://journals.iucr.org/"+journal.getAbbreviation()+"/contents/backissuesbdy.html";
		URI issueUri = new URI(url, false);
		Document doc = HttpUtils.getWebpageAsXML(issueUri);
		List<Node> currentIssueLink = Utils.queryHTML(doc, "//x:a[contains(@target,'_parent')]");
		Node current = currentIssueLink.get(0);
		if (((Element) current).getValue().contains("preparation")) {
			current = currentIssueLink.get(1);
		}
		String info = ((Element)current).getAttributeValue("href");
		Pattern pattern = Pattern.compile("\\.\\./issues/(\\d\\d\\d\\d)/(\\d\\d/\\d\\d)/issconts.html");
		Matcher matcher = pattern.matcher(info);
		if (!matcher.find() || matcher.groupCount() != 2) {
			throw new Exception("Could not extract the year/issue information " +
					"from :"+issueUri.toString());
		}
		String year = matcher.group(1);
		String issueId = matcher.group(2).replaceAll("/", "-");
		LOG.debug("Found latest issue details for Acta journal "+journal.getFullTitle()+": year="+year+", issue="+issueId+".");
		return new IssueDetails(year, issueId);
	}

	public List<PublisherCifDetails> findCifs(IssueDetails issueDetails) throws Exception {
		return findCifs(issueDetails.getYear(), issueDetails.getIssueId());
	}

	public List<PublisherCifDetails> findCifs(String year, String issueId) throws Exception {
		List<PublisherCifDetails> pcdList = new ArrayList<PublisherCifDetails>();
		String url = "http://journals.iucr.org/"+journal.getAbbreviation()+"/issues/"
		+year+"/"+issueId.replaceAll("-", "/")+"/isscontsbdy.html";
		URI issueUri = new URI(url, false);
		LOG.debug("Started to find CIFs from "+journal.getFullTitle()+", year "+year+", issue "+issueId+".");
		LOG.debug("Starting at the issue homepage: "+issueUri.toString());
		Document doc = HttpUtils.getWebpageAsXML(issueUri);
		List<Node> tocEntries = Utils.queryHTML(doc, "//x:div[@class='toc entry']");
		sleep();
		for (Node tocEntry : tocEntries) {
			List<Node> cifLinks = Utils.queryHTML(tocEntry, ".//x:img[contains(@src,'"
					+journal.getAbbreviation()+"/graphics/cifborder.gif')]/parent::x:*");
			if (cifLinks.size() == 0) {
				continue;
			}
			String cifUrl = ((Element)cifLinks.get(0)).getAttributeValue("href");
			URI cifUri = new URI(cifUrl, false);
			LOG.debug("Found CIF at "+cifUri.toString());
			String doi = null;
			String title = null;
			List<Node> doiNodes = Utils.queryHTML(tocEntry, ".//x:p/x:font[@size='2']");
			if (doiNodes.size() > 0) {
				doi = ((Element) doiNodes.get(0)).getValue().substring(4);
			} else {
				LOG.debug("Could not find the DOI for this toc entry.");
			}
			List<Node> titleNodes = Utils.queryHTML(tocEntry, "./x:h3[1]");
			if (titleNodes.size() > 0) {
				title = ((Element)titleNodes.get(0)).getValue().trim();
			} else {
				LOG.debug("Could not find the TITLE for this toc entry.");
			}
			PublisherCifDetails pcd = new PublisherCifDetails(cifUri, doi, title);
			pcdList.add(pcd);
			sleep();

		}
		LOG.debug("Finished finding CIFs from " + issueUri);
		return pcdList;
	}

	public static void main(String[] args) throws Exception  {
		for (ActaJournal journal : ActaJournal.values()) {
			if (!journal.getAbbreviation().equals("c")) {
				continue;
			}
			ActaCifFinder acf = new ActaCifFinder(journal);
			IssueDetails details = acf.getCurrentIssueDetails();
			List<PublisherCifDetails> pcdList = acf.findCifs(details);
			for (PublisherCifDetails pcd : pcdList) {
				System.out.println(pcd.getDoi());
			}
			break;
		}
	}

}
