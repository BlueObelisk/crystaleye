package wwmm.crystaleye.crawler;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

import java.util.List;

import nu.xom.Document;

import org.junit.Test;

public class ChemSocJapanIssueCrawlerIntegrationTest {
	
	/**
	 * Goes out to the CSJ site to check that the correct number
	 * of DOIs are scraped from a particular issue.  Basically a 
	 * check that the table of contents HTML structure hasn't 
	 * been changed.
	 */
	@Test
	public void testGetIssueDois() {
		IssueDetails details = new IssueDetails("2009", "2");
		ChemSocJapanIssueCrawler crawler = new ChemSocJapanIssueCrawler(ChemSocJapanJournal.CHEMISTRY_LETTERS);
		List<DOI> doiList = crawler.getDOIs(details);
		assertEquals(42, doiList.size());
		assertEquals(new DOI(DOI.DOI_SITE_URL+"/10.1246/cl.2009.126"), doiList.get(9));
	}

	/**
	 * Test that the current issue is returned successfully
	 * i.e. an exception will be thrown if the returned HTTP 
	 * status is not 200.  Is a test that the current issues 
	 * are still at the same URL template.
	 */
	@Test
	public void testGetCurrentIssueHtml() {
		ChemSocJapanIssueCrawler crawler = new ChemSocJapanIssueCrawler(ChemSocJapanJournal.CHEMISTRY_LETTERS);
		Document doc = crawler.getCurrentIssueHtml();
		assertNotNull(doc);
	}
	
}
