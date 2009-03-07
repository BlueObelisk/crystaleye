package wwmm.crystaleye.crawler;

import java.util.List;

import org.apache.commons.httpclient.URI;
import org.apache.commons.lang.StringUtils;

/**
 * <p>
 * The <code>ArticleDetails</code> class provides 
 * a description of a published journal article.  Is a
 * simple container class where the only methods are 
 * getters and setters for each instance variable.
 * </p>
 * 
 * @author Nick Day
 * @version 1.1
 * 
 */
public class ArticleDetails {

	private DOI doi;
	private boolean doiResolved;

	private URI fullTextLink;
	private String title;
	private ArticleReference reference;
	private String authors;
	private List<SupplementaryFileDetails> suppFiles;
	
	// assume initially that the article has been published,
	// it is more likely than not.
	private boolean hasBeenPublished = true;
	
	public ArticleDetails() {
		;
	}

	/**
	 * <p>
	 * States whether or not the article has been 
	 * published, i.e. made available at the publisher's
	 * website with a proper bibliographic reference.
	 * </p>
	 * 
	 * @return boolean of whether the article has been published
	 */
	public boolean hasBeenPublished() {
		return hasBeenPublished;
	}

	/**
	 * <p>
	 * Set whether or not the article has been 
	 * published, i.e. made available at the publisher's
	 * website with a proper bibliographic reference.
	 * </p>
	 * 
	 * @param hasBeenPublished - boolean of whether the article
	 * has been published.
	 */
	public void setHasBeenPublished(boolean hasBeenPublished) {
		this.hasBeenPublished = hasBeenPublished;
	}

	/**
	 * <p>
	 * Get whether or not the DOI for the article resolves
	 * at the DOI providing service (as of 24/02/2009 it is
	 * http://dx.doi.org).
	 * </p>
	 * 
	 * @return boolean of whether the DOI for the article 
	 * resolves at the providing service.
	 */
	public boolean isDoiResolved() {
		return doiResolved;
	}

	/**
	 * <p>
	 * State whether or not the DOI for the article resolves
	 * at the DOI providing service (as of 24/02/2009 it is
	 * http://dx.doi.org).
	 * </p>
	 * 
	 * @param doiResolved - boolean of whether the DOI for the
	 * article resolves at the providing service.
	 */
	public void setDoiResolved(boolean doiResolved) {
		this.doiResolved = doiResolved;
	}

	/**
	 * <p>
	 * Get the URI for the full-text of the article.
	 * </p>
	 * 
	 * @return URI for the full-text of the article.
	 */
	public URI getFullTextLink() {
		return fullTextLink;
	}

	/**
	 * <p>
	 * Set the URI for the full-text of the article.
	 * </p>
	 * 
	 * @param fullTextHtmlLink - URI for the full-text
	 * of the article.
	 */
	public void setFullTextLink(URI fullTextHtmlLink) {
		this.fullTextLink = fullTextHtmlLink;
	}

	/**
	 * <p>
	 * Get details for any files that are provided as 
	 * supplementary information to the article. 
	 * </p>
	 * 
	 * @return details for each supplementary file to the
	 * article.
	 */
	public List<SupplementaryFileDetails> getSuppFiles() {
		return suppFiles;
	}

	/**
	 * <p>
	 * Set details for any files that are provided as 
	 * supplementary information to the article. 
	 * </p>
	 * 
	 * @param suppFiles - details for each supplementary
	 * file to the article.
	 */
	public void setSuppFiles(List<SupplementaryFileDetails> suppFiles) {
		this.suppFiles = suppFiles;
	}

	/**
	 * <p>
	 * Set the article Digital Object Identifier.
	 * </p>
	 * 
	 * @param doi - the Digital Object Identifier of the
	 * article.
	 */
	public void setDoi(DOI doi) {
		this.doi = doi;
	}

	/**
	 * <p>
	 * Get the article Digital Object Identifier.
	 * </p>
	 * 
	 * @return Digital Object Identifier for the article.
	 */
	public DOI getDoi() {
		return doi;
	}

	/**
	 * <p>
	 * Get the title of the article.
	 * </p>
	 * 
	 * @return String title of the article.
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * <p>
	 * Get the data-items that constitute a bibliographic 
	 * reference for the article.
	 * </p>
	 * 
	 * @return bibliographic reference for the article.
	 */
	public ArticleReference getReference() {
		return reference;
	}

	/**
	 * <p>
	 * Get the names of the authors of the article.
	 * </p>
	 * 
	 * @return String containing the authors of the article.
	 */
	public String getAuthors() {
		return authors;
	}

	/**
	 * <p>
	 * Set the title of the article.
	 * </p>
	 * 
	 * @param title of the article.
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * <p>
	 * Provide data items that constitute a bibliographic 
	 * reference to the article.
	 * </p>
	 * 
	 * @param reference - contains data-items that make up
	 * a bibliographic reference to the article.
	 */
	public void setReference(ArticleReference reference) {
		this.reference = reference;
	}

	/**
	 * <p>
	 * Set a <code>String</code> containing the names
	 * of the authors of the article.
	 * </p>
	 * 
	 * @param authors of the article.
	 */
	public void setAuthors(String authors) {
		this.authors = authors;
	}

	/**
	 * <p>
	 * Intended only for debugging.
	 *
	 * Here, if they are not null, the contents of every field 
	 * are placed into the result, with one field per line.
	 * </p>
	 * 
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
			if (fullTextLink != null) {
				result.append("  Full text HTML link: "+fullTextLink+NEW_LINE);
			}
			if (StringUtils.isNotEmpty(title)) {
				result.append("  Title: "+title+NEW_LINE);
			}
			if (StringUtils.isNotEmpty(authors)) {
				result.append("  Authors: "+authors+NEW_LINE);
			}
			result.append("  Bib data: "+NEW_LINE);
			if (StringUtils.isNotEmpty(reference.getJournalTitle())) {
				result.append("    Journal: "+reference.getJournalTitle()+NEW_LINE);
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
			result.append("  Supplementary file details:"+NEW_LINE);
			int scount = 1;
			for (SupplementaryFileDetails sf : suppFiles) {
				if (sf.getURI() != null) {
					result.append("    URI: "+sf.getURI()+NEW_LINE);
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
			if (!hasBeenPublished) {
				result.append("    ### THIS ARTICLE HAS YET TO BE PUBLISHED - REFERENCE DETAILS WILL BE INCOMPLETE ###\n");
			}
		}
		result.append("}"+NEW_LINE);

		return result.toString();
	}

}
