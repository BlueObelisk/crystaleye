package wwmm.crystaleye.index.core;

import java.io.File;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * <p>
 * Abstract class that should be extended by all indexes where
 * the key is any <code>String</code>.
 * </p>
 * 
 * @author Nick Day
 * @version 0.1
 */
public class StringAsKeyIndex extends Index {

	private static final Logger LOG = Logger.getLogger(StringAsKeyIndex.class);

	protected StringAsKeyIndex(File storageRoot, String indexFilename) {
		super(storageRoot, indexFilename);
	}

	/**
	 * <p>
	 * Inserts an entry into the index with the provided key
	 * and value.  If dontDuplicate is set to false, then the
	 * entry will always be inserted into the index, which can
	 * lead to duplicate entries if not careful.  If it
	 * is set to true, then the index will be checked for an
	 * entry that matches the provided one and only insert it
	 * if no matches are found
	 * </p>
	 * 
	 * @param key of the entry to be added.
	 * @param value of the entry to be added.
	 * @param dontDuplicate whether to check for duplicates
	 * before adding the entry.
	 * 
	 * @return true if the entry was successfully added to the
	 * index (also if dontDuplicate was set to true and a 
	 * duplicate was found.  False if the entry was not added
	 * to the index.
	 */
	protected boolean insert(String key, String value, boolean dontDuplicate) {
		if (key == null || value == null) {
			LOG.warn("Provided key or value is null.");
			return false;
		}
		String entry = this.createEntry(key, value);
		List<String> lines = readIndexFile();
		if (!dontDuplicate) {
			lines.add(entry);
		} else {
			// if dontDuplicate has been specified, search for
			// a line that matches the entry to be written, 
			// if one is found, don't add the new one.
			boolean add = true;
			for (int i = 0; i < lines.size(); i++) {
				String line = lines.get(i);
				if (line.equals(entry)) {
					add = false;
					break;
				}
			}
			if (add) {
				lines.add(entry);
			}
		}
		writeIndexFile(lines);
		return true;
	}
	
	/**
	 * <p>
	 * Insert an entry into the index with the provided key
	 * and value.  The index is not checked for duplicate 
	 * entries before the entry is added.
	 * </p>
	 * 
	 * @param key of the entry to be added.
	 * @param value of the entry to be added.
	 * 
	 * @return true if the entry was successfully added to the
	 * index, false if not.
	 */
	protected boolean insert(String key, String value) {
		return this.insert(key, value, false);
	}
	
	/**
	 * <p>
	 * Determine whether the index already contains an entry
	 * with the provided key and value.
	 * </p>
	 * 
	 * @param key of the entry to be found.
	 * @param value of the entry to be found.
	 * 
	 * @return true if the index already contains an entry
	 * with the provided key and value, false if not.
	 */
	protected boolean contains(String key, String value) {
		if (key == null || value == null) {
			LOG.warn("Provided key or value is null.");
			return false;
		}
		String entry = createEntry(key, value);
		List<String> lines = readIndexFile();
		for (String line : lines) {
			if (line.equals(entry)) {
				return true;
			}
		}
		return false;
	}

}
