package wwmm.crystaleye.task.structures;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;

import org.apache.commons.io.IOUtils;
import org.xmlcml.cif.CIF;

import wwmm.crystaleye.model.structures.CifFileDAO;
import wwmm.crystaleye.model.structures.CifXmlFileDAO;
import wwmm.crystaleye.task.Task;

public class CifXml2CifTask extends Task {

	public CifXml2CifTask(File dbRoot, int... keys) {
		super(dbRoot, keys);
	}

	@Override
	public void perform() {
		CifXmlFileDAO cifxmlDao = new CifXmlFileDAO(getDbRoot());
		CIF cifxml = cifxmlDao.getCifXml(getKeys());
		StringWriter sw = new StringWriter();
		try {
			cifxml.writeCIF(sw);
		} catch (IOException e) {
			throw new RuntimeException("Problem creating CIF: "+e.getMessage());
		} finally {
			IOUtils.closeQuietly(sw);
		}
		CifFileDAO cifDao = new CifFileDAO(getDbRoot());
		cifDao.insert(sw.toString(), getKeys());
	}

}
