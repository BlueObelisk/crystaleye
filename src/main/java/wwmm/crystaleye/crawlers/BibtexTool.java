package wwmm.crystaleye.crawlers;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;

/**
 * <p>
 * The <code>BibtexTool</code> class provides a parser for simple Bibtex files, 
 * such as those that are provided alongside published chemistry articles (and
 * therefore contain the details of only one publication).  See 
 * <code>BibtexToolTest</code> for examples of such Bibtex files.  
 * </p>
 * 
 * <p>
 * NB. Do NOT use this class as a generic Bibtex parser.
 * </p>
 * 
 * @author Nick Day
 * @version 1.1
 * 
 */
public class BibtexTool {

	private String bibtexString;
	public Map<String, String> nameValuePairs;

	/**
	 * <p>
	 * Creates an instance of <code>BibtexTool</code> from
	 * a <code>String</code> containing a Bibtex file.  Upon
	 * object creation, the provided Bibtex will be validated
	 * and then parsed into its name-value pairs.
	 * </p>
	 * 
	 * @param bibtexString - <code>String</code> containing a 
	 * Bibtex file.
	 */
	public BibtexTool(String bibtexString) {
		this.bibtexString = bibtexString;
		validate();
		parseBibtexString();
	}

	/**
	 * <p>
	 * Creates an instance of <code>BibtexTool</code> from
	 * a <code>String</code> containing a Bibtex file.  Upon
	 * object creation, the provided Bibtex will be validated
	 * and then parsed into its name-value pairs.
	 * </p>
	 * 
	 * @param bibtexFile - a <code>File</code> containing
	 * Bibtex.
	 * 
	 * @throws IOException if the provided <code>File</code>
	 * cannot be read.
	 */
	public BibtexTool(File bibtexFile) throws IOException {
		this(FileUtils.readFileToString(bibtexFile));
	}
	
	/**
	 * <p>
	 * Simple validation of the provided Bibtex file.
	 * </p>
	 * 
	 */
	private void validate() {
		if (bibtexString == null) {
			throw new NullPointerException("Provided Bibtex file is null, an error must have occurred during parsing.");
		}
		if ("".equals(bibtexString)) {
			throw new IllegalStateException("Provided Bibtex file is blank.");
		}
	}

	/**
	 * <p>
	 * Goes through the provided Bibtex file and creates a map
	 * of each name value pair.
	 * </p>
	 * 
	 */
	private void parseBibtexString() {
		nameValuePairs = new HashMap<String, String>();
		String bibContents = bibtexString.substring(bibtexString.indexOf("{")+1, bibtexString.lastIndexOf("}"));
		String[] lines = bibContents.split("\\n");
		for (String line : lines) {
			if (!line.contains("=")) {
				continue;
			}
			int idx = line.indexOf("=");
			String name = line.substring(0,idx).trim();
			String value = line.substring(idx+1).trim();
			boolean finished = false;
			while(!finished) {
				finished = true;
				if (value.endsWith(",")) {
					value = value.substring(0,value.length()-1);
					finished = false;
				}
				if (value.startsWith("\"") || value.startsWith("'")
						|| value.startsWith("{")) {
					value = value.substring(1);
					finished = false;
				}
				if (value.endsWith("\"") || value.endsWith("'")
						|| value.endsWith("}")) {
					value = value.substring(0,value.length()-1);
					finished = false;
				}
			}
			nameValuePairs.put(name, value);
		}
	}
	
	/**
	 * <p>
	 * Get a data-item value from the Bibtex file corresponding to
	 * the name provided.
	 * </p>
	 * 
	 * @param name - the name of the data-item in the Bibtex file
	 * for which you want the value
	 * 
	 * @return String of the data-item value, <code>null</code> if
	 * it does not exist.`
	 */
	public String getValue(String name) {
		return nameValuePairs.get(name);
	}
	
	/**
	 * <p>
	 * Get the value of the author data-item from the Bibtex file.
	 * </p>
	 * 
	 * @return String of the author data-item
	 */
	public String getAuthors() {
		return getValue("author");
	}

	/**
	 * <p>
	 * Get the value of the title data-item from the Bibtex file.
	 * </p>
	 * 
	 * @return String of the title data-item.
	 */
	public String getTitle() {
		return getValue("title");
	}

	/**
	 * <p>
	 * Gets the values of commonly used data-items from the Bibtex
	 * file and creates an aggregation class to describe the 
	 * typical data-items found in a reference to a published
	 * journal article.
	 * </p>
	 * 
	 * @return ArticleReference providing the data-items typically
	 * found in a reference to a published journal article.
	 */
	public ArticleReference getReference() {
		String journal = getValue("journal");
		String year = getValue("year");
		String volume = getValue("volume");
		String pages = getValue("pages");
		String number = getValue("number");
		ArticleReference ref = new ArticleReference();
		ref.setJournalTitle(journal);
		ref.setYear(year);
		ref.setVolume(volume);
		ref.setPages(pages);
		ref.setNumber(number);
		return ref;
	}
	
	/**
	 * <p>
	 * Get the data items in the Bibtex file.
	 * </p>
	 * 
	 * @return Map containing the data-item name-value
	 * pairs from the Bibtex file.
	 */
	public Map<String, String> getNameValuePairs() {
		return nameValuePairs;
	}

}
