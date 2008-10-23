package wwmm.crystaleye.crawlers;

import static wwmm.crystaleye.CrystalEyeConstants.X_XHTML;

import java.util.ArrayList;
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

public class RscArticle extends Crawler {

	private URI doi;
	private static final Logger LOG = Logger.getLogger(RscArticle.class);

	public RscArticle(URI doi) {
		this.doi = doi;
	}

	public ArticleDetails getDetails() {
		LOG.debug("Finding article details: "+doi);
		Document abstractPageDoc = httpClient.getWebpageDocument(doi);
		
		URI fullTextLink = getFullTextHtmlLink(abstractPageDoc);
		String title = getTitle(abstractPageDoc);
		ArticleReference ref = getReference(abstractPageDoc);
		String authors = getAuthors(abstractPageDoc);
		List<SupplementaryFile> suppFiles = getSupplementaryFiles(abstractPageDoc);

		ArticleDetails ad = new ArticleDetails();
		ad.setDoi(doi);
		ad.setFullTextHtmlLink(fullTextLink);
		ad.setTitle(title);
		ad.setReference(ref);
		ad.setAuthors(authors);
		ad.setSuppFiles(suppFiles);
		LOG.debug("Finished finding article details.");
		return ad;
	}

	private URI getFullTextHtmlLink(Document abstractPageDoc) {
		Nodes links = abstractPageDoc.query(".//x:a[.='HTML article']", X_XHTML);
		if (links.size() != 1) {
			throw new RuntimeException("Problem finding full text HTML link: "+doi);
		}
		String urlPostfix = ((Element)links.get(0)).getAttributeValue("href");
		String url = "http://www.rsc.org"+urlPostfix;
		return createURI(url);
	}

	private List<SupplementaryFile> getSupplementaryFiles(Document abstractPageDoc) {

		return new ArrayList<SupplementaryFile>();
	}

	private Document getSupplementaryDataDocument(Document abstractPageDoc) {
		
		return null;
	}

	private String getAuthors(Document abstractPageDoc) {
		Nodes authorNds = abstractPageDoc.query(".//x:span[@style='font-size:150%;']/following-sibling::x:p[1]/x:strong", X_XHTML);
		if (authorNds.size() != 1) {
			throw new RuntimeException("Problem getting the author string from: "+doi);
		}
		String authors = authorNds.get(0).getValue().trim();
		return authors;
	}

	private ArticleReference getReference(Document abstractPageDoc) {
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
		String journalAbbreviation = matcher.group(1);
		String year = matcher.group(2);
		String pages = matcher.group(3);
		pages = pages.replaceAll("\\s", "");
		return new ArticleReference(journalAbbreviation, year, null, null, pages);
	}

	private String getTitle(Document abstractPageDoc) {
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
