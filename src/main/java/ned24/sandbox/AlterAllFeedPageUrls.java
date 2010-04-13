package ned24.sandbox;

import static wwmm.crystaleye.CrystalEyeConstants_Old.X_ATOM1;

import java.io.File;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;
import nu.xom.Text;
import wwmm.crystaleye.util.Utils;

public class AlterAllFeedPageUrls {
	
	public static void main(String[] args) {
		String dirPath = "e:/crystaleye-new/feed/all/";
		File dir = new File(dirPath);
		
		for (File feedFile : dir.listFiles()) {
			System.out.println("Dealing with: "+feedFile);
			Document doc = Utils.parseXml(feedFile);
			Nodes linkNodes = doc.query("./atom1:feed/atom1:link", X_ATOM1);
			for (int i = 0; i < linkNodes.size(); i++) {
				Element link = (Element)linkNodes.get(i);
				Attribute hrefAtt = link.getAttribute("href");
				String href = hrefAtt.getValue();
				String newHref = href.replaceAll("/atom/", "/all/");
				hrefAtt.setValue(newHref);
			}
			Nodes idNodes = doc.query("./atom1:feed/atom1:id", X_ATOM1);
			for (int i = 0; i < idNodes.size(); i++) {
				Element link = (Element)idNodes.get(i);
				String url = link.getValue();
				String newUrl = url.replaceAll("/atom/", "/all/");
				link.removeChildren();
				link.appendChild(new Text(newUrl));
			}
			Utils.writeXML(feedFile, doc);
		}
	}

}
