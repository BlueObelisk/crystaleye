package wwmm.crystaleye.crawlers;

import static junit.framework.Assert.assertSame;
import static org.mockito.Mockito.mock;

import org.apache.commons.httpclient.URI;
import org.junit.Before;
import org.junit.Test;

import wwmm.crystaleye.crawler.SupplementaryFileDetails;

public class SupplementaryFileDetailsTest {
	
	SupplementaryFileDetails sfd;
	URI uri;
	String linkText;
	String contentType;
	
	@Before
	public void createInstance() {
		uri = mock(URI.class);
		linkText = "Any old link text here";
		contentType = "image/png";
		sfd = new SupplementaryFileDetails(uri, linkText, contentType);
	}
	
	@Test
	public void testGetURI() {
		assertSame(uri, sfd.getUri());
	}
	
	@Test
	public void testGetLinkText() {
		assertSame(linkText, sfd.getLinkText());
	}
	
	@Test
	public void testGetContentType() {
		assertSame(contentType, sfd.getContentType());
	}

}
