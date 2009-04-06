package wwmm.crystaleye.model.impl;

import java.io.File;

import wwmm.crystaleye.model.core.SecondaryFileDAO;

/**
 * <p>
 * Data-access class for accessing the 'parent' CIFXML files in
 * the database which are derived from the 'parent' CIF files.
 * </p>
 * 
 * @author Nick Day
 * @version 0.1
 *
 */
public class ParentCifXmlFileDAO extends SecondaryFileDAO {
	
	public static final String PARENT_CIFXML_MIME = ".cif.xml";

	public ParentCifXmlFileDAO(File storageRoot) {
		super(storageRoot, PARENT_CIFXML_MIME);
	}

}
