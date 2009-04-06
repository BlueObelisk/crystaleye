package wwmm.crystaleye.model.impl;

import java.io.File;

import wwmm.crystaleye.model.core.ChildSecondaryFileDAO;

/**
 * <p>
 * Data-access class for accessing the CheckCIF files in CrystalEye 
 * that are generated from the 'child' CIF files.
 * </p>
 * 
 * @author Nick Day
 * @version 0.1
 */
public class ChildCheckcifXmlFileDAO extends ChildSecondaryFileDAO {
	
	public static final String CHILD_CHECKCIF_XML_MIME = ".checkcif.xml";

	public ChildCheckcifXmlFileDAO(File storageRoot) {
		super(storageRoot, CHILD_CHECKCIF_XML_MIME);
	}

}