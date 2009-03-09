package wwmm.crystaleye.index;

import java.io.File;
import java.util.List;

import org.apache.log4j.Logger;

public class StringAsKeyIndex extends Index {

	private static final Logger LOG = Logger.getLogger(StringAsKeyIndex.class);

	StringAsKeyIndex(File storageRoot, String indexFilename) {
		super(storageRoot, indexFilename);
	}

	/**
	 * <p>
	 * Creates the String of the index entry from the provided
	 * key and value.
	 * </p>
	 * 
	 * @param primaryKey - primary key of the record being added to the index.
	 * @param doi - DOI of the record being added to the index.
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
	
	protected boolean insert(String key, String value) {
		return this.insert(key, value, false);
	}
	
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
