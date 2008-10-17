package wwmm.crystaleye.crawlers;

import static wwmm.crystaleye.CrystalEyeConstants.X_XHTML;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Node;
import nu.xom.Nodes;

import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.log4j.Logger;

import wwmm.crystaleye.Unzip;
import wwmm.crystaleye.crawlers.ActaCrawler.ActaJournal;
import wwmm.crystaleye.util.HttpUtils;
import wwmm.crystaleye.util.Utils;

public class ElsevierCrawler extends JournalCrawler {

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

	public IssueDetails getCurrentIssueDetails() throws Exception {
		Document issueDoc = getCurrentIssueDocument();
		List<Node> titleNodes = Utils.queryHTML(issueDoc, ".//x:title");
		int size = titleNodes.size();
		if (size != 1) {
			throw new Exception("Expected to find 1 element containing" +
					"the year/issue information but found "+size+".");
		}
		String title = titleNodes.get(0).getValue();
		Pattern p = Pattern.compile("[^,]+,\\s+Volume\\s+(\\d+)\\s*,\\s+Issue[s]?\\s+([\\d-]+).*");
		Matcher matcher = p.matcher(title);
		if (!matcher.find() || matcher.groupCount() != 2) {
			throw new Exception("Could not extract the year/issue information.");
		}
		String year = matcher.group(1);
		String issue = matcher.group(2);
		return new IssueDetails(year, issue);
	}
	
	public Document getCurrentIssueDocument() throws Exception {
		String issueUrl = SITE_PREFIX+"/science/journal/"+journal.getAbbreviation();
		URI issueUri = new URI(issueUrl, false);
		return HttpUtils.getWebpageMinusCommentsAsXML(issueUri);
	}
	
	private List<URI> getArticleLinks(Document issueDoc) throws URIException {
		List<URI> links = new ArrayList<URI>();
		List<Node> articleLinks = Utils.queryHTML(issueDoc, ".//x:a[contains(@href,'http://www.sciencedirect.com/science?_ob=ArticleURL')]");
		for (Node articleLink : articleLinks) {
			String link = ((Element)articleLink).getAttributeValue("href");
			links.add(new URI(link, false));
		}
		return links;
	}

	public List<URI> getCurrentIssueDOIs() throws Exception {
		String currentIssueUrl = SITE_PREFIX+"/science/journal/"+journal.getAbbreviation(); 
		URI currentIssueUri = new URI(currentIssueUrl, false);
		LOG.debug("Started to find article DOIs from current issue of "+journal.getFullTitle()+".");
		LOG.debug(currentIssueUri);
		Document currentIssueDoc = HttpUtils.getWebpageMinusCommentsAsXML(currentIssueUri);
		List<URI> articleLinks = getArticleLinks(currentIssueDoc);
		List<URI> dois = new ArrayList<URI>();
		for (URI articleLink : articleLinks) {
			Document articleDoc = HttpUtils.getWebpageAsXML(articleLink);
			List<Node> doiNodes = Utils.queryHTML(articleDoc, ".//x:a[contains(@href,'http://dx.doi.org/10.1016/')]");
			if (doiNodes.size() > 0) {
				String doi = ((Element)doiNodes.get(0)).getAttributeValue("href");
				dois.add(new URI(doi, false));
			}
			sleep();
		}	
		LOG.debug("Finished finding issue DOIs.");
		return dois;
	}
	
	public static void main(String[] args) throws Exception {
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
