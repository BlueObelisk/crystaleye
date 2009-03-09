package wwmm.crystaleye.task;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

import wwmm.crystaleye.crawler.AcsIssueCrawler;
import wwmm.crystaleye.crawler.AcsJournal;
import wwmm.crystaleye.crawler.ArticleDetails;
import wwmm.crystaleye.crawler.CrawlerHttpClient;
import wwmm.crystaleye.crawler.DOI;
import wwmm.crystaleye.crawler.SupplementaryFileDetails;
import wwmm.crystaleye.crawler.cif.AcsCifIssueCrawler;
import wwmm.crystaleye.crawler.cif.CifFileDetails;
import wwmm.crystaleye.crawler.cif.CifIssueCrawler;
import wwmm.crystaleye.index.DoiVsCifFilenameIndex;
import wwmm.crystaleye.index.PrimaryKeyVsDoiIndex;
import wwmm.crystaleye.model.ArticleMetadataDAO;
import wwmm.crystaleye.model.CifFileDAO;

public class CrawlerTask {

	private CifFileDAO cifDao;
	private ArticleMetadataDAO articleMetadataDao;
	private CifIssueCrawler crawler;
	private DoiVsCifFilenameIndex doiVsCifFilenameIndex;
	private PrimaryKeyVsDoiIndex pKeyVsDoiIndex;

	private static final Logger LOG = Logger.getLogger(CrawlerTask.class);

	public CrawlerTask(CifIssueCrawler crawler, File storageRoot) {
		initDAOs(storageRoot);
		this.crawler = crawler;
	}

	private void initDAOs(File storageRoot) {
		cifDao = new CifFileDAO(storageRoot);
		articleMetadataDao = new ArticleMetadataDAO(storageRoot);
		doiVsCifFilenameIndex = new DoiVsCifFilenameIndex(storageRoot);
		pKeyVsDoiIndex = new PrimaryKeyVsDoiIndex(storageRoot);
	}

	public void crawl() {
		for (ArticleDetails ad : crawler.getDetailsForCurrentArticles()) {
			for (SupplementaryFileDetails sfd : ad.getSuppFiles()) {
				if (!isCifFile(sfd)) {
					continue;
				}
				String filename = sfd.getFilename();
				DOI doi = ad.getDoi();
				boolean alreadyInDb = doiVsCifFilenameIndex.contains(doi, filename);
				if (alreadyInDb) {
					continue;
				}
				int primaryKey = writeCIF(sfd);
				insertArticleMetadata(primaryKey, sfd);
				updatePKeyVsDoiIndex(primaryKey, doi);
				updateDoiVsCifFilenameIndex(doi, filename);
			}
		}
	}
	
	private void insertArticleMetadata(int primaryKey, SupplementaryFileDetails sfd) {
		boolean success = articleMetadataDao.insert(primaryKey, sfd.toString());
		if (!success) {
			LOG.warn("Problem inserting article metadata.");
		}
	}
	
	private void updateDoiVsCifFilenameIndex(DOI doi, String filename) {
		boolean success = doiVsCifFilenameIndex.insert(doi, filename);
		if (!success) {
			LOG.warn("Problem updating DOI vs CIF filename index.");
		}
	}

	private void updatePKeyVsDoiIndex(int primaryKey, DOI doi) {
		boolean success = pKeyVsDoiIndex.insert(primaryKey, doi);
		if (!success) {
			LOG.warn("Problem updating PrimaryKey vs DOI index.");
		}
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
		//ActaIssueCrawler actaCrawler = new ActaIssueCrawler(ActaJournal.SECTION_B);
		AcsIssueCrawler acsCrawler = new AcsIssueCrawler(AcsJournal.CRYSTAL_GROWTH_AND_DESIGN);
		CifIssueCrawler crawler = new AcsCifIssueCrawler(acsCrawler);
		CrawlerTask ct = new CrawlerTask(crawler, storageRoot);
		ct.crawl();
	}
}
