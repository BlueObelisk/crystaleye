package ned24.sandbox;

import java.io.File;
import java.io.IOException;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;
import nu.xom.XPathContext;

import org.apache.commons.io.FileUtils;

import wwmm.crystaleye.util.Utils;

public class GetAllFeedData {

	public static void main(String[] args) throws IOException {
		String folderPath = "C:\\Documents and Settings\\ned24.UCC\\Desktop\\all";
		File folder = new File(folderPath);
		File indexFile = new File("C:\\Documents and Settings\\ned24.UCC\\Desktop\\feed-items-index.txt");

		StringBuilder sb = new StringBuilder();
		int count = 0;
		for (File feedfile : folder.listFiles()) {
			Document doc = Utils.parseUnvalidatedXml(feedfile);
			Nodes linkNds = doc.query(".//atom:entry/atom:link[contains(@href, 'summary.html')]", new XPathContext("atom", "http://www.w3.org/2005/Atom"));
			for (int i = 0; i < linkNds.size(); i++) {
				count++;
				Element link = (Element)linkNds.get(0);
				String href = link.getAttributeValue("href");
				sb.append(href+"\n");
				if (count % 25000 == 0) {
					Utils.appendToFile(indexFile, sb.toString());
					sb = new StringBuilder();
				}
			}
		}
		Utils.appendToFile(indexFile, sb.toString());
	}

}
