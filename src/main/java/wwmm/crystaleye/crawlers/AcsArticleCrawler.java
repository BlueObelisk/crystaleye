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

import wwmm.crystaleye.util.Utils;

public class AcsArticleCrawler extends ArticleCrawler {
	
	private static final Logger LOG = Logger.getLogger(AcsArticleCrawler.class);
	
	public AcsArticleCrawler(URI abstractPageUri) {
		super(abstractPageUri);
	}
	
	public ArticleDetails getDetails() {
		List<SupplementaryFileDetails> suppFiles = getSupplementaryFilesDetails();
		if (articleAbstractUriIsADoi && !doiResolved) {
			LOG.warn("The DOI provided for the article abstract ("+articleAbstractUri.toString()+") has not resolved so we cannot get article details.");
			return ad;
		}
		URI fullTextLink = getFullTextLink();
		if (fullTextLink != null) {
			ad.setFullTextLink(fullTextLink);
		}	
		URI doi = getDOI();
		ad.setDoi(doi);
		String title = getTitle();
		String authors = getAuthors();
		ArticleReference ref = getReference();
		ad.setFullTextLink(fullTextLink);
		ad.setTitle(title);
		ad.setReference(ref);
		ad.setAuthors(authors);
		ad.setSuppFiles(suppFiles);
		LOG.debug("Finished finding article details.");
		return ad;
	}
	
	private URI getDOI() {
		
	}
	
	private URI getFullTextLink() {
		Nodes fullTextLinks = articleAbstractDoc.query(".//x:a[contains(@href,'/full/')]", X_XHTML);
		if (fullTextLinks.size() == 0) {
			throw new RuntimeException("Problem getting full text HTML link: "+articleAbstractUri);
		}
		String urlPostfix = ((Element)fullTextLinks.get(0)).getAttributeValue("href");
		String fullTextUrl = ACS_HOMEPAGE_URL+urlPostfix;
		return createURI(fullTextUrl);
	}
	
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
			URI uri = createURI(url);
			String linkText = link.getValue();
			String contentType = httpClient.getContentType(uri);
			SupplementaryFileDetails sf = new SupplementaryFileDetails(uri, linkText, contentType);
			sfList.add(sf);
		}
		return sfList;
	}

	private Document getSupplementaryDataWebpage() {
		Nodes suppPageLinks = articleAbstractDoc.query(".//x:a[contains(@title,'Supporting Information')]", X_XHTML);
		if (suppPageLinks.size() == 0) {
			return null;
		} else if (suppPageLinks.size() > 1) {
			System.out.println("Expected either 0 or 1 links to supporting info page, found "+suppPageLinks.size());
		}
		String urlPostfix = ((Element)suppPageLinks.get(0)).getAttributeValue("href");
		String url = ACS_HOMEPAGE_URL+urlPostfix;
		URI suppPageUri = createURI(url);
		return httpClient.getWebpageHTML(suppPageUri);
	}

	private String getAuthors() {
		Nodes authorNds = articleAbstractDoc.query(".//x:meta[@name='dc.Creator']", X_XHTML);
		if (authorNds.size() == 0) {
			throw new RuntimeException("Problem finding authors at: "+articleAbstractUri);
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

	private ArticleReference getReference() {
		Nodes refNds = articleAbstractDoc.query(".//x:div[@id='citation']", X_XHTML);
		if (refNds.size() != 1) {
			throw new RuntimeException("Problem finding bibliographic text at: "+articleAbstractUri);
		}
		Element refNd = (Element)refNds.get(0);
		Nodes journalNds = refNd.query("./x:cite", X_XHTML);
		if (journalNds.size() != 1) {
			throw new RuntimeException("Problem finding journal text at: "+articleAbstractUri);
		}
		String journal = ((Element)journalNds.get(0)).getValue().trim();
		Nodes yearNds = refNd.query("./x:span[@class='citation_year']", X_XHTML);
		if (yearNds.size() != 1) {
			throw new RuntimeException("Problem finding year text at: "+articleAbstractUri);
		}
		String year = ((Element)yearNds.get(0)).getValue().trim();		
		Nodes volumeNds = refNd.query("./x:span[@class='citation_volume']", X_XHTML);
		if (volumeNds.size() != 1) {
			throw new RuntimeException("Problem finding volume text at: "+articleAbstractUri);
		}
		String volume = ((Element)volumeNds.get(0)).getValue().trim();
		String refContent = refNd.getValue();
		Pattern p = Pattern.compile("[^\\(]*\\((\\d+)\\),\\s+pp\\s+(\\d+).(\\d+).*");
		Matcher matcher = p.matcher(refContent);
		if (!matcher.find() || matcher.groupCount() != 3) {
			throw new RuntimeException("Problem finding issue and pages info at: "+articleAbstractUri);
		}
		String number = matcher.group(1);
		String pages = matcher.group(2)+"-"+matcher.group(3);
		
		ArticleReference ar = new ArticleReference(journal,
				year, volume, number, pages);	
		return ar;
	}

	private String getTitle() {
		Nodes titleNds = articleAbstractDoc.query(".//x:h1[@class='articleTitle']", X_XHTML);
		if (titleNds.size() != 1) {
			throw new RuntimeException("Problem finding title at: "+articleAbstractUri);
		}
		String title = titleNds.get(0).getValue();
		return title;
	}
	
}
