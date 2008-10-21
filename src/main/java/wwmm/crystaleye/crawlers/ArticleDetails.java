package wwmm.crystaleye.crawlers;

import java.util.List;

import org.apache.commons.httpclient.URI;

public class ArticleDetails {
	
	private URI doi;
	private String title;
	private ArticleReference reference;
	private String authors;
	private List<SupplementaryFile> suppFiles;
	
	public ArticleDetails() {
		;
	}
	
	public ArticleDetails(URI doi, String title, ArticleReference ref, String authors, List<SupplementaryFile> suppFiles) {
		this.doi = doi;
		this.title = title;
		this.reference = ref;
		this.authors = authors;
		this.suppFiles = suppFiles;
	}

	public List<SupplementaryFile> getSuppFiles() {
		return suppFiles;
	}

	public void setSuppFiles(List<SupplementaryFile> suppFiles) {
		this.suppFiles = suppFiles;
	}

	public void setDoi(URI doi) {
		this.doi = doi;
	}

	public URI getDoi() {
		return doi;
	}

	public String getTitle() {
		return title;
	}

	public ArticleReference getReference() {
		return reference;
	}

	public String getAuthors() {
		return authors;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setReference(ArticleReference reference) {
		this.reference = reference;
	}

	public void setAuthors(String authors) {
		this.authors = authors;
	}

}
