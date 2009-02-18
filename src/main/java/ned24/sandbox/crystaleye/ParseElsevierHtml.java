package ned24.sandbox.crystaleye;

import nu.xom.Document;

import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;

import wwmm.crystaleye.crawlers.CrawlerHttpClient;

public class ParseElsevierHtml {

	public static void main(String[] args) throws URIException, NullPointerException {
		//String path = "e:/test.xml";
		//Utils.parseXml(new File(path));
		String url = "http://www.sciencedirect.com/science/journal/02775387";
		Document doc =  new CrawlerHttpClient().getResourceHTMLMinusComments(new URI(url, false));
	}
	
}
