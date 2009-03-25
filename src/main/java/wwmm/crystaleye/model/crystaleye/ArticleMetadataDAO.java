package wwmm.crystaleye.model.crystaleye;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

import wwmm.crystaleye.model.core.NonPrimaryFileDAO;

/**
 * <p>
 * Data-access class for accessing article metadata in the 
 * CrystalEye database.
 * </p>
 * 
 * @author Nick Day
 * @version 0.1
 */
public class ArticleMetadataDAO extends NonPrimaryFileDAO {

	public static final String ARTICLE_METADATA_MIME = ".bibliontology.xml";

	private static final Logger LOG = Logger.getLogger(ArticleMetadataDAO.class);

	public ArticleMetadataDAO(File storageRoot) {
		super(storageRoot);
	}

	public boolean insert(int primaryKey, String metadataContents) {
		try {
			return insert(primaryKey, metadataContents, ARTICLE_METADATA_MIME);
		} catch (IOException e) {
			LOG.warn("Problem writing metadata file to key: "+primaryKey);
			return false;
		}
	}

}
