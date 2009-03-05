package wwmm.crystaleye.tasks;

import java.io.File;
import java.io.IOException;

import org.apache.commons.httpclient.URIException;

import wwmm.crystaleye.BasicHttpClient;
import wwmm.crystaleye.crawler.ActaIssueCrawler;
import wwmm.crystaleye.crawler.ActaJournal;
import wwmm.crystaleye.crawler.ArticleDetails;
import wwmm.crystaleye.crawler.CifIssueCrawler;
import wwmm.crystaleye.crawler.SupplementaryFileDetails;
import wwmm.crystaleye.model.CifDAO;


public class CrawlerTask {
	
	CifDAO cifDao;
	CifIssueCrawler crawler;
	
	public CrawlerTask(CifIssueCrawler crawler, File storageRoot) {
		initCifDao(storageRoot);
		this.crawler = crawler;
	}
	
	private void initCifDao(File storageRoot) {
		cifDao = new CifDAO();
		cifDao.setStorageRoot(storageRoot);
	}
	
	public void crawl() throws IOException {
		for (ArticleDetails ad : crawler.getDetailsForCurrentArticles()) {
			for (SupplementaryFileDetails sfd : ad.getSuppFiles()) {
				if (isCIF(sfd)) {
					String cifContents = new BasicHttpClient().getResourceString(sfd.getUri());
					cifDao.insertCif(cifContents);
				}
			}
		}
	}
	
	private boolean isCIF(SupplementaryFileDetails sfd) {
		try {
			if (sfd.getUri().getURI().endsWith(".cif")) {
				return true;
			}
		} catch (URIException e) {
			throw new RuntimeException("Error getting URI string from: "+sfd.getUri());
		}
		return false;
	}
	
	public static void main(String[] args) throws IOException {
		String root = "e:/crystaleye1.2-data";
		File storageRoot = new File(root);
		CifIssueCrawler crawler = new CifIssueCrawler(new ActaIssueCrawler(ActaJournal.SECTION_C));
		CrawlerTask ct = new CrawlerTask(crawler, storageRoot);
		ct.crawl();
	}
}
