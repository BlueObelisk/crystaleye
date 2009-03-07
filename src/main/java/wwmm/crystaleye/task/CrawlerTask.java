package wwmm.crystaleye.task;

import java.io.File;
import java.io.IOException;

import wwmm.crystaleye.crawler.ActaIssueCrawler;
import wwmm.crystaleye.crawler.ActaJournal;
import wwmm.crystaleye.crawler.ArticleDetails;
import wwmm.crystaleye.crawler.CrawlerHttpClient;
import wwmm.crystaleye.crawler.SupplementaryFileDetails;
import wwmm.crystaleye.crawler.cif.ActaCifIssueCrawler;
import wwmm.crystaleye.crawler.cif.CifFileDetails;
import wwmm.crystaleye.crawler.cif.CifIssueCrawler;
import wwmm.crystaleye.model.ArticleMetadataDAO;
import wwmm.crystaleye.model.CifDAO;

public class CrawlerTask {

	CifDAO cifDao;
	ArticleMetadataDAO articleMetadataDao;
	CifIssueCrawler crawler;

	public CrawlerTask(CifIssueCrawler crawler, File storageRoot) {
		initDAOs(storageRoot);
		this.crawler = crawler;
	}

	private void initDAOs(File storageRoot) {
		cifDao = new CifDAO(storageRoot);
		articleMetadataDao = new ArticleMetadataDAO(storageRoot);
	}

	public void crawl() {
		for (ArticleDetails ad : crawler.getDetailsForCurrentArticles()) {
			for (SupplementaryFileDetails sfd : ad.getSuppFiles()) {
				if (isCifFile(sfd)) {
					int key = writeCIF(sfd);
					writeMetadata(key, sfd);
				}
			}
		}
	}
	
	private void writeMetadata(int primaryKey, SupplementaryFileDetails sfd) {
		String metadata = sfd.toString();
		articleMetadataDao.insertArticleMetadata(primaryKey, metadata);
	}
	
	private int writeCIF(SupplementaryFileDetails sfd) {
		String cifContents = new CrawlerHttpClient().getResourceString(sfd.getURI());
		return cifDao.insertCif(cifContents);
	}

	private boolean isCifFile(SupplementaryFileDetails sfd) {
		if (sfd instanceof CifFileDetails) {
			return true;
		} else {
			return false;
		}
	}

	public static void main(String[] args) throws IOException {
		String root = "c:/work/crystaleye1.2-data";
		File storageRoot = new File(root);
		ActaIssueCrawler actaCrawler = new ActaIssueCrawler(ActaJournal.SECTION_C);
		CifIssueCrawler crawler = new ActaCifIssueCrawler(actaCrawler);
		CrawlerTask ct = new CrawlerTask(crawler, storageRoot);
		ct.crawl();
	}
}
