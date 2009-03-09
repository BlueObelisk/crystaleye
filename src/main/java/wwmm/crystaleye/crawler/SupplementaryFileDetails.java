package wwmm.crystaleye.crawler;

import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;

public class SupplementaryFileDetails {

	private URI uri;
	private String filename;
	private String linkText;
	private String contentType;
	
	public SupplementaryFileDetails(URI uri, String filename, String linkText, String contentType) {
		this.uri = uri;
		this.filename = filename;
		this.linkText = linkText;
		this.contentType = contentType;
		validate();
	}
	
	private void validate() {
		if (!getUriString().endsWith(filename)) {
			throw new RuntimeException("The provided filename must be " +
					"the latter part of the provided URI.");
		}
	}

	public String getLinkText() {
		return linkText;
	}

	public URI getURI() {
		return uri;
	}
	
	public String getUriString() {
		String uriStr = null;
		try {
			uriStr = uri.getURI();
		} catch (URIException e) {
			throw new RuntimeException("Exception getting string for URI: "+uri);
		}
		return uriStr;
	}
	
	public String getFilename() {
		return filename;
	}

	public String getContentType() {
		return contentType;
	}
	
}
