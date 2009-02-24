package wwmm.crystaleye.crawlers;

import static wwmm.crystaleye.CrystalEyeConstants.X_XHTML;
import static wwmm.crystaleye.crawlers.CrawlerConstants.DOI_SITE_URL;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Node;
import nu.xom.Nodes;

import org.apache.commons.httpclient.URI;
import org.apache.log4j.Logger;

import wwmm.crystaleye.Utils;

/**
 * <p>
 * The <code>RscIssueCrawler</code> class provides a method for obtaining
 * information about all articles from a particular issue of a journal
 * published by the Royal Society of Chemistry.
 * </p>
 * 
 * @author Nick Day
 * @version 1.1
 * 
 */
public class RscIssueCrawler extends IssueCrawler{

	public RscJournal journal;
	private String volume = "0";
	private static final Logger LOG = Logger.getLogger(RscIssueCrawler.class);

	/**
	 * <p>
	 * Creates an instance of the RscIssueCrawler class and
	 * specifies the journal of the issue to be crawled.
	 * </p>
	 * 
	 * @param doi of the article to be crawled.
	 * 
	 */
	public RscIssueCrawler(RscJournal journal) {
		this.journal = journal;
	}

	/**
	 * <p>
	 * Gets information to identify the last published issue of a
	 * the provided <code>RscJournal</code>.
	 * </p>
	 * 
	 * @return the year and issue identifier.
	 * 
	 */
	@Override
	public IssueDetails getCurrentIssueDetails() {
		Document doc = getCurrentIssueHtml();
		List<Node> journalInfo = Utils.queryHTML(doc, ".//x:h3[contains(text(),'Contents')]");
		int size = journalInfo.size();
		if (size != 1) {
			throw new CrawlerRuntimeException("Expected to find 1 element containing"+
					"the year/issue information but found "+size+".");
		}
		String info = journalInfo.get(0).getValue().trim();
		Pattern pattern = Pattern.compile("[^\\d]*(\\d+),[^\\d]*(\\d+)$");
		Matcher matcher = pattern.matcher(info);
		if (!matcher.find() || matcher.groupCount() != 2) {
			throw new CrawlerRuntimeException("Could not extract the year/issue information.");
		}
		String issueNum = matcher.group(1);
		String year = matcher.group(2);
		return new IssueDetails(year, issueNum);
	}

	/**
	 * <p>
	 * Gets the HTML of the table of contents of the last 
	 * published issue of the provided journal.
	 * </p>
	 * 
	 * @return HTML of the issue table of contents.
	 * 
	 */
	@Override
	public Document getCurrentIssueHtml() {
		String url = "http://rsc.org/Publishing/Journals/"
			+journal.getAbbreviation().toUpperCase()+"/Article.asp?Type=CurrentIssue";
		URI uri = createURI(url);
		return httpClient.getResourceHTML(uri);
	}

	/**
	 * <p>
	 * Gets the DOIs of all of the articles from the last 
	 * published issue of the provided journal.
	 * </p> 
	 * 
	 * @return a list of the DOIs of the articles.
	 * 
	 */
	@Override
	public List<DOI> getCurrentIssueDOIs() {
		IssueDetails details = getCurrentIssueDetails();
		return getDOIs(details);
	}

