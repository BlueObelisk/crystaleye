package wwmm.crystaleye.task;

import java.io.File;
import java.io.IOException;

import nu.xom.Document;

import org.apache.log4j.Logger;
import org.xmlcml.cif.CIFException;
import org.xmlcml.cif.CIFParser;

import wwmm.crystaleye.Utils;
import wwmm.crystaleye.model.crystaleye.CifFileDAO;
import wwmm.crystaleye.model.crystaleye.CifXmlFileDAO;

/**
 * <p>
 * This class provides functionality for the 'splitting-up' of CIFs.
 * This means that any CIF containing data on more than one structure,
 * (i.e. having more than one data containing datablock) will be split
 * up into smaller CIFs containing information on only one structure 
 * each.  The unit of currency in CrystalEye is the structure, not the
 * CIF, so doing this is essential. 
 * </p>
 * 
 * <p>
 * For instance, if a CIF has four datablocks, one being the 'global'
 * block containing the CIF metadata and the other three being 'data'
 * blocks containing structural data then this CIF will be split into
 * three smaller CIFs, each containing the 'global' datablock and the 
 * 'data' datablock for one structure, e.g.
 * 
 * <pre>
 * -CIF-----------
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
 * -CIF-----------
 * data_global
 * data_structure1
 * ---------------
 * -CIF-----------
 * data_global
 * data_structure2
 * ---------------
 * -CIF-----------
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
public class Cif2CifXmlTask {
	
	// TODO - the above comment needs moving when the split task is created.
	private File storageRoot;
	private int primaryKey;
	
	private static final Logger LOG = Logger.getLogger(Cif2CifXmlTask.class);
	
	public Cif2CifXmlTask(File storageRoot, int primaryKey) {
		this.storageRoot = storageRoot;
		this.primaryKey = primaryKey;
	}
	
	/**
	 * <p>
	 * Converts the CIF associated with the primary key provided in the
	 * constructor into CIFXML, which is then written out to a file 
	 * alongside the CIF.
	 * </p>
	 * 
	 * @return true if the CIF was successfully converted to CIFXML and
	 * written out to a file, false if not.
	 * 
	 * @throws CIFException if the CIF is invalid with respect to the 
	 * CIF spec.
	 * @throws IOException if there is a problem reading the provided CIF 
	 * file.
	 */
	public boolean runTask() throws CIFException, IOException {
		CifFileDAO cifFileDao = new CifFileDAO(storageRoot);
		File cifFile = cifFileDao.getFileFromKey(primaryKey);
		String cifxmlStr = getCifXmlString(cifFile);
		CifXmlFileDAO cifXmlFileDao = new CifXmlFileDAO(storageRoot);
		boolean success = cifXmlFileDao.insert(primaryKey, cifxmlStr);
		if (!success) {
			LOG.warn("Problem inserting CIFXML file to primary key: "+primaryKey);
		}
		return success;
	}
	
	/**
	 * <p>
	 * Takes the file containing the CIF, parses it into a CIFXML 
	 * document and then converts it into a <code>String</code>, 
	 * which is returned.
	 * </p>
	 * 
	 * @param cifFile - a File containing a CIF.
	 * 
	 * @return a String containing the contents of the CIFXML created
	 * from the provided CIF.
	 * 
	 * @throws CIFException if the CIF in the file provided is 
	 * invalid with respect to the CIF spec.
	 * @throws IOException if there is a problem read the CIF file
	 * provided.
	 */
	private String getCifXmlString(File cifFile) throws CIFException, IOException {
		CIFParser parser = new CIFParser();
		Document cifXml = parser.parse(cifFile);
		return Utils.toPrettyXMLString(cifXml);
	}
	
	/**
	 * <p>
	 * Main method meant for demonstration purposes only, does not
	 * require any arguments.
	 * </p>
	 */
	public static void main(String[] args) throws CIFException, IOException {
		File storageRoot = new File("c:/Users/ned24/workspace/crystaleye-data");
		int primaryKey = 3;
		Cif2CifXmlTask task = new Cif2CifXmlTask(storageRoot, primaryKey);
		task.runTask();
	}

}
