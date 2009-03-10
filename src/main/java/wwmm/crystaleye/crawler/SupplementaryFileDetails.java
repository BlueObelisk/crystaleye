package wwmm.crystaleye.crawler;

import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;

/**
 * <p>
 * Class used to represent details about a file that is provided
 * as supplementary information to a published journal article.
 * </p>
 * 
 * @author Nick Day
 * @version 0.1
 *
 */
public class SupplementaryFileDetails {

	private URI uri;
	private String fileId;
	private String linkText;
	private String contentType;
	
	/**
	 * <p>
	 * Creates an instance of the SupplementaryFileDetails class. 
	 * NOTE that the provided fileId MUST be part of the provided URI.
	 * The reason this must be provided is that there is no way of
	 * automatically asserting the files ID from the URI alone.
	 * </p>
	 * 
	 * @param uri - the URI that the file resides at.
	 * @param fileId - the site-specific identifier for the file.
	 * @param linkText - the text from the HTML link that points to the file.
	 * @param contentType - the Content-type of the file from its HTTP headers.
	 */
	public SupplementaryFileDetails(URI uri, String fileId, String linkText, String contentType) {
		this.uri = uri;
		this.fileId = fileId;
		this.linkText = linkText;
		this.contentType = contentType;
		validate();
	}
	
	/**
	 * Make sure that the provided file ID is part of the supplementary
	 * files URL. 
	 */
	private void validate() {
		if (!getUriString().contains(fileId)) {
			throw new RuntimeException("The provided filename must be " +
					"the latter part of the provided URI.");
		}
	}

	/**
	 * <p>
	 * Gets the text from HTML link that points to the supplementary file.
	 * </p>
	 * 
	 * @return text from the HTML link that points to the
	 * supplementary file.
	 */
	public String getLinkText() {
		return linkText;
	}

	/**
	 * <p>
	 * Gets the URI that points to the supplementary file.
	 * </p>
	 * 
	 * @return the URI that points to the supplementary file.
	 */
	public URI getURI() {
		return uri;
	}
	
	/**
	 * <p>
	 * Gets the URI that points to the supplementary file in
	 * String form.
	 * </p>
	 * 
	 * @return String form of the URI that points to the
	 * supplementary file.
	 */
	public String getUriString() {
		String uriStr = null;
		try {
			uriStr = uri.getURI();
		} catch (URIException e) {
			throw new RuntimeException("Exception getting string for URI: "+uri);
		}
		return uriStr;
	}
	
	/**
	 * <p>
	 * Gets the site-specific ID of the supplementary file.
	 * </p>
	 * 
	 * @return the site-specific ID of the supplementary file.
	 */
	public String getFileId() {
		return fileId;
	}

	/**
	 * <p>
	 * Gets the Content-type of the supplementary file, as described
	 * in its HTTP header.
	 * </p>
	 * 
	 * @return the Content-type of the supplementary file, as described
	 * in its HTTP header.
	 */
	public String getContentType() {
		return contentType;
	}
	
}
