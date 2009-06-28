package wwmm.crystaleye.task;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;

import nu.xom.Document;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.xmlcml.cif.CIF;
import org.xmlcml.cif.CIFException;

import wwmm.crystaleye.model.impl.ChildCifFileDAO;
import wwmm.crystaleye.model.impl.ChildCifXmlFileDAO;
import wwmm.pubcrawler.Utils;

/**
 * <p>
 * Manages the creation and inserting into the database of 
 * 'child' CIF files from 'child' CIFXML files.
 * </p>
 * 
 * @author Nick Day
 * @version 0.1
 */
public class ChildCifXml2ChildCifTask {

	private File storageRoot;
	private int primaryKey;
	private int childKey;

	private static final Logger LOG = Logger.getLogger(ChildCifXml2ChildCifTask.class);

	public ChildCifXml2ChildCifTask(File storageRoot, int primaryKey, int childKey) {
		this.storageRoot = storageRoot;
		this.primaryKey = primaryKey;
		this.childKey = childKey;
	}

	/**
	 * <p>
	 * Reads in a 'child' CIFXML file, converts it into CIF format
	 * and then writes out a 'child' CIF file.
	 * </p>
	 * 
	 * @return true if the CIF file was created and written 
	 * successfully, false if not.
	 */
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
	 * Gets the child CIFXML document for this objects primary key and
	 * child key.
	 * </p>
	 * 
	 * @return the parent CIF XML document for this objects primary and
	 * child keys. 
	 * 
	 * @throws CIFException if the child CIFXML file is not valid CIFXML.
	 */
	private CIF getChildCifXml() {
		ChildCifXmlFileDAO childCifXmlFileDao = new ChildCifXmlFileDAO(storageRoot);
		File childCifXmlFile = childCifXmlFileDao.getFileFromKeys(primaryKey, childKey);
		if (!childCifXmlFile.exists()) {
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
		int primaryKey = 2;
		int childKey = 1;
		ChildCifXml2ChildCifTask task = new ChildCifXml2ChildCifTask(storageRoot, primaryKey, childKey);
		task.runTask();
	}

}
