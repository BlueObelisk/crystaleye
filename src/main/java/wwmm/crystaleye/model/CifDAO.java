package wwmm.crystaleye.model;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

/**
 * <p>
 * Data-access class for accessing CIFs in the CrystalEye database.
 * </p>
 * 
 * @author Nick Day
 * @version 0.1
 */
public class CifDAO {

	private File storageRoot;
	private PrimaryKeyDAO keyDao;
	public static final String CIF_MIME = ".cif";

	private static final Logger LOG = Logger.getLogger(CifDAO.class);
	
	public CifDAO(File storageRoot) {
		setStorageRoot(storageRoot);
	}

	/**
	 * Provide the root folder at which the CrystalEye database sits.
	 * 
	 * @param storageRoot - the root folder at which the CrystalEye 
	 * database sits.
	 */
	public void setStorageRoot(File storageRoot) {
		this.storageRoot = storageRoot;
		this.keyDao = new PrimaryKeyDAO(storageRoot);
	}

	/**
	 * Adds the CIF in the provided <code>InputStream</code> 
	 * into the CIF database.  Returns the primary key that the
	 * inserted CIF has been assigned.
	 * 
	 * @param in - InputStream containing the CIF to be inserted.
	 * 
	 * @return the primary key that the written CIF has been 
	 * assigned.
	 * @throws IOException 
	 */
	public int insertCif(String cifContents) {
		int key = keyDao.insertPrimaryKey();
		File keyFile = keyDao.getFileFromKey(key);
		File cifFile = new File(keyFile, key+CIF_MIME);
		LOG.info("Inserting CIF to: "+cifFile.getAbsolutePath());
		try {
			FileUtils.writeStringToFile(cifFile, cifContents);
		} catch (IOException e) {
			throw new RuntimeException("Could not write CIF string to: "+cifFile.getAbsolutePath(), e);
		}
		return key;
	}

}
