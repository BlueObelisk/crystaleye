package wwmm.crystaleye.crawler.core;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

import java.util.List;

import nu.xom.Document;

import org.junit.Test;

import wwmm.crawler.core.DOI;
import wwmm.crawler.core.IssueDetails;
import wwmm.crawler.core.RscIssueCrawler;
import wwmm.crawler.core.RscJournal;

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
		assertEquals(new DOI(DOI.DOI_SITE_URL+"/10.1039/b810767j"), doiList.get(9));
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
		Document doc = crawler.getCurrentIssueHtml();
		assertNotNull(doc);
	}
	
}
