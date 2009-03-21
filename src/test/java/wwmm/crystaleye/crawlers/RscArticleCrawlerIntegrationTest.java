package wwmm.crystaleye.crawlers;

import static org.junit.Assert.*;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.junit.Test;

import wwmm.crystaleye.crawler.ArticleDetails;
import wwmm.crystaleye.crawler.ArticleReference;
import wwmm.crystaleye.crawler.DOI;
import wwmm.crystaleye.crawler.RscArticleCrawler;
import wwmm.crystaleye.crawler.SupplementaryFileDetails;

public class RscArticleCrawlerIntegrationTest {

	/**
	 * Goes out to the abstract page for an article at the
	 * CSJ site and scrapes the article details.  Basically
	 * a test that the abstract page HTML has not changed.
	 */
	@Test
	public void testGetArticleDetails() throws URIException, NullPointerException {
		DOI doi = new DOI(DOI.DOI_SITE_URL+"/10.1039/b821431j");
		RscArticleCrawler crawler = new RscArticleCrawler(doi);
		ArticleDetails details = crawler.getDetails();
		assertNotNull(details);
		String authors = details.getAuthors();
		assertEquals("Celia Ribes, Eva Falomir, Juan Murga, Miguel Carda and J. Alberto Marco", authors);
		DOI detailsDoi = details.getDoi();
		assertEquals(doi, detailsDoi);
		URI fullTextLink = details.getFullTextLink();
		assertEquals(new URI("http://www.rsc.org/delivery/_ArticleLinking/ArticleLinking.cfm?JournalCode=OB&Year=2009&ManuscriptID=b821431j&Iss=7", false), fullTextLink);
		
		ArticleReference ref = details.getReference();
		String journalTitle = ref.getJournalTitle();
		assertEquals("Org. Biomol. Chem.", journalTitle);
		String pages = ref.getPages();
		assertEquals("1355 - 1360", pages);
		String volume = ref.getVolume();
		assertEquals("7", volume);
		String year = ref.getYear();
		assertEquals("2009", year);
		String title = details.getTitle();
		assertEquals("Convergent, stereoselective syntheses of the glycosidase inhibitors broussonetines D and M", title);
		
		List<SupplementaryFileDetails> suppList = details.getSuppFiles();
		assertEquals(3, suppList.size());
		SupplementaryFileDetails sfd0 = suppList.get(0);
		String contentType0 = sfd0.getContentType();
		assertEquals("application/pdf", contentType0);
		String fileId0 = sfd0.getFileId();
		assertEquals("b821431j_1", fileId0);
		String linkText0 = sfd0.getLinkText();
		assertEquals("Additional experimental procedures and tabulated spectral data of compounds 7, 8, 9a, 10, 11, 13, 14, 16–19 and 21", linkText0);
		URI uri0 = sfd0.getURI();
		assertEquals(new URI("http://pubs.rsc.org/suppdata/OB/b8/b821431j/b821431j_1.pdf", false), uri0);
		SupplementaryFileDetails sfd2 = suppList.get(2);
		String contentType2 = sfd2.getContentType();
		assertEquals("text/plain", contentType2);
		String fileId2 = sfd2.getFileId();
		assertEquals("b821431j", fileId2);
		String linkText2 = sfd2.getLinkText();
		assertEquals("Crystal structure data", linkText2);
		URI uri2 = sfd2.getURI();
		assertEquals(new URI("http://pubs.rsc.org/suppdata/OB/b8/b821431j/b821431j.txt", false), uri2);		
	}
	
	/**
	 * Two different types of reference appear on the article abstract 
	 * pages, each with differing amount of detail, e.g.:
	 * 
	 * 1. Chem. Commun., 2009, 1658 - 1660, DOI: 10.1039/b815825h
	 * 2. Org. Biomol. Chem., 2009, 7, 1280 - 1283, DOI: 10.1039/b900026g
	 * 
	 * So sometimes the volume is included, sometimes not.  This is taken
	 * into account in the crawler.  This test checks that it is working
	 * correctly.
	 */
	@Test
	public void testGetReference() {
		DOI doi1 = new DOI(DOI.DOI_SITE_URL+"/10.1039/b815825h");
		RscArticleCrawler crawler1 = new RscArticleCrawler(doi1);
		ArticleDetails details1 = crawler1.getDetails();
		ArticleReference ref1 = details1.getReference();
		String title1 = ref1.getJournalTitle();
		assertEquals("Chem. Commun.", title1);
		String pages1 = ref1.getPages();
		assertEquals("1658-1660", pages1);
		String vol1 = ref1.getVolume();
		assertNull(vol1);
		String year1 = ref1.getYear();
		assertEquals("2009", year1);
		DOI doi2 = new DOI(DOI.DOI_SITE_URL+"/10.1039/b900026g");
		RscArticleCrawler crawler2 = new RscArticleCrawler(doi2);
		ArticleDetails details2 = crawler2.getDetails();
		ArticleReference ref2 = details2.getReference();
		String title2 = ref2.getJournalTitle();
		assertEquals("Org. Biomol. Chem.", title2);
		String pages2 = ref2.getPages();
		assertEquals("1280 - 1283", pages2);
		String vol2 = ref2.getVolume();
		assertEquals("7", vol2);
		String year2 = ref2.getYear();
		assertEquals("2009", year2);
	}
	
}
