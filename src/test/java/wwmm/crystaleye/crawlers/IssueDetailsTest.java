package wwmm.crystaleye.crawlers;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

public class IssueDetailsTest {

	@Test
	public void testConstructorAndGetters() {
		String year = "2009";
		String issueId = "28";
		IssueDetails id = new IssueDetails(year, issueId);
		assertSame(year, id.getYear());
		assertSame(issueId, id.getIssueId());
	}

	@Test
	public void testConstructorWithInvalidYear() {
		String invalidYear1 = "200";
		String invalidYear2 = "dave";
		String invalidYear3 = "99999";
		String issueId = "28";
		try {
			IssueDetails id1 = new IssueDetails(invalidYear1, issueId);
			fail("Invalid year string provided ("+invalidYear1+") constructor should have failed.");
		} catch(IllegalStateException e) {
			assertTrue("Should throw like this if the year is invalid.", true);
		}
		try {
			IssueDetails id2 = new IssueDetails(invalidYear2, issueId);
			fail("Invalid year string provided ("+invalidYear2+") constructor should have failed.");
		} catch(IllegalStateException e) {
			assertTrue("Should throw like this if the year in invalid.", true);
		}
		try {
			IssueDetails id3 = new IssueDetails(invalidYear3, issueId);
			fail("Invalid year string provided ("+invalidYear3+") constructor should have failed.");
		} catch(IllegalStateException e) {
			assertTrue("Should throw like this if the year in invalid.", true);
		}
	}

}
