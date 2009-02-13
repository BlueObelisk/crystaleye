package wwmm.crystaleye.crawlers;

import static wwmm.crystaleye.crawlers.CrawlerConstants.DOI_SITE_URL;

import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;

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
	
	private void validate() {
		String doiUrl = doiUri.toString();
		if (!doiUri.toString().startsWith(DOI_SITE_URL)) {
			throw new DOIException("URI "+doiUrl+" is not a DOI.");
		}
	}
	
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
