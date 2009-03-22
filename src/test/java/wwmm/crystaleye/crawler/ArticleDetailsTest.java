package wwmm.crystaleye.crawler;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertSame;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.URI;
import org.junit.Test;

public class ArticleDetailsTest {
	
	@Test
	public void testConstructor() {
		ArticleDetails ad = new ArticleDetails();
		assertNull("DOI should initially be null when using the default constructor.", ad.getDoi());
		assertNull("Full-text HTML link should initially be null when using the default constructor.", ad.getFullTextLink());
		assertNull("Title should initially be null when using the default constructor.", ad.getTitle());
		assertNull("Authors should initially be null when using the default constructor.", ad.getAuthors());
		assertNull("Reference should initially be null when using the default constructor.", ad.getReference());
		assertNull("Supplementary file list should initially be null when using the default constructor.", ad.getSuppFiles());
		assertTrue(ad.hasBeenPublished());
	}
	
	@Test
	public void testSetAndGetDOI() {
		ArticleDetails ad = new ArticleDetails();
		DOI doi = mock(DOI.class);
		ad.setDoi(doi);
		assertSame(doi, ad.getDoi());
	}
	
	@Test
	public void testSetAndGetFulltextHtmlLink() {
		ArticleDetails ad = new ArticleDetails();
		URI uri = mock(URI.class);
		ad.setFullTextLink(uri);
		assertSame(uri, ad.getFullTextLink());
	}
	
	@Test 
	public void testSetAndGetTitle() {
		ArticleDetails ad = new ArticleDetails();
		String title = "Any old title will do";
		ad.setTitle(title);
		assertSame(title, ad.getTitle());
	}
	
	@Test
	public void testSetAndGetAuthors() {
		ArticleDetails ad = new ArticleDetails();
		String authors = "N. E. Day, O. J. Downing, J. A. Townsend, P. Murray-Rust";
		ad.setAuthors(authors);
		assertSame(authors, ad.getAuthors());
	}
	
	@Test
	public void testSetAndGetReference() {
		ArticleDetails ad = new ArticleDetails();
		ArticleReference reference = mock(ArticleReference.class);
		ad.setReference(reference);
		assertSame(reference, ad.getReference());
	}
	
	@Test
	public void testSetAndGetSupplementaryFileList() {
		ArticleDetails ad = new ArticleDetails();
		List<SupplementaryFileDetails> sfd = mock(ArrayList.class);
		ad.setSuppFiles(sfd);
		assertEquals(sfd, ad.getSuppFiles());
	}

}
