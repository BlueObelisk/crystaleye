package wwmm.crystaleye.task;

import java.io.File;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.xmlcml.cml.element.CMLMolecule;

import wwmm.crystaleye.model.impl.ChildDerivedCmlFileDAO;
import wwmm.crystaleye.tools.InchiTool;

/**
 * <p>
 * Manages the creation and addition of InChIs for the molecules
 * in the 'derived' CML files.
 * </p>
 * 
 * @author Nick Day
 * @version 0.1
 */
public class InchiTask {

	private File storageRoot;
	private int primaryKey;
	private int childKey;

	private static final Logger LOG = Logger.getLogger(InchiTask.class);

	public InchiTask(File storageRoot, int primaryKey, int childKey) {
		this.storageRoot = storageRoot;
		this.primaryKey = primaryKey;
		this.childKey = childKey;
	}

	/**
	 * <p>
	 * Reads in the CML molecules in the file defined by the objects 
	 * primary/child keys, creates the InChI for each, adds them to the
	 * molecule CML and then writes the file back out to the same
	 * location.
	 * </p>
	 * 
	 * @return true if InChIs were successfully created and added for all
	 * molecules in the CML, false if not.
	 */
	public boolean runTask() {
		boolean overallSuccess = true;
		ChildDerivedCmlFileDAO dao = new ChildDerivedCmlFileDAO(storageRoot);
		List<CMLMolecule> molList = dao.getChildMolecules(primaryKey, childKey);
		String inchiOptions = "";
		for (CMLMolecule mol : molList) {
			String moleculeId = mol.getId();
			if (StringUtils.isEmpty(moleculeId)) {
				LOG.warn("Cannot find molecule ID, so cannot add InChI for " +
						"primary/child keys: "+primaryKey+"/"+childKey);
				return false;
			}
			String inchi = createInchi(mol, inchiOptions);
			boolean success = dao.insertInchi(primaryKey, childKey, moleculeId, inchi);
			if (!success) {
				overallSuccess = false;
			}
		}
		return overallSuccess;
	}
	
	/**
	 * <p>
	 * Creates the InChI for the structure represented in the provided
	 * CML molecule.
	 * </p>
	 * 
	 * @param molecule to create the InChI for.
	 * 
	 * @return InChI string for provided molecule.
	 */
	private String createInchi(CMLMolecule molecule, String options) {
		InchiTool tool = new InchiTool(molecule);
		return tool.generateInchi(options);
	}
	
	/**
	 * <p>
	 * Main method meant for demonstration purposes only, does not
	 * require any arguments.
	 * </p>
	 */
	public static void main(String[] args) {
		File storageRoot = new File("c:/Users/ned24/workspace/crystaleye-data");
		int primaryKey = 3;
		int childKey = 1;
		InchiTask task = new InchiTask(storageRoot, primaryKey, childKey);
		task.runTask();
	}
	
}
