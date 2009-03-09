package wwmm.crystaleye.crawlers;

import org.junit.Test;

import wwmm.crystaleye.crawler.ActaIssueCrawler;
import wwmm.crystaleye.crawler.ActaJournal;

public class ActaCrawlerIntegrationTest {

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
