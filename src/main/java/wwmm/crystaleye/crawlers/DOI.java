package wwmm.crystaleye.crawlers;

import static wwmm.crystaleye.crawlers.CrawlerConstants.DOI_SITE_URL;

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
	 * Make sure that the provided <code>URI</code> is a valid DOI.
	 * 
	 * @throws RuntimeException if the provided URI does not
	 * 
	 */
	private void validate() {
		String doiUrl = doiUri.toString();
		if (!doiUri.toString().startsWith(DOI_SITE_URL)) {
			throw new DOIRuntimeException("URI "+doiUrl+" is not a DOI.");
		}
	}
	
	/**
	 * 
	 * 
	 * @param url
	 * @return URI
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
	
	public URI getUri() {
		return doiUri;
	}
	
	@Override
	public String toString() {
		return doiUri.toString();
	}
	
}
