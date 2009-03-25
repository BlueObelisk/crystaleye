package wwmm.crystaleye.index.crystaleye;

import java.io.File;
import java.util.List;

import org.apache.log4j.Logger;

import wwmm.crystaleye.crawler.DOI;
import wwmm.crystaleye.index.core.PrimaryKeyIndex;

/**
 * <p>
 * Provides methods for access and manipulation for an index 
 * of the database primary key against the DOI for that record.
 * </p>
 * 
 * @author Nick Day
 * @version 0.1
 */
public class PrimaryKeyVsDoiIndex extends PrimaryKeyIndex {
	
	public static final String INDEX_FILENAME = "primarykey-doi_index.txt";
	
	private static final Logger LOG = Logger.getLogger(PrimaryKeyVsDoiIndex.class);
	
	public PrimaryKeyVsDoiIndex(File storageRoot) {
		super(storageRoot, INDEX_FILENAME);
	}
	
	/**
	 * <p>
	 * Insert a record into the index.
	 * </p>
	 * 
	 * @param primaryKey - the primary key of the record to index.
	 * @param doi - the Digital Object Identifier of the record to index.
	 */
	public boolean insert(int primaryKey, DOI doi) {
		return super.insert(primaryKey, doi.getPostfix());
	}
	
	/**
	 * <p>
	 * Returns the DOI for the provided primary key.  If the primary
	 * key does not exist, then NULL is returned.
	 * </p>
	 * 
	 * @param primaryKey of the DOI desired.
	 * 
	 * @return DOI for the provided primary key.  If the key does
	 * not exist, then NULL is returned.
	 */
	public DOI getDOI(int primaryKey) {
		List<String> lines = readIndexFile();
		if (primaryKey > lines.size()) {
			return null;
		}
		String entry = lines.get(primaryKey-1);
		return createDoiFromEntry(entry);
	}
	
	/**
	 * <p>
	 * Returns the DOI part of the provided entry.
	 * </p>
	 * 
	 * @param entry that you want the DOI for.
	 * 
	 * @return DOI for the provided entry.
	 */
	private DOI createDoiFromEntry(String entry) {
		String value = getValueFromEntry(entry);
		if (value == null) {
			return null;
		}
		return new DOI(DOI.DOI_SITE_URL+"/"+value);
	}
	
	/**
	 * <p>
	 * States whether the index already contains an entry
	 * referring to the provided DOI.
	 * </p>
	 * 
	 * @return true if the index contains an entry referring to
	 * the provided <code>DOI</code>, false if not.
	 */
	public boolean containsDOI(DOI doi) {
		// TODO don't make the class read the index file each time
		// one of these is called.  Make it so that an instance variable
		// is set so that the reading can be reused.
		List<String> lines = readIndexFile();
		for (String entry : lines) {
			DOI entryDoi = createDoiFromEntry(entry);
			if (entryDoi.equals(doi)) {
				return true;
			}
		}
		return false;
	}

}
