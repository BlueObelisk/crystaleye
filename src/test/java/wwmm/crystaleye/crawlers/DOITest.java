package wwmm.crystaleye.crawlers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.junit.Before;
import org.junit.Test;

public class DOITest {

	String invalidUriUrl;
	URI invalidUri;
	String invalidDoiUrl;
	URI invalidDoiUri;
	String validDoiUrl;
	URI validDoiUri;

	@Before
	public void setupUrlsAndUris() throws URIException, NullPointerException {
		invalidUriUrl = "thisisnotavaliduri";
		invalidUri = new URI(invalidUriUrl, false);
		invalidDoiUrl = "http://www.google.com";
		invalidDoiUri = new URI(invalidDoiUrl, false);
		validDoiUrl = "http://dx.doi.org/10.1039/b815603d";
		validDoiUri = new URI(validDoiUrl, false);
	}

	@Test
	public void testStringConstructor() {
		try {
			DOI doi1 = new DOI(invalidUriUrl);
			fail("Invalid URI string provided ("+invalidUriUrl+") constructor should have failed.");
		} catch(Exception e) {
			//fails as expected
			;
		}
		try {
			DOI doi2 = new DOI(invalidDoiUrl);
			fail("Invalid DOI string provided ("+invalidDoiUrl+") constructor should have failed.");
		} catch(Exception e) {
			//fails as expected
			;
		}
		DOI doi3 = new DOI(validDoiUrl);
	}

	@Test
	public void testUriConstructor() {
		try {
			DOI doi1 = new DOI(invalidUri);
			fail("Invalid URI provided ("+invalidUri.toString()+") constructor should have failed.");
		} catch(Exception e) {
			//fails as expected
			;
		}
		try {
		DOI doi2 = new DOI(invalidDoiUri);
		fail("Invalid DOI provided ("+invalidDoiUri.toString()+") constructor should have failed.");
		} catch(Exception e) {
			//fails as expected
			;
		}
		DOI doi3 = new DOI(validDoiUri);
	}
	
	@Test
	public void testGetURI() {
		DOI doi1 = new DOI(validDoiUri);
		URI uri = doi1.getUri();
		assertSame(validDoiUri, uri);
	}
	
	@Test
	public void testToString() {
		DOI doi1 = new DOI(validDoiUrl);
		assertEquals(validDoiUrl, doi1.toString());
		DOI doi2 = new DOI(validDoiUri);
		assertEquals(validDoiUrl, doi2.toString());
	}
}
