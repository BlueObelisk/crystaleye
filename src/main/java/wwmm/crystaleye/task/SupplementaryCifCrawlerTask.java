package wwmm.crystaleye.task;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

import wwmm.crystaleye.crawler.core.ArticleDetails;
import wwmm.crystaleye.crawler.core.RscIssueCrawler;
import wwmm.crystaleye.crawler.core.RscJournal;
import wwmm.crystaleye.crawler.impl.CifIssueCrawler;
import wwmm.crystaleye.crawler.impl.RscCifIssueCrawler;
import wwmm.crystaleye.model.impl.SupplementaryCifDAO;

/**
 * <p>
 * Main class for handling the scraping of CIFs from publishers
 * websites and writing them to the CrystalEye database.
 * </p>
 * 
 * @author Nick Day
 * @version 0.1
 */
public class SupplementaryCifCrawlerTask {
	
	private CifIssueCrawler crawler;
	private SupplementaryCifDAO supplementaryCifDAO;

	private static final Logger LOG = Logger.getLogger(SupplementaryCifCrawlerTask.class);

	public SupplementaryCifCrawlerTask(CifIssueCrawler crawler, File storageRoot) {
		this.crawler = crawler;
		supplementaryCifDAO = new SupplementaryCifDAO(storageRoot);
	}	

	/**
	 * <p>
	 * Starts off the crawler, which finds articles with CIFs as
	 * supplementary data.  The ArticleDetails are passed to a
	 * data-access object, which will then get the CIFs and write 
	 * them out to the database.
	 * </p> 
	 * 
	 */
	public void crawl() {
		for (ArticleDetails ad : crawler.getDetailsForCurrentArticles()) {
			supplementaryCifDAO.insert(ad);
		}
	}
	
	/**
	 * <p>
	 * Main method is only for demonstration of class use.  Does not
	 * require any arguments.
	 * </p>
	 * 
	 */
	public static void main(String[] args) throws IOException {
		String root = "c:/Users/ned24/workspace/crystaleye-data";
		File storageRoot = new File(root);
		RscIssueCrawler rscCrawler = new RscIssueCrawler(RscJournal.DALTON_TRANSACTIONS);
		CifIssueCrawler crawler = new RscCifIssueCrawler(rscCrawler);
		SupplementaryCifCrawlerTask ct = new SupplementaryCifCrawlerTask(crawler, storageRoot);
		ct.crawl();
	}
}
