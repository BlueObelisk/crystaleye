package wwmm.crystaleye.crawler.cif;

import java.util.ArrayList;
import java.util.List;

import wwmm.crystaleye.crawler.ArticleDetails;
import wwmm.crystaleye.crawler.IssueCrawler;
import wwmm.crystaleye.crawler.IssueDetails;

public abstract class CifIssueCrawler {

	IssueCrawler crawler;
	
	public CifIssueCrawler(IssueCrawler crawler) {
		this.crawler = crawler;
	}
	
	final public List<ArticleDetails> getDetailsForArticles(IssueDetails details) {
		List<ArticleDetails> adList = crawler.getDetailsForArticles(details);
		List<ArticleDetails> cifAdList = new ArrayList<ArticleDetails>();
		for (ArticleDetails ad : adList) {
			if (isCifArticle(ad)) {
				cifAdList.add(ad);
			}
		}
		return cifAdList;
	}
	
	final public List<ArticleDetails> getDetailsForCurrentArticles() {
		IssueDetails details = crawler.getCurrentIssueDetails();
		return getDetailsForArticles(details);
	}
	
	protected abstract boolean isCifArticle(ArticleDetails details);
}
