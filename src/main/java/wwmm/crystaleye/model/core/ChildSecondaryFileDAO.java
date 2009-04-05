package wwmm.crystaleye.model.core;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

/**
 * <p>
 * Data-access class for a secondary file in a 'child' folder.
 * Each 'child' folder corresponds to one crystal structure, and the
 * secondary files are derived from the primary file for that 'child'.
 * Inserting a secondary file will not create a new child key, instead
 * the child key must be provided for the secondary file to be written 
 * to.
 * </p>
 * 
 * @author Nick Day
 * @version 0.1
 */
public class ChildSecondaryFileDAO {
	
	private ChildKeyDAO childKeyDao;
	protected String fileExtension;

	private static final Logger LOG = Logger.getLogger(ChildSecondaryFileDAO.class);

	protected ChildSecondaryFileDAO(File storageRoot, String fileExtension) {
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
		this.childKeyDao = new ChildKeyDAO(storageRoot);
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
	 */
	public boolean insert(int primaryKey, int childKey, String fileContents) {
		if (fileExtension == null) {
			throw new IllegalStateException("fileExtension field has not " +
					"been set in the implementing subclass of ChildPrimaryFileDAO.");
		}
		File childKeyFolder = childKeyDao.getFolderFromKeys(primaryKey, childKey);
		if (childKeyFolder == null) {
			LOG.warn("The combination of primary and child key provided ("+primaryKey+"/"+childKey+") does not exist in the database.");
			return false;
		}
		File childSecondaryFile = new File(childKeyFolder, childKey+fileExtension);
		if (childSecondaryFile.exists()) {
			LOG.warn("Cannot insert file as it already exists: "+childSecondaryFile);
			return false;
		}
		try {
			FileUtils.writeStringToFile(childSecondaryFile, fileContents);
		} catch (IOException e) {
			LOG.warn("Problem inserting file to: "+childSecondaryFile.getAbsolutePath()+"\n" +
					e.getMessage());
			return false;
		}
		return true;
	}
	
	/**
	 * <p>
	 * Updates the file at the provided primary and child keys with
	 * the <code>fileContents</code> parameter.
	 * </p>
	 * 
	 * @param primaryKey of the file that is to be updated.
	 * @param fileContents - contains the contents of the file to be 
	 * updated.
	 * 
	 * @return true if the file was successfully updated, false if not.
	 */
	public boolean update(int primaryKey, int childKey, String fileContents) {
		if (fileExtension == null) {
			throw new IllegalStateException("fileExtension field has not " +
					"been set in the implementing subclass of ChildPrimaryFileDAO.");
		}
		File childKeyFolder = childKeyDao.getFolderFromKeys(primaryKey, childKey);
		if (childKeyFolder == null) {
			LOG.warn("The combination of primary and child key provided ("+primaryKey+"/"+childKey+") does not exist in the database.");
			return false;
		}
		File childSecondaryFile = new File(childKeyFolder, childKey+fileExtension);
		if (!childSecondaryFile.exists()) {
			LOG.warn("Cannot update file as it does not exist: "+childSecondaryFile);
			return false;
		}
		try {
			FileUtils.writeStringToFile(childSecondaryFile, fileContents);
		} catch (IOException e) {
			LOG.warn("Problem inserting file to: "+childSecondaryFile.getAbsolutePath()+"\n" +
					e.getMessage());
			return false;
		}
		return true;
	}
	
	/**
	 * <p>
	 * Returns the child primary file associated with the primary 
	 * key and child key provided. 
	 * </p>
	 *  
	 * @param primaryKey of the primary file you wish returned.
	 * @param childKey of the primary file you wish returned.
	 * 
	 * @return File of the child primary file at the provided 
	 * primary and child keys. If it does not exist, then null is 
	 * returned.
	 */
	public File getFileFromKeys(int primaryKey, int childKey) {
		// TODO - note that this is the same method used in ChildSecondaryFileDAO
		// perhaps extract into superclass?
		if (fileExtension == null) {
			throw new IllegalStateException("fileExtension field has not " +
					"been set in the implementing subclass of ChildSecondaryFileDAO.");
		}
		File childKeyFolder = childKeyDao.getFolderFromKeys(primaryKey, childKey);
		if (childKeyFolder == null) {
			return null;
		}
		File file = new File(childKeyFolder, childKey+fileExtension);
		if (file.exists()) {
			return file;
		} else {
			return null;
		}
	}

}
