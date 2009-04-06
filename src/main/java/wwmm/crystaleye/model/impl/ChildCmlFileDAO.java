package wwmm.crystaleye.model.impl;

import java.io.File;

import wwmm.crystaleye.model.core.ChildSecondaryFileDAO;

/**
 * <p>
 * Data-access class for accessing the CML files in CrystalEye that
 * are literal translations of the CIFs.
 * </p>
 * 
 * @author Nick Day
 * @version 0.1
 */
public class ChildCmlFileDAO extends ChildSecondaryFileDAO {
	
	public static final String CHILD_CML_MIME = ".cml";

	public ChildCmlFileDAO(File storageRoot) {
		super(storageRoot, CHILD_CML_MIME);
	}

}
