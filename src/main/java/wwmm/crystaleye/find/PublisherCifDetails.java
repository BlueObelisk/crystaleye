package wwmm.crystaleye.find;

import org.apache.commons.httpclient.URI;

public class PublisherCifDetails {

	URI cifUri;
	String doi;
	String articleTitle;
	
	public PublisherCifDetails(URI cifUri, String doi, String articleTitle) {
		this.cifUri = cifUri;
		this.doi = doi;
		this.articleTitle = articleTitle;
	}

	public URI getCifUri() {
		return cifUri;
	}

	public void setCifUri(URI cifUri) {
		this.cifUri = cifUri;
	}

	public String getDoi() {
		return doi;
	}

	public void setDoi(String doi) {
		this.doi = doi;
	}

	public String getArticleTitle() {
		return articleTitle;
	}

	public void setArticleTitle(String articleTitle) {
		this.articleTitle = articleTitle;
	}
	
}
