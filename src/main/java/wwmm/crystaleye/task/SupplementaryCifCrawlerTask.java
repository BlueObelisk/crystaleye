package wwmm.crystaleye.task;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

import wwmm.crystaleye.crawler.ArticleDetails;
import wwmm.crystaleye.crawler.RscIssueCrawler;
import wwmm.crystaleye.crawler.RscJournal;
import wwmm.crystaleye.crawler.cif.CifIssueCrawler;
import wwmm.crystaleye.crawler.cif.RscCifIssueCrawler;
import wwmm.crystaleye.model.SupplementaryCifDAO;

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
	 * Starts off the crawler, which finds CIFs and then writes the
	 * scraped data out to the database.
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
		String root = "e:/crystaleye1.2-data";
		File storageRoot = new File(root);
		RscIssueCrawler rscCrawler = new RscIssueCrawler(RscJournal.DALTON_TRANSACTIONS);
		CifIssueCrawler crawler = new RscCifIssueCrawler(rscCrawler);
		SupplementaryCifCrawlerTask ct = new SupplementaryCifCrawlerTask(crawler, storageRoot);
		ct.crawl();
	}
}
