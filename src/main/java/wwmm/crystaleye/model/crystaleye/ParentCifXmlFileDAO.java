package wwmm.crystaleye.model.crystaleye;

import java.io.File;

import wwmm.crystaleye.model.core.SecondaryFileDAO;

public class ParentCifXmlFileDAO extends SecondaryFileDAO {
	
	public static final String PARENT_CIFXML_MIME = ".cif.xml";

	public ParentCifXmlFileDAO(File storageRoot) {
		super(storageRoot, PARENT_CIFXML_MIME);
	}

}
