package wwmm.crystaleye.crawler;

import static junit.framework.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

public class ActaIssueCrawlerIntegrationTest {
	
	/**
	 * Goes out to the Acta site to check that the correct number
	 * of DOIs are scraped from a particular issue.  Basically a 
	 * check that the table of contents HTML structure hasn't 
	 * been changed.
	 */
	@Test
	public void testGetIssueDois() {
		IssueDetails details = new IssueDetails("2009", "01-00");
		ActaIssueCrawler crawler = new ActaIssueCrawler(ActaJournal.SECTION_C);
		List<DOI> doiList = crawler.getDOIs(details);
		assertEquals(23, doiList.size());
	}

	/**
	 * Test that the current issue is returned successfully
	 * i.e. an exception will be thrown if the returned HTTP 
	 * status is not 200.  Is a test that the current issues 
	 * are still at the same URL template.
	 */
	@Test
	public void testGetCurrentIssueHtml() {
		ActaIssueCrawler crawler = new ActaIssueCrawler(ActaJournal.SECTION_C);
		crawler.getCurrentIssueHtml();
	}

}
