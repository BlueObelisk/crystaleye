package wwmm.crystaleye.model.impl;

import java.io.File;

import wwmm.crystaleye.model.core.ChildSecondaryFileDAO;

/**
 * <p>
 * Data-access class for accessing the CML files in CrystalEye that
 * are to be used for displaying in Jmol.  These are derived from 
 * DerivedChildCmlFiles and contain only enough information for 
 * displaying of the structure so that loading in Jmol on 
 * webpages is as quick as possible.
 * </p>
 * 
 * @author Nick Day
 * @version 0.1
 */
public class ChildWebpageDataFileDAO extends ChildSecondaryFileDAO {
	
	public static final String CHILD_WEBPAGE_DATA_MIME = ".webpage.data.txt";

	public ChildWebpageDataFileDAO(File storageRoot) {
		super(storageRoot, CHILD_WEBPAGE_DATA_MIME);
	}

}
