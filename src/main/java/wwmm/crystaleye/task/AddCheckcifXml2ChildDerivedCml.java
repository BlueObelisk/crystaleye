package wwmm.crystaleye.task;

import java.io.File;

import nu.xom.Document;
import nu.xom.Element;

import org.apache.log4j.Logger;
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
		if (checkcifXmlFile == null) {
			LOG.warn("CheckCIF XML file does not exist for primary/child keys: "+
					primaryKey+"/"+childKey);
			return false;
		}
		Element checkcifXmlRootElement = null;
		try {
			Document checkcifXmlDoc = Utils.parseXml(checkcifXmlFile);
			checkcifXmlRootElement = checkcifXmlDoc.getRootElement();
			checkcifXmlRootElement.detach();
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
		boolean success = derivedCmlFileDao.insertCheckcifXml(primaryKey, childKey, checkcifXmlRootElement);
		if (success) {
			return true;
		} else {
			return false;
		}
	}

}
