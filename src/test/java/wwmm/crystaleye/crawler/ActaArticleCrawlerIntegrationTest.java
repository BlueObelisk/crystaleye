package wwmm.crystaleye.crawler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.junit.Test;

public class ActaArticleCrawlerIntegrationTest {
	
	/**
	 * Goes out to the abstract page for an article at the
	 * Acta site and scrapes the article details.  Basically
	 * a test that the abstract page HTML has not changed.
	 */
	@Test
	public void testGetArticleDetails() throws URIException, NullPointerException {
		DOI doi = new DOI(DOI.DOI_SITE_URL+"/10.1107/S0108270109006118");
		ActaArticleCrawler crawler = new ActaArticleCrawler(doi);
		ArticleDetails details = crawler.getDetails();
		assertNotNull(details);
		String authors = details.getAuthors();
		assertEquals("Kim, Jinyoung and Ahn, Docheon and Kulshreshtha, Chandramouli and Sohn, Kee-Sun and Shin, Namsoo", authors);
		DOI detailsDoi = details.getDoi();
		assertEquals(doi, detailsDoi);
		URI fullTextLink = details.getFullTextLink();
		assertEquals(new URI("http://journals.iucr.org/c/issues/2009/04/00/sq3185/index.html", false), fullTextLink);
		
		ArticleReference ref = details.getReference();
		String journalTitle = ref.getJournalTitle();
		assertEquals("Acta Crystallographica Section C", journalTitle);
		String number = ref.getNumber();
		assertEquals("4", number);
		String pages = ref.getPages();
		assertEquals("i14--i16", pages);
		String volume = ref.getVolume();
		assertEquals("65", volume);
		String year = ref.getYear();
		assertEquals("2009", year);
		String title = details.getTitle();
		assertEquals("Lithium barium silicate, Li${\\sb 2}$BaSiO${\\sb 4}$, from synchrotron powder data", title);
		
		List<SupplementaryFileDetails> suppList = details.getSuppFiles();
		assertEquals(1, suppList.size());
		SupplementaryFileDetails sfd0 = suppList.get(0);
		String contentType0 = sfd0.getContentType();
		assertEquals("text/plain; charset=utf-8", contentType0);
		String fileId0 = sfd0.getFileId();
		assertEquals("sq3185sup1", fileId0);
		String linkText0 = sfd0.getLinkText();
		assertEquals("CIF", linkText0);
		URI uri0 = sfd0.getURI();
		assertEquals(new URI("http://scripts.iucr.org/cgi-bin/sendcif?sq3185sup1", false), uri0);
	}
	
	/**
	 * Most article in Acta journals only have 1 CIF as supp info.  These
	 * are linked directly from the article abstract page.  If there is more
	 * than 1 CIF for an article, another page has to be scraped, so there
	 * is another chance for the scraper to fail.  This test checks that the
	 * scraping of the extra page is working.
	 */
	@Test
	public void testGetMultipleCifsFromArticle() {
		DOI doi = new DOI(DOI.DOI_SITE_URL+"/10.1107/S0108768109004066");
		ActaArticleCrawler crawler = new ActaArticleCrawler(doi);
		ArticleDetails details = crawler.getDetails();
		assertEquals(2, details.getSuppFiles().size());
	}

}
