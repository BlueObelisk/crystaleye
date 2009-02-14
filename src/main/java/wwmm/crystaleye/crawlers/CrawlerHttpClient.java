package wwmm.crystaleye.crawlers;

import nu.xom.Document;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.log4j.Logger;

import wwmm.crystaleye.BasicHttpClient;

/**
 * A wrapper class for all public methods in BasicHttpClient.  All HttpClient 
 * calls also include a period of sleep so that we do not cause any Denial Of 
 * Service (or draw attention to our activities).
 * 
 * @author nickday
 *
 */
public class CrawlerHttpClient extends BasicHttpClient {
	
	int maxSleep = 1500;
	
	private static final Logger LOG = Logger.getLogger(CrawlerHttpClient.class);
	
	public CrawlerHttpClient() {
		super();
	}
	
	protected void sleep() {
		int maxTime = Integer.valueOf(maxSleep);
		try {
			Thread.sleep(((int) (maxTime * Math.random())));
		} catch (InterruptedException e) {
			LOG.debug("Sleep interrupted.");
		}
	}
	
	@Override
	public String getWebpageString(URI uri) {
		sleep();
		return super.getWebpageString(uri);
	}

	@Override
	public Document getWebpageHTML(URI uri) {
		sleep();
		return super.getWebpageHTML(uri);
	}
	
	@Override
	public Document getWebpageXML(URI uri) {
		sleep();
		return super.getWebpageXML(uri);
	}

	@Override
	public Document getWebpageDocumentMinusComments(URI uri) {
		sleep();
		return super.getWebpageDocumentMinusComments(uri);
	}
	
	@Override
	public String getPostResultString(PostMethod postMethod) {
		sleep();
		return super.getPostResultString(postMethod);
	}
	
	@Override
	public Document getPostResultDocument(PostMethod postMethod) {
		sleep();
		return super.getPostResultDocument(postMethod);
	}

	@Override
	public Header[] getHeaders(URI uri) {
		sleep();
		return super.getHeaders(uri);
	}

	@Override
	public GetMethod executeGET(URI uri) {
		sleep();
		return super.executeGET(uri);
	}

	@Override
	public HeadMethod executeHEAD(URI uri) {
		sleep();
		return super.executeHEAD(uri);
	}

	@Override
	public String getContentType(URI uri) {
		sleep();
		return super.getContentType(uri);
	}

}
