package wwmm.crystaleye.model.crystaleye;

import java.io.File;

import wwmm.crystaleye.model.core.ChildSecondaryFileDAO;

/**
 * <p>
 * Data-access class for accessing the 'derived' CML files in 
 * CrystalEye that are literal translations of the CIFs.
 * </p>
 * 
 * @author Nick Day
 * @version 0.1
 */
public class ChildDerivedCmlFileDAO extends ChildSecondaryFileDAO {
	
	public static final String DERIVED_CHILD_CML_MIME = ".derived.cml";

	public ChildDerivedCmlFileDAO(File storageRoot) {
		super(storageRoot, DERIVED_CHILD_CML_MIME);
	}

}
