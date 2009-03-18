package wwmm.crystaleye.crawler;

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

import wwmm.crystaleye.Utils;

/**
 * <p>
 * The <code>ActaIssueCrawler</code> class provides a method for obtaining
 * information about all articles from a particular issue of a journal
 * published by Acta Crystallographica.
 * </p>
 * 
 * @author Nick Day
 * @version 1.1
 * 
 */
public class ActaIssueCrawler extends IssueCrawler {

	public ActaJournal journal;
	private static final Logger LOG = Logger.getLogger(ActaIssueCrawler.class);

	/**
	 * <p>
	 * Creates an instance of the ActaIssueCrawler class and
	 * specifies the journal of the issue to be crawled.
	 * </p>
	 * 
	 * @param doi of the article to be crawled.
	 */
	public ActaIssueCrawler(ActaJournal journal) {
		this.journal = journal;
	}

	/**
	 * <p>
	 * Gets information to identify the last published issue of a
	 * the provided <code>ActaJournal</code>.
	 * </p>
	 * 
	 * @return the year and issue identifier.
	 * 
	 */
	@Override
	public IssueDetails getCurrentIssueDetails() {
		Document doc = getCurrentIssueHtml();
		List<Node> currentIssueLink = Utils.queryHTML(doc, "//x:a[contains(@target,'_parent')]");
		Node current = currentIssueLink.get(0);
		if (((Element) current).getValue().contains("preparation")) {
			current = currentIssueLink.get(1);
		}
		String info = ((Element)current).getAttributeValue("href");
		Pattern pattern = Pattern.compile("\\.\\./issues/(\\d\\d\\d\\d)/(\\d\\d/\\d\\d)/issconts.html");
		Matcher matcher = pattern.matcher(info);
		if (!matcher.find() || matcher.groupCount() != 2) {
			throw new CrawlerRuntimeException("Could not extract the year/issue information " +
					"from current issue for Acta journal, "+journal.getFullTitle()+".");
		}
		String year = matcher.group(1);
		String issueId = matcher.group(2).replaceAll("/", "-");
		LOG.info("Found latest issue details for Acta journal "+journal.getFullTitle()+": year="+year+", issue="+issueId+".");
		return new IssueDetails(year, issueId);
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
		String url = "http://journals.iucr.org/"+journal.getAbbreviation()+"/contents/backissuesbdy.html";
		URI issueUri = createURI(url);
		return httpClient.getResourceHTML(issueUri);
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
	 * Gets the DOIs of all articles in the issue defined
	 * by the <code>ActaJournal</code> and the provided	year 
	 * and issue identifier (wrapped in the 
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
		List<DOI> dois = new ArrayList<DOI>();
		String url = "http://journals.iucr.org/"+journal.getAbbreviation()+"/issues/"
		+year+"/"+issueId.replaceAll("-", "/")+"/isscontsbdy.html";
		URI issueUri = createURI(url);
		LOG.info("Started to find article DOIs from "+journal.getFullTitle()+", year "+year+", issue "+issueId+".");
		LOG.debug(issueUri);
		Document issueDoc = httpClient.getResourceHTML(issueUri);
		List<Node> doiNodes = Utils.queryHTML(issueDoc, ".//x:a[contains(@href,'"+DOI.DOI_SITE_URL+"/10.1107/')]/@href");
		for (Node doiNode : doiNodes) {
			String doiStr = ((Attribute)doiNode).getValue();
			DOI doi = new DOI(createURI(doiStr));
			dois.add(doi);
		}
		LOG.info("Finished finding issue DOIs.");
		return dois;
	}

	/**
	 * <p>
	 * Gets information describing all articles in the issue 
	 * defined by the <code>ActaJournal</code> and the 
	 * provided	year and issue identifier (wrapped in the 
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
		List<DOI> dois = getDOIs(details);
		List<ArticleDetails> adList = new ArrayList<ArticleDetails>(dois.size());
		for (DOI doi : dois) {
			ArticleDetails ad = new ActaArticleCrawler(doi).getDetails();
			adList.add(ad);
		}
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
		ActaIssueCrawler acf = new ActaIssueCrawler(ActaJournal.SECTION_B);
		IssueDetails details = acf.getCurrentIssueDetails();
		List<ArticleDetails> adList = acf.getDetailsForArticles(details);
		for (ArticleDetails ad : adList) {
			System.out.println(ad.toString());
		}
	}

}
