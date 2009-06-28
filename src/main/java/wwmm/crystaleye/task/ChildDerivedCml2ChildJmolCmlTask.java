package wwmm.crystaleye.task;

import java.io.File;

import nu.xom.Document;

import org.apache.log4j.Logger;
import org.xmlcml.cml.element.CMLMolecule;

import wwmm.crystaleye.Utils;
import wwmm.crystaleye.model.impl.ChildDerivedCmlFileDAO;
import wwmm.crystaleye.model.impl.ChildJmolCmlFileDAO;
import wwmm.crystaleye.tools.JmolCmlTool;

/**
 * <p>
 * Manages the creation of a Jmol CML file from a 'derived' CML
 * file.  Reads a 'derived' CML file from the database, gets the
 * structure from within, creates a minimal CML molecule for display
 * in Jmol and then writes the resulting CML back out to the db.
 * </p>
 * 
 * @author Nick Day
 * @version 0.1
 */
public class ChildDerivedCml2ChildJmolCmlTask {

	private File storageRoot;
	private int primaryKey;
	private int childKey;

	private static final Logger LOG = Logger.getLogger(ChildDerivedCml2ChildJmolCmlTask.class);

	public ChildDerivedCml2ChildJmolCmlTask(File storageRoot, int primaryKey, int childKey) {
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
		CMLMolecule containerMol = cmlDao.getContainerMolecule(primaryKey, childKey);
		if (containerMol == null) {
			return false;
		}
		Document jmolDoc = new JmolCmlTool(containerMol).getMolAsDocument();
		ChildJmolCmlFileDAO jmolCmlDao = new ChildJmolCmlFileDAO(storageRoot);
		boolean success = jmolCmlDao.insert(primaryKey, childKey, Utils.toPrettyXMLString(jmolDoc));
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
	public static void main(String[] args) {
		File storageRoot = new File("c:/Users/ned24/workspace/crystaleye-data");
		int primaryKey = 2;
		int childKey = 1;
		ChildDerivedCml2ChildJmolCmlTask task = new ChildDerivedCml2ChildJmolCmlTask(storageRoot, primaryKey, childKey);
		task.runTask();
	}

}
