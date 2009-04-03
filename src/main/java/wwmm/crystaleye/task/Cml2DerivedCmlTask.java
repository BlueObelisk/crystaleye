package wwmm.crystaleye.task;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.xmlcml.cml.converters.ConverterCommand;
import org.xmlcml.cml.converters.cif.RawCML2CompleteCMLConverter;

import wwmm.crystaleye.model.crystaleye.ChildCmlFileDAO;
import wwmm.crystaleye.model.crystaleye.DerivedChildCmlFileDAO;

public class Cml2DerivedCmlTask {
	
	private File storageRoot;
	private int primaryKey;
	private int childKey;

	private static final Logger LOG = Logger.getLogger(Cml2DerivedCmlTask.class);

	public Cml2DerivedCmlTask(File storageRoot, int primaryKey, int childKey) {
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
		RawCML2CompleteCMLConverter converter = new RawCML2CompleteCMLConverter();
		converter.setCommand(new ConverterCommand());
		OutputStream out = null;
		BufferedOutputStream bout = null;
		try {
			out = new ByteArrayOutputStream();
			bout = new BufferedOutputStream(out);
			converter.convert(cmlFile, bout);
		} catch (Exception e) {
			LOG.warn("Problem converting from CIFXML to CML: "+cmlFile+
					"\n"+e.getMessage());
			return false;
		} finally {
			IOUtils.closeQuietly(bout);
			IOUtils.closeQuietly(out);
		}
		DerivedChildCmlFileDAO childCmlDao = new DerivedChildCmlFileDAO(storageRoot);
		boolean success = childCmlDao.insert(primaryKey, childKey, out.toString());
		if (success) {
			return true;
		} else {
			return false;
		}
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
		ChildCmlFileDAO childCifXmlFileDao = new ChildCmlFileDAO(storageRoot);
		return childCifXmlFileDao.getFileFromKeys(primaryKey, childKey);
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
		Cml2DerivedCmlTask task = new Cml2DerivedCmlTask(storageRoot, primaryKey, childKey);
		task.runTask();
	}

}
