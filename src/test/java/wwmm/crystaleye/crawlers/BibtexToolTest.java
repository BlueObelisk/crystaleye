package wwmm.crystaleye.crawlers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import wwmm.crawler.ArticleReference;
import wwmm.crawler.BibtexTool;

public class BibtexToolTest {

	@Test
	public void testFailsOnEmptyFile() throws IOException {
		String bibStr = IOUtils.toString(getClass().getClassLoader()
				.getResourceAsStream("bibtex/empty-file.bib"));
		try {
			new BibtexTool(bibStr);
			fail("Parsing the empty file should have caused an exception.");
		} catch(IllegalStateException e) {
			assertTrue("Should throw like this if the Bibtex file is empty", true);
		}
	}
	
	@Test
	public void testReadsValidFilesWithoutFailing() throws IOException {
		String bibStr1 = IOUtils.toString(getClass().getClassLoader()
				.getResourceAsStream("bibtex/10.1021_cg801336t.bib"));
		new BibtexTool(bibStr1);
		
		String bibStr2 = IOUtils.toString(getClass().getClassLoader()
				.getResourceAsStream("bibtex/10.1107_S010876730804333X.bib"));
		new BibtexTool(bibStr2);
		
		String bibStr3 = IOUtils.toString(getClass().getClassLoader()
				.getResourceAsStream("bibtex/10.1107_S1600536809002645.bib"));
		new BibtexTool(bibStr3);
	}
	
	@Test
	public void testGetsCorrectNumberOfDataItems() throws IOException {
		String bibStr1 = IOUtils.toString(getClass().getClassLoader()
				.getResourceAsStream("bibtex/10.1107_S0021889809003987.bib"));
		BibtexTool bt1 = new BibtexTool(bibStr1);
		Map<String, String> nvps1 = bt1.getNameValuePairs();
		assertEquals(10, nvps1.size());
		
		String bibStr2 = IOUtils.toString(getClass().getClassLoader()
				.getResourceAsStream("bibtex/10.1021_cm802828z.bib"));
		BibtexTool bt2 = new BibtexTool(bibStr2);
		Map<String, String> nvps2 = bt2.getNameValuePairs();
		assertEquals(10, nvps2.size());
	}
	
	@Test
	public void testGetsCorrectValues() throws IOException {
		String bibStr1 = IOUtils.toString(getClass().getClassLoader()
				.getResourceAsStream("bibtex/10.1107_S0108270109003370.bib"));
		BibtexTool bt1 = new BibtexTool(bibStr1);
		String journal = bt1.getValue("journal");
		assertEquals("Acta Crystallographica Section C", journal);
		String year = bt1.getValue("year");
		assertEquals("2009", year);
		String volume = bt1.getValue("volume");
		assertEquals("65", volume);
		String number = bt1.getValue("number");
		assertEquals("3", number);
		String pages = bt1.getValue("pages");
		assertEquals("o92--o96", pages);
		String doi = bt1.getValue("doi");
		assertEquals("10.1107/S0108270109003370", doi);
	}
	
	@Test
	public void testGetReference() throws IOException {
		String bibStr1 = IOUtils.toString(getClass().getClassLoader()
				.getResourceAsStream("bibtex/10.1021_la8028484.bib"));
		BibtexTool bt1 = new BibtexTool(bibStr1);
		ArticleReference ar = bt1.getReference();
		assertNotNull(ar);
		String journal = ar.getJournalTitle();
		assertEquals("Langmuir", journal);
		String year = ar.getYear();
		assertEquals("2009", year);
		String volume = ar.getVolume();
		assertEquals("25", volume);
		String number = ar.getNumber();
		assertEquals("4", number);
		String pages = ar.getPages();
		assertEquals("1893-1896", pages);
	}

}
