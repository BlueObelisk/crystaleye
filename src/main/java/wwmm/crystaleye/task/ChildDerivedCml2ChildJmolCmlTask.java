package wwmm.crystaleye.task;

import static wwmm.crystaleye.CrystalEyeConstants.X_CML;

import java.io.File;

import nu.xom.Document;
import nu.xom.Nodes;

import org.apache.log4j.Logger;
import org.xmlcml.cml.converters.cif.IOUtils;
import org.xmlcml.cml.element.CMLCml;
import org.xmlcml.cml.element.CMLMolecule;

import wwmm.crystaleye.Utils;
import wwmm.crystaleye.model.crystaleye.ChildDerivedCmlFileDAO;
import wwmm.crystaleye.model.crystaleye.ChildJmolCmlFileDAO;

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

	public boolean runTask() {
		File cmlFile = getCmlFile();
		if (cmlFile == null) {
			LOG.warn("CML file does not exist for the provided primary and " +
					"child keys: "+primaryKey+"/"+childKey);
			return false;
		}
		Document cmlDoc = null;
		try {
			cmlDoc = IOUtils.parseCmlFile(cmlFile);
		} catch (Exception e) {
			LOG.warn("Problem parsing CML file: "+cmlFile+
					"\n"+e.getMessage());
			return false;
		}
		CMLMolecule molecule = null;
		try {
			CMLCml cml = (CMLCml)cmlDoc.getRootElement();
			molecule = getMolecule(cml);
		} catch (Exception e) {
			LOG.warn("Problem obtaining container molecule from: "+cmlFile+
					"\n"+e.getMessage());
			return false;
		}
		Document jmolDoc = getJmolMolecule(molecule);
		ChildJmolCmlFileDAO dao = new ChildJmolCmlFileDAO(storageRoot);
		boolean success = dao.insert(primaryKey, childKey, Utils.toPrettyXMLString(jmolDoc));
		if (success) {
			return true;
		} else {
			return false;
		}
	}

	private Document getJmolMolecule(CMLMolecule molecule) {
		CMLMolecule molCopy = (CMLMolecule)molecule.copy();
		detachCrystalChildren(molCopy);
		detachFormulaNodes(molCopy);
		detachAtomChildren(molCopy);
		detachBondChildren(molCopy);
		return new Document(molCopy);
	}
	
	private void detachNodes(Nodes nds) {
		for (int i = 0; i < nds.size(); i++) {
			nds.get(i).detach();
		}
	}
	
	private void detachCrystalChildren(CMLMolecule molecule) {
		String[] attsToRemove = {"dataType", "errorValue"};
		for (String att : attsToRemove) {
			detachNodes(molecule.query("./cml:crystal/cml:scalar/@"+att, X_CML));
		}
		detachNodes(molecule.query("./cml:crystal/cml:symmetry/cml:transform3", X_CML));
	}
	
	private void detachBondChildren(CMLMolecule molecule) {
		String[] attsToRemove = {"atomRefs", "id", "userCyclic"};
		for (String att : attsToRemove) {
			detachNodes(molecule.query("./cml:bondArray/cml:bond/@"+att, X_CML));
		}
	}
	
	private void detachAtomChildren(CMLMolecule molecule) {
		detachNodes(molecule.query("./cml:atomArray/cml:atom/child::cml:*", X_CML));
		String[] attsToRemove = {"xFract", "yFract", "zFract", "x2", "y2"};
		for (String att : attsToRemove) {
			detachNodes(molecule.query("./cml:atomArray/cml:atom/@"+att, X_CML));
		}
	}
	
	private void detachFormulaNodes(CMLMolecule molecule) {
		detachNodes(molecule.query(".//cml:formula", X_CML));
	}

	private CMLMolecule getMolecule(CMLCml cml) {
		Nodes molNds = cml.query("./cml:molecule", X_CML);
		if (molNds.size() != 1) {
			return null;
		}
		return (CMLMolecule)molNds.get(0);
	}

	/**
	 * <p>
	 * Gets the child CIFXML File for the provided primary key and
	 * child key.
	 * </p>
	 * 
	 * @return the CIFXML File for the provided primary and
	 * child keys.  Returns null if the file does not exist
	 * for the provided keys.
	 */
	private File getCmlFile() {
		ChildDerivedCmlFileDAO dao = new ChildDerivedCmlFileDAO(storageRoot);
		return dao.getFileFromKeys(primaryKey, childKey);
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
