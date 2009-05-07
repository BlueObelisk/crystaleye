package wwmm.crystaleye.crawler.core;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

import java.util.List;

import nu.xom.Document;

import org.junit.Test;

import wwmm.crawler.core.AcsIssueCrawler;
import wwmm.crawler.core.AcsJournal;
import wwmm.crawler.core.DOI;
import wwmm.crawler.core.IssueDetails;

public class AcsIssueCrawlerIntegrationTest {
	
	/**
	 * Goes out to the ACS site to check that the correct number
	 * of DOIs are scraped from a particular issue.  Basically a 
	 * check that the table of contents HTML structure hasn't 
	 * been changed.
	 */
	@Test
	public void testGetIssueDois() {
		IssueDetails details = new IssueDetails("2009", "2");
		AcsIssueCrawler crawler = new AcsIssueCrawler(AcsJournal.THE_JOURNAL_OF_ORGANIC_CHEMISTRY);
		List<DOI> doiList = crawler.getDOIs(details);
		assertEquals(66, doiList.size());
		assertEquals(new DOI(DOI.DOI_SITE_URL+"/10.1021/jo8019237"), doiList.get(9));
	}

	/**
	 * Test that the current issue is returned successfully
	 * i.e. an exception will be thrown if the returned HTTP 
	 * status is not 200.  Is a test that the current issues 
	 * are still at the same URL template.
	 */
	@Test
	public void testGetCurrentIssueHtml() {
		AcsIssueCrawler crawler = new AcsIssueCrawler(AcsJournal.JOURNAL_OF_THE_AMERICAN_CHEMICAL_SOCIETY);
		Document doc = crawler.getCurrentIssueHtml();
		assertNotNull(doc);
	}

}
