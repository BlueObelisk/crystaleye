package wwmm.crystaleye.model.core;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

/**
 * <p>
 * Data-access class that manages 'child' folders of the primary key
 * folders in the CrystalEye database.  Each 'child key' folder corresponds
 * to one crystal structure from the 'parent' CIF file.
 * </p>
 * 
 * @author Nick Day
 * @version 0.1
 */
public class ChildKeyDAO {
	
	private PrimaryKeyDAO primaryKeyDao;
	
	private static final Logger LOG = Logger.getLogger(ChildKeyDAO.class);

	public ChildKeyDAO(File storageRoot) {
		this.primaryKeyDao = new PrimaryKeyDAO(storageRoot);
	}
	
	/**
	 * <p>
	 * Adds a folder corresponding to the next available
	 * child key for the provided primary key. Returns the key.
	 * </p>
	 * 
	 * @return int corresponding to the key that has 
	 * been inserted. Returns -1 if the primary key provided
	 * doesn't exist.
	 */
	public int insert(int primaryKey) {
		File primaryKeyFolder = primaryKeyDao.getFolderFromKey(primaryKey);
		if (primaryKeyFolder == null) {
			LOG.warn("Primary key doesn't exist: "+primaryKey);
			return -1;
		}
		int childKey = getNextAvailableKey(primaryKey);
		File childKeyFolder = new File(primaryKeyFolder, String.valueOf(childKey));
		if (childKeyFolder.exists()) {
			LOG.warn("Cannot insert folder as it already exists: "+childKeyFolder);
			return -1;
		}
		childKeyFolder.mkdir();
		return childKey;
	}

	/**
	 * <p>
	 * Returns the folder in the database which represents the primary 
	 * key and child key provided as parameters. If the folder for the 
	 * keys does not yet exist, then NULL is returned.
	 * </p>
	 * 
	 * @param primaryKey - primary key you wish to find the storage 
	 * folder for.
	 * @param childKey - the child key that you wish to find the 
	 * storage folder for.
	 * 
	 * @return File in the db which represents the primary key and
	 * child key provided as parameters. Returns null if either the
	 * primary key or the child key do not exist.
	 */
	public File getFolderFromKeys(int primaryKey, int childKey) {
		File primaryKeyFolder = primaryKeyDao.getFolderFromKey(primaryKey);
		if (primaryKeyFolder == null) {
			return null;
		}
		File childFile = new File(primaryKeyFolder, String.valueOf(childKey));
		if (!childFile.exists()) {
			return null;
		} else {
			return childFile;
		}
	}

	/**
	 * <p>
	 * Gets the next available child key for the provided primary 
	 * key.
	 * </p>
	 * 
	 * @param primaryKey - the primary key that you want to find 
	 * the next available child key for.
	 * 
	 * @return the next available child key for the provided
	 * primary key or -1 if the primary key provided does not yet
	 * exist.
	 */
	private int getNextAvailableKey(int primaryKey) {
		File primaryKeyFolder = primaryKeyDao.getFolderFromKey(primaryKey);
		if (primaryKeyFolder == null) {
			LOG.warn("Attempting to find next available child key for a " +
					"primary key that doesn't exist: "+primaryKey);
			return -1;
		}
		int count = 1;
		while (true) {
			File file = new File(primaryKeyFolder, String.valueOf(count));
			if (!file.exists()) {
				break;
			} else {
				count++;
			}
		}
		return count;
	}
	
	/**
	 * <p>
	 * Remove the folder associated with the provided child key
	 * for the primary key associated with this class.
	 * </p>
	 * 
	 * @param childKey that you wish removed.
	 * 
	 * @return true if the key folder was successfully removed, 
	 * false if not or the key folder does not exist.
	 */
	public boolean remove(int primaryKey, int childKey) {
		File primaryKeyFolder = primaryKeyDao.getFolderFromKey(primaryKey);
		if (primaryKeyFolder == null) {
			LOG.warn("Attempting to delete a child key whose primary key doesn't exist: "+primaryKey);
			return false;
		}
		File childFile = new File(primaryKeyFolder, String.valueOf(childKey));
		if (childFile == null) {
			return false;
		}
		try {
			FileUtils.forceDelete(childFile);
		} catch (IOException e) {
			LOG.warn("Problem deleting child key folder: "+childFile.getAbsolutePath()+"\n" +
					e.getMessage());
			return false;
		}
		return true;
	}

}
