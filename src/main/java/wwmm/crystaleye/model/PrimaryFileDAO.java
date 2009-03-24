package wwmm.crystaleye.model;

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
	protected int insert(String fileContents, String fileMime) throws IOException {
		int key = keyDao.insert();
		File keyFolder = keyDao.getFolderFromKey(key);
		File primaryFile = new File(keyFolder, key+fileMime);
		LOG.info("Inserting file to: "+primaryFile.getAbsolutePath());
		FileUtils.writeStringToFile(primaryFile, fileContents);
		return key;
	}

}
