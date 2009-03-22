package wwmm.crystaleye.crawler;

import java.util.List;

import static junit.framework.Assert.*;

import org.junit.Test;

import wwmm.crystaleye.crawler.AcsIssueCrawler;
import wwmm.crystaleye.crawler.AcsJournal;
import wwmm.crystaleye.crawler.DOI;
import wwmm.crystaleye.crawler.IssueDetails;

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
		crawler.getCurrentIssueHtml();
	}

}
