package wwmm.crystaleye.crawlers;

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

	public void setLinkText(String linkText) {
		this.linkText = linkText;
	}

	public void setUri(URI uri) {
		this.uri = uri;
	}

	public URI getUri() {
		return uri;
	}

	public String getContentType() {
		return contentType;
	}

	public void setMimetype(String mimetype) {
		this.contentType = mimetype;
	}
	
	
	
}
