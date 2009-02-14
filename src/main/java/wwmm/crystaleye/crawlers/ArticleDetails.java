package wwmm.crystaleye.crawlers;

import java.util.List;

import org.apache.commons.httpclient.URI;
import org.apache.commons.lang.StringUtils;

public class ArticleDetails {

	private DOI doi;
	private boolean doiResolved;

	private URI fullTextHtmlLink;
	private String title;
	private ArticleReference reference;
	private String authors;
	private List<SupplementaryFileDetails> suppFiles;

	public ArticleDetails() {
		;
	}

	public boolean isDoiResolved() {
		return doiResolved;
	}

	public void setDoiResolved(boolean doiResolved) {
		this.doiResolved = doiResolved;
	}

	public URI getFullTextHtmlLink() {
		return fullTextHtmlLink;
	}

	public void setFullTextLink(URI fullTextHtmlLink) {
		this.fullTextHtmlLink = fullTextHtmlLink;
	}

	public List<SupplementaryFileDetails> getSuppFiles() {
		return suppFiles;
	}

	public void setSuppFiles(List<SupplementaryFileDetails> suppFiles) {
		this.suppFiles = suppFiles;
	}

	public void setDoi(DOI doi) {
		this.doi = doi;
	}

	public DOI getDoi() {
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
	 * Here, if they are not null, the contents of every field 
	 * are placed into the result, with one field per line.
	 */
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		String NEW_LINE = System.getProperty("line.separator");

		result.append(this.getClass().getName()+" Object {"+NEW_LINE);
		if (doi != null) {
			result.append("  DOI: "+doi+NEW_LINE);
		}

		if (!isDoiResolved()) {
			result.append("  ### DOI DID NOT RESOLVE - NO ARTICLE DETAILS OBTAINED ###"+NEW_LINE);
		} else {
			if (fullTextHtmlLink != null) {
				result.append("  Full text HTML link: "+fullTextHtmlLink+NEW_LINE);
			}
			if (StringUtils.isNotEmpty(title)) {
				result.append("  Title: "+title+NEW_LINE);
			}
			if (StringUtils.isNotEmpty(authors)) {
				result.append("  Authors: "+authors+NEW_LINE);
			}
			result.append("  Bib data: "+NEW_LINE);
			if (StringUtils.isNotEmpty(reference.getJournal())) {
				result.append("    Journal: "+reference.getJournal()+NEW_LINE);
			}
			if (StringUtils.isNotEmpty(reference.getYear())) {
				result.append("    Year: "+reference.getYear()+NEW_LINE);
			}
			if (StringUtils.isNotEmpty(reference.getVolume())) {
				result.append("    Volume: "+reference.getVolume()+NEW_LINE);
			}
			if (StringUtils.isNotEmpty(reference.getNumber())) {
				result.append("    Number: "+reference.getNumber()+NEW_LINE);
			}
			if (StringUtils.isNotEmpty(reference.getPages())) {
				result.append("    Pages: "+reference.getPages()+NEW_LINE);
			}
			if (!reference.hasBeenPublished()) {
				result.append("    ### THIS ARTICLE HAS YET TO BE PUBLISHED - REFERENCE DETAILS WILL BE INCOMPLETE ###\n");
			}
			result.append("  Supplementary file details:"+NEW_LINE);
			int scount = 1;
			for (SupplementaryFileDetails sf : suppFiles) {
				if (sf.getUri() != null) {
					result.append("    URI: "+sf.getUri()+NEW_LINE);
				}
				if (!StringUtils.isEmpty(sf.getLinkText())) {
					result.append("    Link text: "+sf.getLinkText()+NEW_LINE);
				}
				if (!StringUtils.isEmpty(sf.getContentType())) {
					result.append("    Content-type: "+sf.getContentType()+NEW_LINE);
				}
				if (suppFiles.size() > 1 && scount < suppFiles.size()) {
					result.append("    -----"+NEW_LINE);
				}
				scount++;
			}
			if (suppFiles.size() == 0) {
				result.append("    --no supplementary files--"+NEW_LINE);
			}
		}
		result.append("}"+NEW_LINE);

		return result.toString();
	}

}
