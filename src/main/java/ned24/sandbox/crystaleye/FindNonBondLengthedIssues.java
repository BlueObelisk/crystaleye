package ned24.sandbox.crystaleye;

import java.util.ArrayList;
import java.util.List;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;
import wwmm.crystaleye.util.XmlUtils;

public class FindNonBondLengthedIssues {

	public static void main(String[] args) {
		String allPath = "e:/download-log-lengths.xml";
		String latestPath = "e:/download-log.xml";
		
		Document allDoc = XmlUtils.parseXmlFile(allPath);
		Nodes issueNodes = allDoc.query(".//issue");
		List<String> allIds = new ArrayList<String>();
		for (int i = 0; i < issueNodes.size(); i++) {
			Element issue = (Element)issueNodes.get(i);
			Element year = (Element)issue.getParent();
			Element journal = (Element)year.getParent();
			Element publisher = (Element)journal.getParent();
			String issueId = publisher.getAttributeValue("abbreviation")+"_"+journal.getAttributeValue("abbreviation")+"_"+year.getAttributeValue("id")+"_"+issue.getAttributeValue("id");
			allIds.add(issueId);
		}
		
		Document latestDoc = XmlUtils.parseXmlFile(latestPath);
		issueNodes = latestDoc.query(".//issue");
		List<String> latestIds = new ArrayList<String>();
		for (int i = 0; i < issueNodes.size(); i++) {
			Element issue = (Element)issueNodes.get(i);
			Element year = (Element)issue.getParent();
			Element journal = (Element)year.getParent();
			Element publisher = (Element)journal.getParent();
			String issueId = publisher.getAttributeValue("abbreviation")+"_"+journal.getAttributeValue("abbreviation")+"_"+year.getAttributeValue("id")+"_"+issue.getAttributeValue("id");
			latestIds.add(issueId);
		}
		
		List<String> doneIds = new ArrayList<String>();
		for (String id : latestIds) {
			if (allIds.contains(id)) {
				doneIds.add(id);
			}
		}
		
		for (String doneId : doneIds) {
			String[] a = doneId.split("_");
			Nodes nodes = latestDoc.query(".//publisher[@abbreviation='"+a[0]+"']/" +
					"journal[@abbreviation='"+a[1]+"']/year[@id='"+a[2]+"']/issue[@id='"+a[3]+"']");
			if (nodes.size() != 1) {
				throw new RuntimeException("Not 1 node.");
			}
			Element el = (Element)nodes.get(0);
			System.out.println(el.toXML());
		}
	}
	
}
