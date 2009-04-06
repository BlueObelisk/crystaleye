package wwmm.crystaleye.task;

import java.io.File;

import org.apache.log4j.Logger;

import wwmm.crystaleye.model.impl.ChildCheckcifFileDAO;
import wwmm.crystaleye.model.impl.ChildCifFileDAO;
import wwmm.crystaleye.tools.CheckCifTool;

/**
 * <p>
 * Manages the generation of a CheckCIF file from a 'child' CIF
 * file.
 * </p>
 * 
 * @author Nick Day
 * @version 0.1
 */
public class ChildCif2ChildCheckcifTask {

	private File storageRoot;
	private int primaryKey;
	private int childKey;

	private static final Logger LOG = Logger.getLogger(ChildCif2ChildCheckcifTask.class);

	public ChildCif2ChildCheckcifTask(File storageRoot, int primaryKey, int childKey) {
		this.storageRoot = storageRoot;
		this.primaryKey = primaryKey;
		this.childKey = childKey;
	}

	/**
	 * <p>
	 * For the provided primary and child key, reads a 'child' CIF
	 * file from the database, submits it to the IUCr CheckCIF 
	 * service, gets the resulting HTML and writes it to the database.
	 * </p>
	 * 
	 * @return true if the CheckCIF was successfully obtained and 
	 * written to the database, false if not.
	 */
	public boolean runTask() {
		ChildCifFileDAO childCifDao = new ChildCifFileDAO(storageRoot);
		File childCifFile = childCifDao.getFileFromKeys(primaryKey, childKey);
		if (childCifFile == null) {
			LOG.warn("Child CIF does not exist for the primary/child keys: "+
					primaryKey+"/"+childKey);
			return false;
		}
		CheckCifTool checkCifTool = new CheckCifTool();
		String checkCifStr = null;
		try {
			checkCifStr = checkCifTool.getCheckcifString(childCifFile);
		} catch (Exception e) {
			LOG.warn("Problem calculating CheckCIF for CIF file: "+childCifFile+
					"\n"+e.getMessage());
			return false;
		}
		ChildCheckcifFileDAO checkCifDao = new ChildCheckcifFileDAO(storageRoot);
		boolean success = checkCifDao.insert(primaryKey, childKey, checkCifStr);
		if (success) {
			return true;
		} else {
			return false;
		}
	}

}
