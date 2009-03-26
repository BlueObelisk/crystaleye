package wwmm.crystaleye.model.crystaleye;

import java.io.File;

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
	
	/**
	 * An implementing subclass of NonPrimaryFileDAO needs to set the
	 * file extension to be used by the primary file of the database.
	 */
	static {
		fileExtension = ".bibliontology.xml";
	}

	private static final Logger LOG = Logger.getLogger(ArticleMetadataDAO.class);

	public ArticleMetadataDAO(File storageRoot) {
		super(storageRoot);
	}

}
