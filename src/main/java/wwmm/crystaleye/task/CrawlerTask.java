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
				int primaryKey = writeCIF(sfd);
				insertArticleMetadata(primaryKey, sfd);
				updatePKeyVsDoiIndex(primaryKey, doi);
				updateDoiVsCifFilenameIndex(doi, filename);
			}
		}
	}
	
	/**
	 * <p>
	 * Writes a file out containing the provided article metadata.
	 * </p>
	 * 
	 * @param primaryKey - the key for which you are inserting the
	 * metadata.
	 * @param sfd - details about the supplementary file.
	 */
	private void insertArticleMetadata(int primaryKey, SupplementaryFileDetails sfd) {
		boolean success = articleMetadataDao.insert(primaryKey, sfd.toString());
		if (!success) {
			LOG.warn("Problem inserting article metadata.");
		}
	}
	
	/**
	 * <p>
	 * Updates the index of the CIFs origin DOI against the CIFs 
	 * file name at the site it was scraped from. 
	 * </p>
	 * 
	 * @param doi of the article the CIF was obtained from.
	 * @param filename of the CIF at the site it was scraped from.
	 */
	private void updateDoiVsCifFilenameIndex(DOI doi, String filename) {
		boolean success = doiVsCifFilenameIndex.insert(doi, filename);
		if (!success) {
			LOG.warn("Problem updating DOI vs CIF filename index.");
		}
	}

	/**
	 * <p>
	 * Updates an index of the primary key for the CIF against
	 * the DOI of the article the CIF was scraped from.
	 * </p>
	 * 
	 * @param primaryKey of the CIF in the database.
	 * @param doi of the article the CIF was obtained from.
	 */
	private void updatePKeyVsDoiIndex(int primaryKey, DOI doi) {
		boolean success = pKeyVsDoiIndex.insert(primaryKey, doi);
		if (!success) {
			LOG.warn("Problem updating PrimaryKey vs DOI index.");
		}
	}

	/**
	 * <p>
	 * Writes the CIF described by the provided 
	 * <code>SupplementaryFileDetails</code> to the database.
	 * </p>
	 * 
	 * @param sfd - details of the CIF to be written.
	 * 
	 * @return the database primary key that the CIF was 
	 * assigned when it was inserted into the database.
	 */
	private int writeCIF(SupplementaryFileDetails sfd) {
		String cifContents = new CrawlerHttpClient().getResourceString(sfd.getURI());
		return cifDao.insertCif(cifContents);
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
		String root = "c:/work/crystaleye1.2-data";
		File storageRoot = new File(root);
		//ActaIssueCrawler actaCrawler = new ActaIssueCrawler(ActaJournal.SECTION_B);
		AcsIssueCrawler acsCrawler = new AcsIssueCrawler(AcsJournal.CRYSTAL_GROWTH_AND_DESIGN);
		CifIssueCrawler crawler = new AcsCifIssueCrawler(acsCrawler);
		CrawlerTask ct = new CrawlerTask(crawler, storageRoot);
		ct.crawl();
	}
}
