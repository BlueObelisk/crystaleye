package wwmm.crystaleye.model;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

public class ArticleMetadataDAO {
	
	private PrimaryKeyDAO keyDao;
	private File storageRoot;
	private static final String ARTICLE_METADATA_MIME = ".mets.xml";
	
	private static final Logger LOG = Logger.getLogger(ArticleMetadataDAO.class);
	
	public ArticleMetadataDAO(File storageRoot) {
		setStorageRoot(storageRoot);
	}
	
	/**
	 * Provide the root folder at which the CrystalEye database sits.
	 * 
	 * @param storageRoot - the root folder at which the CrystalEye 
	 * database sits.
	 */
	public void setStorageRoot(File storageRoot) {
		this.storageRoot = storageRoot;
		this.keyDao = new PrimaryKeyDAO(storageRoot);
	}
	
	public void insertArticleMetadata(int primaryKey, String metadata) {
		File keyFile = keyDao.getFileFromKey(primaryKey);
		File metadataFile = new File(keyFile, primaryKey+ARTICLE_METADATA_MIME);
		LOG.info("Inserting CIF to: "+metadataFile.getAbsolutePath());
		try {
			FileUtils.writeStringToFile(metadataFile, metadata);
		} catch (IOException e) {
			throw new RuntimeException("Could not write metadata string to: "+metadataFile.getAbsolutePath(), e);
		}
	}

}
