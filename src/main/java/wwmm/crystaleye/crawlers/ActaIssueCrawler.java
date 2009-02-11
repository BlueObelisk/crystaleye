package wwmm.crystaleye.crawlers;

import static wwmm.crystaleye.crawlers.CrawlerConstants.DOI_SITE_URL;

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

public class ActaIssueCrawler extends Crawler {

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
	private static final Logger LOG = Logger.getLogger(ActaIssueCrawler.class);

	public ActaIssueCrawler(ActaJournal journal) {
		this.journal = journal;
	}

	public IssueDetails getCurrentIssueDetails() {
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
			throw new RuntimeException("Could not extract the year/issue information " +
					"from current issue for Acta journal, "+journal.getFullTitle()+".");
		}
		String year = matcher.group(1);
		String issueId = matcher.group(2).replaceAll("/", "-");
		LOG.debug("Found latest issue details for Acta journal "+journal.getFullTitle()+": year="+year+", issue="+issueId+".");
		return new IssueDetails(year, issueId);
	}

	public Document getCurrentIssueDocument() {
		String url = "http://journals.iucr.org/"+journal.getAbbreviation()+"/contents/backissuesbdy.html";
		URI issueUri = createURI(url);
		return httpClient.getWebpageDocument(issueUri);
	}

	public List<URI> getCurrentIssueDOIs() {
		IssueDetails details = getCurrentIssueDetails();
		return getDOIs(details);
	}

	public List<URI> getDOIs(String year, String issueId) {
		List<URI> dois = new ArrayList<URI>();
		String url = "http://journals.iucr.org/"+journal.getAbbreviation()+"/issues/"
		+year+"/"+issueId.replaceAll("-", "/")+"/isscontsbdy.html";
		URI issueUri = createURI(url);
		LOG.debug("Started to find article DOIs from "+journal.getFullTitle()+", year "+year+", issue "+issueId+".");
		LOG.debug(issueUri);
		Document issueDoc = httpClient.getWebpageDocument(issueUri);
		List<Node> doiNodes = Utils.queryHTML(issueDoc, ".//x:a[contains(@href,'"+DOI_SITE_URL+"/10.1107/')]/@href");
		for (Node doiNode : doiNodes) {
			String doi = ((Attribute)doiNode).getValue();
			URI doiUri = createURI(doi);
			dois.add(doiUri);
		}
		LOG.debug("Finished finding issue DOIs.");
		return dois;
	}

	public List<URI> getDOIs(IssueDetails id) {
		return getDOIs(id.getYear(), id.getIssueId());
	}

	public List<ArticleDetails> getArticleDetails(String year, String issueId) {
		List<URI> dois = getDOIs(year, issueId);
		List<ArticleDetails> adList = new ArrayList<ArticleDetails>(dois.size());
		for (URI doi : dois) {
			ArticleDetails ad = new ActaArticleCrawler(doi).getDetails();
			adList.add(ad);
		}
		return adList;
	}

	public List<ArticleDetails> getArticleDetails(IssueDetails id) {
		return getArticleDetails(id.getYear(), id.getIssueId());
	}

	/**
	 * Main method only for demonstration of class use. Does not require
	 * any arguments.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		for (ActaJournal journal : ActaJournal.values()) {
			if (!journal.getAbbreviation().equals("c")) {
				continue;
			}
			ActaIssueCrawler acf = new ActaIssueCrawler(journal);
			IssueDetails details = acf.getCurrentIssueDetails();
			List<ArticleDetails> adList = acf.getArticleDetails(details);
			for (ArticleDetails ad : adList) {
				System.out.println(ad.toString());
			}
			break;
		}
	}

}
