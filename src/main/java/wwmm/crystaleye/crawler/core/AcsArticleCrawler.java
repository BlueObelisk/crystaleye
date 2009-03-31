package wwmm.crystaleye.crawler.core;

import static wwmm.crystaleye.CrystalEyeConstants.X_XHTML;
import static wwmm.crystaleye.crawler.core.CrawlerConstants.ACS_HOMEPAGE_URL;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Node;
import nu.xom.Nodes;

import org.apache.commons.httpclient.URI;
import org.apache.log4j.Logger;

import wwmm.crystaleye.Utils;

/**
 * <p>
 * The <code>AcsArticleCrawler</code> class uses a provided DOI to get
 * information about an article that is published in a journal of the 
 * American Chemical Society.
 * </p>
 * 
 * @author Nick Day
 * @version 1.1
 * 
 */
public class AcsArticleCrawler extends ArticleCrawler {

	private static final Logger LOG = Logger.getLogger(AcsArticleCrawler.class);

	/**
	 * <p>
	 * Creates an instance of the AcsArticleCrawler class and
	 * specifies the DOI of the article to be crawled.
	 * </p>
	 * 
	 * @param doi of the article to be crawled.
	 */
	public AcsArticleCrawler(DOI doi) {
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
		String authors = getAuthors();
		ArticleReference ref = getReference();
		List<SupplementaryFileDetails> suppFiles = getSupplementaryFilesDetails();
		ad.setFullTextLink(fullTextLink);
		ad.setTitle(title);
		ad.setReference(ref);
		ad.setAuthors(authors);
		ad.setSuppFiles(suppFiles);
		LOG.debug("Finished finding article details: "+doi.toString());
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
		Nodes fullTextLinks = articleAbstractHtml.query(".//x:a[contains(@href,'/full/')]", X_XHTML);
		if (fullTextLinks.size() == 0) {
			LOG.warn("Problem getting full text HTML link: "+doi);
			return null;
		}
		String urlPostfix = ((Element)fullTextLinks.get(0)).getAttributeValue("href");
		String fullTextUrl = ACS_HOMEPAGE_URL+urlPostfix;
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
		Document suppPageDoc = getSupplementaryDataWebpage();
		if (suppPageDoc == null) {
			return Collections.EMPTY_LIST;
		}
		List<Node> suppLinks = Utils.queryHTML(suppPageDoc, ".//x:div[@id='supInfoBox']//x:a[contains(@href,'/suppl/')]");
		List<SupplementaryFileDetails> sfList = new ArrayList<SupplementaryFileDetails>(suppLinks.size());
		for (Node suppLink : suppLinks) {
			Element link = (Element)suppLink;
			String urlPostfix = link.getAttributeValue("href");
			String url = ACS_HOMEPAGE_URL+urlPostfix;
			String filename = getFilenameFromUrl(url);
			URI uri = createURI(url);
			String linkText = link.getValue();
			String contentType = httpClient.getContentType(uri);
			SupplementaryFileDetails sf = new SupplementaryFileDetails(uri, filename, linkText, contentType);
			sfList.add(sf);
		}
		return sfList;
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
	 * Crawls the abstract webpage to find a link to a webpage listing the
	 * article supplementary files.  This is then retrieved and returned.
	 * </p>
	 * 
	 * @return HTML of the webpage listing the article supplementary files.
	 * 
	 */
	private Document getSupplementaryDataWebpage() {
		Nodes suppPageLinks = articleAbstractHtml.query(".//x:a[contains(@title,'Supporting Information')]", X_XHTML);
		if (suppPageLinks.size() == 0) {
			return null;
		} else if (suppPageLinks.size() > 1) {
			LOG.warn("Expected either 0 or 1 links to supporting info page, found "+suppPageLinks.size());
		}
		String urlPostfix = ((Element)suppPageLinks.get(0)).getAttributeValue("href");
		String url = ACS_HOMEPAGE_URL+urlPostfix;
		URI suppPageUri = createURI(url);
		return httpClient.getResourceHTML(suppPageUri);
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
		Nodes authorNds = articleAbstractHtml.query(".//x:meta[@name='dc.Creator']", X_XHTML);
		if (authorNds.size() == 0) {
			LOG.warn("Problem finding authors at: "+doi);
			return null;
		}
		StringBuilder authors = new StringBuilder();
		for (int i = 0; i < authorNds.size(); i++) {
			String author = ((Element)authorNds.get(i)).getAttributeValue("content");
			authors.append(author);
			if (i < authorNds.size() - 1) {
				authors.append(", ");
			}
		}
		return authors.toString();
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
		Nodes citationNds = articleAbstractHtml.query(".//x:div[@id='citation']", X_XHTML);
		if (citationNds.size() != 1) {
			LOG.warn("Problem finding bibliographic text at: "+doi);
			return null;
		}

		Element citationNd = (Element)citationNds.get(0);
		Nodes journalNds = citationNd.query("./x:cite", X_XHTML);
		String journal = null;
		if (journalNds.size() != 1) {
			LOG.warn("Problem finding journal text at: "+doi);
		} else {
			journal = ((Element)journalNds.get(0)).getValue().trim();
		}

		String citationContent = citationNd.getValue();
		String year = null;
		String volume = null;
		String number = null;
		String pages = null;
		if (!citationContent.contains("Article ASAP")) {
			ad.setHasBeenPublished(true);
			Nodes yearNds = citationNd.query("./x:span[@class='citation_year']", X_XHTML);
			if (yearNds.size() != 1) {
				LOG.warn("Problem finding year text at: "+doi);
			}
			year = ((Element)yearNds.get(0)).getValue().trim();		
			Nodes volumeNds = citationNd.query("./x:span[@class='citation_volume']", X_XHTML);
			if (volumeNds.size() != 1) {
				LOG.warn("Problem finding volume text at: "+doi);
			}
			volume = ((Element)volumeNds.get(0)).getValue().trim();
			String refContent = citationNd.getValue();
			Pattern p = Pattern.compile("[^\\(]*\\((\\d+)\\),\\s+pp\\s+(\\d+).(\\d+).*");
			Matcher matcher = p.matcher(refContent);
			if (!matcher.find() || matcher.groupCount() != 3) {
				LOG.warn("Problem finding issue and pages info at: "+doi);
			} else {
				number = matcher.group(1);
				pages = matcher.group(2)+"-"+matcher.group(3);
			}
		} else {
			ad.setHasBeenPublished(false);
		}

		ArticleReference ar = new ArticleReference();
		ar.setJournalTitle(journal);
		ar.setVolume(volume);
		ar.setYear(year);
		ar.setNumber(number);
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
		Nodes titleNds = articleAbstractHtml.query(".//x:h1[@class='articleTitle']", X_XHTML);
		if (titleNds.size() != 1) {
			LOG.warn("Problem finding title at: "+doi);
			return null;
		}
		String title = titleNds.get(0).getValue();
		return title;
	}

}
