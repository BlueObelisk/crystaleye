package wwmm.crystaleye.crawlers;

import static wwmm.crystaleye.CrystalEyeConstants.XHTML_NS;
import static wwmm.crystaleye.CrystalEyeConstants.X_XHTML;
import static wwmm.crystaleye.crawlers.CrawlerConstants.CHEMSOCJAPAN_HOMEPAGE_URL;

import java.util.ArrayList;
import java.util.List;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;
import nu.xom.Text;

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
		if (!hasDoiResolved(abstractPageDoc)) {
			LOG.warn("It looks like this DOI has not resolved - check and submit an error on the form "+doi);
		}

		List<SupplementaryFile> suppFiles = getSupplementaryFiles();
		ArticleDetails ad = new ArticleDetails();
		ad.setDoi(doi);
		URI fullTextHtmlLink = getFullTextHtmlLink();
		if (fullTextHtmlLink != null) {
			ad.setFullTextHtmlLink(fullTextHtmlLink);
		}
		
		setBibtexTool();
		if (bibtexTool != null) {
			String title = getTitle();
			ArticleReference ref = getReference();
			String authors = getAuthors();
			ad.setTitle(title);
			ad.setReference(ref);
			ad.setAuthors(authors);
			ad.setSuppFiles(suppFiles);
		}

		return ad;
	}
	
	private boolean hasDoiResolved(Document doc) {
		Nodes nodes = doc.query(".//x:body[contains(.,'Error - DOI Not Found')]", X_XHTML);
		if (nodes.size() > 0) {
			return false;
		} else {
			return true;
		}
	}

	private void setBibtexTool() {
		Nodes bibtexLinks = abstractPageDoc.query(".//x:a[contains(@href,'/_bib/')]", X_XHTML);
		if (bibtexLinks.size() != 1) {
			return;
		}
		String urlPostfix = ((Element)bibtexLinks.get(0)).getAttributeValue("href");
		String bibUrl = CHEMSOCJAPAN_HOMEPAGE_URL+urlPostfix;
		URI bibtexUri = createURI(bibUrl);
		String bibStr = httpClient.getWebpageString(bibtexUri);
		bibtexTool = new BibtexTool(bibStr);
	}

	private URI getFullTextHtmlLink() {
		Nodes pdfLinks = abstractPageDoc.query(".//x:a[contains(@href,'_pdf') and contains(.,'PDF')]", X_XHTML);
		if (pdfLinks.size() == 0) {
			return null;
		}
		String urlPostfix = ((Element)pdfLinks.get(0)).getAttributeValue("href");
		String pdfUrl = CHEMSOCJAPAN_HOMEPAGE_URL+urlPostfix;
		return createURI(pdfUrl);
	}

	private List<SupplementaryFile> getSupplementaryFiles() {
		Nodes suppListLinks = abstractPageDoc.query(".//x:a[contains(@href,'_applist')]", X_XHTML);
		if (suppListLinks.size() == 0) {
			return new ArrayList<SupplementaryFile>(0);
		}
		String urlPostfix = ((Element)suppListLinks.get(0)).getAttributeValue("href");
		String suppListUrl = CHEMSOCJAPAN_HOMEPAGE_URL+urlPostfix;
		Document suppListDoc = httpClient.getWebpageDocument(createURI(suppListUrl));
		Nodes suppTableNodes = suppListDoc.query(".//x:table[@cellpadding='2' and @cellspacing='3']", X_XHTML);
		Element suppTable = (Element)suppTableNodes.get(1);
		Nodes tableRows = suppTable.query(".//x:tr", X_XHTML);
		if (tableRows.size() < 3) {
			throw new RuntimeException("Expected the supplementary document table to have at least 3 rows, found "+tableRows.size());
		}
		List<SupplementaryFile> suppFiles = new ArrayList<SupplementaryFile>(1);
		for (int i = 2; i < tableRows.size(); i++) {
			Element row = (Element)tableRows.get(i);
			Nodes cells = row.query(".//x:td", X_XHTML);
			if (cells.size() != 4) {
				continue;
			}
			Element cell0 = (Element)cells.get(0);
			String linkText = ((Text)cell0.getChild(0)).getValue();
			Element cell3 = (Element)cells.get(3);
			Element suppLink = cell3.getFirstChildElement("a", XHTML_NS);
			String suppUrlPostfix = suppLink.getAttributeValue("href");
			String suppUrl = CHEMSOCJAPAN_HOMEPAGE_URL+suppUrlPostfix;
			URI suppUri = createURI(suppUrl);
			String contentType = getContentType(suppUri);
			SupplementaryFile suppFile = new SupplementaryFile(suppUri, linkText, contentType);
			suppFiles.add(suppFile);
		}
		return suppFiles;
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
