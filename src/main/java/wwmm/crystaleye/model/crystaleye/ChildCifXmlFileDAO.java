package wwmm.crystaleye.model.crystaleye;

import java.io.File;

import wwmm.crystaleye.model.core.ChildPrimaryFileDAO;

/**
 * <p>
 * Data-access class for accessing 'child' CIFXML files in CrystalEye.
 * These are generated from the Parent CIFXML file, and act as the
 * primary file for data corresponding to a single crystal structure
 * (which is the basis for the data provided by CrystalEye).
 * </p>
 * 
 * @author Nick Day
 * @version 0.1
 */
public class ChildCifXmlFileDAO extends ChildPrimaryFileDAO {
	
	/**
	 * The implementing subclass of PrimaryFileDAO needs to set the
	 * file extension to be used by the primary file of the database.
	 */
	static {
		fileExtension = ".cif.xml";
	}
	
	public ChildCifXmlFileDAO(File storageRoot) {
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
