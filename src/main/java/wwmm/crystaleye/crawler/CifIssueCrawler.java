package wwmm.crystaleye.crawler;

import java.util.ArrayList;
import java.util.List;

import wwmm.crawler.ArticleDetails;
import wwmm.crawler.IssueCrawler;
import wwmm.crawler.IssueDetails;

public class CifIssueCrawler {

	IssueCrawler issueCrawler;
	
	public CifIssueCrawler(IssueCrawler issueCrawler) {
		this.issueCrawler = issueCrawler;
	}
	
	public List<ArticleDetails> getDetailsForCurrentArticles() {
		IssueDetails issueDetails = issueCrawler.getCurrentIssueDetails();
		return getDetailsForArticles(issueDetails);
	}
	
	public List<ArticleDetails> getDetailsForArticles(IssueDetails issueDetails) {
		List<ArticleDetails> adList = issueCrawler.getDetailsForArticles(issueDetails);
		List<ArticleDetails> cifAdList = new ArrayList<ArticleDetails>();
		for (ArticleDetails ad : adList) {
			
		}
		return cifAdList;
	}
	
}
