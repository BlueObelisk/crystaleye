package wwmm.crystaleye.crawlers;

import org.junit.Test;

import wwmm.crystaleye.crawler.AcsIssueCrawler;
import wwmm.crystaleye.crawler.AcsJournal;

public class AcsIssueCrawlerIntegrationTest {

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
