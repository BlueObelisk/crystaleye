package wwmm.crystaleye.task;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.xmlcml.cif.CIFException;
import org.xmlcml.cml.converters.ConverterCommand;
import org.xmlcml.cml.converters.cif.CIFXML2CMLConverter;

import wwmm.crystaleye.model.impl.ChildCifXmlFileDAO;
import wwmm.crystaleye.model.impl.ChildCmlFileDAO;

/**
 * <p>
 * Manages the creation and insertion into the database of
 * 'child' CML files from 'child CIFXML files.
 * </p>
 * 
 * @author Nick Day
 * @version 0.1
 */
public class ChildCifXml2ChildCmlTask {

	private File storageRoot;
	private int primaryKey;
	private int childKey;

	private static final Logger LOG = Logger.getLogger(ChildCifXml2ChildCmlTask.class);

	public ChildCifXml2ChildCmlTask(File storageRoot, int primaryKey, int childKey) {
		this.storageRoot = storageRoot;
		this.primaryKey = primaryKey;
		this.childKey = childKey;
	}

	/**
	 * <p>
	 * Gets the 'child' CIFXML file for this objects primary
	 * and child key, converts it into CIF format, and then
	 * writes it out to the database.
	 * </p>
	 * 
	 * @return true if the 'child' CML was successfully created
	 * and written to the db.  False if not.
	 */
	public boolean runTask() {
		File cifXmlFile = getChildCifXmlFile();
		if (!cifXmlFile.exists()) {
			LOG.warn("CIFXML file does not exist for the provided primary and " +
					"child keys: "+primaryKey+"/"+childKey);
			return false;
		}
		CIFXML2CMLConverter converter = new CIFXML2CMLConverter();
		converter.setCommand(new ConverterCommand());
		OutputStream out = null;
		String contents = null;
		try {
			out = new ByteArrayOutputStream();
			converter.convert(cifXmlFile, out);
			contents = out.toString();
		} catch (Exception e) {
			LOG.warn("Problem converting from CIFXML to CML: "+cifXmlFile+
					"\n"+e.getMessage());
			return false;
		} finally {
			IOUtils.closeQuietly(out);
		}
		ChildCmlFileDAO childCmlDao = new ChildCmlFileDAO(storageRoot);
		boolean success = childCmlDao.insert(primaryKey, childKey, contents);
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
	
	/**
	 * <p>
	 * Main method meant for demonstration purposes only, does not
	 * require any arguments.
	 * </p>
	 */
	public static void main(String[] args) throws CIFException, IOException {
		File storageRoot = new File("c:/Users/ned24/workspace/crystaleye-data");
		int primaryKey = 2;
		int childKey = 1;
		ChildCifXml2ChildCmlTask task = new ChildCifXml2ChildCmlTask(storageRoot, primaryKey, childKey);
		task.runTask();
	}

}
