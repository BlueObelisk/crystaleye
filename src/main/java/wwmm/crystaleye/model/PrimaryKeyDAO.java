package wwmm.crystaleye.model;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

/**
 * <p>
 * Data-access class that manages the primary keys in the CrystalEye
 * database.  As the db is based on the file-system, then each primary
 * key corresponds to a different folder, each of which is a direct
 * child of the root folder of the database, e.g. <root-folder-name>/1
 * corresponds to the first file in the database.
 * </p>
 * 
 * @author Nick Day
 * @version 0.1
 */
public class PrimaryKeyDAO {

	private File storageRoot;
	public static final String KEY_COUNT_FILENAME = "primarykey_count.txt";
	private File keyCountFile;
	
	public PrimaryKeyDAO(File storageRoot) {
		init(storageRoot);
	}

	/**
	 * <p>
	 * Sets the root folder for the CrystalEye database.
	 * </p>
	 * 
	 * @param storageRoot - the root folder for the CrystalEye database.
	 */
	private void init(File storageRoot) {
		this.storageRoot = storageRoot;
		keyCountFile = new File(storageRoot, KEY_COUNT_FILENAME);
	}
	
	/**
	 * Returns the folder in the database which represents the primary 
	 * key provided as a parameter. If the folder for the key does not 
	 * yet exist, then NULL is returned.
	 * 
	 * @param key - primary key you wish to find the storage folder for.
	 * 
	 * @return File where the contents of the primary key are stored.
	 */
	public File getFileFromKey(int key) {
		File f = new File(storageRoot, ""+key);
		if (!f.exists()) {
			return null;
		}
		return f;
	}

	/**
	 * <p>
	 * Adds a folder corresponding to the next available
	 * primary key for the database.  Returns the key.
	 * </p>
	 * 
	 * @return int corresponding to the key that has 
	 * been inserted.
	 */
	public int insertPrimaryKey() {
		int key = getNextAvailableKey();
		File keyFolder = new File(storageRoot, ""+key);
		keyFolder.mkdir();
		updateKeyCountFile(key+1);
		return key;
	}

	/**
	 * <p>
	 * Gets the next available primary key for the database.
	 * </p>
	 */
	private int getNextAvailableKey() {
		int nextAvailableKey = 1;
		keyCountFile = new File(storageRoot, KEY_COUNT_FILENAME);
		String keyStr = "";
		// read the contents of the key count file.  If it doesn't 
		// exist, then create it.
		try {
			if (!keyCountFile.exists()) {
				FileUtils.writeStringToFile(keyCountFile, "1");
			}
			keyStr = FileUtils.readFileToString(keyCountFile);
			nextAvailableKey = Integer.valueOf(keyStr);
			// check that next key is not less than 1, if for
			// some reason it is then reset to 1.
			if (nextAvailableKey < 1) {
				nextAvailableKey = 1;
			}
		} catch (IOException e) {
			throw new RuntimeException("Error setting next available key.", e);
		} catch (NumberFormatException e) {
			throw new RuntimeException("Content of the key count file is not" +
					" an integer as expected: "+keyStr, e);
		}
		// check whether a folder corresponding to nextAvailableKey already
		// exists.  If it does then find the next available folder.  Ensures
		// that no mess is created if the keyCountFile is out of sync with
		// the database.
		while (true) {
			File nextKeyFile = new File(storageRoot, ""+nextAvailableKey);
			if (nextKeyFile.exists()) {
				nextAvailableKey++;
			} else {
				break;
			}
		}
		return nextAvailableKey;
	}

	/**
	 * <p>
	 * Writes the current <code>nextAvailableKey</code> to the 
	 * <code>keyCountFile</code>.  This method should be called
	 * each time a CIF is inserted into the database.
	 * </p>
	 * 
	 */
	private void updateKeyCountFile(int key) {
		try {
			FileUtils.writeStringToFile(keyCountFile, ""+key);
		} catch (IOException e) {
			throw new RuntimeException("Error writing to the keyCountFile: "+
					keyCountFile.getAbsolutePath(), e);
		}
	}

}
