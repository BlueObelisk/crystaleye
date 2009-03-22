package wwmm.crystaleye.model;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

/**
 * <p>
 * Data-access class that manages access to files that are related
 * to the database primary files (which are managed by 
 * <code>PrimaryFileDAO</code>.  The difference being that non-primary
 * files are not assigned a primary key when they are written.  Instead
 * you must provide the primary key for the non-primary file to be 
 * written to, and then they will be written alongside the primary file.
 * </p>
 * 
 * @author Nick Day
 * @version 0.1
 */
public abstract class NonPrimaryFileDAO {

	private PrimaryKeyDAO keyDao;

	private static final Logger LOG = Logger.getLogger(NonPrimaryFileDAO.class);

	protected NonPrimaryFileDAO(File storageRoot) {
		init(storageRoot);
	}

	/**
	 * <p>
	 * Provide the root folder at which the database sits.
	 * </p>
	 * 
	 * @param storageRoot - the root folder at which the database 
	 * sits.
	 */
	private void init(File storageRoot) {
		this.keyDao = new PrimaryKeyDAO(storageRoot);
	}

	/**
	 * <p>
	 * Insert a file containing the contents provided in the 
	 * <code>String</code> parameter at the provided primary key 
	 * location.
	 * </p>
	 * 
	 * @param primaryKey - int of the primary key that the file is 
	 * to be inserted to.
	 * @param fileContents - contains the contents of the file to be 
	 * written to the database.
	 * 
	 * @return true if the file was successfully added to the 
	 * database, false if not.
	 * 
	 * @throws IOException if there is a problem writing the file
	 * to the database. 
	 */
	public boolean insert(int primaryKey, String fileContents, String fileMime) throws IOException {
		File keyFolder = keyDao.getFileFromKey(primaryKey);
		if (keyFolder == null) {
			LOG.warn("The primary key provided ("+primaryKey+") does not exist in the database.");
			return false;
		}
		File nonPrimaryFile = new File(keyFolder, primaryKey+fileMime);
		LOG.info("Inserting article metadata to: "+nonPrimaryFile.getAbsolutePath());
		FileUtils.writeStringToFile(nonPrimaryFile, fileContents);
		return true;
	}

}
