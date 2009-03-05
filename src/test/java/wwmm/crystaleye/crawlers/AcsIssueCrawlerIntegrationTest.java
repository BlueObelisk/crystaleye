package wwmm.crystaleye.crawlers;

import java.util.ArrayList;
import java.util.List;

import nu.xom.Document;

import org.junit.Test;

import wwmm.crystaleye.Utils;
import wwmm.crystaleye.crawler.AcsIssueCrawler;
import wwmm.crystaleye.crawler.AcsJournal;

public class AcsIssueCrawlerIntegrationTest {
	
	AcsJournal[] journals = AcsJournal.values();
	int maxSleep = 1500;
	
	List<Document> currentIssueHtmls = new ArrayList<Document>();

	/**
	 * Test that the current issue is returned successfully
	 * i.e. an exception will be thrown if the returned HTTP 
	 * status is not 200. 
	 */
	@Test
	public void testGetCurrentIssueHtml() {
		for (AcsJournal journal : journals) {
			AcsIssueCrawler crawler = new AcsIssueCrawler(journal);
			Document html = crawler.getCurrentIssueHtml();
			Utils.sleep(maxSleep);
		}
	}
	
}
