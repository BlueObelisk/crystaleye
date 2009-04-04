package wwmm.crystaleye.model.crystaleye;

import java.io.File;

import wwmm.crystaleye.model.core.ChildPrimaryFileDAO;

/**
 * <p>
 * Data-access class for accessing 'child' CIFXML files in CrystalEye.
 * These are generated from the Parent CIFXML file, and act as the
 * primary file for data corresponding to a single crystal structure
 * (which is the basis for the data provided by CrystalEye).
 * </p>
 * 
 * @author Nick Day
 * @version 0.1
 */
public class ChildCifXmlFileDAO extends ChildPrimaryFileDAO {
	
	public static final String CHILD_CIFXML_MIME = ".cif.xml";

	public ChildCifXmlFileDAO(File storageRoot) {
		super(storageRoot, CHILD_CIFXML_MIME);
	}

}
