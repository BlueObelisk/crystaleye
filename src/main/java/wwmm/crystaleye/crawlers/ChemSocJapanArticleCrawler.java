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

public class ChemSocJapanArticleCrawler extends ArticleCrawler {

	private static final Logger LOG = Logger.getLogger(ChemSocJapanArticleCrawler.class);

	public ChemSocJapanArticleCrawler(DOI doi) {
		super(doi);
	}

	public ArticleDetails getDetails() {
		if (!doiResolved) {
			LOG.warn("The DOI provided for the article abstract ("+doi.toString()+") has not resolved so we cannot get article details.");
			return ad;
		}
		URI fullTextLink = getFullTextLink();
		if (fullTextLink != null) {
			ad.setFullTextLink(fullTextLink);
		}
		List<SupplementaryFileDetails> suppFiles = getSupplementaryFilesDetails();
		setBibtexTool();
		if (bibtexTool != null) {
			String title = bibtexTool.getTitle();
			ArticleReference ref = bibtexTool.getReference();
			ref.setHasBeenPublished(true);
			String authors = bibtexTool.getAuthors();
			ad.setTitle(title);
			ad.setReference(ref);
			ad.setAuthors(authors);
			ad.setSuppFiles(suppFiles);
		}
		return ad;
	}

	private void setBibtexTool() {
		Nodes bibtexLinks = articleAbstractDoc.query(".//x:a[contains(@href,'/_bib/')]", X_XHTML);
		if (bibtexLinks.size() != 1) {
			return;
		}
		String urlPostfix = ((Element)bibtexLinks.get(0)).getAttributeValue("href");
		String bibUrl = CHEMSOCJAPAN_HOMEPAGE_URL+urlPostfix;
		URI bibtexUri = createURI(bibUrl);
		String bibStr = httpClient.getWebpageString(bibtexUri);
		bibtexTool = new BibtexTool(bibStr);
	}

	private URI getFullTextLink() {
		Nodes pdfLinks = articleAbstractDoc.query(".//x:a[contains(@href,'_pdf') and contains(.,'PDF')]", X_XHTML);
		if (pdfLinks.size() == 0) {
			return null;
		}
		String urlPostfix = ((Element)pdfLinks.get(0)).getAttributeValue("href");
		String pdfUrl = CHEMSOCJAPAN_HOMEPAGE_URL+urlPostfix;
		return createURI(pdfUrl);
	}

	private List<SupplementaryFileDetails> getSupplementaryFilesDetails() {
		Nodes suppListLinks = articleAbstractDoc.query(".//x:a[contains(@href,'_applist')]", X_XHTML);
		if (suppListLinks.size() == 0) {
			return new ArrayList<SupplementaryFileDetails>(0);
		}
		String urlPostfix = ((Element)suppListLinks.get(0)).getAttributeValue("href");
		String suppListUrl = CHEMSOCJAPAN_HOMEPAGE_URL+urlPostfix;
		Document suppListDoc = httpClient.getWebpageHTML(createURI(suppListUrl));
		Nodes suppTableNodes = suppListDoc.query(".//x:table[@cellpadding='2' and @cellspacing='3']", X_XHTML);
		Element suppTable = (Element)suppTableNodes.get(1);
		Nodes tableRows = suppTable.query(".//x:tr", X_XHTML);
		if (tableRows.size() < 3) {
			throw new CrawlerRuntimeException("Expected the supplementary document table to have at least 3 rows, found "+tableRows.size());
		}
		List<SupplementaryFileDetails> suppFiles = new ArrayList<SupplementaryFileDetails>(1);
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
			String contentType = httpClient.getContentType(suppUri);
			SupplementaryFileDetails suppFile = new SupplementaryFileDetails(suppUri, linkText, contentType);
			suppFiles.add(suppFile);
		}
		return suppFiles;
	}
	
}
