package wwmm.crystaleye.crawler.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import junitx.util.PrivateAccessor;

import org.apache.commons.httpclient.URI;
import org.junit.Test;

import wwmm.crystaleye.crawler.core.ActaIssueCrawler;
import wwmm.crystaleye.crawler.core.SupplementaryFileDetails;
import wwmm.crystaleye.crawler.impl.ActaCifIssueCrawler;

public class ActaCifIssueCrawlerTest {
	
	/**
	 * Test makes sure that files that are CIFs are recognised as such
	 * in the isCifFile() method.
	 */
	@Test
	public void testIsCifFile() throws Throwable {
		ActaCifIssueCrawler crawler = new ActaCifIssueCrawler(mock(ActaIssueCrawler.class));
		SupplementaryFileDetails sfd1 = mock(SupplementaryFileDetails.class);
		URI cifUri = new URI("http://scripts.iucr.org/cgi-bin/sendcif?lg3009sup1", false);
		when(sfd1.getURI()).thenReturn(cifUri);
		// use reflection to access private isCifFile method for testing
		boolean isCif1 = (Boolean) PrivateAccessor.invoke(crawler, "isCifFile", 
				new Class[]{SupplementaryFileDetails.class}, new Object[]{sfd1});
		assertTrue(isCif1);
		
		URI notCifUri = new URI("http://journals.iucr.org/c/issues/2009/04/00/sq3182/sq3182Isup2.hkl", false);
		when(sfd1.getURI()).thenReturn(notCifUri);
		// use reflection to access private isCifFile method for testing
		boolean isCif2 = (Boolean) PrivateAccessor.invoke(crawler, "isCifFile", 
				new Class[]{SupplementaryFileDetails.class}, new Object[]{sfd1});
		assertFalse(isCif2);
	}

}
