package wwmm.crystaleye.model.core;

import static wwmm.crystaleye.CrystaleyeConstants.ORTEP_EXTENSION;

import java.io.File;

import ned24.fsdb.SecondaryFileDAO;

public class OrtepFileDAO extends SecondaryFileDAO {

	private static final int LEVELS_DOWN = 1;
	
	public OrtepFileDAO(File dbRoot) {
		super(dbRoot, ORTEP_EXTENSION, LEVELS_DOWN);
	}
	
}
