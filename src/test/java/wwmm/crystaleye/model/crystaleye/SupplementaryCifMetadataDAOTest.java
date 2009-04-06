package wwmm.crystaleye.model.crystaleye;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import wwmm.crystaleye.model.impl.SupplementaryCifMetadataDAO;

public class SupplementaryCifMetadataDAOTest {
	
	private static File fixturesRoot;
	private static String foldername = "articlemetadatadao";
	
	private static final Logger LOG = Logger.getLogger(SupplementaryCifMetadataDAOTest.class);

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
		SupplementaryCifMetadataDAO metadataDao = new SupplementaryCifMetadataDAO(storageRoot);
		// assert that the key folder already exists
		File expectedPKeyLocation = new File(storageRoot, "1");
		assertTrue(expectedPKeyLocation.exists());
		String metadata = "this cif is AWESOME";
		boolean success = metadataDao.insert(1, metadata);
		assertTrue(success);
		File expectedMetadataLocation = new File(expectedPKeyLocation, "1"+SupplementaryCifMetadataDAO.SUPPLEMENTARY_CIF_METADATA_MIME);
		// assert file has been created at expected location
		assertTrue(expectedMetadataLocation.exists());
		// assert the contents are exactly as in original data string
		String contents = FileUtils.readFileToString(expectedMetadataLocation);
		assertEquals(metadata, contents);
	}
	
	@Test
	public void testInsertMetadataToNonExistingPrimaryKey() {
		File storageRoot = new File(fixturesRoot, "storage_root");
		SupplementaryCifMetadataDAO metadataDao = new SupplementaryCifMetadataDAO(storageRoot);
		String metadata = "some metadata";
		boolean success = metadataDao.insert(99, metadata);
		// primary key 99 does not exists, so insertion should be false.
		assertFalse(success);
	}

}
