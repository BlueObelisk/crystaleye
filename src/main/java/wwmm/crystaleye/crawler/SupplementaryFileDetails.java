package wwmm.crystaleye.crawler;

import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;

public class SupplementaryFileDetails {

	private URI uri;
	private String linkText;
	private String contentType;
	
	public SupplementaryFileDetails(URI uri, String linkText, String contentType) {
		this.uri = uri;
		this.linkText = linkText;
		this.contentType = contentType;
	}

	public String getLinkText() {
		return linkText;
	}

	public URI getURI() {
		return uri;
	}
	
	public String getUriString() throws URIException {
		return uri.getURI();
	}

	public String getContentType() {
		return contentType;
	}
	
}
