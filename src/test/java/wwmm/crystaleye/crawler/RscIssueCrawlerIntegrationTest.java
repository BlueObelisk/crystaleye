package wwmm.crystaleye.crawler;

import static junit.framework.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import wwmm.crystaleye.crawler.AcsIssueCrawler;
import wwmm.crystaleye.crawler.AcsJournal;
import wwmm.crystaleye.crawler.DOI;
import wwmm.crystaleye.crawler.IssueDetails;
import wwmm.crystaleye.crawler.RscIssueCrawler;
import wwmm.crystaleye.crawler.RscJournal;

public class RscIssueCrawlerIntegrationTest {
	
	/**
	 * Goes out to the RSC site to check that the correct number
	 * of DOIs are scraped from a particular issue.  Basically a 
	 * check that the table of contents HTML structure hasn't 
	 * been changed.
	 */
	@Test
	public void testGetIssueDois() {
		IssueDetails details = new IssueDetails("2009", "2");
		RscIssueCrawler crawler = new RscIssueCrawler(RscJournal.DALTON_TRANSACTIONS);
		List<DOI> doiList = crawler.getDOIs(details);
		assertEquals(20, doiList.size());
	}

	/**
	 * Test that the current issue is returned successfully
	 * i.e. an exception will be thrown if the returned HTTP 
	 * status is not 200.  Is a test that the current issues 
	 * are still at the same URL template.
	 */
	@Test
	public void testGetCurrentIssueHtml() {
		RscIssueCrawler crawler = new RscIssueCrawler(RscJournal.CHEMCOMM);
		crawler.getCurrentIssueHtml();
	}
	
}
