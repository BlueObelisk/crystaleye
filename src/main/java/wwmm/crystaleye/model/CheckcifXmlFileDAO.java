package wwmm.crystaleye.model;

import static wwmm.crystaleye.CrystaleyeConstants.CHECKCIF_XML_EXTENSION;

import java.io.File;

import ned24.fsdb.SecondaryFileDAO;

public class CheckcifXmlFileDAO extends SecondaryFileDAO {

	private static final int LEVELS_DOWN = 1;
	
	public CheckcifXmlFileDAO(File dbRoot) {
		super(dbRoot, CHECKCIF_XML_EXTENSION, LEVELS_DOWN);
	}
	
}
