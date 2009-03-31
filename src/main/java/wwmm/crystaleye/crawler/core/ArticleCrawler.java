package wwmm.crystaleye.crawler.core;

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
	protected Document articleAbstractHtml;
	protected boolean doiResolved;
	protected ArticleDetails ad = new ArticleDetails();
	protected BibtexTool bibtexTool;

	public ArticleCrawler(DOI doi) {
		this.doi = doi;
		init();
	}

	/**
	 * <p>
	 * Uses the provided <code>DOI</code> to initialise some instance 
	 * variables, including:
	 *  1. getting the article abstract webpage HTML from the provided DOI
	 *     and setting it as <code>articleAbstractHtml</code>.
	 *  2. checking whether the provided DOI has resolved (NB. if a DOI does not 
	 *     resolve, then http://dx.doi.org still returns a webpage with HTTP 
	 *     status 200 (OK).  So to check if something has gone awry, we need to 
	 *     parse the HTML to check for the error message =0 ). 
	 *  3. adds a boolean flag to an <code>ArticleDetails</code> instance, 
	 *     which should be completed by <code>getDetails()</code> of the 
	 *     implementing subclass of this.
	 * </p>
	 * 
	 */
	private void init() {
		articleAbstractHtml = httpClient.getResourceHTML(doi.getUri());
		setHasDoiResolved();
		ad.setDoiResolved(doiResolved);
		ad.setDoi(doi);
	}

	/**
	 * <p>
	 * Sets a boolean which specifies whether the provided DOI resolves 
	 * at http://dx.doi.org.
	 * </p>
	 * 
	 */
	private void setHasDoiResolved() {
		Nodes nodes = articleAbstractHtml.query(".//x:body[contains(.,'Error - DOI Not Found')]", X_XHTML);
		if (nodes.size() > 0) {
			doiResolved = false;
		} else {
			doiResolved = true;
		}
	}
	
	/**
	 * <p>
	 * Uses the instance DOI to construct an ArticleDetails class
	 * describing the important details for an article.  This is the 
	 * only method that each subclass should have to implement.
	 * </p>
	 * 
	 * @return ArticleDetails
	 */
	abstract public ArticleDetails getDetails();

}
