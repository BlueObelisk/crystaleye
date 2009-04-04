package wwmm.crystaleye.model.crystaleye;

import java.io.File;

import wwmm.crystaleye.model.core.ChildSecondaryFileDAO;

/**
 * <p>
 * Data-access class for accessing 'child' CIF files.  These are
 * derived from the 'child' CIFXML files and represent one crystal
 * structure and its metadata each. 
 * </p>
 * 
 * @author Nick Day
 * @version 0.1
 */
public class ChildCifFileDAO extends ChildSecondaryFileDAO {
	
	public static final String CHILD_CIF_MIME = ".cif";

	public ChildCifFileDAO(File storageRoot) {
		super(storageRoot, CHILD_CIF_MIME);
	}

}
