package wwmm.crystaleye.crawlers;

import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertSame;

import org.junit.Test;

import wwmm.crystaleye.crawler.ArticleReference;

public class ArticleReferenceTest {
	
	@Test
	public void testConstructor() {
		ArticleReference ar = new ArticleReference();
		assertNull(ar.getJournalTitle());
		assertNull(ar.getYear());
		assertNull(ar.getVolume());
		assertNull(ar.getNumber());
		assertNull(ar.getPages());
	}
	
	@Test 
	public void testSetAndGetJournal() {
		ArticleReference ar = new ArticleReference();
		String journal = "Acta. Cryst. E";
		ar.setJournalTitle(journal);
		assertSame(journal, ar.getJournalTitle());
	}
	
	@Test 
	public void testSetAndGetYear() {
		ArticleReference ar = new ArticleReference();
		String year = "2009";
		ar.setYear(year);
		assertSame(year, ar.getYear());
	}
	
	@Test 
	public void testSetAndGetVolume() {
		ArticleReference ar = new ArticleReference();
		String volume = "8";
		ar.setVolume(volume);
		assertSame(volume, ar.getVolume());
	}
	
	@Test 
	public void testSetAndGetNumber() {
		ArticleReference ar = new ArticleReference();
		String number = "28";
		ar.setNumber(number);
		assertSame(number, ar.getNumber());
	}
	
	@Test 
	public void testSetAndGetPage() {
		ArticleReference ar = new ArticleReference();
		String pages = "499-508";
		ar.setPages(pages);
		assertSame(pages, ar.getPages());
	}

}
