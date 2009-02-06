package wwmm.crystaleye.crawlers;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Node;

import org.apache.commons.httpclient.URI;
import org.apache.log4j.Logger;

import wwmm.crystaleye.util.Utils;

public class ChemSocJapanIssueCrawler extends Crawler {

	public enum ChemSocJapanJournal {
		CHEMISTRY_LETTERS("chem-lett", "Chemistry Letters");

		private final String abbreviation;
		private final String fullTitle;

		ChemSocJapanJournal(String abbreviation, String fullTitle) {
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

	public ChemSocJapanJournal journal;
	private static final Logger LOG = Logger.getLogger(ChemSocJapanIssueCrawler.class);

	public ChemSocJapanIssueCrawler(ChemSocJapanJournal journal) {
		this.journal = journal;
	}

	protected IssueDetails getCurrentIssueDetails() {
		Document doc = getCurrentIssueDocument();
		List<Node> journalInfo = Utils.queryHTML(doc, "//x:span[@class='augr']");
		int size = journalInfo.size();
		if (size != 1) {
			throw new RuntimeException("Expected to find 1 element containing" +
					"the year/issue information but found "+size+".");
		}
		String info = journalInfo.get(0).getValue();
		Pattern pattern = Pattern.compile("[^,]*,\\s+\\w+\\.\\s+(\\d+)\\s+\\([^,]*,\\s+(\\d\\d\\d\\d)\\)");
		Matcher matcher = pattern.matcher(info);
		if (!matcher.find() || matcher.groupCount() != 2) {
			throw new RuntimeException("Could not extract the year/issue information.");
		}
		String year = matcher.group(2);
		String issueNum = matcher.group(1);
		return new IssueDetails(year, issueNum);
	}
	
	public Document getCurrentIssueDocument() {
		String url = "http://www.csj.jp/journals/"+journal.getAbbreviation()+"/cl-cont/newissue.html";
		URI issueUri = createURI(url);
		return httpClient.getWebpageDocumentMinusComments(issueUri);
	}
	
	public List<URI> getCurrentIssueDOIs() {
		IssueDetails details = getCurrentIssueDetails();
		return getDOIs(details);
	}

	public List<URI> getDOIs(String year, String issueId) {
		String url = "http://www.chemistry.or.jp/journals/chem-lett/cl-cont/cl"+year+"-"+issueId+".html";
		URI issueUri = createURI(url);
		LOG.debug("Started to find DOIs from "+journal.getFullTitle()+", year "+year+", issue "+issueId+".");
		LOG.debug(issueUri.toString());
		Document issueDoc = httpClient.getWebpageDocumentMinusComments(issueUri);
		List<Node> textLinks = Utils.queryHTML(issueDoc, ".//x:a[contains(@href,'http://www.is.csj.jp/cgi-bin/journals/pr/index.cgi?n=li') and not(contains(@href,'li_s'))]/@href");
		System.out.println(textLinks.size());
		List<URI> dois = new ArrayList<URI>();
		for (Node textLink : textLinks) {
			String link = ((Attribute)textLink).getValue();
			int idx = link.indexOf("id=");
			String articleId = link.substring(idx+3).replaceAll("/", ".");
			String doi = "http://dx.doi.org/10.1246/"+articleId;
			URI doiUri = createURI(doi);
			dois.add(doiUri);
		}
		LOG.debug("Finished finding issue DOIs.");
		return dois;
	}
	
	public List<URI> getDOIs(IssueDetails details) {
		return getDOIs(details.getYear(), details.getIssueId());
	}
	
	public List<ArticleDetails> getArticleDetails(String year, String issueId) {
		List<URI> dois = getDOIs(year, issueId);
		List<ArticleDetails> adList = new ArrayList<ArticleDetails>(dois.size());
		for (URI doi : dois) {
			ArticleDetails ad = new ChemSocJapanArticleCrawler(doi).getDetails();
			adList.add(ad);
		}
		return adList;
	}

	public List<ArticleDetails> getArticleDetails(IssueDetails id) {
		return getArticleDetails(id.getYear(), id.getIssueId());
	}

	public static void main(String[] args) {
		for (ChemSocJapanJournal journal : ChemSocJapanJournal.values()) {
			ChemSocJapanIssueCrawler acf = new ChemSocJapanIssueCrawler(journal);
			IssueDetails details = acf.getCurrentIssueDetails();
			List<ArticleDetails> adList = acf.getArticleDetails(details);
			for (ArticleDetails ad : adList) {
				System.out.println(ad.toString());
			}
			break;
		}
	}
}
