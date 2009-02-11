package wwmm.crystaleye.crawlers;

import static wwmm.crystaleye.CrystalEyeConstants.X_XHTML;
import static wwmm.crystaleye.crawlers.CrawlerConstants.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;

import org.apache.commons.httpclient.URI;
import org.apache.log4j.Logger;

public class RscArticleCrawler extends ArticleCrawler {
	
	private static final Logger LOG = Logger.getLogger(RscArticleCrawler.class);

	public RscArticleCrawler(URI doi) {
		super(doi);
	}

	public ArticleDetails getDetails() {
		List<SupplementaryFileDetails> suppFiles = getSupplementaryFilesDetails();
		if (!doiResolved) {
			LOG.warn("This DOI has not resolved so cannot get article details: "+doi.toString());
			return ad;
		}
		
		URI fullTextLink = getFullTextLink();
		if (fullTextLink != null) {
			ad.setFullTextHtmlLink(fullTextLink);
		}		
		String title = getTitle();
		ArticleReference ref = getReference();
		String authors = getAuthors();

		ad.setDoi(doi);
		ad.setFullTextHtmlLink(fullTextLink);
		ad.setTitle(title);
		ad.setReference(ref);
		ad.setAuthors(authors);
		ad.setSuppFiles(suppFiles);
		LOG.debug("Finished finding article details.");
		return ad;
	}

	private URI getFullTextLink() {
		Nodes links = abstractPageDoc.query(".//x:a[.='HTML article']", X_XHTML);
		if (links.size() != 1) {
			throw new RuntimeException("Problem finding full text HTML link: "+doi);
		}
		String urlPostfix = ((Element)links.get(0)).getAttributeValue("href");
		String url = "http://www.rsc.org"+urlPostfix;
		return createURI(url);
	}

	private List<SupplementaryFileDetails> getSupplementaryFilesDetails() {
		Nodes nds = abstractPageDoc.query(".//x:a[contains(.,'ESI')]", X_XHTML);
		if (nds.size() == 0) {
			return Collections.EMPTY_LIST;
		}
		String suppListUrlPostfix = ((Element)nds.get(0)).getAttributeValue("href");
		String suppListUrl = RSC_HOMEPAGE_URL+suppListUrlPostfix;
		URI suppListUri = createURI(suppListUrl);
		Document suppListDoc = httpClient.getWebpageDocument(suppListUri);
		Nodes linkNds = suppListDoc.query(".//x:li/x:a", X_XHTML);
		
		List<SupplementaryFileDetails> sfdList = new ArrayList<SupplementaryFileDetails>(linkNds.size());
		for (int i = 0; i < linkNds.size(); i++) {
			Element linkNd = (Element)linkNds.get(i);
			String linkText = linkNd.getValue();
			String filename = linkNd.getAttributeValue("href");
			String suppFileUrlPrefix = suppListUrl.substring(0,suppListUrl.lastIndexOf("/")+1);
			String suppFileUrl = suppFileUrlPrefix+filename;
			URI suppFileUri = createURI(suppFileUrl);
			String contentType = httpClient.getContentType(suppFileUri);
			SupplementaryFileDetails sfd = new SupplementaryFileDetails(suppFileUri, linkText, contentType);
			sfdList.add(sfd);
		}
		return new ArrayList<SupplementaryFileDetails>();
	}

	private String getAuthors() {
		Nodes authorNds = abstractPageDoc.query(".//x:span[@style='font-size:150%;']/following-sibling::x:p[1]/x:strong", X_XHTML);
		if (authorNds.size() != 1) {
			throw new RuntimeException("Problem getting the author string from: "+doi);
		}
		String authors = authorNds.get(0).getValue().trim();
		return authors;
	}

	private ArticleReference getReference() {
		Nodes refNds = abstractPageDoc.query(".//x:p[./x:strong[contains(.,'DOI:')]]", X_XHTML);
		if (refNds.size() != 1) {
			throw new RuntimeException("Problem getting bibliographic data: "+doi);
		}
		String ref = refNds.get(0).getValue();
		Pattern pattern = Pattern.compile("\\s*([^,]+),\\s*(\\d+),\\s*([^,]+),.*");
		Matcher matcher = pattern.matcher(ref);
		System.out.println(ref);
		if (!matcher.find()) {
			throw new RuntimeException("Problem finding bibliographic text at: "+doi);
		}
		String journal = matcher.group(1);
		String year = matcher.group(2);
		String pages = matcher.group(3);
		pages = pages.replaceAll("\\s", "");
		return new ArticleReference(journal, year, null, null, pages);
	}

	private String getTitle() {
		Nodes titleNds = abstractPageDoc.query(".//x:span[@style='font-size:150%;']//x:font", X_XHTML);
		if (titleNds.size() != 1) {
			throw new RuntimeException("Problem getting title: "+doi);
		}
		String title = titleNds.get(0).toXML();
		title = title.replaceAll("<font color=\"#9C0000\">", "");
		title = title.replaceAll("</font>", "");
		title = title.trim();
		return title;
	}

}
