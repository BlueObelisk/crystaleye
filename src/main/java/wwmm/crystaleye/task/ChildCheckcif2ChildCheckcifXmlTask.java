package wwmm.crystaleye.task;

import java.io.File;
import java.io.IOException;

import nu.xom.Document;

import org.apache.log4j.Logger;

import wwmm.crystaleye.Utils;
import wwmm.crystaleye.model.impl.ChildCheckcifFileDAO;
import wwmm.crystaleye.model.impl.ChildCheckcifXmlFileDAO;
import wwmm.crystaleye.tools.CheckCifParser;

/**
 * <p>
 * Manages the generation of CheckCIF XML from CheckCIF HTML.
 * </p>
 * 
 * @author Nick Day
 * @version 0.1
 */
public class ChildCheckcif2ChildCheckcifXmlTask {

	private File storageRoot;
	private int primaryKey;
	private int childKey;

	private static final Logger LOG = Logger.getLogger(ChildCheckcif2ChildCheckcifXmlTask.class);

	public ChildCheckcif2ChildCheckcifXmlTask(File storageRoot, int primaryKey, int childKey) {
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
		ChildCheckcifFileDAO checkcifDao = new ChildCheckcifFileDAO(storageRoot);
		File checkcifFile = checkcifDao.getFileFromKeys(primaryKey, childKey);
		if (!checkcifFile.exists()) {
			LOG.warn("CheckCIF file does not exist for primary/child keys: "+
					primaryKey+"/"+childKey);
			return false;
		}
		CheckCifParser parser = null;
		try {
			parser = new CheckCifParser(checkcifFile);
		} catch (IOException e) {
			LOG.warn("Problem reading CheckCIF file: "+checkcifFile);
			return false;
		}
		Document checkcifXml = parser.parseService();
		ChildCheckcifXmlFileDAO checkcifXmlDao = new ChildCheckcifXmlFileDAO(storageRoot);
		boolean success = checkcifXmlDao.insert(primaryKey, childKey, Utils.toPrettyXMLString(checkcifXml));
		if (success) {
			return true;
		} else {
			return false;
		}
	}

}
