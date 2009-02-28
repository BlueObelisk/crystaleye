package wwmm.crystaleye.crawler;

import static wwmm.crystaleye.crawler.CrawlerConstants.DOI_SITE_URL;

import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;

/**
 * <p>
 * The <code>DOI</code> class provides a representation of a Digital Object 
 * Identifier (DOI).  It is a simple wrapper class based around a URI.
 * </p>
 * 
 * @author Nick Day
 * @version 1.1
 * 
 */
public class DOI {

	URI doiUri;

	public DOI(String doiUrl) {
		doiUri = createURI(doiUrl);
		validate();
	}

	public DOI(URI doiUri) {
		this.doiUri = doiUri;
		validate();
	}
	
	/**
	 * <p>
	 * Make sure that the provided <code>URI</code> is a 
	 * valid DOI.
	 * </p>
	 * 
	 * @throws RuntimeException if the provided URI is not
	 * a valid DOI.
	 * 
	 */
	private void validate() {
		String doiUrl = doiUri.toString();
		if (!doiUri.toString().startsWith(DOI_SITE_URL)) {
			throw new DOIRuntimeException("URI "+doiUrl+" is not a DOI.");
		}
	}
	
	/**
	 * Convenience method for create URIs and handling any
	 * exceptions.
	 * 
	 * @param url of the resource you want to create a URI for.
	 * 
	 * @return URI for the provided URL.
	 */
	private URI createURI(String url) {
		URI uri = null;
		try {
			uri = new URI(url, false);
		} catch (URIException e) {
			throw new RuntimeException("Problem creating URI from: "+url, e);
		} catch (NullPointerException e) {
			throw new RuntimeException("Cannot create a URI from a null String.", e);
		}
		return uri;
	}
	
	/**
	 * Get the DOIs URI.
	 * 
	 * @return the URI for the DOI.
	 */
	public URI getUri() {
		return doiUri;
	}
	
	/**
	 * Simple method to get the URI string.
	 * 
	 */
	@Override
	public String toString() {
		return doiUri.toString();
	}
	
}
