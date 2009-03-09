package wwmm.crystaleye.index;

import java.io.File;
import java.util.List;

import org.apache.log4j.Logger;

public abstract class PrimaryKeyIndex extends Index {
	
	private static final Logger LOG = Logger.getLogger(PrimaryKeyIndex.class);
	
	public PrimaryKeyIndex(File storageRoot, String indexFilename) {
		super(storageRoot, indexFilename);
	}
	
	/**
	 * <p>
	 * Creates the String of the index entry from the provided
	 * primary key and DOI.
	 * </p>
	 * 
	 * @param primaryKey - primary key of the record being added to the index.
	 * @param doi - DOI of the record being added to the index.
	 * 
	 * @return String representing the index entry.
	 */
	protected String createEntry(int primaryKey, String value) {
		StringBuilder sb = new StringBuilder();
		sb.append(""+primaryKey);
		sb.append(ENTRY_SEPARATOR);
		sb.append(value);
		return sb.toString();
	}
	
	protected boolean insert(int primaryKey, String value) {
		if (value == null) {
			LOG.warn("Provided DOI is null.");
			return false;
		}
		List<String> lines = readIndexFile();
		String entry = this.createEntry(primaryKey, value);
		int size = lines.size();
		if (primaryKey == size+1) {
			lines.add(entry);
		} else if (primaryKey > size+1) {
			// if primary key is greater than the next available primary key
			// then need to create blank lines so that the line number is the
			// same as the primary key item on that line.
			for (int i = size; i < primaryKey-1; i++) {
				lines.add("");
			}
			lines.add(entry);
		} else if (primaryKey <= size) {
			lines.remove(primaryKey-1);
			lines.add(primaryKey-1, entry);
		} else {
			throw new RuntimeException("This block should be unreachable, " +
					"there is an error in the code.");
		}
		writeIndexFile(lines);
		return true;
	}

}
