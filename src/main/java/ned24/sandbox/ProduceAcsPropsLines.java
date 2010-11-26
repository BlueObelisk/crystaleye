package ned24.sandbox;

import java.io.File;

import wwmm.crystaleye.util.Utils;
import wwmm.pubcrawler.core.Journal;
import wwmm.pubcrawler.journal.acs.AcsJournalIndex;

public class ProduceAcsPropsLines {
	
	public static void main(String[] args) {
		String path = "e:/workspace/temp.txt";
		File file = new File(path);
		StringBuilder sb = new StringBuilder();
		sb.append("acs.full.title=The American Chemical Society\n");
		sb.append("acs.journal.abbreviations=");
		for (Journal journal : AcsJournalIndex.getIndex().values()) {
			sb.append(journal.getAbbreviation()+",");
		}
		sb.append("\n");
		sb.append("acs.journal.full.titles=");
		for (Journal journal : AcsJournalIndex.getIndex().values()) {
			sb.append(journal.getFullTitle()+",");
		}
		sb.append("\n");
		Utils.writeText(file, sb.toString());
	}

}
