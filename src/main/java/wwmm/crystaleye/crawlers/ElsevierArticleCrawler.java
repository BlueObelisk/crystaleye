package wwmm.crystaleye.crawlers;

import static wwmm.crystaleye.CrystalEyeConstants.X_XHTML;
import static wwmm.crystaleye.crawlers.CrawlerConstants.ACS_HOMEPAGE_URL;

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
 * The <code>ElsevierArticleCrawler</code> class uses a provided DOI to get
 * information about an article that is published in a journal of the 
 * American Chemical Society.
 * </p>
 * 
 * @author Nick Day
 * @version 1.1
 * 
 */
public class ElsevierArticleCrawler extends ArticleCrawler {

	private static final Logger LOG = Logger.getLogger(ElsevierArticleCrawler.class);

	/**
	 * <p>
	 * Creates an instance of the AcsArticleCrawler class and
	 * specifies the DOI of the article to be crawled.
	 * </p>
	 * 
	 * @param doi of the article to be crawled.
	 */
	public ElsevierArticleCrawler(DOI doi) {
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
		String title = getTitle();
		ArticleReference ref = getReference();
		String authors = getAuthors();
		List<SupplementaryFileDetails> suppFiles = getSupplementaryFilesDetails();
		ad.setFullTextLink(fullTextLink);
		ad.setTitle(title);
		ad.setReference(ref);
		ad.setAuthors(authors);
		ad.setSuppFiles(suppFiles);
		LOG.debug("Finished finding article details.");
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
		return doi.getUri();
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
		/*
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
			URI uri = createURI(url);
			String linkText = link.getValue();
			String contentType = httpClient.getContentType(uri);
			SupplementaryFileDetails sf = new SupplementaryFileDetails(uri, linkText, contentType);
			sfList.add(sf);
		}
		return sfList;
		*/
		return Collections.EMPTY_LIST;
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
			throw new CrawlerRuntimeException("Problem finding authors at: "+doi);
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
		Nodes nds = articleAbstractHtml.query(".//x:div[@id='artihead']", X_XHTML);
		if (nds.size() != 1) {
			throw new IllegalStateException("Expected 1 node, found: "+nds.size());
		}
		String contents = nds.get(0).getValue();
		contents = contents.replaceAll("\\s+", " ");
		Pattern p = Pattern.compile("\\s+(\\w+)\\s+Volume\\s+(\\d+),\\s+Issue\\s+(\\d+)" +
				",\\s+\\d+\\s+\\w+\\s+(\\d{4}),\\s+Pages\\s+(\\d+).(\\d+).*");
		Matcher matcher = p.matcher(contents);
		if (!matcher.find() || matcher.groupCount() != 6) {
			throw new CrawlerRuntimeException("Problem finding issue and pages info at: "+doi);
		}
		String journal = matcher.group(1);
		String volume = matcher.group(2);
		String issue = matcher.group(3);
		String year = matcher.group(4);
		String pages = matcher.group(5)+"-"+matcher.group(6);
		ArticleReference ar = new ArticleReference();
		ar.setJournal(journal);
		ar.setVolume(volume);
		ar.setYear(year);
		ar.setNumber(issue);
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
		Nodes titleNds = articleAbstractHtml.query(".//x:div[@class='articleTitle']/x:p", X_XHTML);
		if (titleNds.size() != 1) {
			throw new CrawlerRuntimeException("Problem finding title at: "+doi);
		}
		String title = titleNds.get(0).getValue();
		return title;
	}

}
