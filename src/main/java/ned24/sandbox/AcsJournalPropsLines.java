package ned24.sandbox;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import wwmm.pubcrawler.core.Journal;
import wwmm.pubcrawler.journal.acs.AcsJournalIndex;

public class AcsJournalPropsLines {
	
	public static void main(String[] args) {
		String propsPath = "e:/crystaleye-new/docs/cif-flow-props.txt";
		File propsFile = new File(propsPath);
		
		List<String> titles = new LinkedList<String>();
		List<String> abbs = new LinkedList<String>();
		for (Journal journal : AcsJournalIndex.getIndex().values()) {
			titles.add(journal.getFullTitle());
			abbs.add(journal.getAbbreviation());
		}
		
		StringBuilder sb = new StringBuilder();
		for (String s : titles) {
			sb.append(s+",");
		}
		sb.append("\n");
		for (String s : abbs) {
			sb.append(s+",");
		}
		
		System.out.println(sb.toString());
	}

}
