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
import org.xmlcml.cml.base.CMLBuilder;

public class ChildCifXml2ChildCmlTaskTest {

	private static File fixturesRoot;
	private static String foldername = "childcifxml2cml";

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
		int childKey = 15;
		ChildCifXml2ChildCmlTask task = new ChildCifXml2ChildCmlTask(storageRoot, primaryKey, childKey);
		File expectedFile = new File(storageRoot, "1/15/15.cml");
		assertFalse(expectedFile.exists());
		boolean success = task.runTask();
		assertTrue(success);
		assertTrue(expectedFile.exists());
		try {
			CMLBuilder builder = new CMLBuilder();
			builder.build(expectedFile);
		} catch (Exception e) {
			fail("CML file should have been parsed correctly: "+expectedFile);
		}
	}
	
	@Test
	public void testRunTaskForNonExisingPrimaryKey() {
		File storageRoot = new File(fixturesRoot, "storage_root");
		int primaryKey = 99;
		int childKey = 1;
		ChildCifXml2ChildCmlTask task = new ChildCifXml2ChildCmlTask(storageRoot, primaryKey, childKey);
		boolean success = task.runTask();
		assertFalse(success);
	}
	
	@Test
	public void testRunTaskForNonExistingChildKey() {
		File storageRoot = new File(fixturesRoot, "storage_root");
		int primaryKey = 1;
		int childKey = 99;
		ChildCifXml2ChildCmlTask task = new ChildCifXml2ChildCmlTask(storageRoot, primaryKey, childKey);
		boolean success = task.runTask();
		assertFalse(success);
	}
	
	@Test
	public void testRunTaskForNonExistingChildCifXml() {
		File storageRoot = new File(fixturesRoot, "storage_root");
		int primaryKey = 1;
		int childKey = 3;
		ChildCifXml2ChildCmlTask task = new ChildCifXml2ChildCmlTask(storageRoot, primaryKey, childKey);
		boolean success = task.runTask();
		assertFalse(success);
	}
	
	@Test
	public void testRunTaskForInvalidCifXml() {
		File storageRoot = new File(fixturesRoot, "storage_root");
		int primaryKey = 1;
		int childKey = 4;
		ChildCifXml2ChildCmlTask task = new ChildCifXml2ChildCmlTask(storageRoot, primaryKey, childKey);
		boolean success = task.runTask();
		assertFalse(success);
	}

}
