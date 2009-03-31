package wwmm.crystaleye.model.crystaleye;

import java.io.File;

import wwmm.crystaleye.model.core.SecondaryFileDAO;

/**
 * <p>
 * Data-access class for accessing the metadata associated with a
 * CIF that was obtained from published article supplementary data.
 * </p>
 * 
 * @author Nick Day
 * @version 0.1
 */
public class SupplementaryCifMetadataDAO extends SecondaryFileDAO {
	
	/**
	 * An implementing subclass of SecondaryFileDAO needs to set the
	 * file extension to be used by the primary file of the database.
	 */
	static {
		fileExtension = ".bibliontology.xml";
	}

	public SupplementaryCifMetadataDAO(File storageRoot) {
		super(storageRoot);
	}
	
	/**
	 * <p>
	 * Gets the file extension used for this file.
	 * </p>
	 * 
	 * @return the file extension used for this file.
	 */
	public static String getFileExtension() {
		return fileExtension;
	}

}
