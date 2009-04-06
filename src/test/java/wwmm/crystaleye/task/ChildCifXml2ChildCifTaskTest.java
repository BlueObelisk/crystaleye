package wwmm.crystaleye.task;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xmlcml.cif.CIFParser;

public class ChildCifXml2ChildCifTaskTest {

	private static File fixturesRoot;
	private static String foldername = "childcifxml2childcif";

	@BeforeClass
	public static void setUpTestStorageRoot() throws IOException {
		File target = new File("./target");
		File fixturesSrc = new File("./src/test/resources/task/"+foldername);
		fixturesRoot = new File(target, foldername);
		FileUtils.deleteDirectory(fixturesRoot);
		FileUtils.copyDirectory(fixturesSrc, fixturesRoot);
	}

	/**
	 * Remove the directory that was copied over to the ./target folder
	 */
	@AfterClass
	public static void removeTestStorageRoot() throws IOException {
		FileUtils.forceDelete(fixturesRoot);
	}

	@Test
	public void testRunTask() {
		File storageRoot = new File(fixturesRoot, "storage_root");
		int primaryKey = 1;
		int childKey = 2;
		ChildCifXml2ChildCifTask task = new ChildCifXml2ChildCifTask(storageRoot, primaryKey, childKey);
		File expectedFile = new File(storageRoot, "/1/2/2.cif");
		assertFalse(expectedFile.exists());
		boolean success = task.runTask();
		assertTrue(success);
		assertTrue(expectedFile.exists());
		CIFParser parser = new CIFParser();
		try {
			parser.parse(expectedFile);
			assertTrue(true);
		} catch (Exception e) {
			fail("Should not fail upon parsing the created CIF file: "+e.getMessage());
		}
	}

	@Test
	public void testRunTaskForNonExistingPrimaryKey() {
		File storageRoot = new File(fixturesRoot, "storage_root");
		int primaryKey = 99;
		int childKey = 1;
		ChildCifXml2ChildCifTask task = new ChildCifXml2ChildCifTask(storageRoot, primaryKey, childKey);
		boolean success = task.runTask();
		assertFalse(success);
	}

	@Test
	public void testRunTaskForNonExistingChildKey() {
		File storageRoot = new File(fixturesRoot, "storage_root");
		int primaryKey = 1;
		int childKey = 
			99;
		ChildCifXml2ChildCifTask task = new ChildCifXml2ChildCifTask(storageRoot, primaryKey, childKey);
		boolean success = task.runTask();
		assertFalse(success);
	}

	@Test
	public void testRunTaskForNonExistingChildCifXml() {
		File storageRoot = new File(fixturesRoot, "storage_root");
		int primaryKey = 1;
		int childKey = 3;
		ChildCifXml2ChildCifTask task = new ChildCifXml2ChildCifTask(storageRoot, primaryKey, childKey);
		boolean success = task.runTask();
		assertFalse(success);
	}

	@Test
	public void testRunTaskForInvalidChildCifXml() {
		File storageRoot = new File(fixturesRoot, "storage_root");
		int primaryKey = 99;
		int childKey = 4;
		ChildCifXml2ChildCifTask task = new ChildCifXml2ChildCifTask(storageRoot, primaryKey, childKey);
		boolean success = task.runTask();
		assertFalse(success);
	}

}
