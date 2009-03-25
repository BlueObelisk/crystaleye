package wwmm.crystaleye.model.crystaleye;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

import wwmm.crystaleye.model.core.PrimaryFileDAO;

/**
 * <p>
 * Data-access class for accessing CIFs in the CrystalEye database.
 * </p>
 * 
 * @author Nick Day
 * @version 0.1
 */
public class CifFileDAO extends PrimaryFileDAO {

	/**
	 * The implementing subclass of PrimaryFileDAO needs to set the
	 * file extension to be used by the primary file of the database.
	 */
	static {
		fileExtension = ".cif";
	}

	private static final Logger LOG = Logger.getLogger(CifFileDAO.class);
	
	public CifFileDAO(File storageRoot) {
		super(storageRoot);
	}
	
	/**
	 * <p>
	 * Inserts a CIF into the database.  Returns the primary key
	 * that the CIF was assigned when it was written.
	 * </p>
	 * 
	 * @param cifContents - a <code>String</code> containing the
	 * contents of the CIF to be written.
	 * 
	 * @return the database primary key assigned to the inserted CIF.
	 */
	public int insert(String cifContents) {
		int key = -1;
		try {
			key = insert(cifContents, fileExtension);
		} catch (IOException e) {
			LOG.warn("Exception try to insert CIF to the database.");
		}
		return key;
	}
	
	/**
	 * <p>
	 * Gets the CIF <code>File</code> associated with the provided
	 * primary key.
	 * </p>
	 * 
	 * @param primaryKey of the CIF file you wish to retrieve.
	 * 
	 * @return the CIF file at the primary key provided.
	 */
	public File getCifFileFromKey(int primaryKey) {
		return getPrimaryFileFromKey(primaryKey);
	}

}
