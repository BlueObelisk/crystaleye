package wwmm.crystaleye.crawler;

import org.apache.commons.httpclient.URI;

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

	public URI getUri() {
		return uri;
	}

	public String getContentType() {
		return contentType;
	}
	
}
