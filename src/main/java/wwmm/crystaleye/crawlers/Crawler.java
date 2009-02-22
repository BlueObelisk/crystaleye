package wwmm.crystaleye.crawlers;

import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.log4j.Logger;

import wwmm.crystaleye.BasicHttpClient;

/**
 * <p>
 * The abstract <code>Crawler</code> class is intended to be used as a 
 * superclass for any web crawler classes.  It contains objects (e.g. 
 * a HTTP client) and methods generic to the use and manipulation of 
 * web resources.  
 * </p>
 * 
 * @author Nick Day
 * @version 1.1
 * 
 */
public abstract class Crawler {

	BasicHttpClient httpClient;
	
	private static final Logger LOG = Logger.getLogger(Crawler.class);
	
	public Crawler() {
		httpClient = new CrawlerHttpClient();
	}
	
	/**
	 * <p>
	 * Convenience method to handle the exceptions in creating a URI
	 * that has not yet been escaped.
	 * </p>
	 * 
	 * @param url
	 * 
	 * @return URI representing the provided <code>url</code>.
	 */
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
	
	/**
	 * <p>
	 * Convenience method to handle the exceptions in creating a URI
	 * that may or may not have been escaped.
	 * </p>
	 * 
	 * @param url
	 * 
	 * @return URI representing the provided <code>url</code>.
	 */
	protected URI createURI(String url, boolean escaped) {
		URI uri = null;
		try {
			uri = new URI(url, true);
		} catch (URIException e) {
			throw new CrawlerRuntimeException("Problem creating URI from: "+url, e);
		} catch (NullPointerException e) {
			throw new CrawlerRuntimeException("Cannot create a URI from a null String.", e);
		}
		return uri;
	}
	
}
