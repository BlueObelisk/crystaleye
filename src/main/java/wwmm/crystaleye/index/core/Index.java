package wwmm.crystaleye.index.core;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

/**
 * <p>
 * Abstract class that all indexes in the CrystalEye database extend.
 * </p>
 * 
 * @author Nick Day
 * @version 0.1
 */
public abstract class Index {
	
	protected File storageRoot;
	protected File indexFile;
	protected String indexFilename;
	protected static final String ENTRY_SEPARATOR = "=";
	
	private static final Logger LOG = Logger.getLogger(Index.class);
	
	/**
	 * <p>
	 * Implementing subclasses must call this to ensure
	 * initialisation is performed correctly.
	 * </p>
	 * 
	 * @param storageRoot - root of the CrystalEye database.
	 * @param indexFilename - the name of the file where the 
	 * index is stored.
	 */
	public Index(File storageRoot, String indexFilename) {
		if (storageRoot == null) {
			throw new IllegalArgumentException("Provided storage " +
					"root is null.");
		}
		if (indexFilename == null) {
			throw new IllegalArgumentException("Provided index " +
					"filename is null.");
		}
		init(storageRoot, indexFilename);
	}
	
	/**
	 * <p>
	 * Sets the root folder for the CrystalEye database and sets location
	 * of the index.  If the index does not exist, then one is created.
	 * </p>
	 * 
	 * @param storageRoot - the root folder for the CrystalEye database.
	 */
	protected void init(File storageRoot, String indexFilename) {
		this.storageRoot = storageRoot;
		indexFile = new File(storageRoot, indexFilename);
		if (!indexFile.exists()) {
			try {
				indexFile.createNewFile();
			} catch (IOException e) {
				throw new RuntimeException("Attempted to create missing index" +
						" file, though an exception occurred: "+
						indexFile.getAbsolutePath(), e);
			}
			LOG.info("Created missing index file at: "+indexFile.getAbsolutePath());
		}
	}
	
	/**
	 * <p>
	 * Reads the contents of the index file into a
	 * list of <code>String</code>s.
	 * </p>
	 * 
	 * @return a list of <code>String</code>s representing
	 * the contents of the index file.
	 */
	protected List<String> readIndexFile() {
		List<String> lines = null;
		try {
			lines = FileUtils.readLines(indexFile);
		} catch (IOException e) {
			LOG.warn("Exception occurred whilst trying to " +
					"read index file: "+indexFile.getAbsolutePath(), e);
		}
		return lines;
	}
	
	/**
	 * <p>
	 * Writes the provided lines to the index file.
	 * </p>
	 * 
	 * @param lines representing the contents of the
	 * index file.
	 */
	protected void writeIndexFile(List<String> lines) {
		try {
			FileUtils.writeLines(indexFile, lines);
		} catch (IOException e) {
			LOG.warn("Exception occurred whilst trying to " +
					"write index file: "+indexFile.getAbsolutePath()+"\n" +
							e.getMessage());
		}
	}
	
	/**
	 * <p>
	 * Returns the value of the provided entry.
	 * </p>
	 * 
	 * @param entry that you want the value for.
	 * 
	 * @return value for the provided entry.
	 */
	protected String getValueFromEntry(String entry) {
		if ("".equals(entry)) {
			return null;
		}
		int idx = entry.indexOf("=");
		if (idx == -1) {
			throw new RuntimeException("Could not find entry separator, " +
					"index entry is corrupt: "+entry);
		}
		return entry.substring(idx+1);
	}
	
	/**
	 * <p>
	 * Creates the String of the index entry from the provided
	 * key and value.
	 * </p>
	 * 
	 * @param key of the record being added to the index.
	 * @param value of the record being added to the index.
	 * 
	 * @return String representing the index entry.
	 */
	protected String createEntry(String key, String value) {
		StringBuilder sb = new StringBuilder();
		sb.append(key);
		sb.append(ENTRY_SEPARATOR);
		sb.append(value);
		return sb.toString();
	}

}
