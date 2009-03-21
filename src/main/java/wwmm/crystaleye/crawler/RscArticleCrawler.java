package wwmm.crystaleye.crawler;

import static wwmm.crystaleye.CrystalEyeConstants.X_XHTML;
import static wwmm.crystaleye.crawler.CrawlerConstants.RSC_HOMEPAGE_URL;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;

import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.log4j.Logger;

/**
 * <p>
 * The <code>RscArticleCrawler</code> class uses a provided DOI to get
 * information about an article that is published in a journal of the 
 * Royal Society of Chemistry.
 * </p>
 * 
 * @author Nick Day
 * @version 1.1
 * 
 */
public class RscArticleCrawler extends ArticleCrawler {

	private static final Logger LOG = Logger.getLogger(RscArticleCrawler.class);

	/**
	 * <p>
	 * Creates an instance of the RscArticleCrawler class and
	 * specifies the DOI of the article to be crawled.
	 * </p>
	 * 
	 * @param doi of the article to be crawled.
	 */
	public RscArticleCrawler(DOI doi) {
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
		String title = getTitle();
		ArticleReference ref = getReference();
		String authors = getAuthors();
		List<SupplementaryFileDetails> suppFiles = getSupplementaryFilesDetails();
		ad.setFullTextLink(fullTextLink);
		ad.setTitle(title);
		ad.setReference(ref);
		ad.setAuthors(authors);
		ad.setSuppFiles(suppFiles);
		ad.setHasBeenPublished(true);
		LOG.info("Finished finding article details: "+doi);
		return ad;
	}

	/**
	 * <p>
	 * Gets the URI of the article full-text.
	 * </p>
	 * 
	 * @return URI of the article full-text.
	 */
	private URI getFullTextLink() {
		Nodes links = articleAbstractHtml.query(".//x:a[.='HTML article']", X_XHTML);
		if (links.size() != 1) {
			LOG.warn("Problem finding full text HTML link: "+doi);
			return null;
		}
		String urlPostfix = ((Element)links.get(0)).getAttributeValue("href");
		String url = "http://www.rsc.org"+urlPostfix;
		return createURI(url);
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
		Nodes nds = articleAbstractHtml.query(".//x:a[contains(.,'ESI')]", X_XHTML);
		if (nds.size() == 0) {
			return Collections.EMPTY_LIST;
		}
		String suppListUrlPostfix = ((Element)nds.get(0)).getAttributeValue("href");
		String suppListUrl = RSC_HOMEPAGE_URL+suppListUrlPostfix;
		URI suppListUri = createURI(suppListUrl);
		Document suppListDoc = httpClient.getResourceHTML(suppListUri);
		Nodes linkNds = suppListDoc.query(".//x:li/x:a", X_XHTML);
		List<SupplementaryFileDetails> sfdList = new ArrayList<SupplementaryFileDetails>(linkNds.size());
		for (int i = 0; i < linkNds.size(); i++) {
			Element linkNd = (Element)linkNds.get(i);
			String linkText = linkNd.getValue();
			String filename = linkNd.getAttributeValue("href");
			String suppFileUrlPrefix = suppListUrl.substring(0,suppListUrl.lastIndexOf("/")+1);
			String suppFileUrl = suppFileUrlPrefix+filename;
			String suppFilename = getFilenameFromUrl(suppFileUrl);
			URI suppFileUri = createURI(suppFileUrl);
			String contentType = httpClient.getContentType(suppFileUri);
			SupplementaryFileDetails sfd = new SupplementaryFileDetails(suppFileUri, suppFilename, linkText, contentType);
			sfdList.add(sfd);
		}
		return sfdList;
	}

	/**
	 * <p>
	 * Gets the ID of the supplementary file at the publisher's site from
	 * the supplementary file URL.
	 * </p>
	 * 
	 * @param cifUrl - the URL from which to obtain the filename.
	 * 
	 * @return the filename of the supplementary file.
	 */
	private String getFilenameFromUrl(String cifUrl) {
		int idx = cifUrl.lastIndexOf("/");
		String namePlusMime = cifUrl.substring(idx+1);
		int dot = namePlusMime.indexOf(".");
		return namePlusMime.substring(0,dot);
	}

