package wwmm.crystaleye.crawler.cif;

import wwmm.crystaleye.crawler.ActaIssueCrawler;
import wwmm.crystaleye.crawler.ArticleDetails;

public class ActaCifIssueCrawler extends CifIssueCrawler {

	public ActaCifIssueCrawler(ActaIssueCrawler crawler) {
		super(crawler);
	}

	@Override
	protected boolean isCifArticle(ArticleDetails details) {
		// TODO Auto-generated method stub
		return false;
	}
	
}
