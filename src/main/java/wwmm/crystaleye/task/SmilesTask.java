package wwmm.crystaleye.task;

import java.io.File;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.xmlcml.cml.element.CMLMolecule;

import wwmm.crystaleye.model.impl.ChildDerivedCmlFileDAO;
import wwmm.crystaleye.tools.SmilesTool;

/**
 * <p>
 * Tool for the creation of SMILES from CML molecules.
 * </p>
 * 
 * @author Nick Day
 * @version 0.1
 */
public class SmilesTask {
	
	private File storageRoot;
	private int primaryKey;
	private int childKey;

	private static final Logger LOG = Logger.getLogger(SmilesTask.class);

	public SmilesTask(File storageRoot, int primaryKey, int childKey) {
		this.storageRoot = storageRoot;
		this.primaryKey = primaryKey;
		this.childKey = childKey;
	}

	/**
	 * <p>
	 * Reads in the CML molecules in the file defined by the objects 
	 * primary/child keys, creates the SMILES for each, adds them to the
	 * molecule CML and then writes the file back out to the same
	 * location.
	 * </p>
	 * 
	 * @return true if SMILES were successfully created and added for all
	 * molecules in the CML, false if not.
	 */
	public boolean runTask() {
		boolean overallSuccess = true;
		ChildDerivedCmlFileDAO dao = new ChildDerivedCmlFileDAO(storageRoot);
		List<CMLMolecule> molList = dao.getChildMolecules(primaryKey, childKey);
		for (CMLMolecule mol : molList) {
			String moleculeId = mol.getId();
			if (StringUtils.isEmpty(moleculeId)) {
				LOG.warn("Cannot find molecule ID, so cannot add SMILES for " +
						"primary/child keys: "+primaryKey+"/"+childKey);
				return false;
			}
			String smiles = createSmiles(mol);
			boolean success = dao.insertSmiles(primaryKey, childKey, moleculeId, smiles);
			if (!success) {
				overallSuccess = false;
			}
		}
		return overallSuccess;
	}
	
	/**
	 * <p>
	 * Creates a SMILES representation of the provided CML molecule 
	 * using the provided OpenBabel options. 
	 * </p>
	 * 
	 * @param molecule you want the SMILES for.
	 * @param options - Babel options to use whilst generating the 
	 * SMILES.
	 * 
	 * @return SMILES string representing the provided CML molecule.
	 */
	private String createSmiles(CMLMolecule molecule) {
		SmilesTool tool = new SmilesTool(molecule);
		return tool.generateSmiles();
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
		SmilesTask task = new SmilesTask(storageRoot, primaryKey, childKey);
		task.runTask();
	}

}
