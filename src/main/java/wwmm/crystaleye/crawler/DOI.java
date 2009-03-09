package wwmm.crystaleye.crawler;

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

	private URI doiUri;
	public static final String DOI_SITE_URL = "http://dx.doi.org";

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
		if (!doiUri.toString().startsWith(DOI_SITE_URL) ||
				doiUrl.length() <= DOI_SITE_URL.length()+1) {
			throw new DOIRuntimeException("URI "+doiUrl+" is not a valid DOI.");
		}
	}
	
	/**
	 * <p>
	 * Trims the DOI providers website URL off the start of
	 * the provided DOI (e.g. it will return 10.1021/b789765f
	 * from a DOI of http://dx.doi.org/10.1021/b789765f).
	 * </p>
	 * 
	 * @param doi the DOI for which you want the postfix.
	 * 
	 * @return the postfix of the provided DOI.
	 */
	public String getPostfix() {
		String doiStr = this.toString();
		return doiStr.replaceAll(DOI.DOI_SITE_URL+"/", "");
	}

	/**
	 * <p>
	 * Convenience method for create URIs and handling any
	 * exceptions.
	 * </p>
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
	 * <p>
	 * Get the DOIs URI.
	 * </p>
	 * 
	 * @return the URI for the DOI.
	 */
	public URI getUri() {
		return doiUri;
	}
	
	/**
	 * <p>
	 * Simple method to get the DOIs URI string.
	 * </p>
	 * 
	 */
	@Override
	public String toString() {
		try {
			return doiUri.getURI();
		} catch (URIException e) {
			throw new RuntimeException("Error getting DOI string: "+doiUri, e);
		}
	}

	/**
	 * <p>
	 * Method to override equals in <code>Object</code> class.
	 * </p>
	 * 
	 * @return true if the supplied Object is a DOI with the same
	 * URI as this, false if not. 
	 */
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if((obj == null) || (obj.getClass() != this.getClass())) {
			return false;
		}
		DOI doi = (DOI)obj;
		if (this.doiUri.equals(doi.getUri())) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * <p>
	 * Overrides <code>hashCode</code> method in <code>Object</code> class.  
	 * Implemented because I have overriden <code>equals(Object)</code>.
	 * </p>
	 */
	@Override
	public int hashCode() {
		int hash = 7;
		hash = 31 * hash + (null == doiUri ? 0 : doiUri.hashCode());
		return hash;
	}

}
