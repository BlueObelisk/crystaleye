package wwmm.crystaleye.crawlers;

import org.junit.Test;

import wwmm.crystaleye.crawler.RscIssueCrawler;
import wwmm.crystaleye.crawler.RscJournal;

public class RscCrawlerIntegrationTest {

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
