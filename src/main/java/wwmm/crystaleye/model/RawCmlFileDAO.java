package wwmm.crystaleye.model;

import static wwmm.crystaleye.CrystaleyeConstants.RAW_CML_EXTENSION;

import java.io.File;

import ned24.fsdb.SecondaryFileDAO;

public class RawCmlFileDAO extends SecondaryFileDAO {

	private static final int LEVELS_DOWN = 1;
	
	public RawCmlFileDAO(File dbRoot) {
		super(dbRoot, RAW_CML_EXTENSION, LEVELS_DOWN);
	}
	
}
