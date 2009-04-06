package wwmm.crystaleye.tools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static wwmm.crystaleye.CrystalEyeConstants.X_DC;
import static wwmm.crystaleye.CrystalEyeConstants.X_RDF;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;

import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.junit.BeforeClass;
import org.junit.Test;

import wwmm.crystaleye.Utils;
import wwmm.crystaleye.crawler.core.ArticleDetails;
import wwmm.crystaleye.crawler.core.ArticleReference;
import wwmm.crystaleye.crawler.core.DOI;
import wwmm.crystaleye.crawler.core.SupplementaryFileDetails;
import wwmm.crystaleye.tools.BibliontologyTool;

public class BibliontologyToolTest {

	static ArticleDetails ad;
	static Document actualDoc;
	static Document expectedDoc;

	@BeforeClass
	public static void beforeClass() throws URIException, NullPointerException {
		createArticleDetails();
		readExpectedDocFromFile();
	}

	/**
	 * <p>
	 * Make sure that the provided ArticleDetails is parsed by
	 * BibliontologyTool and that null is not returned.
	 * </p> 
	 */
	@Test
	public void testParseArticleDetails() throws NullPointerException, IOException { 
		BibliontologyTool bt = new BibliontologyTool(ad);
		actualDoc = bt.getDocument();
		assertNotNull(actualDoc);
	}

	/**
	 * Make sure the created XML document is as expected.
	 * @throws IOException 
	 */
	@Test
	public void checkCreatedAgainstActualDoc() throws IOException {
		// NOTE that as BibliontologyTool creates UUIDs for certain
		// RDF statements, that the created and expected XML docs will
		// never be the same.  So we have to remove the UUID elements
		// before comparing the docs.
		removeUuidStrings(actualDoc);
		removeUuidStrings(expectedDoc);
		String actualStr = Utils.toPrettyXMLString(actualDoc);
		String expectedStr = Utils.toPrettyXMLString(expectedDoc);
		assertEquals(expectedStr, actualStr);
	}

	/**
	 * Pass in a null ArticleDetails object, this should cause
	 * BibTool to fail.
	 */
	@Test
	public void testConstructorWithNullArticleDetails() {
		ArticleDetails ad = null;
		try {
			new BibliontologyTool(ad);
			fail("Should have failed on passing null ArticleDetails to the constructor.");
		} catch (IllegalStateException e) {
			assertTrue(true);
		}
	}

	/**
	 * Pass in an ArticleDetails object with all fields apart from 
	 * DOI set to null. This should not cause BibTool to fail, just 
	 * return an almost empty RDF XML document.
	 */
	@Test
	public void testParseArticleDetailsWithOnlyDoiSet() {
		ArticleDetails ad = new ArticleDetails();
		ad.setDoi(new DOI("http://dx.doi.org/a/valid/doi"));
		BibliontologyTool bt = new BibliontologyTool(ad);
		bt.getDocument();
	}

	/**
	 * The BibTool requires a DOI in the provided ArticleDetails,
	 * so it should fail.
	 */
	@Test
	public void testParseArticleDetailsWithNoDoiSet() {
		ArticleDetails ad = new ArticleDetails();
		BibliontologyTool bt = new BibliontologyTool(ad);
		try {
			bt.getDocument();
			fail("Should have failed as there is no DOI provided.");
		} catch (IllegalStateException e) {
			assertTrue(true);
		}
	}

	/**
	 * Find the UUID strings in the provided XML and remove them.
	 */
	private void removeUuidStrings(Document doc) {
		Nodes isPartOfNds = doc.query(".//dc:isPartOf", X_DC);
		for (int i = 0; i < isPartOfNds.size(); i++) {
			Element el = (Element)isPartOfNds.get(i);
			for (int j = 0; j < el.getChildCount(); j++) {
				el.getChild(j).detach();
			}
		}
		Nodes descNds = doc.query(".//rdf:Description/@rdf:about[contains(.,'urn:uuid')]", X_RDF);
		descNds.get(0).detach();
	}

	/**
	 * Parse the RDF XML file from the file-system that the tests
	 * will be compared against.
	 */
	private static void readExpectedDocFromFile() {
		File expectedFile = new File("./src/test/resources/task/bibliontologytool/example.bibliontology.xml");
		expectedDoc = Utils.parseXml(expectedFile);
	}

	/**
	 * Create the ArticleDetails object that will be used in the tests.
	 */
	private static void createArticleDetails() throws URIException, NullPointerException {
		ad = new ArticleDetails();
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
		List<SupplementaryFileDetails> sfdList = new ArrayList<SupplementaryFileDetails>();
		SupplementaryFileDetails sfd = new SupplementaryFileDetails(new URI("http://supp.file.com/file.txt", false),
				"file.txt", "Link text", "text/plain");
		sfdList.add(sfd);
		ad.setSuppFiles(sfdList);
	}
}