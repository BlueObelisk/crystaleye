package wwmm.crystaleye.model.crystaleye;

import java.io.File;

import org.apache.log4j.Logger;

import wwmm.crystaleye.model.core.NonPrimaryFileDAO;

public class CifXmlFileDAO extends NonPrimaryFileDAO {
	
	/**
	 * An implementing subclass of NonPrimaryFileDAO needs to set the
	 * file extension to be used by the primary file of the database.
	 */
	static {
		fileExtension = ".cif.xml";
	}
	
	private static final Logger LOG = Logger.getLogger(CifXmlFileDAO.class);

	public CifXmlFileDAO(File storageRoot) {
		super(storageRoot);
	}

}
