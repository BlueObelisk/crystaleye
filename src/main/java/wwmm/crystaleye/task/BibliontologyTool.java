package wwmm.crystaleye.task;

import static wwmm.crystaleye.CrystalEyeConstants.BIBO_NS;
import static wwmm.crystaleye.CrystalEyeConstants.BIBO_PREFIX;
import static wwmm.crystaleye.CrystalEyeConstants.DC_NS;
import static wwmm.crystaleye.CrystalEyeConstants.DC_PREFIX;
import static wwmm.crystaleye.CrystalEyeConstants.RDF_NS;
import static wwmm.crystaleye.CrystalEyeConstants.RDF_PREFIX;

import java.util.List;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Text;

import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.log4j.Logger;

import wwmm.crystaleye.crawler.ArticleDetails;
import wwmm.crystaleye.crawler.ArticleReference;
import wwmm.crystaleye.crawler.DOI;
import wwmm.crystaleye.crawler.SupplementaryFileDetails;

/**
 * <p>
 * Provides a method of converting the information in an 
 * <code>ArticleDetails</code> object into Bibliontology metadata.
 * </p>
 * 
 * @author Nick Day
 * @version 0.1
 */
public class BibliontologyTool {
	
	ArticleDetails ad;
	
	private static final Logger LOG = Logger.getLogger(BibliontologyTool.class);
	
	public BibliontologyTool(ArticleDetails ad) {
		this.ad = ad;
	}
	
	public String toString() {
		return toDocument().toXML();
	}
	
	public Document toDocument() {
		Element rdfRoot = new Element(RDF_PREFIX+":RDF", RDF_NS);
		rdfRoot.addNamespaceDeclaration(BIBO_PREFIX, BIBO_NS);
		rdfRoot.addNamespaceDeclaration(DC_PREFIX, DC_NS);
		
		Document bibDoc = new Document(rdfRoot);
		Element doiDesc = createRdfDescription(ad.getDoi().toString());
		rdfRoot.appendChild(doiDesc);
		
		Element rdfType = createDcElement("type", BIBO_PREFIX+":Article");
		doiDesc.appendChild(rdfType);
		
		String authors = ad.getAuthors();
		Element authorsEl = createBiboElement("authorList", authors);
		doiDesc.appendChild(authorsEl);
		
		URI fullTextLink = ad.getFullTextLink();
		ArticleReference ref = ad.getReference();
		String title = ad.getTitle();
		List<SupplementaryFileDetails> sfdList = ad.getSuppFiles();
		
		return bibDoc;
	}
	
	private Element createBiboElement(String name, String value) {
		Element el = new Element(BIBO_PREFIX+":"+name, BIBO_NS);
		el.appendChild(new Text(value));
		return el;
	}
	
	private Element createRdfDescription(String uriString) {
		Element desc = new Element(RDF_PREFIX+":Description", RDF_NS);
		Attribute about = new Attribute(RDF_PREFIX+":about", RDF_NS, uriString);
		desc.addAttribute(about);
		return desc;
	}
	
	private Element createDcElement(String name, String value) {
		Element el = new Element(DC_PREFIX+":"+name, DC_NS);
		el.appendChild(new Text(value));
		return el;
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
		DOI doi = new DOI("http://dx.doi.org/article/10.1039/asdflk7");
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
		BibliontologyTool bt = new BibliontologyTool(ad);
		System.out.println(bt.toString());
	}

}
