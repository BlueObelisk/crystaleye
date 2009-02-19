package wwmm.crystaleye.crawlers;

import static wwmm.crystaleye.CrystalEyeConstants.X_XHTML;

import java.util.ArrayList;
import java.util.List;

import nu.xom.Element;
import nu.xom.Nodes;

import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.log4j.Logger;

/**
 * <p>
 * The <code>ActaArticleCrawler</code> class uses a provided DOI to get
 * information about an article that is published in a journal of Acta
 * Crytallographica.
 * </p>
 * 
 * @author Nick Day
 * @version 1.1
 * 
 */
public class ActaArticleCrawler extends ArticleCrawler {
	
	private static final Logger LOG = Logger.getLogger(ActaArticleCrawler.class);

	/**
	 * <p>
	 * Creates an instance of the ActaArticleCrawler class and
	 * specifies the DOI of the article to be crawled.
	 * </p>
	 * 
	 * @param doi of the article to be crawled.
	 */
	public ActaArticleCrawler(DOI doi) {
		super(doi);
	}

	/**
	 * <p>
	 * Crawls the article abstract webpage for information, which is 
	 * returned in an ArticleDetails object.
	 * </p> 
	 * 
	 * @return ArticleDetails object containing important details about
	 * the article (e.g. title, authors, reference, supplementary 
	 * files).
	 * 
	 */
	@Override
	public ArticleDetails getDetails() {
		if (!doiResolved) {
			LOG.warn("The DOI provided for the article abstract ("+doi.toString()+") has not resolved so we cannot get article details.");
			return ad;
		}
		URI fullTextLink = getFullTextLink();
		if (fullTextLink != null) {
			ad.setFullTextLink(fullTextLink);
		}
		List<SupplementaryFileDetails> suppFiles = getSupplementaryFilesDetails();
		setBibtexTool();
		if (bibtexTool != null) {
			String title = bibtexTool.getTitle();
			ArticleReference ref = bibtexTool.getReference();
			ad.setHasBeenPublished(true);
			String authors = bibtexTool.getAuthors();
			ad.setTitle(title);
			ad.setReference(ref);
			ad.setAuthors(authors);
			ad.setSuppFiles(suppFiles);
		}
		return ad;
	}
	
	/**
	 * <p>
	 * Gets the article Bibtex file from the abstract webpage and sets
	 * the superclass <code>bibtexTool</code>.
	 * </p>
	 * 
	 */
	private void setBibtexTool() {
		String articleId = getArticleId();
		PostMethod postMethod = new PostMethod("http://scripts.iucr.org/cgi-bin/biblio");
		NameValuePair[] nvps = {
		        new NameValuePair("name", "saveas"),
		        new NameValuePair("cnor", articleId),
		        new NameValuePair("Action", "download")
		      };
		postMethod.setRequestBody(nvps);
		String bibstr = httpClient.getPostResultString(postMethod);
		bibtexTool = new BibtexTool(bibstr);
	}
	
	/**
	 * <p>
	 * Gets the article's unique ID (as provided by the publisher) from
	 * the abstract webpage.
	 * </p>
	 * 
	 * @return String containing the article's unique ID.
	 * 
	 */
	private String getArticleId() {
		Nodes nds = articleAbstractHtml.query(".//x:input[@name='cnor']", X_XHTML);
		if (nds.size() == 0) {
			throw new CrawlerRuntimeException("Could not find the article ID for "+doi.toString()+
					" webpage structure must have changed.  Crawler needs rewriting!");
		}
		return ((Element)nds.get(0)).getAttributeValue("value");
	}
	
	/**
	 * <p>
	 * Gets the URI of the article full-text.
	 * </p>
	 * 
	 * @return URI of the article full-text.
	 * 
	 */
	private URI getFullTextLink() {
		Nodes fullTextHtmlLinks = articleAbstractHtml.query(".//x:a[./x:img[contains(@src,'graphics/htmlborder.gif')]]", X_XHTML);
		if (fullTextHtmlLinks.size() != 1) {
			throw new CrawlerRuntimeException("Problem finding full text HTML link: "+doi);
		}
		String fullTextUrl = ((Element)fullTextHtmlLinks.get(0)).getAttributeValue("href");
		return createURI(fullTextUrl);
	}

	/**
	 * <p>
	 * Gets the details of any supplementary files provided alongside
	 * the published article.
	 * </p>
	 * 
	 * @return a list where each item describes a separate supplementary
	 * data file (as a <code>SupplementaryFileDetails</code> object).
	 * 
	 */
	private List<SupplementaryFileDetails> getSupplementaryFilesDetails() {
		Nodes cifNds = articleAbstractHtml.query(".//x:a[contains(@href,'http://scripts.iucr.org/cgi-bin/sendcif') and not(contains(@href,'mime'))]", X_XHTML);
		if (cifNds.size() == 0) {
			return new ArrayList<SupplementaryFileDetails>(0);
		}
		String cifUrl = ((Element)cifNds.get(0)).getAttributeValue("href");
		URI cifUri = createURI(cifUrl);
		String contentType = httpClient.getContentType(cifUri);
		SupplementaryFileDetails suppFile = new SupplementaryFileDetails(cifUri, "CIF", contentType);
		List<SupplementaryFileDetails> suppFiles = new ArrayList<SupplementaryFileDetails>(1);
		suppFiles.add(suppFile);
		return suppFiles;
	}
	
}
