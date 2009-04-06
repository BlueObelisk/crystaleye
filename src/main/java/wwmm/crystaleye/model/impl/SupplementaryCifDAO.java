package wwmm.crystaleye.model.impl;

import static wwmm.crystaleye.CrystalEyeConstants.CIF_CONTENT_TYPE;

import java.io.File;

import org.apache.log4j.Logger;

import wwmm.crystaleye.crawler.core.ArticleDetails;
import wwmm.crystaleye.crawler.core.CrawlerHttpClient;
import wwmm.crystaleye.crawler.core.DOI;
import wwmm.crystaleye.crawler.core.SupplementaryFileDetails;
import wwmm.crystaleye.index.impl.DoiVsCifFilenameIndex;
import wwmm.crystaleye.index.impl.PrimaryKeyVsDoiIndex;
import wwmm.crystaleye.tools.BibliontologyTool;

/**
 * <p>
 * Data-access class that takes the details of an article 
 * containing a CIF a supplementary data, and writes out the
 * CIF and related data to the database.  Also updates related
 * indexes.
 * </p>
 * 
 * @author Nick Day
 * @version 0.1
 *
 */
public class SupplementaryCifDAO {
	
	private ParentCifFileDAO cifDao;
	private SupplementaryCifMetadataDAO articleMetadataDao;
	private DoiVsCifFilenameIndex doiVsCifFilenameIndex;
	private PrimaryKeyVsDoiIndex pKeyVsDoiIndex;
	
	private static final Logger LOG = Logger.getLogger(SupplementaryCifDAO.class);
	
	public SupplementaryCifDAO(File storageRoot) {
		init(storageRoot);
	}
	
	/**
	 * <p>
	 * Initialises the database storage root and data-access objects 
	 * used to write the data.
	 * </p>
	 * 
	 * @param storageRoot - the root folder of the database.
	 */
	private void init(File storageRoot) {
		cifDao = new ParentCifFileDAO(storageRoot);
		articleMetadataDao = new SupplementaryCifMetadataDAO(storageRoot);
		doiVsCifFilenameIndex = new DoiVsCifFilenameIndex(storageRoot);
		pKeyVsDoiIndex = new PrimaryKeyVsDoiIndex(storageRoot);
	}
	
	/**
	 * <p>
	 * Checks the provided article details and writes out any CIF
	 * files that are attached as supplemental data to the database.  
	 * Also writes out related article metadata and updates related
	 * indexes.
	 * </p>
	 * 
	 * @param ad - details of a published article containing a CIF as
	 * supplemental data.
	 */
	public void insert(ArticleDetails ad) {
		// FIXME - need to do cleanup if later file writing fails when
		// some has already been written?
		
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
			int primaryKey = cifDao.insert(cifContents);
			String articleMetadata = new BibliontologyTool(ad).toString();
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
	
	/**
	 * <p>
	 * Checks whether the SupplementaryFileDetails
	 * is an instance of CifFileDetails.
	 * </p>
	 * 
	 * @param sfd - details to be checked.
	 * 
	 * @return true if the instance is a CifFileDetails, false
	 * if not.
	 */
	private boolean isCifFile(SupplementaryFileDetails sfd) {
		String contentType = sfd.getContentType();
		if (contentType.contains(CIF_CONTENT_TYPE)) {
			return true;
		} else {
			return false;
		}
	}
	
}
