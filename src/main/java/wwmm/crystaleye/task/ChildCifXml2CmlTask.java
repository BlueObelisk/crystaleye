package wwmm.crystaleye.task;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.xmlcml.cml.converters.ConverterCommand;
import org.xmlcml.cml.converters.cif.CIFXML2CMLConverter;

import wwmm.crystaleye.model.crystaleye.ChildCifXmlFileDAO;
import wwmm.crystaleye.model.crystaleye.ChildCmlDAO;

public class ChildCifXml2CmlTask {

	private File storageRoot;
	private int primaryKey;
	private int childKey;

	private static final Logger LOG = Logger.getLogger(ChildCifXml2CmlTask.class);

	public ChildCifXml2CmlTask(File storageRoot, int primaryKey, int childKey) {
		this.storageRoot = storageRoot;
		this.primaryKey = primaryKey;
		this.childKey = childKey;
	}

	public boolean runTask() {
		File cifXmlFile = getChildCifXmlFile();
		if (cifXmlFile == null) {
			LOG.warn("CIFXML file does not exist for the provided primary and " +
					"child keys: "+primaryKey+"/"+childKey);
			return false;
		}
		CIFXML2CMLConverter converter = new CIFXML2CMLConverter();
		converter.setCommand(new ConverterCommand());
		OutputStream out = null;
		BufferedOutputStream bout = null;
		try {
			out = new ByteArrayOutputStream();
			bout = new BufferedOutputStream(out);
			converter.convert(cifXmlFile, bout);
		} catch (Exception e) {
			LOG.warn("Problem converting from CIFXML to CML: "+cifXmlFile+
					"\n"+e.getMessage());
			return false;
		} finally {
			IOUtils.closeQuietly(bout);
			IOUtils.closeQuietly(out);
		}
		ChildCmlDAO childCmlDao = new ChildCmlDAO(storageRoot);
		boolean success = childCmlDao.insert(primaryKey, childKey, out.toString());
		if (success) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * <p>
	 * Gets the child CIFXML File for the provided primary key and
	 * child key.
	 * </p>
	 * 
	 * @return the CIFXML File for the provided primary and
	 * child keys.  Returns null if the file does not exist
	 * for the provided keys.
	 */
	private File getChildCifXmlFile() {
		ChildCifXmlFileDAO childCifXmlFileDao = new ChildCifXmlFileDAO(storageRoot);
		return childCifXmlFileDao.getFileFromKeys(primaryKey, childKey);
	}

}