	/**
	 * <p>
	 * Gets the DOIs of all articles in the issue 
	 * defined by the <code>RscJournal</code> and the provided
	 * year and issue identifier (wrapped in the 
	 * <code>issueDetails</code> parameter.
	 * </p>
	 * 
	 * @param issueDetails - contains the year and issue
	 * identifier of the issue to be crawled.
	 * 
	 * @return a list of the DOIs of the articles for the issue.
	 * 
	 */
	@Override
	public List<DOI> getDOIs(IssueDetails details) {
		String year = details.getYear();
		String issueId = details.getIssueId();
		String journalAbbreviation = journal.getAbbreviation();
		String issueUrl = "http://rsc.org/Publishing/Journals/"+journalAbbreviation
		+"/article.asp?Journal="+journalAbbreviation+"81&VolumeYear="+year+volume+"&Volume="+volume
		+"&JournalCode="+journalAbbreviation+"&MasterJournalCode="+journalAbbreviation+"&SubYear="+year
		+"&type=Issue&Issue="+issueId+"&x=11&y=5";
		URI issueUri = createURI(issueUrl);
		LOG.debug("Started to find DOIs from "+journal.getFullTitle()+", year "+year+", issue "+issueId+".");
		Document issueDoc = httpClient.getResourceHTML(issueUri);
		List<Node> articleNodes = Utils.queryHTML(issueDoc, ".//x:p[./x:a[contains(@title,'DOI:10.1039')]]");
		List<DOI> dois = new ArrayList<DOI>();
		for (Node articleNode : articleNodes) {
			Element articleElement = (Element)articleNode;
			if (!isArticle(articleElement)) {
				continue;
			}
			Nodes doiNodes = articleNode.query(".//x:a[contains(.,'10.1039/')]", X_XHTML);
			if (doiNodes.size() != 1) {
				throw new CrawlerRuntimeException("Problem getting DOI link from article element:\n"+articleElement.toXML());
			}
			String doiPrefix = ((Element)doiNodes.get(0)).getValue();
			String doiStr = DOI_SITE_URL+"/"+doiPrefix;
			DOI doi = new DOI(createURI(doiStr));
			dois.add(doi);
		}
		LOG.debug("Finished finding DOIs.");
		return dois;
	}

	/**
	 * <p>
	 * Unfortunately RSC give DOIs to the electronic version of their front 
	 * cover, contents list, back cover a few other things that aren't actually
	 * articles.  As we want to write an article crawler that fails if it can't 
	 * find the full-text HTML of an article, we don't want these non-article 
	 * DOIs being passed further down the crawler's processing.  They are 
	 * weeded out by this method.
	 * </p>
	 * 
	 * @param articleElement
	 * @return boolean stating whether the doiElement links to an article or not
	 * 
	 */
	private boolean isArticle(Element articleElement) {
		Nodes linkNds = articleElement.query(".//x:a[contains(@title,'DOI:')]", X_XHTML);
		if (linkNds.size() == 0) {
			throw new CrawlerRuntimeException("Problem getting DOI link nodes from article element:\n"+articleElement.toXML());
		}
		String value = ((Element)linkNds.get(0)).getValue();
		if (value.contains("Front cover") || 
				value.contains("Inside front cover") ||
				value.contains("Contents and") ||
				value.trim().equals("Contents") ||
				value.contains("Back matter") ||
				value.contains("Back cover"))  {
			return false;
		} else {
			return true;
		}
	}
	
	/**
	 * <p>
	 * Gets information describing all articles in the issue 
	 * defined by the <code>RscJournal</code> and the provided
	 * year and issue identifier (wrapped in the 
	 * <code>issueDetails</code> parameter.
	 * </p>
	 * 
	 * @param issueDetails - contains the year and issue
	 * identifier of the issue to be crawled.
	 * 
	 * @return a list where each item contains the details for 
	 * a particular article from the issue.
	 * 
	 */
	@Override
	public List<ArticleDetails> getDetailsForArticles(IssueDetails details) {
		String year = details.getYear();
		String issueId = details.getIssueId();
		LOG.debug("Starting to find issue article details: "+year+"-"+issueId);
		List<DOI> dois = getDOIs(details);
		List<ArticleDetails> adList = new ArrayList<ArticleDetails>(dois.size());
		for (DOI doi : dois) {
			ArticleDetails ad = new RscArticleCrawler(doi).getDetails();
			adList.add(ad);
		}
		LOG.debug("Finished finding issue article details: "+year+"-"+issueId);
		return adList;
	}

	/**
	 * <p>
	 * Main method only for demonstration of class use. Does not require
	 * any arguments.
	 * </p>
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		for (RscJournal journal : RscJournal.values()) {
			if (!journal.getAbbreviation().equals("cc")) {
				continue;
			}
			RscIssueCrawler acf = new RscIssueCrawler(journal);
			IssueDetails details = acf.getCurrentIssueDetails();
			List<ArticleDetails> adList = acf.getDetailsForArticles(details);
			for (ArticleDetails ad : adList) {
				System.out.println(ad.toString());
			}
			break;
		}
	}

}
