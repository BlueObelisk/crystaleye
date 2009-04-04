package wwmm.crystaleye.model.crystaleye;

import java.io.File;

import wwmm.crystaleye.model.core.ChildSecondaryFileDAO;

public class ChildCifFileDAO extends ChildSecondaryFileDAO {
	
	public static final String CHILD_CIF_MIME = ".cif";

	public ChildCifFileDAO(File storageRoot) {
		super(storageRoot, CHILD_CIF_MIME);
	}

}
