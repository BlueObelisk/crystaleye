package wwmm.crystaleye.crawlers;

import org.apache.commons.httpclient.URI;

public class SupplementaryFile {

	private URI uri;
	private String linkText;
	private String mimetype;
	
	public SupplementaryFile(URI uri, String linkText, String mimetype) {
		this.uri = uri;
		this.linkText = linkText;
		this.mimetype = mimetype;
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

	public String getTitle() {
		return linkText;
	}

	public String getMimetype() {
		return mimetype;
	}

	public void setTitle(String title) {
		this.linkText = title;
	}

	public void setMimetype(String mimetype) {
		this.mimetype = mimetype;
	}
	
	
	
}
