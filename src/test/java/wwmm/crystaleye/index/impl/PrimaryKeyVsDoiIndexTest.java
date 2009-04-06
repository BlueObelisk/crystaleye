package wwmm.crystaleye.index.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import wwmm.crystaleye.crawler.core.DOI;
import wwmm.crystaleye.index.impl.PrimaryKeyVsDoiIndex;

public class PrimaryKeyVsDoiIndexTest {

	private static File fixturesRoot;
	private static String foldername = "primarykeyvsdoiindex";
	private final String EXPECTED_RESULT_FILENAME = "index_file_expected_result.txt";
	private static final Logger LOG = Logger.getLogger(PrimaryKeyVsDoiIndexTest.class);

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
	public void testAddItemWhenIndexDoesNotExist() throws IOException {
		File storageRoot = new File(fixturesRoot, "storage_root_no_index");
		File indexFile = new File(storageRoot, PrimaryKeyVsDoiIndex.INDEX_FILENAME);
		assertTrue(!indexFile.exists());
		PrimaryKeyVsDoiIndex index = new PrimaryKeyVsDoiIndex(storageRoot);
		// creation of index object checks for index existence, it
		// should have created the missing index.
		assertTrue(indexFile.exists());
		DOI doi = new DOI("http://dx.doi.org/10.1039/b829777c");
		index.insert(1, doi);
		String actualIndexContents = FileUtils.readFileToString(indexFile);
		File expectedFile = new File(storageRoot, EXPECTED_RESULT_FILENAME);
		String expectedIndexContents = FileUtils.readFileToString(expectedFile);
		assertEquals(expectedIndexContents, actualIndexContents);
	}
	
	@Test
	public void testAddItemThatIsOneMoreThanPreviousHighestKey() throws IOException {
		File storageRoot = new File(fixturesRoot, "storage_root_with_index_1to2");
		File indexFile = new File(storageRoot, PrimaryKeyVsDoiIndex.INDEX_FILENAME);
		PrimaryKeyVsDoiIndex index = new PrimaryKeyVsDoiIndex(storageRoot);
		DOI doi = new DOI("http://dx.doi.org/10.1039/b829777c");
		index.insert(3, doi);
		String actualIndexContents = FileUtils.readFileToString(indexFile);
		File expectedFile = new File(storageRoot, EXPECTED_RESULT_FILENAME);
		String expectedIndexContents = FileUtils.readFileToString(expectedFile);
		assertEquals(expectedIndexContents, actualIndexContents);
	}
	
	@Test
	public void testAddItemManyMoreThanPreviousHighestKey() throws IOException {
		File storageRoot = new File(fixturesRoot, "storage_root_with_index_1to3");
		File indexFile = new File(storageRoot, PrimaryKeyVsDoiIndex.INDEX_FILENAME);
		PrimaryKeyVsDoiIndex index = new PrimaryKeyVsDoiIndex(storageRoot);
		DOI doi = new DOI("http://dx.doi.org/10.1039/b829777c");
		index.insert(9, doi);
		String actualIndexContents = FileUtils.readFileToString(indexFile);
		File expectedFile = new File(storageRoot, EXPECTED_RESULT_FILENAME);
		String expectedIndexContents = FileUtils.readFileToString(expectedFile);
		assertEquals(expectedIndexContents, actualIndexContents);
	}
	
	@Test
	public void testAddItemToMiddleOfIndex() throws IOException {
		File storageRoot = new File(fixturesRoot, "storage_root_with_index_1to5_with_missing");
		File indexFile = new File(storageRoot, PrimaryKeyVsDoiIndex.INDEX_FILENAME);
		PrimaryKeyVsDoiIndex index = new PrimaryKeyVsDoiIndex(storageRoot);
		DOI doi = new DOI("http://dx.doi.org/10.1039/b829777c");
		index.insert(3, doi);
		String actualIndexContents = FileUtils.readFileToString(indexFile);
		File expectedFile = new File(storageRoot, EXPECTED_RESULT_FILENAME);
		String expectedIndexContents = FileUtils.readFileToString(expectedFile);
		assertEquals(expectedIndexContents, actualIndexContents);
	}
	
	@Test
	public void testRetrieveItemForExistingKey() {
		File storageRoot = new File(fixturesRoot, "storage_root_with_index_1to3");
		PrimaryKeyVsDoiIndex index = new PrimaryKeyVsDoiIndex(storageRoot);
		DOI doi = index.getDOI(2);
		String actualDoiStr = doi.toString();
		String expectedDoiStr = DOI.DOI_SITE_URL+"/10.1039/b819894b";
		assertEquals(expectedDoiStr, actualDoiStr);
	}
	
	@Test
	public void testAttemptRetrieveNonExistingKeyGreaterThanMaxInIndex() {
		File storageRoot = new File(fixturesRoot, "storage_root_with_index_1to3");
		PrimaryKeyVsDoiIndex index = new PrimaryKeyVsDoiIndex(storageRoot);
		DOI doi = index.getDOI(99);
		assertNull(doi);
	}
	
	@Test
	public void testAttemptRetrieveNonExistingKeyLessThanMaxInIndex() {
		File storageRoot = new File(fixturesRoot, "storage_root_with_index_1to5_with_missing");
		PrimaryKeyVsDoiIndex index = new PrimaryKeyVsDoiIndex(storageRoot);
		DOI doi = index.getDOI(2);
		assertNull(doi);
	}
	
	@Test
	public void testGetKeysForDoi() {
		File storageRoot = new File(fixturesRoot, "storage_root_with_index_1to5_with_duplicate_dois");
		PrimaryKeyVsDoiIndex index = new PrimaryKeyVsDoiIndex(storageRoot);
		DOI doi = new DOI(DOI.DOI_SITE_URL+"/10.1039/b819997c");
		assertTrue(index.containsDOI(doi));
		DOI doi2 = new DOI(DOI.DOI_SITE_URL+"/10.1039/b837362f");
		assertTrue(index.containsDOI(doi2));
		DOI doi3 = new DOI(DOI.DOI_SITE_URL+"/10.1039/b000000h");
		assertFalse(index.containsDOI(doi3));
	}
	
}
