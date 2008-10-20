package wwmm.crystaleye.crawlers;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Node;

import org.apache.commons.httpclient.URI;
import org.apache.log4j.Logger;

import wwmm.crystaleye.util.Utils;

public class ActaCrawler extends JournalCrawler {

	public enum ActaJournal {
		SECTION_A("a", "Section A: Foundations of Crystallography"),
		SECTION_B("b", "Section B: Structural Science"),
		SECTION_C("c", "Section C: Crystal Structure Communications"),
		SECTION_D("d", "Section D: Biological Crystallography"),
		SECTION_E("e", "Section E: Structure Reports"),
		SECTION_F("f", "Section F: Structural Biology and Crystallization Communications"),
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
	private static final Logger LOG = Logger.getLogger(ActaCrawler.class);

	public ActaCrawler(ActaJournal journal) {
		this.journal = journal;
	}

	public IssueDetails getCurrentIssueDetails() throws Exception {
		Document doc = getCurrentIssueDocument();
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
					"from current issue for Acta journal, "+journal.getFullTitle()+".");
		}
		String year = matcher.group(1);
		String issueId = matcher.group(2).replaceAll("/", "-");
		LOG.debug("Found latest issue details for Acta journal "+journal.getFullTitle()+": year="+year+", issue="+issueId+".");
		return new IssueDetails(year, issueId);
	}

	public Document getCurrentIssueDocument() throws Exception {
		String url = "http://journals.iucr.org/"+journal.getAbbreviation()+"/contents/backissuesbdy.html";
		URI issueUri = new URI(url, false);
		return httpClient.getWebpageDocument(issueUri);
	}
	
	public List<URI> getCurrentIssueDOIs() throws Exception {
		IssueDetails details = getCurrentIssueDetails();
		return getIssueDOIs(details);
	}
	
	public List<URI> getIssueDOIs(String year, String issueId) throws Exception {
		List<URI> dois = new ArrayList<URI>();
		String url = "http://journals.iucr.org/"+journal.getAbbreviation()+"/issues/"
		+year+"/"+issueId.replaceAll("-", "/")+"/isscontsbdy.html";
		URI issueUri = new URI(url, false);
		LOG.debug("Started to find article DOIs from "+journal.getFullTitle()+", year "+year+", issue "+issueId+".");
		LOG.debug(issueUri);
		Document issueDoc = httpClient.getWebpageDocument(issueUri);
		List<Node> doiNodes = Utils.queryHTML(issueDoc, ".//x:a[contains(@href,'http://dx.doi.org/10.1107/')]/@href");
		for (Node doiNode : doiNodes) {
			String doi = ((Attribute)doiNode).getValue();
			dois.add(new URI(doi, false));
		}
		LOG.debug("Finished finding issue DOIs.");
		return dois;
	}
	
	public List<URI> getIssueDOIs(IssueDetails details) throws Exception {
		return getIssueDOIs(details.getYear(), details.getIssueId());
	}

	public static void main(String[] args) throws Exception {
		for (ActaJournal journal : ActaJournal.values()) {
			if (!journal.getAbbreviation().equals("c")) {
				continue;
			}
			ActaCrawler acf = new ActaCrawler(journal);
			IssueDetails details = acf.getCurrentIssueDetails();
			List<URI> dois = acf.getIssueDOIs(details.getYear(), details.getIssueId());
			for (URI doi : dois) {
				System.out.println(doi);
			}
			break;
		}
	}
}
