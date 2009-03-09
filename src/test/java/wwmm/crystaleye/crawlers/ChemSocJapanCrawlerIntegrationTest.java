package wwmm.crystaleye.crawlers;

import org.junit.Test;

import wwmm.crystaleye.crawler.ChemSocJapanIssueCrawler;
import wwmm.crystaleye.crawler.ChemSocJapanJournal;

public class ChemSocJapanCrawlerIntegrationTest {

	/**
	 * Test that the current issue is returned successfully
	 * i.e. an exception will be thrown if the returned HTTP 
	 * status is not 200.  Is a test that the current issues 
	 * are still at the same URL template.
	 */
	@Test
	public void testGetCurrentIssueHtml() {
		ChemSocJapanIssueCrawler crawler = new ChemSocJapanIssueCrawler(ChemSocJapanJournal.CHEMISTRY_LETTERS);
		crawler.getCurrentIssueHtml();
	}
	
}
