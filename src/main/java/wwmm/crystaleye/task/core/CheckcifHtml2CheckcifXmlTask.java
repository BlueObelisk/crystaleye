package wwmm.crystaleye.task.core;

import java.io.File;
import java.io.IOException;

import nu.xom.Document;
import wwmm.crystaleye.model.core.CheckcifHtmlFileDAO;
import wwmm.crystaleye.model.core.CheckcifXmlFileDAO;
import wwmm.crystaleye.task.Task;
import wwmm.crystaleye.tools.CheckCifParser;

public class CheckcifHtml2CheckcifXmlTask extends Task {

	public CheckcifHtml2CheckcifXmlTask(File storageRoot, int... keys) {
		super(storageRoot, keys);
	}

	@Override
	public void perform() {
		CheckcifHtmlFileDAO checkcifDao = new CheckcifHtmlFileDAO(getDbRoot());
		File checkcifFile = checkcifDao.getAndEnsureFileFromKeys(getKeys());
		CheckCifParser parser;
		try {
			parser = new CheckCifParser(checkcifFile);
		} catch (IOException e) {
			throw new RuntimeException("Problem parsing CheckCif HTML ("+checkcifFile+") into CheckCif XML: "+e.getMessage());
		}
		Document checkcifXml = parser.parseService();
		CheckcifXmlFileDAO checkcifXmlDao = new CheckcifXmlFileDAO(getDbRoot());
		checkcifXmlDao.insert(checkcifXml, getKeys());
	}
	
}
