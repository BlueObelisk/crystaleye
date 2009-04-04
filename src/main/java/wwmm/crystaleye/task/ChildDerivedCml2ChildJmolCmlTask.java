package wwmm.crystaleye.task;

import static wwmm.crystaleye.CrystalEyeConstants.X_CML;

import java.io.File;

import nu.xom.Document;
import nu.xom.Nodes;

import org.apache.log4j.Logger;
import org.xmlcml.cml.element.CMLMolecule;

import wwmm.crystaleye.Utils;
import wwmm.crystaleye.model.crystaleye.ChildDerivedCmlFileDAO;
import wwmm.crystaleye.model.crystaleye.ChildJmolCmlFileDAO;

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
		Document jmolDoc = createJmolMolecule(containerMol);
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
	 * From a provided molecule, all those elements superfluous
	 * to display in Jmol are stripped out to make display as
	 * fast as possible.
	 * </p>
	 * 
	 * @param molecule to be stripped..
	 * 
	 * @return a Document containing the stripped molecule.
	 */
	private Document createJmolMolecule(CMLMolecule molecule) {
		detachCrystalChildren(molecule);
		detachFormulaNodes(molecule);
		detachAtomChildren(molecule);
		detachBondChildren(molecule);
		molecule.detach();
		return new Document(molecule);
	}
	
	/**
	 * <p>
	 * For a given <code>Nodes</nodes>, iterates through each 
	 * <code>Node</code> detaching it from its parent element.
	 * </p>
	 * 
	 * @param nds - the <code>Nodes</code> to be detached.
	 */
	private void detachNodes(Nodes nds) {
		for (int i = 0; i < nds.size(); i++) {
			nds.get(i).detach();
		}
	}
	
	/**
	 * <p>
	 * Detaches all those elements in a crystal element that
	 * are superfluous to display in Jmol.
	 * </p>
	 * 
	 * @param molecule to be stripped.
	 */
	private void detachCrystalChildren(CMLMolecule molecule) {
		String[] attsToRemove = {"dataType", "errorValue"};
		for (String att : attsToRemove) {
			detachNodes(molecule.query("./cml:crystal/cml:scalar/@"+att, X_CML));
		}
		detachNodes(molecule.query("./cml:crystal/cml:symmetry/cml:transform3", X_CML));
	}
	
	/**
	 * <p>
	 * Detaches all those elements in a bond element that
	 * are superfluous to display in Jmol.
	 * </p>
	 * 
	 * @param molecule to be stripped.
	 */
	private void detachBondChildren(CMLMolecule molecule) {
		String[] attsToRemove = {"atomRefs", "id", "userCyclic"};
		for (String att : attsToRemove) {
			detachNodes(molecule.query("./cml:bondArray/cml:bond/@"+att, X_CML));
		}
	}
	
	/**
	 * <p>
	 * Detaches all those elements in a atom element that
	 * are superfluous to display in Jmol.
	 * </p>
	 * 
	 * @param molecule to be stripped.
	 */
	private void detachAtomChildren(CMLMolecule molecule) {
		detachNodes(molecule.query("./cml:atomArray/cml:atom/child::cml:*", X_CML));
		String[] attsToRemove = {"xFract", "yFract", "zFract", "x2", "y2"};
		for (String att : attsToRemove) {
			detachNodes(molecule.query("./cml:atomArray/cml:atom/@"+att, X_CML));
		}
	}
	
	/**
	 * <p>
	 * Detaches all formula elements in the provided molecule.
	 * </p>
	 * 
	 * @param molecule to be stripped.
	 */
	private void detachFormulaNodes(CMLMolecule molecule) {
		detachNodes(molecule.query(".//cml:formula", X_CML));
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
		ChildDerivedCml2ChildJmolCmlTask task = new ChildDerivedCml2ChildJmolCmlTask(storageRoot, primaryKey, childKey);
		task.runTask();
	}

}
