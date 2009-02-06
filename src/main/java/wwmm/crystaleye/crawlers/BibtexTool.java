package wwmm.crystaleye.crawlers;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
		Pattern p = Pattern.compile("(\\w+=\\{.+\\})");
		Matcher m = p.matcher(bibContents);
		while(m.find()) {
			String item = m.group();
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
