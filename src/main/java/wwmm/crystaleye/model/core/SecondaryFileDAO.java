package wwmm.crystaleye.model.core;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;


/**
 * <p>
 * Data-access class that manages access to files that are related
 * to the database primary files (which are managed by 
 * <code>PrimaryFileDAO</code>.  The difference being that secondary
 * files are not assigned a primary key when they are written.  Instead
 * you must provide the primary key for the secondary file to be 
 * written to, and then they will be written alongside the primary file.
 * </p>
 * 
 * @author Nick Day
 * @version 0.1
 */
public abstract class SecondaryFileDAO {

	private PrimaryKeyDAO keyDao;
	protected String fileExtension;

	private static final Logger LOG = Logger.getLogger(SecondaryFileDAO.class);

	protected SecondaryFileDAO(File storageRoot, String fileExtension) {
		init(storageRoot);
		this.fileExtension = fileExtension;
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
	public boolean insert(int primaryKey, String fileContents) {
		if (fileExtension == null) {
			throw new IllegalStateException("fileExtension field has not " +
					"been set in the implementing subclass of PrimaryFileDAO.");
		}
		File keyFolder = keyDao.getFolderFromKey(primaryKey);
		if (keyFolder == null) {
			LOG.warn("The primary key provided ("+primaryKey+") does not exist in the database.");
			return false;
		}
		File secondaryFile = new File(keyFolder, primaryKey+fileExtension);
		if (secondaryFile.exists()) {
			LOG.warn("Cannot insert secondary file as it already exists: "+secondaryFile);
			return false;
		}
		LOG.info("Inserting secondary file to: "+secondaryFile.getAbsolutePath());
		try {
			FileUtils.writeStringToFile(secondaryFile, fileContents);
		} catch (IOException e) {
			LOG.warn("Problem inserting file to: "+secondaryFile.getAbsolutePath()+"\n" +
					e.getMessage());
			return false;
		}
		return true;
	}
	
	/**
	 * <p>
	 * Returns the secondary file associated with the primary key and 
	 * file extension provided. 
	 * </p>
	 *  
	 * @param primaryKey of the primary file you wish returned.
	 * 
	 * @return File of the primary file at the provided primary 
	 * key. If it does not exist, then null is returned.
	 */
	public File getFileFromKey(int primaryKey) {
		// TODO - note that this is the same method used in PrimaryFileDAO
		// perhaps extract into superclass?
		if (fileExtension == null) {
			throw new IllegalStateException("fileExtension field has not " +
					"been set in the implementing subclass of SecondaryFileDAO.");
		}
		File keyFolder = keyDao.getFolderFromKey(primaryKey);
		if (keyFolder == null) {
			return null;
		}
		File file = new File(keyFolder, primaryKey+fileExtension);
		if (file.exists()) {
			return file;
		} else {
			return null;
		}
	}

}
