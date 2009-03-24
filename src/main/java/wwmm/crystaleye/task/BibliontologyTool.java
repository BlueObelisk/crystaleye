package wwmm.crystaleye.task;

import static wwmm.crystaleye.CrystalEyeConstants.BIBO_NS;
import static wwmm.crystaleye.CrystalEyeConstants.BIBO_PREFIX;
import static wwmm.crystaleye.CrystalEyeConstants.CRYSTALEYE_NS;
import static wwmm.crystaleye.CrystalEyeConstants.CRYSTALEYE_PREFIX;
import static wwmm.crystaleye.CrystalEyeConstants.DC_NS;
import static wwmm.crystaleye.CrystalEyeConstants.DC_PREFIX;
import static wwmm.crystaleye.CrystalEyeConstants.RDF_NS;
import static wwmm.crystaleye.CrystalEyeConstants.RDF_PREFIX;

import java.util.List;
import java.util.UUID;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Text;

import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.log4j.Logger;

import wwmm.crystaleye.Utils;
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
	Element rdfRoot;

	private static final Logger LOG = Logger.getLogger(BibliontologyTool.class);

	public BibliontologyTool(ArticleDetails ad) {
		this.ad = ad;
		if (ad == null) {
			throw new IllegalStateException("ArticleDetails passed to the constructor is null.");
		}
	}

	/**
	 * <p>
	 * Gets a String representation of Bibliontology RDF XML of 
	 * the class ArticleDetails.
	 * </p>
	 * 
	 * @return String containing the XML of the data in the class
	 * ArticleDetails.
	 */
	public String toString() {
		return Utils.toPrettyXMLString(getDocument());
	}

	/**
	 * <p>
	 * Gets the Bibliontology RDF XML of the class ArticleDetails.
	 * </p>
	 * 
	 * @return XML document containing the data in the class
	 * ArticleDetails.
	 */
	public Document getDocument() {
		rdfRoot = new Element(RDF_PREFIX+":RDF", RDF_NS);
		rdfRoot.addNamespaceDeclaration(BIBO_PREFIX, BIBO_NS);
		rdfRoot.addNamespaceDeclaration(DC_PREFIX, DC_NS);
		rdfRoot.addNamespaceDeclaration(CRYSTALEYE_PREFIX, CRYSTALEYE_NS);

		Document bibDoc = new Document(rdfRoot);

		UUID uuid = UUID.randomUUID();
		String uuidStr = "urn:uuid:"+uuid.toString();
		rdfRoot.appendChild(createArticleRdf(uuidStr));
		rdfRoot.appendChild(createJournalRdf(uuidStr));

		return bibDoc;
	}

	/**
	 * <p>
	 * Creates Bibliontology RDF XML containing details about
	 * the articles containing journal.
	 * </p>
	 * 
	 * @param uuidStr - the UUID that will be used to represent
	 * the journals ID in the output RDF.
	 * 
	 * @return XML element containing the RDF relating to the
	 * article containing journal.
	 */
	private Element createJournalRdf(String uuidStr) {
		Element journalRdf = createRdfDescription(uuidStr);
		Element rdfType = createDcElement("type", BIBO_PREFIX+":Journal");
		journalRdf.appendChild(rdfType);

		ArticleReference ref = ad.getReference();
		if (ref != null) {
			String journalTitle = ref.getJournalTitle();
			if (journalTitle != null) {
				Element titleEl = createBiboElement("shortTitle", journalTitle);
				journalRdf.appendChild(titleEl);
			}
		}
		return journalRdf;
	}

	/**
	 * <p>
	 * Creates Bibliontology RDF XML containing details about
	 * the article.
	 * </p>
	 * 
	 * @param uuidStr - the UUID that will be used to represent
	 * the ID for the articles containing journal in the output RDF. 
	 * 
	 * @return XML element containing Bibliontology RDF XML about
	 * the article.
	 */
	private Element createArticleRdf(String uuidStr) {
		LOG.debug("DOI: "+ad.getDoi());
		DOI doi = ad.getDoi();
		if (doi == null) {
			throw new IllegalStateException("The ArticleDetails must have " +
			"a DOI set to create the XML.");
		}
		Element articleRdf = createRdfDescription(doi.toString());

		Element rdfType = createDcElement("type", BIBO_PREFIX+":Article");
		articleRdf.appendChild(rdfType);

		String title = ad.getTitle();
		if (title != null) {
			Element titleEl = createDcElement("title", title);
			articleRdf.appendChild(titleEl);
		}

		String authors = ad.getAuthors();
		if (authors != null) {
			Element authorsEl = createBiboElement("authorList", authors);
			articleRdf.appendChild(authorsEl);
		}

		URI fullTextLink = ad.getFullTextLink();
		if (fullTextLink != null) {
			try {
				String url = fullTextLink.getURI();
				Element urlEl = createBiboElement("uri", url);
				articleRdf.appendChild(urlEl);
			} catch (URIException e) {
				LOG.warn("Problem getting URL string from URI: "+fullTextLink);
			}
		}


		ArticleReference ref = ad.getReference();
		if (ref != null) {
			String year = ref.getYear();
			if (year != null) {
				Element yearEl = createDcElement("date", year);
				articleRdf.appendChild(yearEl);
			}

			String volume = ref.getVolume();
			if (volume != null) {
				Element volEl = createBiboElement("volume", volume);
				articleRdf.appendChild(volEl);
			}

			String number = ref.getNumber();
			if (number != null) {
				Element issueEl = createBiboElement("issue", number);
				articleRdf.appendChild(issueEl);
			}

			String pages = ref.getPages();
			if (pages != null) {
				Element pagesEl = createBiboElement("pages", pages);
				articleRdf.appendChild(pagesEl);
			}
		}

		Element isPartOfEl = createDcElement("isPartOf", uuidStr);
		articleRdf.appendChild(isPartOfEl);

		List<SupplementaryFileDetails> suppFiles = ad.getSuppFiles();
		if (suppFiles != null) {
			for (SupplementaryFileDetails sfd : suppFiles) {
				String suppUrl = sfd.getUriString();
				Element suppEl = createCrystalEyeElement("hasSupplementaryFile", suppUrl);
				articleRdf.appendChild(suppEl);

				Element suppDescEl = createRdfDescription(suppUrl);
				rdfRoot.appendChild(suppDescEl);
				String contentType = sfd.getContentType();
				Element dcFormat = createDcElement("format", contentType);
				suppDescEl.appendChild(dcFormat);
				Element supplementaryFileEl = createDcElement("type", CRYSTALEYE_PREFIX+":supplementaryFile");
				suppDescEl.appendChild(supplementaryFileEl);
			}
		}

		return articleRdf;
	}

	/**
	 * <p>
	 * Creates an XML element representing an RDF Description. 
	 * </p>
	 * 
	 * @param value to be contained in the element.
	 * 
	 * @return the RDF Description XML.
	 */
	private Element createRdfDescription(String value) {
		Element desc = createRdfElement("Description", null); 
		Attribute about = new Attribute(RDF_PREFIX+":about", RDF_NS, value);
		desc.addAttribute(about);
		return desc;
	}

	/**
	 * <p>
	 * Creates an XML element with the CrystalEye namespace and prefix.
	 * </p>
	 * 
	 * @param name - the name of the created element.
	 * @param value - the value of the created element.
	 * 
	 * @return the created element.
	 */
	private Element createCrystalEyeElement(String name, String value) {
		Element el = new Element(CRYSTALEYE_PREFIX+":"+name, CRYSTALEYE_NS);
		el.appendChild(new Text(value));
		return el;
	}

	/**
	 * <p>
	 * Creates an XML element with the Bibliontoloy namespace and prefix.
	 * </p>
	 * 
	 * @param name - the name of the created element.
	 * @param value - the value of the created element.
	 * 
	 * @return the created element.
	 */
	private Element createBiboElement(String name, String value) {
		Element el = new Element(BIBO_PREFIX+":"+name, BIBO_NS);
		el.appendChild(new Text(value));
		return el;
	}

	/**
	 * <p>
	 * Creates an XML element with the RDF namespace and prefix.
	 * </p>
	 * 
	 * @param name - the name of the created element.
	 * @param value - the value of the created element.
	 * 
	 * @return the created element.
	 */
	private Element createRdfElement(String name, String value) {
		Element el = new Element(RDF_PREFIX+":"+name, RDF_NS);
		if (value != null) {
			el.appendChild(new Text(value));
		}
		return el;
	}

	/**
	 * <p>
	 * Creates an XML element with the Dublin Core namespace and prefix.
	 * </p>
	 * 
	 * @param name - the name of the created element.
	 * @param value - the value of the created element.
	 * 
	 * @return the created element.
	 */
	private Element createDcElement(String name, String value) {
		Element el = new Element(DC_PREFIX+":"+name, DC_NS);
		el.appendChild(new Text(value));
		return el;
	}

}
