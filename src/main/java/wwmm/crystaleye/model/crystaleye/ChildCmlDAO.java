package wwmm.crystaleye.model.crystaleye;

import java.io.File;

import wwmm.crystaleye.model.core.ChildSecondaryFileDAO;

public class ChildCmlDAO extends ChildSecondaryFileDAO {
	
	/**
	 * An implementing subclass of ChildSecondaryFileDAO needs to set the
	 * file extension to be used by the primary file of the database.
	 */
	static {
		fileExtension = ".cml";
	}

	public ChildCmlDAO(File storageRoot) {
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
