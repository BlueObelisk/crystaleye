package wwmm.crystaleye.crawlers;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Node;

import org.apache.commons.httpclient.URI;
import org.apache.log4j.Logger;

import wwmm.crystaleye.util.Utils;

public class ElsevierCrawler extends Crawler {

	public enum ElsevierJournal {
		POLYHEDRON("02775387", "Polyhedron");

		private final String abbreviation;
		private final String fullTitle;

		ElsevierJournal(String abbreviation, String fullTitle) {
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

	public ElsevierJournal journal;
	private static final String SITE_PREFIX = "http://www.sciencedirect.com";
	private static final Logger LOG = Logger.getLogger(ElsevierCrawler.class);

	public ElsevierCrawler(ElsevierJournal journal) {
		this.journal = journal;
	}

	public IssueDetails getCurrentIssueDetails() {
		Document issueDoc = getCurrentIssueDocument();
		List<Node> titleNodes = Utils.queryHTML(issueDoc, ".//x:title");
		int size = titleNodes.size();
		if (size != 1) {
			throw new RuntimeException("Expected to find 1 element containing" +
					"the year/issue information but found "+size+".");
		}
		String title = titleNodes.get(0).getValue();
		Pattern p = Pattern.compile("[^,]+,\\s+Volume\\s+(\\d+)\\s*,\\s+Issue[s]?\\s+([\\d-]+).*");
		Matcher matcher = p.matcher(title);
		if (!matcher.find() || matcher.groupCount() != 2) {
			throw new RuntimeException("Could not extract the year/issue information.");
		}
		String year = matcher.group(1);
		String issue = matcher.group(2);
		return new IssueDetails(year, issue);
	}
	
	public Document getCurrentIssueDocument() {
		String issueUrl = SITE_PREFIX+"/science/journal/"+journal.getAbbreviation();
		URI issueUri = createURI(issueUrl);
		return httpClient.getWebpageDocumentMinusComments(issueUri);
	}
	
	private List<URI> getArticleLinks(Document issueDoc) {
		List<URI> links = new ArrayList<URI>();
		List<Node> articleLinks = Utils.queryHTML(issueDoc, ".//x:a[contains(@href,'http://www.sciencedirect.com/science?_ob=ArticleURL')]");
		for (Node articleLink : articleLinks) {
			String link = ((Element)articleLink).getAttributeValue("href");
			URI linkUri = createURI(link);
			links.add(linkUri);
		}
		return links;
	}

	public List<URI> getCurrentIssueDOIs() {
		String currentIssueUrl = SITE_PREFIX+"/science/journal/"+journal.getAbbreviation(); 
		URI currentIssueUri = createURI(currentIssueUrl);
		LOG.debug("Started to find article DOIs from current issue of "+journal.getFullTitle()+".");
		LOG.debug(currentIssueUri);
		Document currentIssueDoc = httpClient.getWebpageDocumentMinusComments(currentIssueUri);
		List<URI> articleLinks = getArticleLinks(currentIssueDoc);
		List<URI> dois = new ArrayList<URI>();
		for (URI articleLink : articleLinks) {
			Document articleDoc = httpClient.getWebpageDocument(articleLink);
			List<Node> doiNodes = Utils.queryHTML(articleDoc, ".//x:a[contains(@href,'http://dx.doi.org/10.1016/')]");
			if (doiNodes.size() > 0) {
				String doi = ((Element)doiNodes.get(0)).getAttributeValue("href");
				URI doiUri = createURI(doi);
				dois.add(doiUri);
			}
			sleep();
		}	
		LOG.debug("Finished finding issue DOIs.");
		return dois;
	}
	
	public static void main(String[] args) {
		for (ElsevierJournal journal : ElsevierJournal.values()) {
			ElsevierCrawler acf = new ElsevierCrawler(journal);
			List<URI> dois = acf.getCurrentIssueDOIs();
			for (URI doi : dois) {
				System.out.println(doi);
			}
			break;
		}
	}
	
}
