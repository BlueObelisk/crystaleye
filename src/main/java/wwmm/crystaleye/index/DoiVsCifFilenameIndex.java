package wwmm.crystaleye.index;

import java.io.File;

import org.apache.log4j.Logger;

import wwmm.crystaleye.crawler.DOI;

public class DoiVsCifFilenameIndex extends StringAsKeyIndex {

	public static final String INDEX_FILENAME = "doi-ciffilename_index.txt";

	private static final Logger LOG = Logger.getLogger(DoiVsCifFilenameIndex.class);

	public DoiVsCifFilenameIndex(File storageRoot) {
		super(storageRoot, INDEX_FILENAME);
	}
	
	public boolean insert(DOI doi, String filename) {
		return super.insert(doi.getPostfix(), filename);
	}
	
	public boolean insert(DOI doi, String filename, boolean dontDuplicate) {
		return super.insert(doi.getPostfix(), filename, dontDuplicate);
	}
	
	public boolean contains(DOI doi, String filename) {
		return super.contains(doi.getPostfix(), filename);
	}
	
}
