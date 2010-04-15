package wwmm.crystaleye.model;

import static wwmm.crystaleye.CrystaleyeConstants.CIF_EXTENSION;

import java.io.File;

import ned24.fsdb.SecondaryFileDAO;

public class CifFileDAO extends SecondaryFileDAO {

	private static final int LEVELS_DOWN = 1;
	
	public CifFileDAO(File dbRoot) {
		super(dbRoot, CIF_EXTENSION, LEVELS_DOWN);
	}
	
}
