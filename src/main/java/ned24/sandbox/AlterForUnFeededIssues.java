package ned24.sandbox;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;

import org.apache.commons.io.FileUtils;

import wwmm.atomarchiver.Utils;

public class AlterForUnFeededIssues {

	public static void main(String[] args) throws IOException {
		String indexpath = "C:\\Documents and Settings\\ned24.UCC\\Desktop\\feed-items-index.txt";
		File indexFile = new File(indexpath);
		Set<String> set = new HashSet<String>();
		String prefix = "http://wwmm.ch.cam.ac.uk/crystaleye/summary/";
		int pl = prefix.length();
		String postfix = "/data/";
		List<String> lines = FileUtils.readLines(indexFile); 
		for (String line : lines) {
			String ss = line.substring(pl);
			ss = ss.substring(0, ss.indexOf(postfix));
			set.add(ss);
		}
		
		String logPath = "C:\\Documents and Settings\\ned24.UCC\\Desktop\\download-log.xml";
		File logFile = new File(logPath);
		Document doc = Utils.parseXml(logFile);
		Nodes nds = doc.query(".//issue");
		for (int i = 0; i < nds.size(); i++) {
			Element issueEl = (Element)nds.get(i);
			Element yearEl = (Element)issueEl.getParent();
			Element journalEl = (Element)yearEl.getParent();
			Element pubEl = (Element)journalEl.getParent();
			String issue = issueEl.getAttributeValue("id");
			String year = yearEl.getAttributeValue("id");
			String journal = journalEl.getAttributeValue("abbreviation");
			String pub = pubEl.getAttributeValue("abbreviation");
			String ss = pub+"/"+journal+"/"+year+"/"+issue;
			if (!set.contains(ss)) {
				Element el = issueEl.getFirstChildElement("rss");
				el.getAttribute("value").setValue("false");
			}
		}
		Utils.writeXML(doc, logPath+".2");
		
		/*
		for (String s : set) {
			String[] ss = s.split("/");
			if (ss.length != 4) {
				throw new IllegalStateException("s");
			}
			Nodes nds = doc.query(".//publisher[@abbreviation='"+ss[0]+"']/journal[@abbreviation='"+ss[1]+"']/year[@id='"+ss[2]+"']/issue[@id='"+ss[3]+"']");
			System.out.println(nds.size());
		}
		*/
	}
	
}
