package wwmm.crystaleye.crawlers;

import static wwmm.crystaleye.CrystalEyeConstants.X_XHTML;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;

import org.apache.commons.httpclient.URI;
import org.apache.log4j.Logger;

public class ChemSocJapanArticleCrawler extends Crawler {

	private URI doi;
	private Document abstractPageDoc;
	private BibtexTool bibtexTool;
	
	private static final Logger LOG = Logger.getLogger(ChemSocJapanArticleCrawler.class);

	public ChemSocJapanArticleCrawler(URI doi) {
		this.doi = doi;
	}

	public ArticleDetails getDetails() {
		abstractPageDoc = httpClient.getWebpageDocument(doi);
		setBibtexTool();
		
		URI fullTextHtmlLink = getFullTextHtmlLink();
		String title = getTitle();
		ArticleReference ref = getReference();
		String authors = getAuthors();
		List<SupplementaryFile> suppFiles = getSupplementaryFiles();

		ArticleDetails ad = new ArticleDetails();
		ad.setDoi(doi);
		ad.setFullTextHtmlLink(fullTextHtmlLink);
		ad.setTitle(title);
		ad.setReference(ref);
		ad.setAuthors(authors);
		ad.setSuppFiles(suppFiles);
		return ad;
	}
	
	private void setBibtexTool() {
		Nodes bibtexLinks = abstractPageDoc.query(".//x:a[contains(@href,'/_bib/')]", X_XHTML);
		if (bibtexLinks.size() != 1) {
			throw new RuntimeException("Expected to find 1 link to the article BibTex document, found "+bibtexLinks.size());
		}
		String urlPostfix = ((Element)bibtexLinks.get(0)).getAttributeValue("href");
		String bibUrl = "http://www.jstage.jst.go.jp"+urlPostfix;
		URI bibtexUri = createURI(bibUrl);
		String bibStr = httpClient.getWebpageString(bibtexUri);
		bibtexTool = new BibtexTool(bibStr);
	}

	private URI getFullTextHtmlLink() {
		
		return null;
	}

	private List<SupplementaryFile> getSupplementaryFiles() {

		return new ArrayList();
	}

	private String getAuthors() {
		return bibtexTool.getValue("author");
	}

	private ArticleReference getReference() {
		String journalAbbreviation = bibtexTool.getValue("journal");
		String year = bibtexTool.getValue("year");
		String volume = bibtexTool.getValue("volume");
		String pages = bibtexTool.getValue("pages");
		String number = bibtexTool.getValue("number");
		ArticleReference ref = new ArticleReference(journalAbbreviation, year, volume, number, pages);
		return ref;
	}

	private String getTitle() {
		return bibtexTool.getValue("title");
	}

}
