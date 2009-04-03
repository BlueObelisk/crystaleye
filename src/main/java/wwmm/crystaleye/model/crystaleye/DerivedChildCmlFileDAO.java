package wwmm.crystaleye.model.crystaleye;

import java.io.File;

import wwmm.crystaleye.model.core.ChildSecondaryFileDAO;

/**
 * <p>
 * Data-access class for accessing the 'derived' CML files in 
 * CrystalEye that are literal translations of the CIFs.
 * </p>
 * 
 * @author Nick Day
 * @version 0.1
 */
public class DerivedChildCmlFileDAO extends ChildSecondaryFileDAO {
	
	/**
	 * An implementing subclass of ChildSecondaryFileDAO needs to set the
	 * file extension to be used by the primary file of the database.
	 */
	static {
		fileExtension = ".derived.cml";
	}

	public DerivedChildCmlFileDAO(File storageRoot) {
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
