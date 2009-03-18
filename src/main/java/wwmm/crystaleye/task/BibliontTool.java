package wwmm.crystaleye.task;

import java.io.FileOutputStream;
import java.io.IOException;

import nu.xom.Document;
import nu.xom.Serializer;

import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.log4j.Logger;

import wwmm.crystaleye.crawler.ArticleDetails;
import wwmm.crystaleye.crawler.ArticleReference;
import wwmm.crystaleye.crawler.DOI;
import wwmm.crystaleye.model.CifFileDAO;

/**
 * <p>
 * Provides a method of converting the information in an 
 * <code>ArticleDetails</code> object into Bibliontology metadata.
 * </p>
 * 
 * @author Nick Day
 * @version 0.1
 */
public class BibliontTool {
	
	ArticleDetails ad;
	
	private static final Logger LOG = Logger.getLogger(BibliontTool.class);
	
	public BibliontTool(ArticleDetails ad) {
		this.ad = ad;
	}
	
	public String toString() {
		return toDocument().toXML();
	}
	
	public Document toDocument() {
		return null;
	}
	
	/**
	 * <p>
	 * Main method for demonstration of use only.  No args required.
	 * </p>
	 */
	public static void main(String[] args) throws URIException, NullPointerException {
		ArticleDetails ad = new ArticleDetails();
		String authors = "A. N. Other";
		ad.setAuthors(authors);
		DOI doi = new DOI("http://dx.doi.org/article/doi");
		ad.setDoi(doi);
		URI fullTextHtmlLink = new URI("http://the.articles.url/here", false);
		ad.setFullTextLink(fullTextHtmlLink);
		ArticleReference ar = new ArticleReference();
		ar.setJournalTitle("The journal title");
		ar.setNumber("10");
		ar.setPages("1--10");
		ar.setVolume("1");
		ar.setYear("2009");
		ad.setReference(ar);
		String title = "The article's title";
		ad.setTitle(title);
		BibliontTool bt = new BibliontTool(ad);
		System.out.println(bt.toString());
	}

}
