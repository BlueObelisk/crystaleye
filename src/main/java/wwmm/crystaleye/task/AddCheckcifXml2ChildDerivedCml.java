package wwmm.crystaleye.task;

import java.io.File;
import java.io.IOException;

import nu.xom.Document;
import nu.xom.Element;

import org.apache.log4j.Logger;
import org.xmlcml.cif.CIFException;
import org.xmlcml.cml.element.CMLCml;

import wwmm.crystaleye.model.impl.ChildCheckcifXmlFileDAO;
import wwmm.crystaleye.model.impl.ChildDerivedCmlFileDAO;
import wwmm.pubcrawler.Utils;

public class AddCheckcifXml2ChildDerivedCml {
	
	private File storageRoot;
	private int primaryKey;
	private int childKey;

	private static final Logger LOG = Logger.getLogger(AddCheckcifXml2ChildDerivedCml.class);

	public AddCheckcifXml2ChildDerivedCml(File storageRoot, int primaryKey, int childKey) {
		this.storageRoot = storageRoot;
		this.primaryKey = primaryKey;
		this.childKey = childKey;
	}

	/**
	 * <p>
	 * For the provided primary and child key, reads a CheckCIF HTML
	 * file and converts the data within in to CheckCIF XML before
	 * writing it out to the database.
	 * </p>
	 * 
	 * @return true if the CheckCIF was successfully obtained and 
	 * written to the database, false if not.
	 */
	public boolean runTask() {
		ChildCheckcifXmlFileDAO checkcifXmlDao = new ChildCheckcifXmlFileDAO(storageRoot);
		File checkcifXmlFile = checkcifXmlDao.getFileFromKeys(primaryKey, childKey);
		if (!checkcifXmlFile.exists()) {
			LOG.warn("CheckCIF XML file does not exist for primary/child keys: "+
					primaryKey+"/"+childKey);
			return false;
		}
		Element checkcifXmlRootElement = null;
		try {
			Document checkcifXmlDoc = Utils.parseXml(checkcifXmlFile);
			checkcifXmlRootElement = checkcifXmlDoc.getRootElement();
		} catch (Exception e) {
			LOG.warn("Problem getting CheckCIF XML root element for file: "+checkcifXmlFile+
					"\n"+e.getMessage());
			return false;
		}
		ChildDerivedCmlFileDAO derivedCmlFileDao = new ChildDerivedCmlFileDAO(storageRoot);
		CMLCml cml = derivedCmlFileDao.getCml(primaryKey, childKey);
		if (cml == null) {
			LOG.warn("Problem getting CMLCml from 'derived' CML file with primary/child " +
					"keys: "+primaryKey+"/"+childKey);
			return false;
		}
		boolean success = derivedCmlFileDao.insertElementAtRoot(primaryKey, childKey, (Element)checkcifXmlRootElement.copy());
		if (success) {
			return true;
		} else {
			return false;
		}
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
		AddCheckcifXml2ChildDerivedCml task = new AddCheckcifXml2ChildDerivedCml(storageRoot, primaryKey, childKey);
		task.runTask();
	}

}
