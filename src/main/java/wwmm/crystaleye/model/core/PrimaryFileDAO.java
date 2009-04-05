package wwmm.crystaleye.model.core;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;


/**
 * <p>
 * Data-access class that manages access to the primary data source
 * files in a database.  
 * </p>
 * <p>
 * NOTE: this class should only ever be extended by ONE class per 
 * database.
 * </p>
 * 
 * @author Nick Day
 * @version 0.1
 */
public abstract class PrimaryFileDAO {
	
	private PrimaryKeyDAO keyDao;
	protected String fileExtension;

	private static final Logger LOG = Logger.getLogger(PrimaryFileDAO.class);

	protected PrimaryFileDAO(File storageRoot, String fileExtension) {
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
		this.keyDao = new PrimaryKeyDAO(storageRoot);
	}

	/**
	 * <p>
	 * Adds the contents in the provided String into the database.
	 * Returns the primary key that the inserted content has been 
	 * assigned.
	 * </p>
	 * 
	 * @param String containing the contents to be written to the
	 * database.
	 * 
	 * @return the primary key that the written CIF has been 
	 * assigned. If there was a problem inserting the file, then -1
	 * will be returned.
	 */
	public int insert(String fileContents) {
		if (fileExtension == null) {
			throw new IllegalStateException("fileExtension field has not " +
					"been set in the implementing subclass of PrimaryFileDAO.");
		}
		int key = keyDao.insert();
		File keyFolder = keyDao.getFolderFromKey(key);
		File primaryFile = new File(keyFolder, key+fileExtension);
		if (primaryFile.exists()) {
			LOG.warn("Cannot insert primary file as it already exists: "+primaryFile);
			return -1;
		}
		LOG.info("Inserting file to: "+primaryFile.getAbsolutePath());
		try {
			FileUtils.writeStringToFile(primaryFile, fileContents);
		} catch (IOException e) {
			LOG.warn("Problem trying to insert primary file to: "+primaryFile.getAbsolutePath()+"\n" +
					e.getMessage());
			return -1;
		}
		return key;
	}
	
	/**
	 * <p>
	 * Returns the primary file associated with the primary key 
	 * provided. 
	 * </p>
	 *  
	 * @param primaryKey of the primary file you wish returned.
	 * 
	 * @return File of the primary file at the provided primary 
	 * key. If it does not exist, then null is returned.
	 */
	public File getFileFromKey(int primaryKey) {
		// TODO - note that this is the same method used in SecondaryFileDAO
		// perhaps extract into superclass?
		if (fileExtension == null) {
			throw new IllegalStateException("fileExtension field has not " +
					"been set in the implementing subclass of PrimaryFileDAO.");
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
