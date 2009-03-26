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
	protected static String fileExtension;

	private static final Logger LOG = Logger.getLogger(PrimaryFileDAO.class);

	protected PrimaryFileDAO(File storageRoot) {
		init(storageRoot);
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
	 * Adds the CIF in the provided <code>InputStream</code> 
	 * into the CIF database.  Returns the primary key that the
	 * inserted CIF has been assigned.
	 * </p>
	 * 
	 * @param in - InputStream containing the CIF to be inserted.
	 * 
	 * @return the primary key that the written CIF has been 
	 * assigned.
	 * 
	 * @throws IOException if there is a problem writing the file
	 * to the database.  
	 */
	public int insert(String fileContents) {
		if (fileExtension == null) {
			throw new IllegalStateException("fileExtension field has not " +
					"been set in the implementing subclass of PrimaryFileDAO.");
		}
		int key = keyDao.insert();
		File keyFolder = keyDao.getFolderFromKey(key);
		File primaryFile = new File(keyFolder, key+fileExtension);
		LOG.info("Inserting file to: "+primaryFile.getAbsolutePath());
		try {
			FileUtils.writeStringToFile(primaryFile, fileContents);
		} catch (IOException e) {
			LOG.warn("Problem trying to insert primary file to: "+primaryFile.getAbsolutePath()+"\n" +
					e.getMessage());
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
	
	/**
	 * <p>
	 * Get the file extension of the primary file.
	 * </p>
	 * 
	 * @return the file extension of the primary file.
	 */
	public static String getFileExtension() {
		return fileExtension;
	}

}
