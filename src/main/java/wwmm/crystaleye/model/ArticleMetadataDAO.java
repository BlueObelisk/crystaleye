package wwmm.crystaleye.model;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

/**
 * <p>
 * Data-access class for accessing article metadata in the 
 * CrystalEye database.
 * </p>
 * 
 * @author Nick Day
 * @version 0.1
 */
public class ArticleMetadataDAO {
	
	// FIXME - instead of having unrelated separate classes like this, there
	// should be an abstract FileDAO with these variable and method names.
	
	private PrimaryKeyDAO keyDao;
	private File storageRoot;
	public static final String ARTICLE_METADATA_MIME = ".mets.xml";
	
	private static final Logger LOG = Logger.getLogger(ArticleMetadataDAO.class);
	
	public ArticleMetadataDAO(File storageRoot) {
		init(storageRoot);
	}
	
	/**
	 * <p>
	 * Provide the root folder at which the CrystalEye database sits.
	 * </p>
	 * 
	 * @param storageRoot - the root folder at which the CrystalEye 
	 * database sits.
	 */
	private void init(File storageRoot) {
		this.storageRoot = storageRoot;
		this.keyDao = new PrimaryKeyDAO(storageRoot);
	}
	
	/**
	 * <p>
	 * Insert the metadata provided in the <code>String</code> parameter 
	 * at the provided primary key location.
	 * </p>
	 * 
	 * @param primaryKey - int of the primary key that the metadata is 
	 * to be inserted to.
	 * @param metadata - contains the contents of the metadata to be 
	 * written to the database.
	 * 
	 * @return true if the metadata was successfully added to the 
	 * database, false if not.
	 */
	public boolean insert(int primaryKey, String metadata) {
		File keyFile = keyDao.getFileFromKey(primaryKey);
		if (keyFile == null) {
			LOG.warn("The primary key provided ("+primaryKey+") does not exist in the database.");
			return false;
		}
		File metadataFile = new File(keyFile, primaryKey+ARTICLE_METADATA_MIME);
		LOG.info("Inserting article metadata to: "+metadataFile.getAbsolutePath());
		try {
			FileUtils.writeStringToFile(metadataFile, metadata);
		} catch (IOException e) {
			LOG.warn("Could not write metadata string to: "+metadataFile.getAbsolutePath(), e);
			return false;
		}
		return true;
	}

}
