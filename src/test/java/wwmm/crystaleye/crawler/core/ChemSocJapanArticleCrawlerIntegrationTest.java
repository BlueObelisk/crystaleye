package wwmm.crystaleye.crawler.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.junit.Test;

public class ChemSocJapanArticleCrawlerIntegrationTest {

	/**
	 * Goes out to the abstract page for an article at the
	 * CSJ site and scrapes the article details.  Basically
	 * a test that the abstract page HTML has not changed.
	 */
	@Test
	public void testGetArticleDetails() throws URIException, NullPointerException {
		DOI doi = new DOI(DOI.DOI_SITE_URL+"/10.1246/cl.2008.682");
		ChemSocJapanArticleCrawler crawler = new ChemSocJapanArticleCrawler(doi);
		ArticleDetails details = crawler.getDetails();
		assertNotNull(details);
		String authors = details.getAuthors();
		assertEquals("Koichiro Takao and Yasuhisa Ikeda", authors);
		DOI detailsDoi = details.getDoi();
		assertEquals(doi, detailsDoi);
		URI fullTextLink = details.getFullTextLink();
		assertEquals(new URI("http://www.jstage.jst.go.jp/article/cl/37/7/682/_pdf", false), fullTextLink);
		
		ArticleReference ref = details.getReference();
		String journalTitle = ref.getJournalTitle();
		assertEquals("Chemistry Letters", journalTitle);
		String number = ref.getNumber();
		assertEquals("7", number);
		String pages = ref.getPages();
		assertEquals("682-683", pages);
		String volume = ref.getVolume();
		assertEquals("37", volume);
		String year = ref.getYear();
		assertEquals("2008", year);
		String title = details.getTitle();
		assertEquals("Alternative Route to Metal Halide Free Ionic Liquids", title);
		
		List<SupplementaryFileDetails> suppList = details.getSuppFiles();
		assertEquals(2, suppList.size());
		SupplementaryFileDetails sfd0 = suppList.get(0);
		String contentType0 = sfd0.getContentType();
		assertEquals("application/pdf", contentType0);
		String fileId0 = sfd0.getFileId();
		assertEquals("1", fileId0);
		String linkText0 = sfd0.getLinkText();
		assertEquals("Supporting Information", linkText0);
		URI uri0 = sfd0.getURI();
		assertEquals(new URI("http://www.jstage.jst.go.jp/article/cl/37/7/37_682/_appendix/1", false), uri0);
		SupplementaryFileDetails sfd1 = suppList.get(1);
		String contentType1 = sfd1.getContentType();
		assertEquals("application/octet-stream", contentType1);
		String fileId1 = sfd1.getFileId();
		assertEquals("2", fileId1);
		String linkText1 = sfd1.getLinkText();
		assertEquals("Crystallographic Information File (CIF)", linkText1);
		URI uri1 = sfd1.getURI();
		assertEquals(new URI("http://www.jstage.jst.go.jp/article/cl/37/7/37_682/_appendix/2", false), uri1);
	}
	
}
