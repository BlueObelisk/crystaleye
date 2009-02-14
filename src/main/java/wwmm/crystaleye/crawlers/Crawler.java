package wwmm.crystaleye.crawlers;

import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.log4j.Logger;

import wwmm.crystaleye.BasicHttpClient;

public abstract class Crawler {

	BasicHttpClient httpClient;
	
	private static final Logger LOG = Logger.getLogger(Crawler.class);
	
	public Crawler() {
		httpClient = new CrawlerHttpClient();
	}
	
	protected URI createURI(String url) {
		URI uri = null;
		try {
			uri = new URI(url, false);
		} catch (URIException e) {
			throw new CrawlerRuntimeException("Problem creating URI from: "+url, e);
		} catch (NullPointerException e) {
			throw new CrawlerRuntimeException("Cannot create a URI from a null String.", e);
		}
		return uri;
	}
	
}
