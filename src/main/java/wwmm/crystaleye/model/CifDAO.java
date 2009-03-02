package wwmm.crystaleye.model;

import static wwmm.crystaleye.CrystalEyeConstants.CIF_MIME;

import java.io.File;
import java.io.InputStream;

import org.apache.log4j.Logger;

import wwmm.crystaleye.Utils;

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

	private static final Logger LOG = Logger.getLogger(CifDAO.class);

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
	 */
	public int insertCif(InputStream in) {
		int key = keyDao.insertPrimaryKey();
		File keyFile = keyDao.getFileFromKey(key);
		File cifFile = new File(keyFile, key+CIF_MIME);
		Utils.writeInputStreamToFile(in, cifFile);
		return key;
	}

}
