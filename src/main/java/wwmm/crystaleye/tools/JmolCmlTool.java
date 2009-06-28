package wwmm.crystaleye.tools;

import static org.xmlcml.cml.base.CMLConstants.CML_XPATH;

import java.util.List;

import nu.xom.Document;
import nu.xom.Nodes;

import org.xmlcml.cml.element.CMLAtom;
import org.xmlcml.cml.element.CMLBond;
import org.xmlcml.cml.element.CMLCrystal;
import org.xmlcml.cml.element.CMLMolecule;

import wwmm.crystaleye.Utils;

/**
 * <p>
 * Takes a CML Molecule and strips away all parts that are not
 * necessary for display in Jmol.  This is useful if you want
 * to reduce load times for large CML files.
 * </p>
 * 
 * @author Nick Day
 * @version 0.1
 *
 */
public class JmolCmlTool {

	private CMLMolecule oldMol;
	private CMLMolecule jmolMol;
	
	private static int NUM_DECIMAL_PLACES = 3;

	// hide the default constructor
	private JmolCmlTool() {
		;
	}

	public JmolCmlTool(CMLMolecule mol) {
		this.oldMol = mol;
	}

	/**
	 * <p>
	 * Gets the 'Jmol-ready' CML Molecule.
	 * </p>
	 * 
	 * @return the new CML Molecule as an XML Document
	 * 
	 */
	public Document getMolAsDocument() {
		jmolMol = new CMLMolecule();
		copyCrystal();
		copyMolecules();
		return new Document(jmolMol);
	}

	private void copyMolecules() {
		List<CMLMolecule> molList = oldMol.getDescendantsOrMolecule();
		if (molList.size() == 1) {
			// there is only one moiety
			copyAtomsAndBonds(oldMol, jmolMol);
		} else {
			// there is more than one moiety
			for (CMLMolecule mol : molList) {
				CMLMolecule moiety = new CMLMolecule();
				jmolMol.addMolecule(moiety);
				copyAtomsAndBonds(mol, moiety);
			}
		}
	}

	private void copyAtomsAndBonds(CMLMolecule fromMol, CMLMolecule toMol) {
		for (CMLAtom atom : fromMol.getAtoms()) {
			CMLAtom newAtom = new CMLAtom();
			newAtom.setId(atom.getId());
			newAtom.setElementType(atom.getElementType());
			newAtom.setFormalCharge(atom.getFormalCharge());
			double x3 = Utils.round(atom.getX3(), NUM_DECIMAL_PLACES);
			newAtom.setX3(x3);
			double y3 = Utils.round(atom.getY3(), NUM_DECIMAL_PLACES);
			newAtom.setY3(y3);
			double z3 = Utils.round(atom.getZ3(), NUM_DECIMAL_PLACES);
			newAtom.setZ3(z3);
			toMol.addAtom(newAtom);
		}
		for (CMLBond bond : fromMol.getBonds()) {
			CMLBond newBond = new CMLBond();
			newBond.setAtomRefs2(bond.getAtomRefs2());
			newBond.setOrder(bond.getOrder());
		}
	}

	private void copyCrystal() {
		CMLCrystal crystal = (CMLCrystal)oldMol.getFirstCMLChild(CMLCrystal.TAG);
		crystal.detach();
		Nodes nds = crystal.query(".//cml:symmetry/child::cml:*", CML_XPATH);
		for (int i = 0; i < nds.size(); i++) {
			nds.get(i).detach();
		}
		jmolMol.appendChild(crystal);
	} 

}
