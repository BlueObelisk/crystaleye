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
import nu.xom.Text;

import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;

import wwmm.crystaleye.util.Utils;

public class ActaArticleCrawler extends Crawler {

	private URI doi;

	public ActaArticleCrawler(URI doi) {
		this.doi = doi;
	}

	public ArticleDetails getDetails() {
		Document abstractPageDoc = httpClient.getWebpageDocument(doi);

		String title = getTitle(abstractPageDoc);
		ArticleReference ref = getReference(abstractPageDoc);
		String authors = getAuthors(abstractPageDoc);
		List<SupplementaryFile> suppFiles = getSupplementaryFiles(abstractPageDoc);

		ArticleDetails ad = new ArticleDetails();
		ad.setDoi(doi);
		ad.setTitle(title);
		ad.setReference(ref);
		ad.setAuthors(authors);
		ad.setSuppFiles(suppFiles);
		return ad;
	}

	private List<SupplementaryFile> getSupplementaryFiles(
			Document abstractPageDoc) {
		Nodes cifNds = abstractPageDoc.query(".//x:a[contains(@href,'http://scripts.iucr.org/cgi-bin/sendcif') and not(contains(@href,'mime'))]", X_XHTML);
		if (cifNds.size() != 1) {
			throw new RuntimeException("Problem finding CIF link at: "+doi+" - expected 1 link, found "+cifNds.size());
		}
		String cifUrl = ((Element)cifNds.get(0)).getAttributeValue("href");
		URI cifUri = null;;
		try {
			cifUri = new URI(cifUrl, false);
		} catch (URIException e) {
			throw new RuntimeException("Problem creating URI from: "+cifUrl);
		}
		String contentType = getContentType(cifUri);
		SupplementaryFile suppFile = new SupplementaryFile(cifUri, "CIF", contentType);
		List<SupplementaryFile> suppFiles = new ArrayList<SupplementaryFile>(1);
		suppFiles.add(suppFile);
		return suppFiles;
	}

	private String getAuthors(Document abstractPageDoc) {
		Nodes authorNds = abstractPageDoc.query(".//x:div[@class='bibline']/following-sibling::x:h3[2]", X_XHTML);
		if (authorNds.size() != 1) {
			throw new RuntimeException("Problem finding author name text at: "+doi);
		}
		String authors = authorNds.get(0).getValue();
		return authors;
	}

	private ArticleReference getReference(Document abstractPageDoc) {
		Nodes bibNds = abstractPageDoc.query(".//x:div[@class='bibline']", X_XHTML);
		if (bibNds.size() != 1) {
			throw new RuntimeException("Could not find bibdata at: "+doi);
		}
		String bibline = bibNds.get(0).getValue();
		Pattern pattern = Pattern.compile("([^\\.]+\\.)\\s+\\((\\d+)\\)\\.\\s*(\\w+),\\s*(\\w+\\-\\w+).*");
		Matcher matcher = pattern.matcher(bibline);
		if (!matcher.find() || matcher.groupCount() != 4) {
			throw new RuntimeException("Problem finding bibdata at: "+doi);
		}
		String journalAbbreviation = matcher.group(1);
		String year = matcher.group(2);
		String volume = matcher.group(3);
		String pages = matcher.group(4);
		ArticleReference ref = new ArticleReference(journalAbbreviation, year, volume, pages);
		return ref;
	}

	private String getTitle(Document abstractPageDoc) {
		Nodes titleNds = abstractPageDoc.query(".//x:div[@class='bibline']/following-sibling::x:h3[1]", X_XHTML);
		if (titleNds.size() != 1) {
			throw new RuntimeException("Could not find article title at: "+doi);
		} 
		Element titleElement = (Element)titleNds.get(0).copy();
		String title = sanitizeTitle(titleElement);
		return title;
	}
	
	private String sanitizeTitle(Element title) {
		List<Node> iSpans = Utils.queryHTML(title, ".//x:span[@class='it']");
		for (Node span : iSpans) {
			Element parent = (Element)span.getParent();
			int idx = parent.indexOf(span);
			for (int i = 0; i < span.getChildCount(); i++) {
				Element child = (Element)span.getChild(i);
				child.detach();
				parent.insertChild(child, idx+i);
			}
			span.detach();
		}
		
		List<Node> imgNds = Utils.queryHTML(title, ".//x:img[contains(@src,'/logos/entities')]");
		for (Node imgNd : imgNds) {
			Element img = (Element)imgNd;
			Element parent = (Element)img.getParent();
			int idx = parent.indexOf(img);
			String alt = img.getAttributeValue("alt");
			alt = "__/"+alt.substring(1,alt.length()-1)+"/__";
			parent.insertChild(new Text(alt), idx);
			imgNd.detach();
		}
		
		String titleStr = title.toXML();
		titleStr = titleStr.replaceAll("<h3 xmlns=\"http://www.w3.org/1999/xhtml\">", "");
		titleStr = titleStr.replaceAll("</h3>", "");
		titleStr = titleStr.replaceAll("__/./__", "...");
		titleStr = titleStr.replaceAll("__/", "&");
		titleStr = titleStr.replaceAll("/__", ";");
		return titleStr;
	}
}
