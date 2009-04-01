package wwmm.crystaleye.task;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;

import nu.xom.Document;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.xmlcml.cif.CIF;
import org.xmlcml.cif.CIFException;

import wwmm.crystaleye.Utils;
import wwmm.crystaleye.model.crystaleye.ChildCifFileDAO;
import wwmm.crystaleye.model.crystaleye.ChildCifXmlFileDAO;

public class ChildCifXml2CifTask {

	private File storageRoot;
	private int primaryKey;
	private int childKey;

	private static final Logger LOG = Logger.getLogger(ChildCifXml2CifTask.class);

	public ChildCifXml2CifTask(File storageRoot, int primaryKey, int childKey) {
		this.storageRoot = storageRoot;
		this.primaryKey = primaryKey;
		this.childKey = childKey;
	}

	public boolean runTask() {
		CIF childCifXml = null;
		try {
			childCifXml = getChildCifXml();
		} catch (Exception e) {
			LOG.warn(e.getMessage());
			return false;
		}
		StringWriter sw = new StringWriter();
		try {
			childCifXml.writeCIF(sw);
		} catch (IOException e) {
			LOG.warn("Problem creating String from CIFXML CIF, primary/child " +
					"keys are: "+primaryKey+"/"+childKey);
			return false;
		} finally {
			IOUtils.closeQuietly(sw);
		}
		ChildCifFileDAO childCifFileDao = new ChildCifFileDAO(storageRoot);
		boolean success = childCifFileDao.insert(primaryKey, childKey, sw.toString());
		if (success) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * <p>
	 * Gets the chlid CIFXML document for the provided primary key and
	 * child key.
	 * </p>
	 * 
	 * @return the parent CIF XML document for the provided primary and
	 * child keys. 
	 * 
	 * @throws CIFException if the child CIFXML file is not valid CIFXML.
	 */
	private CIF getChildCifXml() {
		ChildCifXmlFileDAO childCifXmlFileDao = new ChildCifXmlFileDAO(storageRoot);
		File childCifXmlFile = childCifXmlFileDao.getFileFromKeys(primaryKey, childKey);
		if (childCifXmlFile == null) {
			throw new IllegalStateException("Parent CIFXML file does not exist for primary key: "+primaryKey);
		}
		Document childCifXmlDoc = null;
		try {
			childCifXmlDoc = Utils.parseXml(childCifXmlFile);
		} catch (Exception e) {
			throw new RuntimeException("Problem parsing XML for: "+childCifXmlFile);
		}
		CIF cif = null;
		try {
			cif = new CIF(childCifXmlDoc, true);
		} catch (CIFException e) {
			throw new RuntimeException("Problem creating CIFXML CIF object: "+childCifXmlFile);
		}
		return cif;
	}
	
	/**
	 * <p>
	 * Main method meant for demonstration purposes only, does not
	 * require any arguments.
	 * </p>
	 */
	public static void main(String[] args) throws CIFException, IOException {
		File storageRoot = new File("c:/Users/ned24/workspace/crystaleye-data");
		int primaryKey = 3;
		int childKey = 2;
		ChildCifXml2CifTask task = new ChildCifXml2CifTask(storageRoot, primaryKey, childKey);
		task.runTask();
	}

}
