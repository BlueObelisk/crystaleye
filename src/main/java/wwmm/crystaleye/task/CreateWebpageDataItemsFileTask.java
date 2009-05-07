package wwmm.crystaleye.task;

import java.io.File;

import org.apache.log4j.Logger;
import org.xmlcml.cml.element.CMLCml;

import wwmm.crawler.Utils;
import wwmm.crystaleye.model.impl.ChildDerivedCmlFileDAO;
import wwmm.crystaleye.model.impl.ChildWebpageDataFileDAO;

/**
 * <p>
 * Manages the creation of a file that contains the data items from
 * a 'derived' CML file that will be used to create a crystal structure
 * summary page for the CrystalEye website.
 * </p>
 * 
 * @author Nick Day
 * @version 0.1
 */
public class CreateWebpageDataItemsFileTask {
	
	private File storageRoot;
	private int primaryKey;
	private int childKey;

	private static final Logger LOG = Logger.getLogger(CreateWebpageDataItemsFileTask.class);

	public CreateWebpageDataItemsFileTask(File storageRoot, int primaryKey, int childKey) {
		this.storageRoot = storageRoot;
		this.primaryKey = primaryKey;
		this.childKey = childKey;
	}

	/**
	 * <p>
	 * For the provided primary and child key, reads a 'derived' CML 
	 * file from the database, gets the structure from within, 
	 * creates a minimal CML molecule for display in Jmol and then 
	 * writes the resulting CML back out to the db.
	 * </p>
	 * 
	 * @return true if the Jmol CML was successfully created and 
	 * written to the database, false if not.
	 */
	public boolean runTask() {
		ChildDerivedCmlFileDAO cmlDao = new ChildDerivedCmlFileDAO(storageRoot);
		CMLCml cml = cmlDao.getCml(primaryKey, childKey);
		if (cml == null) {
			return false;
		}
		String contents = createFileContents(cml);
		ChildWebpageDataFileDAO webpageDataDao = new ChildWebpageDataFileDAO(storageRoot);
		boolean success = webpageDataDao.insert(primaryKey, childKey, contents);
		if (success) {
			return true;
		} else {
			return false;
		}
	}
	
	private String createFileContents(CMLCml cml) {
		
	}

}
