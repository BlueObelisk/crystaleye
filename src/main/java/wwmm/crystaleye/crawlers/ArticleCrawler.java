package wwmm.crystaleye.crawlers;

import static wwmm.crystaleye.CrystalEyeConstants.X_XHTML;
import nu.xom.Document;
import nu.xom.Nodes;

/**
 * <p>
 * The abstract <code>ArticleCrawler</code> class provides a base implementation
 * for crawling the webpages of published articles.  It is assumed that all 
 * articles have a DOI, which can be used to find all the necessary details.  Hence,
 * this class has a single constructor which takes a DOI parameter.
 * </p>
 * 
 * @author Nick Day
 * @version 1.1
 * 
 */
public abstract class ArticleCrawler extends Crawler {

	protected DOI doi;
	protected Document articleAbstractDoc;
	protected boolean doiResolved;
	protected ArticleDetails ad = new ArticleDetails();
	protected BibtexTool bibtexTool;

	public ArticleCrawler(DOI doi) {
		this.doi = doi;
		init();
	}

	/**
	 * Uses the provided DOI to init some instance variables, including:
	 *  1. getting the HTML from the provided article abstract URI.
	 *  2. Check whether the provided DOI has resolved (NB. if a DOI does not 
	 *     resolve, then dx.doi.org still returns a webpage with HTTP status 200 
	 *     (OK).  So to check if something has gone awry, we need to parse the 
	 *     HTML to check for the error message =0 ). 
	 *  3. adds boolean flags for points 3 and 4 to a ArticleDetails instance, 
	 *     which should be completed by getDetails() of the implementing 
	 *     subclass of this.
	 * 
	 */
	private void init() {
		articleAbstractDoc = httpClient.getResourceHTML(doi.getUri());
		setHasDoiResolved();
		ad.setDoiResolved(doiResolved);
		ad.setDoi(doi);
	}

	/**
	 * Sets a boolean which specifies whether the provided DOI resolves 
	 * at http://dx.doi.org.
	 * 
	 */
	private void setHasDoiResolved() {
		Nodes nodes = articleAbstractDoc.query(".//x:body[contains(.,'Error - DOI Not Found')]", X_XHTML);
		if (nodes.size() > 0) {
			doiResolved = false;
		} else {
			doiResolved = true;
		}
	}

}
