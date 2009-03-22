package wwmm.crystaleye.model;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

/**
 * <p>
 * Data-access class for accessing CIFs in the CrystalEye database.
 * </p>
 * 
 * @author Nick Day
 * @version 0.1
 */
public class CifFileDAO extends PrimaryFileDAO {

	public static final String CIF_MIME = ".cif";

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
			key = insert(cifContents, CIF_MIME);
		} catch (IOException e) {
			LOG.warn("Exception try to insert CIF to the database.");
		}
		return key;
	}

}
