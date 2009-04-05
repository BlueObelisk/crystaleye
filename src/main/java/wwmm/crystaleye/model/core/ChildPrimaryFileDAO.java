package wwmm.crystaleye.model.core;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

/**
 * <p>
 * Data-access class for the primary file for each 'child' folder.
 * Each 'child' folder corresponds to one crystal structure, and the
 * primary file is the source of all derived data for that structure.
 * Inserting a child primary file adds a new child key to a primary 
 * key.
 * </p>
 * 
 * @author Nick Day
 * @version 0.1
 */
public abstract class ChildPrimaryFileDAO {
	
	private ChildKeyDAO keyDao;
	protected String fileExtension;

	private static final Logger LOG = Logger.getLogger(ChildPrimaryFileDAO.class);

	protected ChildPrimaryFileDAO(File storageRoot, String fileExtension) {
		init(storageRoot);
		this.fileExtension = fileExtension;
	}
	
	/**
	 * <p>
	 * Provide the root folder at which the CrystalEye database sits.
	 * </p>
	 * 
	 * @param storageRoot - the root folder at which the CrystalEye 
	 * database sits.
	 */
	private void init(File storageRoot) {
		this.keyDao = new ChildKeyDAO(storageRoot);
	}
	
	/**
	 * <p>
	 * Adds the contents in the provided String into the database.
	 * Returns the child key that the inserted content has been 
	 * assigned.
	 * </p>
	 * 
	 * @param primaryKey to insert the child primary file into.
	 * @param fileContents - String containing the contents to be 
	 * written to the database.
	 * 
	 * @return the primary key that the written CIF has been 
	 * assigned or -1 if there was a problem during insertion.
	 */
	public int insert(int primaryKey, String fileContents) {
		if (fileExtension == null) {
			throw new IllegalStateException("fileExtension field has not " +
					"been set in the implementing subclass of PrimaryFileDAO.");
		}
		int childKey = keyDao.insert(primaryKey);
		File childKeyFolder = keyDao.getFolderFromKeys(primaryKey, childKey);
		File childPrimaryFile = new File(childKeyFolder, childKey+fileExtension);
		if (childPrimaryFile.exists()) {
			LOG.warn("Cannot insert file as it already exists: "+childPrimaryFile);
			return -1;
		}
		LOG.info("Inserting file to: "+childPrimaryFile.getAbsolutePath());
		try {
			FileUtils.writeStringToFile(childPrimaryFile, fileContents);
		} catch (IOException e) {
			LOG.warn("Problem trying to insert primary file to: "+childPrimaryFile.getAbsolutePath()+"\n" +
					e.getMessage());
			return -1;
		}
		return childKey;
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
					"been set in the implementing subclass of ChildPrimaryFileDAO.");
		}
		File childKeyFolder = keyDao.getFolderFromKeys(primaryKey, childKey);
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
