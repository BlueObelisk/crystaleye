package wwmm.crystaleye.task;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

import wwmm.crystaleye.crawler.ArticleDetails;
import wwmm.crystaleye.crawler.CrawlerHttpClient;
import wwmm.crystaleye.crawler.DOI;
import wwmm.crystaleye.crawler.RscIssueCrawler;
import wwmm.crystaleye.crawler.RscJournal;
import wwmm.crystaleye.crawler.SupplementaryFileDetails;
import wwmm.crystaleye.crawler.cif.CifFileDetails;
import wwmm.crystaleye.crawler.cif.CifIssueCrawler;
import wwmm.crystaleye.crawler.cif.RscCifIssueCrawler;
import wwmm.crystaleye.index.DoiVsCifFilenameIndex;
import wwmm.crystaleye.index.PrimaryKeyVsDoiIndex;
import wwmm.crystaleye.model.ArticleMetadataDAO;
import wwmm.crystaleye.model.CifFileDAO;

/**
 * <p>
 * Main class for handling the scraping of CIFs from websites and
 * writing them to the CrystalEye database.
 * </p>
 * 
 * @author Nick Day
 * @version 0.1
 */
public class CrawlerTask {
	
	// TODO pull out all the DAO stuff and moved to a CIFRecordDAO.

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

	/**
	 * <p>
	 * Starts off the crawler, which finds CIFs and then writes the
	 * scraped data out to the database.
	 * </p> 
	 * 
	 */
	public void crawl() {
		for (ArticleDetails ad : crawler.getDetailsForCurrentArticles()) {
			for (SupplementaryFileDetails sfd : ad.getSuppFiles()) {
				if (!isCifFile(sfd)) {
					continue;
				}
				String filename = sfd.getFileId();
				DOI doi = ad.getDoi();
				boolean alreadyInDb = doiVsCifFilenameIndex.contains(doi, filename);
				if (alreadyInDb) {
					continue;
				}
				String cifContents = new CrawlerHttpClient().getResourceString(sfd.getURI());
				int primaryKey = cifDao.insertCif(cifContents);
				String articleMetadata = new BibliontTool(ad).toString();
				if (!articleMetadataDao.insert(primaryKey, articleMetadata)) {
					LOG.warn("Problem inserting article metadata.");
				}
				if (!pKeyVsDoiIndex.insert(primaryKey, doi)) {
					LOG.warn("Problem updating PrimaryKey vs DOI index.");
				}
				if (!doiVsCifFilenameIndex.insert(doi, filename)) {
					LOG.warn("Problem updating DOI vs CIF filename index.");
				}
			}
		}
	}
	
	/**
	 * <p>
	 * Initialises the data-access objects used to write the scraped
	 * data.
	 * </p>
	 * 
	 * @param storageRoot - the root folder of the database.
	 */
	private void initDAOs(File storageRoot) {
		cifDao = new CifFileDAO(storageRoot);
		articleMetadataDao = new ArticleMetadataDAO(storageRoot);
		doiVsCifFilenameIndex = new DoiVsCifFilenameIndex(storageRoot);
		pKeyVsDoiIndex = new PrimaryKeyVsDoiIndex(storageRoot);
	}

	/**
	 * <p>
	 * Checks whether the provided SupplementaryFileDetails
	 * is an instance of CifFileDetails.
	 * </p>
	 * 
	 * @param sfd - details to be checked.
	 * 
	 * @return true if the instance is a CifFileDetails, false
	 * if not.
	 */
	private boolean isCifFile(SupplementaryFileDetails sfd) {
		if (sfd instanceof CifFileDetails) {
			return true;
		} else {
			return false;
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
		//ActaIssueCrawler actaCrawler = new ActaIssueCrawler(ActaJournal.SECTION_B);
		//AcsIssueCrawler acsCrawler = new AcsIssueCrawler(AcsJournal.CRYSTAL_GROWTH_AND_DESIGN);
		RscIssueCrawler rscCrawler = new RscIssueCrawler(RscJournal.DALTON_TRANSACTIONS);
		CifIssueCrawler crawler = new RscCifIssueCrawler(rscCrawler);
		CrawlerTask ct = new CrawlerTask(crawler, storageRoot);
		ct.crawl();
	}
}
