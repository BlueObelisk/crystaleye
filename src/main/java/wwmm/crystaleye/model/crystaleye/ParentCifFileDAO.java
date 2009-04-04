package wwmm.crystaleye.model.crystaleye;

import java.io.File;

import wwmm.crystaleye.model.core.PrimaryFileDAO;

/**
 * <p>
 * Data-access class for accessing 'parent' CIFs in the CrystalEye 
 * database. These are CIFs in the form they are first deposited 
 * into the db, and may contain data for more than one structure.
 * They are the source of all derived data in CrystalEye. 
 * </p>
 * 
 * @author Nick Day
 * @version 0.1
 */
public class ParentCifFileDAO extends PrimaryFileDAO {

	public static final String PARENT_CIF_MIME = ".cif";

	public ParentCifFileDAO(File storageRoot) {
		super(storageRoot, PARENT_CIF_MIME);
	}

}
