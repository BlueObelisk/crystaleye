package wwmm.crystaleye.task;

import java.io.File;
import java.io.IOException;

import nu.xom.Document;

import org.apache.log4j.Logger;
import org.xmlcml.cif.CIFException;
import org.xmlcml.cif.CIFParser;

import wwmm.crystaleye.Utils;
import wwmm.crystaleye.model.crystaleye.ParentCifFileDAO;
import wwmm.crystaleye.model.crystaleye.ParentCifXmlFileDAO;

/**
 * <p>
 * Manages the conversion of the 'parent' CIF files into 'parent'
 * CIFXML.
 * </p>
 * 
 * @author Nick Day
 * @version 0.1
 */
public class ParentCif2CifXmlTask {
	
	private File storageRoot;
	private int primaryKey;
	
	private static final Logger LOG = Logger.getLogger(ParentCif2CifXmlTask.class);
	
	public ParentCif2CifXmlTask(File storageRoot, int primaryKey) {
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
	public boolean runTask() {
		ParentCifFileDAO cifFileDao = new ParentCifFileDAO(storageRoot);
		File cifFile = cifFileDao.getFileFromKey(primaryKey);
		if (cifFile == null) {
			LOG.warn("Parent CIF file for provided primary key does not exist: "+primaryKey);
			return false;
		}
		String cifxmlStr = null;
		try {
			cifxmlStr = getCifXmlString(cifFile);
		} catch (CIFException e) {
			LOG.warn("Problem with CIFXML validity when getting CIFXML String: "+cifFile.getAbsolutePath()+"\n"+
					e.getMessage());
			return false;
		} catch (IOException e) {
			LOG.warn("Problem reading file when getting CIFXML String: "+cifFile.getAbsolutePath()+"\n"+
					e.getMessage());
			return false;
		}
		ParentCifXmlFileDAO cifXmlFileDao = new ParentCifXmlFileDAO(storageRoot);
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
		parser.setSkipHeader(true);
		parser.setCheckDuplicates(true);
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
		ParentCif2CifXmlTask task = new ParentCif2CifXmlTask(storageRoot, primaryKey);
		task.runTask();
	}

}
