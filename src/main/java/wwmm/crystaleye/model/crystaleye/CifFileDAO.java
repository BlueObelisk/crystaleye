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

}
