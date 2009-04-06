package wwmm.crystaleye.task;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class ChildCif2ChildCheckcifTaskTest {

	private static File fixturesRoot;
	private static String foldername = "childcif2childcheckcif";

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
	public void testRunTask() throws IOException {
		File storageRoot = new File(fixturesRoot, "storage_root");
		int primaryKey = 1;
		int childKey = 2;
		ChildCif2ChildCheckcifTask task = new ChildCif2ChildCheckcifTask(storageRoot, primaryKey, childKey);
		File expectedFile = new File(storageRoot, "/1/2/2.checkcif.html");
		assertFalse(expectedFile.exists());
		boolean success = task.runTask();
		assertTrue(success);
		assertTrue(expectedFile.exists());
		String checkcifHtmlStr = FileUtils.readFileToString(expectedFile);
		assertNotNull(checkcifHtmlStr);
		// assert the CheckCIF string contains a known chunk of text
		// to make the CheckCIF has been correctly created.
		assertTrue(checkcifHtmlStr.contains("Datablock: I"));
	}

	@Test
	public void testRunTaskForNonExistingPrimaryKey() {
		File storageRoot = new File(fixturesRoot, "storage_root");
		int primaryKey = 99;
		int childKey = 1;
		ChildCif2ChildCheckcifTask task = new ChildCif2ChildCheckcifTask(storageRoot, primaryKey, childKey);
		boolean success = task.runTask();
		assertFalse(success);
	}

	@Test
	public void testRunTaskForNonExistingChildKey() {
		File storageRoot = new File(fixturesRoot, "storage_root");
		int primaryKey = 1;
		int childKey = 99;
		ChildCif2ChildCheckcifTask task = new ChildCif2ChildCheckcifTask(storageRoot, primaryKey, childKey);
		boolean success = task.runTask();
		assertFalse(success);
	}

	@Test
	public void testRunTaskForNonExistingChildCif() {
		File storageRoot = new File(fixturesRoot, "storage_root");
		int primaryKey = 1;
		int childKey = 3;
		ChildCif2ChildCheckcifTask task = new ChildCif2ChildCheckcifTask(storageRoot, primaryKey, childKey);
		boolean success = task.runTask();
		assertFalse(success);
	}

	@Test
	public void testRunTaskForInvalidChildCif() {
		File storageRoot = new File(fixturesRoot, "storage_root");
		int primaryKey = 99;
		int childKey = 4;
		ChildCif2ChildCheckcifTask task = new ChildCif2ChildCheckcifTask(storageRoot, primaryKey, childKey);
		boolean success = task.runTask();
		assertFalse(success);
	}

}
