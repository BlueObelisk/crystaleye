package wwmm.crystaleye.model.crystaleye;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import wwmm.crystaleye.model.impl.ParentCifFileDAO;

public class ParentCifFileDAOTest {

	private static File fixturesRoot;
	private static String foldername = "cifdao";
	
	private static final Logger LOG = Logger.getLogger(ParentCifFileDAOTest.class);

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
	public void testInsertCif() throws IOException {
		File storageRoot = new File(fixturesRoot, "storage_root");
		ParentCifFileDAO cifDao = new ParentCifFileDAO(storageRoot);
		String cifContents = "data_1\ncell_measurement_temperature 298\n";
		File key1File = new File(storageRoot, "1");
		assertTrue(!key1File.exists());
		cifDao.insert(cifContents);
		assertTrue(key1File.exists());
		File key1Cif = new File(key1File, "1"+ParentCifFileDAO.PARENT_CIF_MIME);
		assertTrue(key1Cif.exists());
		assertEquals(cifContents, FileUtils.readFileToString(key1Cif));
	}

}
