package wwmm.crystaleye.crawler.core;

import static wwmm.crystaleye.CrystalEyeConstants.X_XHTML;
import static wwmm.crystaleye.crawler.core.CrawlerConstants.ACS_HOMEPAGE_URL;

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
 * The <code>AcsIssueCrawler</code> class provides a method for obtaining
 * information about all articles from a particular issue of a journal
 * published by the American Chemical Society.
 * </p>
 * 
 * @author Nick Day
 * @version 1.1
 * 
 */
public class AcsIssueCrawler extends IssueCrawler {

	private AcsJournal journal;

	private static final Logger LOG = Logger.getLogger(AcsIssueCrawler.class);

	/**
	 * <p>
	 * Creates an instance of the AcsIssueCrawler class and
	 * specifies the journal of the issue to be crawled.
	 * </p>
	 * 
	 * @param doi of the article to be crawled.
	 */
	public AcsIssueCrawler(AcsJournal journal) {
		this.journal = journal;
	}

	/**
	 * <p>
	 * Gets information to identify the last published issue of a
	 * the provided <code>AcsJournal</code>.
	 * </p>
	 * 
	 * @return the year and issue identifier.
	 * 
	 */
	@Override
	public IssueDetails getCurrentIssueDetails() {
		Document doc = getCurrentIssueHtml();
		Nodes journalInfo = doc.query(".//x:div[@id='tocMeta']", X_XHTML);
		int size = journalInfo.size();
		if (size != 1) {
			throw new CrawlerRuntimeException("Expected to find 1 element containing" +
					" the year/issue information but found "+size+".");
		}
		String info = journalInfo.get(0).getValue().trim();
		Pattern pattern = Pattern.compile("[^,]*,\\s*(\\d+)\\s+Volume\\s+(\\d+),\\s+Issue\\s+(\\d+)\\s+Pages\\s+(\\d+-\\d+).*");
		Matcher matcher = pattern.matcher(info);
		if (!matcher.find() || matcher.groupCount() != 4) {
			throw new CrawlerRuntimeException("Could not extract the year/issue information.");
		}
		String year = matcher.group(1);
		String issueId = matcher.group(3);
		LOG.debug("Found latest issue details for ACS journal "+journal.getFullTitle()+": year="+year+", issue="+issueId+".");
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
		String url = "http://pubs.acs.org/toc/"+journal.getAbbreviation()+"/current";
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
	 * by the <code>AcsJournal</code> and the provided
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
	public List<DOI> getDOIs(IssueDetails issueDetails) {
		String year = issueDetails.getYear();
		String issueId = issueDetails.getIssueId();
		List<DOI> dois = new ArrayList<DOI>();
		int volume = Integer.valueOf(year)-journal.getVolumeOffset();
		String issueUrl = ACS_HOMEPAGE_URL+"/toc/"+journal.getAbbreviation()+"/"+volume+"/"+issueId;
		URI issueUri = createURI(issueUrl);
		LOG.info("Started to find DOIs from "+journal.getFullTitle()+", year "+year+", issue "+issueId+".");
		Document issueDoc = httpClient.getResourceHTML(issueUri);
		List<Node> doiNodes = Utils.queryHTML(issueDoc, ".//x:div[@class='DOI']");
		for (Node doiNode : doiNodes) {
			String contents = ((Element)doiNode).getValue();
			String doiPostfix = contents.replaceAll("DOI:", "").trim();
			String doiStr = DOI.DOI_SITE_URL+"/"+doiPostfix;
			DOI doi = new DOI(createURI(doiStr)); 
			dois.add(doi);
		}
		LOG.info("Finished finding issue DOIs.");
		return dois;
	}

	/**
	 * <p>
	 * Gets information describing all articles in the issue 
	 * defined by the <code>AcsJournal</code> and the provided
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
		LOG.debug("Starting to find issue article details: "+details.getYear()+"-"+details.getIssueId());
		List<DOI> dois = getDOIs(details);
		List<ArticleDetails> adList = new ArrayList<ArticleDetails>(dois.size());
		for (DOI doi : dois) {
			ArticleDetails ad = new AcsArticleCrawler(doi).getDetails();
			adList.add(ad);
		}
		LOG.debug("Finished finding issue article details: "+details.getYear()+"-"+details.getIssueId());
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
		AcsIssueCrawler acf = new AcsIssueCrawler(AcsJournal.THE_JOURNAL_OF_ORGANIC_CHEMISTRY);
		IssueDetails details = acf.getCurrentIssueDetails();
		List<ArticleDetails> adList = acf.getDetailsForArticles(details);
		for (ArticleDetails ad : adList) {
			System.out.println(ad.toString());
		}
	}

}
