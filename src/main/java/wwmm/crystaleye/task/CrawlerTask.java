package wwmm.crystaleye.task;

import java.io.File;
import java.io.IOException;

import wwmm.crystaleye.BasicHttpClient;
import wwmm.crystaleye.crawler.ActaIssueCrawler;
import wwmm.crystaleye.crawler.ActaJournal;
import wwmm.crystaleye.crawler.ArticleDetails;
import wwmm.crystaleye.crawler.IssueCrawler;
import wwmm.crystaleye.crawler.SupplementaryFileDetails;
import wwmm.crystaleye.crawler.cif.ActaCifIssueCrawler;
import wwmm.crystaleye.crawler.cif.CifIssueCrawler;
import wwmm.crystaleye.model.CifDAO;


public class CrawlerTask {
	
	CifDAO cifDao;
	CifIssueCrawler crawler;
	
	public CrawlerTask(CifIssueCrawler crawler, File storageRoot) {
		initCifDao(storageRoot);
		this.crawler = crawler;
	}
	
	private void initCifDao(File storageRoot) {
		cifDao = new CifDAO(storageRoot);
	}
	
	public void crawl() throws IOException {
		for (ArticleDetails ad : crawler.getDetailsForCurrentArticles()) {
			System.out.println(ad.toString());
			// FIXME - need to finish this.
		}
	}
	
	public static void main(String[] args) throws IOException {
		String root = "e:/crystaleye1.2-data";
		File storageRoot = new File(root);
		ActaIssueCrawler actaCrawler = new ActaIssueCrawler(ActaJournal.SECTION_C);
		CifIssueCrawler crawler = new ActaCifIssueCrawler(actaCrawler);
		CrawlerTask ct = new CrawlerTask(crawler, storageRoot);
		ct.crawl();
	}
}