	/**
	 * <p>
	 * Gets a authors of the article from the abstract webpage.
	 * </p>
	 * 
	 * @return String containing the article authors.
	 * 
	 */
	private String getAuthors() {
		Nodes authorNds = articleAbstractHtml.query(".//x:span[@style='font-size:150%;']/following-sibling::x:p[1]/x:strong", X_XHTML);
		if (authorNds.size() != 1) {
			LOG.warn("Problem getting the author string from: "+doi);
			return null;
		}
		String authors = authorNds.get(0).getValue().trim();
		return authors;
	}

	/**
	 * <p>
	 * Creates the article bibliographic reference from information found 
	 * on the abstract webpage.
	 * </p>
	 * 
	 * @return the article bibliographic reference.
	 * 
	 */
	private ArticleReference getReference() {
		Nodes refNds = articleAbstractHtml.query(".//x:p[./x:strong[contains(.,'DOI:')]]", X_XHTML);
		if (refNds.size() != 1) {
			LOG.warn("Problem getting bibliographic data: "+doi);
			return null;
		}
		String ref = refNds.get(0).getValue();
		String journal = null;
		String year = null;
		String volume = null;
		String pages = null;
		Pattern firstPattern = Pattern.compile("\\s*([^,]+),[^\\d]*(\\d+),[^\\d]*(\\d+),[^\\d]*([^,]+),\\s*DOI:.*");
		Matcher firstMatcher = firstPattern.matcher(ref);
		if (firstMatcher.find()) {
			journal = firstMatcher.group(1);
			year = firstMatcher.group(2);
			volume = firstMatcher.group(3);
			pages = firstMatcher.group(4);
		} else {
			Pattern secondPattern = Pattern.compile("\\s*([^,]+),[^\\d]*(\\d+),[^\\d]*([^,]+),\\s*DOI:.*");
			Matcher secondMatcher = secondPattern.matcher(ref);
			if (!secondMatcher.find()) {
				LOG.warn("Problem finding bibliographic text at: "+doi);
				return null;
			}
			journal = secondMatcher.group(1);
			year = secondMatcher.group(2);
			pages = secondMatcher.group(3);
			pages = pages.replaceAll("\\s", "");
		}
		ArticleReference ar = new ArticleReference();
		ar.setJournalTitle(journal);
		ar.setYear(year);
		ar.setVolume(volume);
		ar.setPages(pages);
		return ar;
	}

	/**
	 * <p>
	 * Gets the article title from the abstract webpage.
	 * </p>
	 * 
	 * @return the article title.
	 * 
	 */
	private String getTitle() {
		Nodes titleNds = articleAbstractHtml.query(".//x:span[@style='font-size:150%;']//x:font", X_XHTML);
		if (titleNds.size() != 1) {
			LOG.warn("Problem getting title: "+doi);
			return null;
		}
		String title = titleNds.get(0).toXML();
		title = title.replaceAll("<font color=\"#9C0000\">", "");
		title = title.replaceAll("</font>", "");
		title = title.trim();
		return title;
	}

	/**
	 * <p>
	 * Main method only for demonstration of class use. Does not require
	 * any arguments.
	 * </p>
	 * 
	 * @param args
	 * @throws NullPointerException 
	 * @throws URIException 
	 */
	public static void main(String[] args) throws URIException, NullPointerException {
		String ref = "Org. Biomol. Chem., 2009, 7, 1355 - 1360, DOI: 10.1039/b821431j";
		//String p = "\\s*([^,]+),\\s*(\\d+),\\s*(\\d+),\\s*([^,]+),\\s*DOI:.*";
		String p = "\\s*([^,]+),\\s*(\\d+),[^\\d]*(\\d+),\\s*([^,]+),\\s*DOI:.*";
		Pattern firstPattern = Pattern.compile(p);
		Matcher firstMatcher = firstPattern.matcher(ref);
		if (firstMatcher.find()) {
			for (int i = 0; i <= firstMatcher.groupCount(); i++) {
				System.out.println(firstMatcher.group(i));
			}
		} else {
			System.out.println("no match");
		}
	}

}
