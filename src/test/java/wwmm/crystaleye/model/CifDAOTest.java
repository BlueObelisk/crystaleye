package wwmm.crystaleye.model;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static wwmm.crystaleye.model.CifDAO.CIF_MIME;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class CifDAOTest {

	private static File fixturesRoot;
	private static String foldername = "cifdao";

	/**
	 * The tests in subclasses will create various folders and files.  
	 * We don't want to alter the fixtures in ./src/main/resources, 
	 * so the folder is copied over to the ./target directory and 
	 * the tests are run on that.  The directory is removed by the 
	 * method tagged with @AfterClass.
	 */
	@BeforeClass
	public static void setUpTestStorageRoot() throws IOException {
		File target = new File("./target");
		File fixturesSrc = new File("./src/test/resources/model/"+foldername);
		fixturesRoot = new File(target, foldername);
		FileUtils.deleteDirectory(fixturesRoot);
		FileUtils.copyDirectory(fixturesSrc, fixturesRoot);
	}

	/**
	 * Remove the directory that was copied over to the ./target folder
	 */
	@AfterClass
	public static void removeTestStorageRoot() throws IOException {
		FileUtils.deleteDirectory(fixturesRoot);
	}
	
	@Test
	public void testInsertCif() throws IOException {
		File storageRoot = new File(fixturesRoot, "storage_root");
		CifDAO cifDao = new CifDAO(storageRoot);
		String cifContents = "data_1\ncell_measurement_temperature 298\n";
		File key1File = new File(storageRoot, "1");
		assertTrue(!key1File.exists());
		cifDao.insertCif(cifContents);
		assertTrue(key1File.exists());
		File key1Cif = new File(key1File, "1"+CIF_MIME);
		assertTrue(key1Cif.exists());
		assertEquals(cifContents, FileUtils.readFileToString(key1Cif));
	}

}
