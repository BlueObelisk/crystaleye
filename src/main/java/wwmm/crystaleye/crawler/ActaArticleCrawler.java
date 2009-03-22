package wwmm.crystaleye.crawler;

import static wwmm.crystaleye.CrystalEyeConstants.X_XHTML;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nu.xom.Document;
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
		ad.setFullTextLink(fullTextLink);
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
		LOG.info("Finished finding article details: "+doi);
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
			LOG.warn("Could not find the article ID for "+doi.toString()+
			" webpage structure may have changed.  Crawler may need rewriting!");
			return null;
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
			LOG.warn("Problem finding full text HTML link: "+doi);
			return null;
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
		String url = ((Element)cifNds.get(0)).getAttributeValue("href");
		URI uri = createURI(url);
		List<URI> cifUriList = getCifUrisFromUri(uri);
		List<SupplementaryFileDetails> suppFiles = new ArrayList<SupplementaryFileDetails>(cifUriList.size());
		for (URI cifUri : cifUriList) {
			String filename = getFilenameFromUrl(getURIString(cifUri));
			String contentType = httpClient.getContentType(cifUri);
			SupplementaryFileDetails suppFile = new SupplementaryFileDetails(cifUri, filename, "CIF", contentType);
			suppFiles.add(suppFile);
		}
		return suppFiles;
	}

	/**
	 * <p>
	 * Retrieve the CIF URIs for this article from the URI provided.
	 * </p>
	 * 
	 * @param uri from which to obtain the CIF URIs for this article.
	 * 
	 * @return a list of the URIs for the CIFs for this article.
	 */
	private List<URI> getCifUrisFromUri(URI uri) {
		List<URI> cifUriList = new ArrayList<URI>();
		if (uriPointsToCifListPage(uri)) {
			Document pageDoc = httpClient.getResourceHTML(uri);
			Nodes linkNds = pageDoc.query(".//x:a[contains(@href,'http://scripts.iucr.org/cgi-bin/sendcif')]", X_XHTML);
			if (linkNds.size() == 0) {
				LOG.warn("Could not find any CIF links at the supposed CIF list page: "+uri);
			}
			for (int i = 0; i < linkNds.size(); i++) {
				String url = ((Element)linkNds.get(i)).getAttributeValue("href");
				cifUriList.add(createURI(url));
			}
		} else {
			cifUriList.add(uri);
		}
		return cifUriList;
	}

	/**
	 * <p>
	 * If the URI does not contain sup(\\d+) at the end, then it means that
	 * if doesn't point to a CIF, but instead points to another webpage that
	 * contains a list of CIFs.
	 * </p>
	 * 
	 * @param uri
	 * 
	 * @return
	 */
	private boolean uriPointsToCifListPage(URI uri) {
		String url = getURIString(uri);
		int idx = url.lastIndexOf("/");
		String s = url.substring(idx);
		if (s.contains("sup")) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * <p>
	 * Gets the name of the supplementary file at the publisher's site from
	 * the supplementary file URL.
	 * </p>
	 * 
	 * @param fileUrl - the URL from which to obtain the filename.
	 * 
	 * @return the filename of the supplementary file.
	 */
	private String getFilenameFromUrl(String fileUrl) {
		// first, see if the file is a CIF, which has special URLs of the form
		// below
		Pattern pattern = Pattern.compile("http://scripts.iucr.org/cgi-bin/sendcif\\?(.{6}sup\\d+)");
		Matcher matcher = null;
		matcher = pattern.matcher(fileUrl);
		if (matcher.find()) {
			return matcher.group(1);
		} else {
			// if the previous regexp does not match, then it isn't a CIF, so
			// it should be fine just to take substring after the final '/'
			int idx = fileUrl.lastIndexOf("/");
			return fileUrl.substring(idx+1);
		}
	}

}
