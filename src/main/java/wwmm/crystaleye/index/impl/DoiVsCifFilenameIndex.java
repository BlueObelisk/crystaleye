package wwmm.crystaleye.index.impl;

import java.io.File;

import org.apache.log4j.Logger;

import wwmm.crystaleye.crawler.core.DOI;
import wwmm.crystaleye.index.core.StringAsKeyIndex;

/**
 * <p>
 * Provides methods for access and manipulation for an index 
 * of a CIF file's DOI of origin against it's filename on the
 * site it was found.
 * </p>
 * 
 * @author Nick Day
 * @version 0.1
 */
public class DoiVsCifFilenameIndex extends StringAsKeyIndex {

	public static final String INDEX_FILENAME = "doi-ciffilename_index.txt";

	private static final Logger LOG = Logger.getLogger(DoiVsCifFilenameIndex.class);

	public DoiVsCifFilenameIndex(File storageRoot) {
		super(storageRoot, INDEX_FILENAME);
	}
	
	/**
	 * <p>
	 * Insert an entry into the index with the provided DOI and
	 * filename.
	 * </p>
	 * 
	 * @param doi of the entry to be added.
	 * @param filename of the entry to be added.
	 * 
	 * @return true if the entry was successfully added to the 
	 * index, false if not.
	 */
	public boolean insert(DOI doi, String filename) {
		return super.insert(doi.getPostfix(), filename);
	}
	
	/**
	 * 
	 * 
	 * @param doi
	 * @param filename
	 * @param dontDuplicate
	 * 
	 * @return
	 */
	public boolean insert(DOI doi, String filename, boolean dontDuplicate) {
		return super.insert(doi.getPostfix(), filename, dontDuplicate);
	}
	
	/**
	 * <p>
	 * Finds whether an entry already exists in the index with the
	 * provided DOI and filename.
	 * </p>
	 * 
	 * @param doi of the entry to be searched for.
	 * @param filename of the entry to be searched for.
	 * 
	 * @return true if the index already contains an entry with the
	 * provided DOI and filename, false if not.
	 */
	public boolean contains(DOI doi, String filename) {
		return super.contains(doi.getPostfix(), filename);
	}
	
}
