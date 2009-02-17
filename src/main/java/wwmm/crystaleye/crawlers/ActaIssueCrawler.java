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
			throw new CrawlerRuntimeException("Could not extract the year/issue information " +
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
		return httpClient.getResourceHTML(issueUri);
	}

	public List<DOI> getCurrentIssueDOIs() {
		IssueDetails details = getCurrentIssueDetails();
		return getDOIs(details);
	}

	public List<DOI> getDOIs(String year, String issueId) {
		List<DOI> dois = new ArrayList<DOI>();
		String url = "http://journals.iucr.org/"+journal.getAbbreviation()+"/issues/"
		+year+"/"+issueId.replaceAll("-", "/")+"/isscontsbdy.html";
		URI issueUri = createURI(url);
		LOG.debug("Started to find article DOIs from "+journal.getFullTitle()+", year "+year+", issue "+issueId+".");
		LOG.debug(issueUri);
		Document issueDoc = httpClient.getResourceHTML(issueUri);
		List<Node> doiNodes = Utils.queryHTML(issueDoc, ".//x:a[contains(@href,'"+DOI_SITE_URL+"/10.1107/')]/@href");
		for (Node doiNode : doiNodes) {
			String doiStr = ((Attribute)doiNode).getValue();
			DOI doi = new DOI(createURI(doiStr));
			dois.add(doi);
		}
		LOG.debug("Finished finding issue DOIs.");
		return dois;
	}

	public List<DOI> getDOIs(IssueDetails id) {
		return getDOIs(id.getYear(), id.getIssueId());
	}

	public List<ArticleDetails> getArticleDetails(String year, String issueId) {
		List<DOI> dois = getDOIs(year, issueId);
		List<ArticleDetails> adList = new ArrayList<ArticleDetails>(dois.size());
		for (DOI doi : dois) {
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
