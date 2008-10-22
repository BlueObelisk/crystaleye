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

	/**
	 * Intended only for debugging.
	 *
	 * Here, the contents of every field are placed into the result, with
	 * one field per line.
	 */
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		String NEW_LINE = System.getProperty("line.separator");

		result.append(this.getClass().getName()+" Object {"+NEW_LINE);
		result.append("  DOI: "+doi+NEW_LINE);
		result.append("  Title: "+title+NEW_LINE);
		result.append("  Authors: "+authors+NEW_LINE);
		result.append("  Reference: "+reference.getJournalAbbreviation()+", ("+
				reference.getYear()+") "+reference.getVolume()+", "+
				reference.getPages()+"."+NEW_LINE);
		result.append("  Supplementary file details:"+NEW_LINE);
		int scount = 1;
		for (SupplementaryFile sf : suppFiles) {
			result.append("    DOI: "+sf.getUri()+NEW_LINE);
			result.append("    Link text: "+sf.getLinkText()+NEW_LINE);
			result.append("    Content-type: "+sf.getContentType()+NEW_LINE);
			if (suppFiles.size() > 1 && scount < suppFiles.size()) {
				result.append("----------"+NEW_LINE);
			}
			scount++;
		}
		if (suppFiles.size() == 0) {
			result.append("    --no supplementary files--"+NEW_LINE);
		}
		result.append("}"+NEW_LINE);

		return result.toString();
	}

}
