package wwmm.crystaleye.crawlers;

import static wwmm.crystaleye.CrystalEyeConstants.X_XHTML;
import nu.xom.Document;
import nu.xom.Nodes;

import org.apache.commons.httpclient.URI;

public class ArticleCrawler extends Crawler {
	
	protected URI doi;
	protected Document abstractPageDoc;
	protected boolean doiResolved;
	protected ArticleDetails ad = new ArticleDetails();
	protected BibtexTool bibtexTool;
	
	public ArticleCrawler(URI doi) {
		this.doi = doi;
		abstractPageDoc = httpClient.getWebpageDocument(doi);
		setHasDoiResolved(abstractPageDoc);
		ad.setDoi(doi);
		ad.setDoiResolved(doiResolved);
	}
	
	private void setHasDoiResolved(Document doc) {
		Nodes nodes = doc.query(".//x:body[contains(.,'Error - DOI Not Found')]", X_XHTML);
		if (nodes.size() > 0) {
			doiResolved = false;
		} else {
			doiResolved = true;
		}
	}

}
