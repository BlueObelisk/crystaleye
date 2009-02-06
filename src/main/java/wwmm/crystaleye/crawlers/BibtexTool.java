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
		String[] items = bibContents.split(",");
		for (String item : items) {
			if (!item.contains("={")) {
				continue;
			}
			item = item.trim();
			int idx = item.indexOf("={");
			String name = item.substring(0,idx);
			String value = item.substring(idx+2,item.lastIndexOf("}"));
			nameValuePairs.put(name, value);
		}
	}
	
	public String getValue(String name) {
		return nameValuePairs.get(name);
	}

}
