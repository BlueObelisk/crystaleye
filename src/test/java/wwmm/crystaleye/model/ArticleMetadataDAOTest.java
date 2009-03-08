package wwmm.crystaleye.model;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static wwmm.crystaleye.model.CifFileDAO.CIF_MIME;

import java.io.File;
import java.io.IOException;

import static junit.framework.Assert.*;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class ArticleMetadataDAOTest {
	
	private static File fixturesRoot;
	private static String foldername = "articlemetadatadao";
	
	private static final Logger LOG = Logger.getLogger(ArticleMetadataDAOTest.class);

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
		File fixturesSrc = new File("./src/test/resources/model/"+foldername);
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
	public void testInsertMetadataToExistingPrimaryKey() throws IOException {
		File storageRoot = new File(fixturesRoot, "storage_root");
		ArticleMetadataDAO metadataDao = new ArticleMetadataDAO(storageRoot);
		// assert that the key folder already exists
		File expectedPKeyLocation = new File(storageRoot, "1");
		assertTrue(expectedPKeyLocation.exists());
		String metadata = "this cif is AWESOME";
		boolean success = metadataDao.insertArticleMetadata(1, metadata);
		assertTrue(success);
		File expectedMetadataLocation = new File(expectedPKeyLocation, "1"+ArticleMetadataDAO.ARTICLE_METADATA_MIME);
		// assert file has been created at expected location
		assertTrue(expectedMetadataLocation.exists());
		// assert the contents are exactly as in original data string
		String contents = FileUtils.readFileToString(expectedMetadataLocation);
		assertEquals(metadata, contents);
	}
	
	@Test
	public void testInsertMetadataToNonExistingPrimaryKey() {
		File storageRoot = new File(fixturesRoot, "storage_root");
		ArticleMetadataDAO metadataDao = new ArticleMetadataDAO(storageRoot);
		String metadata = "some metadata";
		boolean success = metadataDao.insertArticleMetadata(99, metadata);
		// primary key 99 does not exists, so insertion should be false.
		assertFalse(success);
	}

}
