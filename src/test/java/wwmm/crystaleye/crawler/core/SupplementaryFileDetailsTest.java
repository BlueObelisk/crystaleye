package wwmm.crystaleye.crawler.core;

import static junit.framework.Assert.assertSame;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.junit.Before;
import org.junit.Test;

public class SupplementaryFileDetailsTest {

	SupplementaryFileDetails sfd;
	URI uri;
	String filename;
	String linkText;
	String contentType;

	@Before
	public void createInstance() throws URIException, NullPointerException {
		uri = new URI("http://something/filename.txt", false);
		filename = "filename.txt";
		linkText = "Any old link text here";
		contentType = "image/png";
		sfd = new SupplementaryFileDetails(uri, filename, linkText, contentType);
	}

	@Test
	public void testAttemptCreateInstanceWithWrongFilename() throws URIException, NullPointerException {
		uri = new URI("http://something/filename.txt", false);
		filename = "wrongname";
		linkText = "Any old link text here";
		contentType = "image/png";
		try {
			sfd = new SupplementaryFileDetails(uri, filename, linkText, contentType);
			fail("Should have failed as filename is not the latter part of the" +
			"provided URI.");
		} catch (Exception e) {
			assertTrue(true);
		}
	}

	@Test
	public void testGetURI() {
		assertSame(uri, sfd.getURI());
	}

	@Test
	public void testGetFilename() {
		assertSame(filename, sfd.getFileId());
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
