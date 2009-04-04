package wwmm.crystaleye.model.crystaleye;

import static wwmm.crystaleye.CrystalEyeConstants.X_CML;

import java.io.File;
import java.util.List;

import nu.xom.Document;
import nu.xom.Nodes;

import org.apache.log4j.Logger;
import org.xmlcml.cml.element.CMLCml;
import org.xmlcml.cml.element.CMLMolecule;

import wwmm.crystaleye.Utils;
import wwmm.crystaleye.model.core.ChildSecondaryFileDAO;

/**
 * <p>
 * Data-access class for accessing the 'derived' CML files in 
 * CrystalEye that are literal translations of the CIFs.
 * </p>
 * 
 * @author Nick Day
 * @version 0.1
 */
public class ChildDerivedCmlFileDAO extends ChildSecondaryFileDAO {

	public static final String DERIVED_CHILD_CML_MIME = ".derived.cml";

	private static final Logger LOG = Logger.getLogger(ChildDerivedCmlFileDAO.class);

	public ChildDerivedCmlFileDAO(File storageRoot) {
		super(storageRoot, DERIVED_CHILD_CML_MIME);
	}

	/**
	 * <p>
	 * Reads in the 'derived' CML file at the provided primary and
	 * child keys and returns an XML Document representing the file.
	 * </p>
	 * 
	 * @param primaryKey of the 'derived' CML to be read.
	 * @param childKey of the 'derived' CML to be read.
	 * 
	 * @return Document representing the 'derived' CML at the 
	 * provided primary and child keys.
	 */
	public Document getDocument(int primaryKey, int childKey) {
		File cmlFile = getFileFromKeys(primaryKey, childKey);
		Document cmlDoc = null;
		try {
			cmlDoc = Utils.parseCml(cmlFile);
		} catch (Exception e) {
			LOG.warn("Problem getting Document from file: "+cmlFile+
					"\n"+e.getMessage());
			return null;
		}
		return cmlDoc;
	}

	/**
	 * <p>
	 * Gets the CML (root element) for the 'derived' CML file at
	 * the provided primary and child keys.
	 * </p>
	 * 
	 * @param primaryKey of the 'derived' CML to be read.
	 * @param childKey of the 'derived' CML to be read.
	 * 
	 * @return CMLCml representing the entire 'derived' CML at 
	 * the provided primary and child keys.
	 */
	public CMLCml getCml(int primaryKey, int childKey) {
		Document cmlDoc = getDocument(primaryKey, childKey);
		if (cmlDoc == null) {
			return null;
		}
		CMLCml cml = null;
		try {
			cml = (CMLCml)cmlDoc.getRootElement();
		} catch (Exception e) {
			LOG.warn("Problem getting CML for primary/child key: "+
					primaryKey+"/"+childKey+"\n"+e.getMessage());
			return null;
		}
		return cml;
	}
	
	/**
	 * <p>
	 * Gets the molecule element representing the entire crystal
	 * structure for the 'derived' CML file at the provided primary
	 * and child keys.
	 * </p>
	 * 
	 * @param primaryKey of the 'derived' CML to be read.
	 * @param childKey of the 'derived' CML to be read.
	 * 
	 * @return CMLMolecule representing the container molecule
	 * for the 'derived' CML at the provided primary and child
	 * keys.
	 */
	public CMLMolecule getContainerMolecule(int primaryKey, int childKey) {
		CMLCml cml = getCml(primaryKey, childKey);
		if (cml == null) {
			return null;
		}
		Nodes molNds = cml.query("./cml:molecule", X_CML);
		if (molNds.size() != 1) {
			LOG.warn("Expected to find 1 container molecule in CML, found: "+molNds.size());
			return null;
		}
		return (CMLMolecule)molNds.get(0);
	}
	
	/**
	 * <p>
	 * Gets a list molecules representing the individual moieties
	 * in the crystal in the 'derived' CML at the provided primary
	 * and child keys.
	 * </p>
	 * 
	 * @param primaryKey of the 'derived' CML to be read.
	 * @param childKey of the 'derived' CML to be read.
	 * 
	 * @return list of molecules representing the individual 
	 * moieties in the crystal from the 'derived' CML file
	 * at the provided primary and child keys.  NOTE that
	 * if there is only one moiety in the crystal, then 
	 * the child molecule returned will be the same as the
	 * container molecule.
	 */
	public List<CMLMolecule> getChildMolecules(int primaryKey, int childKey) {
		CMLMolecule molecule = getContainerMolecule(primaryKey, childKey);
		if (molecule == null) {
			return null;
		}
		return molecule.getDescendantsOrMolecule();
	}

}
