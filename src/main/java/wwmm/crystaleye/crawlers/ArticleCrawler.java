package wwmm.crystaleye.crawlers;

import static wwmm.crystaleye.CrystalEyeConstants.X_XHTML;
import static wwmm.crystaleye.crawlers.CrawlerConstants.*;
import nu.xom.Document;
import nu.xom.Nodes;

import org.apache.commons.httpclient.URI;

public abstract class ArticleCrawler extends Crawler {
	
	protected URI articleAbstractUri;
	protected Document articleAbstractDoc;
	protected boolean doiResolved;
	protected boolean articleAbstractUriIsADoi;
	protected ArticleDetails ad = new ArticleDetails();
	protected BibtexTool bibtexTool;
	
	public ArticleCrawler(URI articleAbstractUri) {
		this.articleAbstractUri = articleAbstractUri;
		init();
	}
	
	/**
	 * Initialises some instance variables, including:
	 *  1. getting the HTML from the provided article abstract URI.
	 *  2. checking whether the provided URI is a DOI (Digital Object 
	 *     Identifier).
	 *  3. If it is a DOI, a check is made as to whether it has resolved (NB.
	 *     if a DOI does not resolve, then dx.doi.org still returns a webpage 
	 *     with HTTP status 200 (OK).  So to check if something has gone awry, 
	 *     we need to parse the HTML to check for the error message =0 ). 
	 *  4. adds boolean flags for points 3 and 4 to a ArticleDetails instance, 
	 *     which should be completed by getDetails() of the implementing 
	 *     subclass of this.
	 * 
	 */
	private void init() {
		articleAbstractDoc = httpClient.getWebpageHTML(articleAbstractUri);
		setAbstractPageUriIsADoi();
		if (articleAbstractUriIsADoi) {
			setHasDoiResolved(articleAbstractDoc);
			ad.setDoiResolved(doiResolved);
			ad.setDoi(articleAbstractUri);
		}
	}
	
	/**
	 * Sets a boolean which specifies if the provided URI is a DOI.
	 * 
	 */
	private void setAbstractPageUriIsADoi() {
		if (articleAbstractUri.toString().startsWith(DOI_SITE_URL)) {
			articleAbstractUriIsADoi = true;
		} else {
			articleAbstractUriIsADoi = false;
		}
	}
	
	/**
	 * Sets a boolean which specifies whether the provided DOI resolves 
	 * at http://dx.doi.org. Should only get called if 
	 * articleAbstractUriIsADoi is true.
	 * 
	 * @param doc
	 */
	private void setHasDoiResolved(Document doc) {
		if (articleAbstractUriIsADoi == false) {
			throw new IllegalStateException("Method should only be called when abstractPageUriIsADoi is true.");
		}
		Nodes nodes = doc.query(".//x:body[contains(.,'Error - DOI Not Found')]", X_XHTML);
		if (nodes.size() > 0) {
			doiResolved = false;
		} else {
			doiResolved = true;
		}
	}

}
