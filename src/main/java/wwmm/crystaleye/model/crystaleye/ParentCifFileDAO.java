package wwmm.crystaleye.model.crystaleye;

import java.io.File;

import wwmm.crystaleye.model.core.PrimaryFileDAO;

/**
 * <p>
 * Data-access class for accessing 'parent' CIFs in the CrystalEye 
 * database. These are CIFs in the form they are first deposited 
 * into the db, and may contain data for more than one structure.
 * They are the source of all derived data in CrystalEye. 
 * </p>
 * 
 * @author Nick Day
 * @version 0.1
 */
public class ParentCifFileDAO extends PrimaryFileDAO {

	/**
	 * The implementing subclass of PrimaryFileDAO needs to set the
	 * file extension to be used by the primary file of the database.
	 */
	static {
		fileExtension = ".cif";
	}
	
	public ParentCifFileDAO(File storageRoot) {
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
