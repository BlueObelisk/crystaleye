package wwmm.crystaleye.crawlers;

import static wwmm.crystaleye.CrystalEyeConstants.X_XHTML;

import java.util.ArrayList;
import java.util.List;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Node;
import nu.xom.Nodes;

import org.apache.commons.httpclient.URI;

import wwmm.crystaleye.util.Utils;

public class ChemSocJapanArticle extends Crawler {

	private URI doi;

	public ChemSocJapanArticle(URI doi) {
		this.doi = doi;
	}

	public ArticleDetails getDetails() {
		Document abstractPageDoc = httpClient.getWebpageDocument(doi);
		
		URI fullTextHtmlLink = getFullTextHtmlLink(abstractPageDoc);
		String title = getTitle(abstractPageDoc);
		ArticleReference ref = getReference(abstractPageDoc);
		String authors = getAuthors(abstractPageDoc);
		List<SupplementaryFile> suppFiles = getSupplementaryFiles(abstractPageDoc);

		ArticleDetails ad = new ArticleDetails();
		ad.setDoi(doi);
		ad.setFullTextHtmlLink(fullTextHtmlLink);
		ad.setTitle(title);
		ad.setReference(ref);
		ad.setAuthors(authors);
		ad.setSuppFiles(suppFiles);
		return ad;
	}

	private URI getFullTextHtmlLink(Document abstractPageDoc) {
		return null;
	}

	private List<SupplementaryFile> getSupplementaryFiles(
			Document abstractPageDoc) {

		return new ArrayList();
	}

	private String getAuthors(Document abstractPageDoc) {

		return null;
	}

	private ArticleReference getReference(Document abstractPageDoc) {

		return new ArticleReference(null, null, null, null, null);
	}

	private String getTitle(Document abstractPageDoc) {
		Nodes titleNds = abstractPageDoc.query(".//x:div[@align='center']/x:table/x:tr/x:td" +
				"/x:table/x:tr/x:td/x:table/x:tr/x:td/x:font[@size='+1']/x:b", X_XHTML);
		if (titleNds.size() != 1) {
			throw new RuntimeException("Problem finding title: "+doi);
		}
		String title = titleNds.get(0).toXML();
		title = title.replaceAll("<b>", "");
		title = title.replaceAll("</b>", "");
		title = title.trim();
		return title;
	}

}
