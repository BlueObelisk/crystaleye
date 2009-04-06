package wwmm.crystaleye.crawler.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import junitx.util.PrivateAccessor;

import org.junit.Test;

import wwmm.crystaleye.crawler.core.RscIssueCrawler;
import wwmm.crystaleye.crawler.core.SupplementaryFileDetails;

public class RscCifIssueCrawlerTest {
	
	/**
	 * Test makes sure that files that are CIFs are recognised as such
	 * in the isCifFile() method.
	 */
	@Test
	public void testIsCifFile() throws Throwable {
		RscCifIssueCrawler crawler = new RscCifIssueCrawler(mock(RscIssueCrawler.class));
		SupplementaryFileDetails sfd1 = mock(SupplementaryFileDetails.class);
		String cifLinkText = "Crystal structure";
		when(sfd1.getLinkText()).thenReturn(cifLinkText);
		// use reflection to access private isCifFile method for testing
		boolean isCif1 = (Boolean) PrivateAccessor.invoke(crawler, "isCifFile", 
				new Class[]{SupplementaryFileDetails.class}, new Object[]{sfd1});
		assertTrue(isCif1);
		
		String notCifLinkText = "NMR spectra";
		when(sfd1.getLinkText()).thenReturn(notCifLinkText);
		// use reflection to access private isCifFile method for testing
		boolean isCif2 = (Boolean) PrivateAccessor.invoke(crawler, "isCifFile", 
				new Class[]{SupplementaryFileDetails.class}, new Object[]{sfd1});
		assertFalse(isCif2);
	}

}
