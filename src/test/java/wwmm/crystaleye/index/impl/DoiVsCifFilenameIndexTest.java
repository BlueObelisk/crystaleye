package wwmm.crystaleye.index.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import wwmm.crawler.core.DOI;

public class DoiVsCifFilenameIndexTest {
	
	private static File fixturesRoot;
	private static String foldername = "doivsciffilenameindex";
	private final String EXPECTED_RESULT_FILENAME = "index_file_expected_result.txt";
	
	private static final Logger LOG = Logger.getLogger(DoiVsCifFilenameIndexTest.class);
	
	/**
	 * <p>
	 * The tests for DAO classes will create various folders and files.  
	 * We don't want to alter the fixtures in ./src/main/resources, 
	 * so the folder is copied over to the ./target directory and 
	 * the tests are run on that.  The directory is removed by the 
	 * method tagged with @AfterClass.
	 * </p>
	 */
	@BeforeClass
	public static void setUpTestStorageRoot() throws IOException {
		File target = new File("./target");
		File fixturesSrc = new File("./src/test/resources/index/"+foldername);
		fixturesRoot = new File(target, foldername);
		FileUtils.deleteDirectory(fixturesRoot);
		LOG.debug("Creating directory at: "+fixturesRoot.getAbsolutePath());
		FileUtils.copyDirectory(fixturesSrc, fixturesRoot);
	}

	/**
	 * <p>
	 * Remove the directory that was copied over to the ./target folder
	 * in <code>setUpTestStorageRoot</code>.
	 * </p>
	 */
	@AfterClass
	public static void removeTestStorageRoot() throws IOException {
		LOG.debug("Deleting directory at: "+fixturesRoot.getAbsolutePath());
		FileUtils.deleteDirectory(fixturesRoot);
	}
	
	@Test
	public void testAddItemWhenNoIndex() throws IOException {
		File storageRoot = new File(fixturesRoot, "testAddItemWhenNoIndex");
		File indexFile = new File(storageRoot, DoiVsCifFilenameIndex.INDEX_FILENAME);
		assertTrue(!indexFile.exists());
		DoiVsCifFilenameIndex index = new DoiVsCifFilenameIndex(storageRoot);
		// creation of index object checks for index existence, it
		// should have created the missing index.
		assertTrue(indexFile.exists());
		DOI doi = new DOI("http://dx.doi.org/10.1039/b829777c");
		index.insert(doi, "b829777c.cif");
		String actualIndexContents = FileUtils.readFileToString(indexFile);
		File expectedFile = new File(storageRoot, EXPECTED_RESULT_FILENAME);
		String expectedIndexContents = FileUtils.readFileToString(expectedFile);
		assertEquals(expectedIndexContents, actualIndexContents);
	}
	
	@Test
	public void testAddItemWithoutDontDuplicate() throws IOException {
		File storageRoot = new File(fixturesRoot, "testAddItemWithoutDontDuplicate");
		DoiVsCifFilenameIndex index = new DoiVsCifFilenameIndex(storageRoot);
		index.insert(new DOI("http://dx.doi.org/10.1039/b829777c"), "b829777c.cif", false);
		File indexFile = new File(storageRoot, DoiVsCifFilenameIndex.INDEX_FILENAME);
		String actualIndexContents = FileUtils.readFileToString(indexFile);
		File expectedFile = new File(storageRoot, EXPECTED_RESULT_FILENAME);
		String expectedIndexContents = FileUtils.readFileToString(expectedFile);
		assertEquals(expectedIndexContents, actualIndexContents);
	}
	
	@Test
	public void testAddItemWithDontDuplicate() throws IOException {
		File storageRoot = new File(fixturesRoot, "testAddItemWithDontDuplicate");
		DoiVsCifFilenameIndex index = new DoiVsCifFilenameIndex(storageRoot);
		index.insert(new DOI("http://dx.doi.org/10.1039/b829777c"), "b829777c.cif", true);
		File indexFile = new File(storageRoot, DoiVsCifFilenameIndex.INDEX_FILENAME);
		String actualIndexContents = FileUtils.readFileToString(indexFile);
		File expectedFile = new File(storageRoot, EXPECTED_RESULT_FILENAME);
		String expectedIndexContents = FileUtils.readFileToString(expectedFile);
		assertEquals(expectedIndexContents, actualIndexContents);
	}
	
	@Test
	public void testContainsWhenEntryExists() {
		File storageRoot = new File(fixturesRoot, "testAddItemWithDontDuplicate");
		DoiVsCifFilenameIndex index = new DoiVsCifFilenameIndex(storageRoot);
		boolean contains = index.contains(new DOI("http://dx.doi.org/10.1039/b829777c"), "b829777c.cif");
		assertTrue(contains);
	}
	
	@Test
	public void testContainsWhenEntryDoesNotExist() {
		File storageRoot = new File(fixturesRoot, "testAddItemWithDontDuplicate");
		DoiVsCifFilenameIndex index = new DoiVsCifFilenameIndex(storageRoot);
		boolean contains = index.contains(new DOI("http://dx.doi.org/10.1039/b829777c"), "newfilename.cif");
		assertFalse(contains);
	}

}
