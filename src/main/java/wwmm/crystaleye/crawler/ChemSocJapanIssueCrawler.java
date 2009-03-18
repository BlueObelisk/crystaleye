package wwmm.crystaleye.crawler;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Node;

import org.apache.commons.httpclient.URI;
import org.apache.log4j.Logger;

import wwmm.crystaleye.Utils;

/**
 * <p>
 * The <code>ChemSocJapanIssueCrawler</code> class provides a method for 
 * obtaining information about all articles from a particular issue of a 
 * journal published by the Chemical Society of Japan.
 * </p>
 * 
 * @author Nick Day
 * @version 1.1
 * 
 */
public class ChemSocJapanIssueCrawler extends IssueCrawler {

	public ChemSocJapanJournal journal;
	private static final Logger LOG = Logger.getLogger(ChemSocJapanIssueCrawler.class);

	/**
	 * <p>
	 * Creates an instance of the ChemSocJapanIssueCrawler class and
	 * specifies the journal of the issue to be crawled.
	 * </p>
	 * 
	 * @param doi of the article to be crawled.
	 */
	public ChemSocJapanIssueCrawler(ChemSocJapanJournal journal) {
		this.journal = journal;
	}

	/**
	 * <p>
	 * Gets information to identify the last published issue of a
	 * the provided <code>ChemSocJapanJournal</code>.
	 * </p>
	 * 
	 * @return the year and issue identifier.
	 * 
	 */
	@Override
	public IssueDetails getCurrentIssueDetails() {
		Document doc = getCurrentIssueHtml();
		List<Node> journalInfo = Utils.queryHTML(doc, "//x:span[@class='augr']");
		int size = journalInfo.size();
		if (size != 1) {
			throw new CrawlerRuntimeException("Expected to find 1 element containing" +
					"the year/issue information but found "+size+".");
		}
		String info = journalInfo.get(0).getValue();
		Pattern pattern = Pattern.compile("[^,]*,\\s+\\w+\\.\\s+(\\d+)\\s+\\([^,]*,\\s+(\\d\\d\\d\\d)\\)");
		Matcher matcher = pattern.matcher(info);
		if (!matcher.find() || matcher.groupCount() != 2) {
			throw new CrawlerRuntimeException("Could not extract the year/issue information.");
		}
		String year = matcher.group(2);
		String issueNum = matcher.group(1);
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
		String url = "http://www.csj.jp/journals/"+journal.getAbbreviation()+"/cl-cont/newissue.html";
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
	 * by the <code>ChemSocJapanJournal</code> and the provided	
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
		String url = "http://www.chemistry.or.jp/journals/chem-lett/cl-cont/cl"+year+"-"+issueId+".html";
		URI issueUri = createURI(url);
		LOG.info("Started to find DOIs from "+journal.getFullTitle()+", year "+year+", issue "+issueId+".");
		LOG.debug(issueUri.toString());
		Document issueDoc = httpClient.getResourceHTML(issueUri);
		List<Node> textLinks = Utils.queryHTML(issueDoc, ".//x:a[contains(@href,'http://www.is.csj.jp/cgi-bin/journals/pr/index.cgi?n=li') and not(contains(@href,'li_s'))]/@href");
		List<DOI> dois = new ArrayList<DOI>();
		for (Node textLink : textLinks) {
			String link = ((Attribute)textLink).getValue();
			int idx = link.indexOf("id=");
			String articleId = link.substring(idx+3).replaceAll("/", ".");
			String doiStr = "http://dx.doi.org/10.1246/"+articleId;
			DOI doi = new DOI(createURI(doiStr));
			dois.add(doi);
		}
		LOG.info("Finished finding issue DOIs.");
		return dois;
	}
	
	/**
	 * <p>
	 * Gets information describing all articles in the issue 
	 * defined by the <code>ChemSocJapanJournal</code> and the 
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
			ArticleDetails ad = new ChemSocJapanArticleCrawler(doi).getDetails();
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
		for (ChemSocJapanJournal journal : ChemSocJapanJournal.values()) {
			ChemSocJapanIssueCrawler acf = new ChemSocJapanIssueCrawler(journal);
			IssueDetails details = acf.getCurrentIssueDetails();
			List<ArticleDetails> adList = acf.getDetailsForArticles(details);
			for (ArticleDetails ad : adList) {
				System.out.println(ad.toString());
			}
			break;
		}
	}
}
