package wwmm.crystaleye.task;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import nu.xom.Document;

import org.apache.log4j.Logger;
import org.xmlcml.cif.CIF;
import org.xmlcml.cif.CIFDataBlock;
import org.xmlcml.cif.CIFException;

import wwmm.crystaleye.Utils;
import wwmm.crystaleye.model.crystaleye.ChildCifXmlFileDAO;
import wwmm.crystaleye.model.crystaleye.ParentCifXmlFileDAO;

/**
 * <p>
 * This class provides functionality for the 'splitting-up' of CIFXML files.
 * This means that any CIFXML containing data on more than one structure,
 * (i.e. having more than one data containing datablock) will be split
 * up into smaller CIFXMLs containing information on only one structure 
 * each.  The unit of currency in CrystalEye is the structure, not the
 * CIFXML, so doing this is essential. 
 * </p>
 * 
 * <p>
 * For instance, if a CIFXML has four datablocks, one being the 'global'
 * block containing the CIF metadata and the other three being 'data'
 * blocks containing structural data then this CIFXML will be split into
 * three smaller CIFs, each containing the 'global' datablock and the 
 * 'data' datablock for one structure, e.g.
 * 
 * <pre>
 * -CIFXML--------
 * data_global
 * data_structure1
 * data_structure2
 * data_structure3
 * ---------------
 * </pre>
 * 
 * will be converted into:
 * 
 * <pre>
 * -CIFXML--------
 * data_global
 * data_structure1
 * ---------------
 * -CIFXML--------
 * data_global
 * data_structure2
 * ---------------
 * -CIFXML--------
 * data_global
 * data_structure3
 * ---------------
 * </pre>
 * </p>
 * 
 * @author Nick Day
 * @version 0.1
 *
 */
public class SplitParentCifXmlTask {

	private File storageRoot;
	private int primaryKey;

	private static final Logger LOG = Logger.getLogger(SplitParentCifXmlTask.class);

	public SplitParentCifXmlTask(File storageRoot, int primaryKey) {
		this.storageRoot = storageRoot;
		this.primaryKey = primaryKey;
	}

	/**
	 * <p>
	 * Converts the parent CIFXML file associated with the objects 
	 * primary key into separate child CIFXML files, where each file
	 * holds the data for one crystal structure.
	 * </p>
	 * 
	 * @return true if the conversion was successful, false if not.
	 */
	public boolean runTask() {
		CIF parentCifXml = null;
		try {
			parentCifXml = getParentCIFXML();
		} catch (CIFException e) {
			LOG.warn("Problem getting parent CIFXML file for primary key: "+primaryKey+"\n"+
					e.getMessage());
			return false;
		} catch (Exception e) {
			LOG.warn(e.getMessage());
			return false;
		}
		// split the datablocks into two lists, those containing metadata (global) and 
		// those containing structural data (struct).
		List<CIFDataBlock> datablockList = parentCifXml.getDataBlockList();
		List<CIFDataBlock> globalBlockList = new ArrayList<CIFDataBlock>(1);
		List<CIFDataBlock> structBlockList = new ArrayList<CIFDataBlock>(1);
		for (CIFDataBlock datablock : datablockList) {
			if (isStructureBlock(datablock)) {
				structBlockList.add(datablock);
			} else {
				// assume that if the datablock is not a structure block, then it must
				// be a global block.
				globalBlockList.add(datablock);
			}
		}
		// iterate through each struct datablock writing each out to a separate 
		// child CIFXML file.  Note that all global blocks are written to each
		// child CIFXML file.
		for (CIFDataBlock datablock : structBlockList) {
			CIF childCifXml = null;
			try {
				childCifXml = createChildCifXml(datablock, globalBlockList);
			} catch (CIFException e) {
				LOG.warn("Problem getting parent CIFXML file for primary key: "+primaryKey+"\n"+
						e.getMessage());
				return false;
			}
			writeChildCifXml(childCifXml);
		}

		return true;
	}

	/**
	 * <p>
	 * Writes a child CIFXML document out to the database.
	 * </p>
	 * 
	 * @param childCifXml to be written to the database.
	 */
	private void writeChildCifXml(CIF childCifXml) {
		ChildCifXmlFileDAO childPrimaryFileDao = new ChildCifXmlFileDAO(storageRoot);
		// FIXME - why does childCifXml.getDocument() return null?? - should be used 
		// instead of the below line.
		Document doc = new Document(childCifXml);
		childPrimaryFileDao.insert(primaryKey, Utils.toPrettyXMLString(doc));
	}

	/**
	 * <p>
	 * Takes a structural datablock and a list of global datablocks and creates a
	 * CIXML document from them representing a single crystal structure.
	 * </p>
	 * 
	 * @param datablock - a structure datablock
	 * @param globalBlockList - a list of global datablocks
	 * 
	 * @return a CIF object containing all of the datablocks passed as parameters.
	 * @throws CIFException 
	 */
	private CIF createChildCifXml(CIFDataBlock datablock, List<CIFDataBlock> globalBlockList) throws CIFException {
		CIF cif = new CIF();
		datablock.detach();
		cif.add(datablock);
		for (CIFDataBlock globalBlock : globalBlockList) {
			cif.appendChild(globalBlock.copy());
		}
		return cif;
	}

	/**
	 * <p>
	 * Checks a CIFDataBlock to see whether it contains data for a crystal structure.
	 * </p>
	 * 
	 * @param datablock that is going to be checked.
	 * 
	 * @return true if the datablock holds data for a crystal structure, false if not.
	 */
	private boolean isStructureBlock(CIFDataBlock datablock) {
		if (datablock.query(".//item[@name='_cell_length_a']").size() > 0) {
			return true;
		}
		if (datablock.query(".//loop[contains(@names,'_atom_site_label')]").size() > 0) {
			return true;
		}
		if (datablock.query(".//loop[contains(@names,'_symmetry_equiv_pos_as_xyz')]").size() > 0) {
			return true;
		}
		return false;
	}

	/**
	 * <p>
	 * Gets the parent CIFXML document for the provided primary key.
	 * </p>
	 * 
	 * @return the parent CIF XML document for the provided primary key. 
	 * 
	 * @throws CIFException if the parent CIFXML file is not valid CIFXML.
	 */
	private CIF getParentCIFXML() throws CIFException {
		ParentCifXmlFileDAO parentCifXmlFileDao = new ParentCifXmlFileDAO(storageRoot);
		File parentCifXmlFile = parentCifXmlFileDao.getFileFromKey(primaryKey);
		if (parentCifXmlFile == null) {
			throw new IllegalStateException("Parent CIFXML file does not exist for primary key: "+primaryKey);
		}
		Document parentCifXmlDoc = null;
		try {
			parentCifXmlDoc = Utils.parseXml(parentCifXmlFile);
		} catch (Exception e) {
			throw new RuntimeException("Problem parsing XML for: "+parentCifXmlFile);
		}
		return new CIF(parentCifXmlDoc, true);
	}

}
