package wwmm.crystaleye.crawlers;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;

public class BibtexTool {

	private String bibtexString;
	private Map<String, String> nameValuePairs;

	public BibtexTool(String bibtexString) {
		this.bibtexString = bibtexString;
		parseBibtexString();
	}

	public BibtexTool(File bibtexFile) throws IOException {
		this(FileUtils.readFileToString(bibtexFile));
	}

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
	
	private String getValue(String name) {
		return nameValuePairs.get(name);
	}
	
	public String getAuthors() {
		return getValue("author");
	}

	public String getTitle() {
		return getValue("title");
	}

	public ArticleReference getReference() {
		String journal = getValue("journal");
		String year = getValue("year");
		String volume = getValue("volume");
		String pages = getValue("pages");
		String number = getValue("number");
		ArticleReference ref = new ArticleReference();
		ref.setJournal(journal);
		ref.setYear(year);
		ref.setVolume(volume);
		ref.setPages(pages);
		ref.setNumber(number);
		return ref;
	}

}
