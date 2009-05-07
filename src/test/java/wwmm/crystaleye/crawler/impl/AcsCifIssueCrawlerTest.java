package wwmm.crystaleye.crawler.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import junitx.util.PrivateAccessor;

import org.junit.Test;

import wwmm.crawler.core.AcsIssueCrawler;
import wwmm.crawler.core.SupplementaryFileDetails;
import wwmm.crawler.impl.AcsCifIssueCrawler;

public class AcsCifIssueCrawlerTest {
	
	/**
	 * Test makes sure that files that are CIFs are recognised as such
	 * in the isCifFile() method.
	 */
	@Test
	public void testIsCifFile() throws Throwable {
		AcsCifIssueCrawler crawler = new AcsCifIssueCrawler(mock(AcsIssueCrawler.class));
		SupplementaryFileDetails sfd1 = mock(SupplementaryFileDetails.class);
		String cifFileId = "this-is-a-cif-file.cif";
		when(sfd1.getFileId()).thenReturn(cifFileId);
		// use reflection to access private isCifFile method for testing
		boolean isCif1 = (Boolean) PrivateAccessor.invoke(crawler, "isCifFile", 
				new Class[]{SupplementaryFileDetails.class}, new Object[]{sfd1});
		assertTrue(isCif1);
		
		String notCifFileId = "not-a-cif.txt";
		when(sfd1.getFileId()).thenReturn(notCifFileId);
		// use reflection to access private isCifFile method for testing
		boolean isCif2 = (Boolean) PrivateAccessor.invoke(crawler, "isCifFile", 
				new Class[]{SupplementaryFileDetails.class}, new Object[]{sfd1});
		assertFalse(isCif2);
	}

}
