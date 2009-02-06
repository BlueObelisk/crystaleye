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

public class AcsArticleCrawler extends Crawler {

	private URI doi;
	private Document abstractPageDoc;
	
	private static final Logger LOG = Logger.getLogger(AcsArticleCrawler.class);
	
	public AcsArticleCrawler(URI doi) {
		this.doi = doi;
	}
	
	public ArticleDetails getDetails() {
		LOG.debug("Finding article details: "+doi);
		abstractPageDoc = httpClient.getWebpageDocument(doi);
		URI fullTextLink = getFullTextHtmlLink();
		String title = getTitle();
		ArticleReference ref = getReference();
		String authors = getAuthors();
		List<SupplementaryFile> suppFiles = getSupplementaryFiles();

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
	
	private URI getFullTextHtmlLink() {
		Nodes fullTextLinks = abstractPageDoc.query(".//x:a[@title='Full Text HTML']", X_XHTML);
		if (fullTextLinks.size() == 0) {
			throw new RuntimeException("Problem getting full text HTML link: "+doi);
		}
		String urlPostfix = ((Element)fullTextLinks.get(0)).getAttributeValue("href");
		String fullTextUrl = "http://pubs.acs.org/"+urlPostfix;
		return createURI(fullTextUrl);
	}
	
	private List<SupplementaryFile> getSupplementaryFiles() {
		Document suppPageDoc = getSupplementaryDataDocument();
		if (suppPageDoc == null) {
			return new ArrayList<SupplementaryFile>(0);
		}
		List<Node> suppLinks = Utils.queryHTML(suppPageDoc, ".//x:a[contains(@href,'/suppinfo/')]");
		List<SupplementaryFile> sfList = new ArrayList<SupplementaryFile>(suppLinks.size());
		for (Node suppLink : suppLinks) {
			Element link = (Element)suppLink;
			String text = link.getValue();
			URI uri = createURI(link.getAttributeValue("href"));
			String contentType = getContentType(uri);
			SupplementaryFile sf = new SupplementaryFile(uri, text, contentType);
			sfList.add(sf);
		}
		return sfList;
	}

	private Document getSupplementaryDataDocument() {
		Nodes suppPageLinks = abstractPageDoc.query(".//x:a[contains(@href,'supporting_information')]", X_XHTML);
		if (suppPageLinks.size() > 1) {
			throw new RuntimeException("Problem finding supplementary page link for: "+doi);
		} else if (suppPageLinks.size() == 0) {
			return null;
		}
		String suppPageUrl = ((Element)suppPageLinks.get(0)).getAttributeValue("href");
		URI suppPageUri = createURI(suppPageUrl);
		return httpClient.getWebpageDocument(suppPageUri);
	}

	private String getAuthors() {
		Nodes authorNds = abstractPageDoc.query(".//x:p[./x:font[@size='+2']]/following-sibling::x:p[1]", X_XHTML);
		if (authorNds.size() != 1) {
			throw new RuntimeException("Problem finding authors at: "+doi);
		}
		Element authorElement = (Element)authorNds.get(0);
		List<Node> supNds = Utils.queryHTML(authorElement, ".//x:sup");
		for (Node sup : supNds) {
			sup.detach();
		}
		String authors = authorElement.getValue();
		authors = authors.replaceAll("\\s+", " ");
		authors = authors.trim();
		return authors;
	}

	private ArticleReference getReference() {
		Nodes refNds = abstractPageDoc.query(".//x:div[@id='articleNav']/following-sibling::x:p[1]", X_XHTML);
		if (refNds.size() != 1) {
			throw new RuntimeException("Problem finding bibliographic text at: "+doi);
		}
		String bibline = refNds.get(0).getValue().trim();
		Pattern pattern = Pattern.compile("\\s*([^\\,]+),\\s+(\\d+)\\s+\\((\\d+)\\),\\s+(\\d+).(\\d+),\\s+(\\d+).*");
		Matcher matcher = pattern.matcher(bibline);
		if (!matcher.find() || matcher.groupCount() != 6) {
			
			throw new RuntimeException("Problem finding bibliographic text at: "+doi);
		}
		String journalAbbreviation = matcher.group(1);
		String number = matcher.group(2);
		String volume = matcher.group(3);
		volume = volume.replaceAll("\\s*", "");
		String pageStart = matcher.group(4);
		String pageEnd = matcher.group(5);
		String pages = pageStart+"-"+pageEnd;
		String year = matcher.group(6);
		
		ArticleReference ar = new ArticleReference(journalAbbreviation,
				year, volume, number, pages);	
		return ar;
	}

	private String getTitle() {
		Nodes titleNds = abstractPageDoc.query(".//x:p/x:font[@size='+2']/x:b", X_XHTML);
		if (titleNds.size() != 1) {
			throw new RuntimeException("Problem finding title at: "+doi);
		}
		String title = titleNds.get(0).toXML();
		title = title.replaceAll("<b>", "");
		title = title.replaceAll("</b>", "");
		title = title.trim();
		return title;
	}
	
}
