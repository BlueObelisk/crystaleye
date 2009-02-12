package wwmm.crystaleye.crawlers;

import static wwmm.crystaleye.crawlers.CrawlerConstants.DOI_SITE_URL;

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

public class RscIssueCrawler extends Crawler{

	public RscJournal journal;
	private String volume = "0";
	private static final Logger LOG = Logger.getLogger(RscIssueCrawler.class);

	public RscIssueCrawler(RscJournal journal) {
		this.journal = journal;
	}

	protected IssueDetails getCurrentIssueDetails() {
		Document doc = getCurrentIssueDocument();
		List<Node> journalInfo = Utils.queryHTML(doc, "//x:h3[contains(text(),'Contents')]");
		int size = journalInfo.size();
		if (size != 1) {
			throw new RuntimeException("Expected to find 1 element containing"+
					"the year/issue information but found "+size+".");
		}
		String info = journalInfo.get(0).getValue().trim();
		Pattern pattern = Pattern.compile("[^\\d]*(\\d+),[^\\d]*(\\d+)$");
		Matcher matcher = pattern.matcher(info);
		if (!matcher.find() || matcher.groupCount() != 2) {
			throw new RuntimeException("Could not extract the year/issue information.");
		}
		String issueNum = matcher.group(1);
		String year = matcher.group(2);
		return new IssueDetails(year, issueNum);
	}

	public Document getCurrentIssueDocument() {
		String url = "http://rsc.org/Publishing/Journals/"
			+journal.getAbbreviation().toUpperCase()+"/Article.asp?Type=CurrentIssue";
		URI uri = createURI(url);
		return httpClient.getWebpageDocumentMinusComments(uri);
	}

	public List<URI> getCurrentIssueDOIs() {
		IssueDetails details = getCurrentIssueDetails();
		return getDOIs(details);
	}

	public List<URI> getDOIs(String year, String issueId) {
		String journalAbbreviation = journal.getAbbreviation();
		String issueUrl = "http://rsc.org/Publishing/Journals/"+journalAbbreviation
		+"/article.asp?Journal="+journalAbbreviation+"81&VolumeYear="+year+volume+"&Volume="+volume
		+"&JournalCode="+journalAbbreviation+"&MasterJournalCode="+journalAbbreviation+"&SubYear="+year
		+"&type=Issue&Issue="+issueId+"&x=11&y=5";
		URI issueUri = createURI(issueUrl);
		LOG.debug("Started to find DOIs from "+journal.getFullTitle()+", year "+year+", issue "+issueId+".");
		Document issueDoc = httpClient.getWebpageDocumentMinusComments(issueUri);
		List<Node> doiNodes = Utils.queryHTML(issueDoc, ".//x:a[contains(@title,'DOI:10.1039')]");
		List<URI> dois = new ArrayList<URI>();
		for (Node doiNode : doiNodes) {
			String doi = ((Element)doiNode).getValue();
			doi = DOI_SITE_URL+"/"+doi;
			URI doiUri = createURI(doi);
			dois.add(doiUri);
		}
		LOG.debug("Finished finding DOIs.");
		return dois;
	}

	public List<URI> getDOIs(IssueDetails details) {
		return getDOIs(details.getYear(), details.getIssueId());
	}
	
	public List<ArticleDetails> getArticleDetails(String year, String issueId) {
		LOG.debug("Starting to find issue article details: "+year+"-"+issueId);
		List<URI> dois = getDOIs(year, issueId);
		List<ArticleDetails> adList = new ArrayList<ArticleDetails>(dois.size());
		for (URI doi : dois) {
			ArticleDetails ad = new RscArticleCrawler(doi).getDetails();
			adList.add(ad);
		}
		LOG.debug("Finished finding issue article details: "+year+"-"+issueId);
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
		for (RscJournal journal : RscJournal.values()) {
			if (!journal.getAbbreviation().equals("cc")) {
				continue;
			}
			RscIssueCrawler acf = new RscIssueCrawler(journal);
			IssueDetails details = acf.getCurrentIssueDetails();
			List<ArticleDetails> adList = acf.getArticleDetails(details);
			for (ArticleDetails ad : adList) {
				System.out.println(ad.toString());
			}
			break;
		}
	}

}
