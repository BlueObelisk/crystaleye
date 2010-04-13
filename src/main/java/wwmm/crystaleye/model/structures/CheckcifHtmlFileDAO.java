package wwmm.crystaleye.model.structures;

import static wwmm.crystaleye.CrystaleyeConstants.CHECKCIF_HTML_EXTENSION;

import java.io.File;

import ned24.fsdb.SecondaryFileDAO;

public class CheckcifHtmlFileDAO extends SecondaryFileDAO {

	private static final int LEVELS_DOWN = 1;
	
	public CheckcifHtmlFileDAO(File dbRoot) {
		super(dbRoot, CHECKCIF_HTML_EXTENSION, LEVELS_DOWN);
	}
	
}
