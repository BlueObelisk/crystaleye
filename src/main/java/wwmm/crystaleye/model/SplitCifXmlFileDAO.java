package wwmm.crystaleye.model;

import static wwmm.crystaleye.CrystaleyeConstants.*;

import java.io.File;

import ned24.fsdb.PrimaryFileDAO;

public class SplitCifXmlFileDAO extends PrimaryFileDAO {
	
private static final int LEVELS_DOWN = 2;
	
	public SplitCifXmlFileDAO(File dbRoot) {
		super(dbRoot, CIFXML_EXTENSION, LEVELS_DOWN);
	}

}
