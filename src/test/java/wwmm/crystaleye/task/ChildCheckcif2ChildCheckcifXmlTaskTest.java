package wwmm.crystaleye.task;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import nu.xom.Document;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import wwmm.pubcrawler.Utils;

public class ChildCheckcif2ChildCheckcifXmlTaskTest {

	private static File fixturesRoot;
	private static String foldername = "childcheckcif2checkcifxml";

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
		ChildCheckcif2ChildCheckcifXmlTask task = new ChildCheckcif2ChildCheckcifXmlTask(storageRoot, primaryKey, childKey);
		File expectedFile = new File(storageRoot, "/1/2/2.checkcif.xml");
		assertFalse(expectedFile.exists());
		boolean success = task.runTask();
		assertTrue(success);
		assertTrue(expectedFile.exists());
		try {
			Document doc = Utils.parseXml(expectedFile);
			assertTrue(true);
			assertTrue(doc.toXML().contains("PLAT340_ALERT_3_C"));
		} catch (Exception e) {
			fail("Should have created valid XML, but hasn't in file at: "+expectedFile);
		}
	}

	@Test
	public void testRunTaskForNonExistingPrimaryKey() {
		File storageRoot = new File(fixturesRoot, "storage_root");
		int primaryKey = 99;
		int childKey = 1;
		ChildCheckcif2ChildCheckcifXmlTask task = new ChildCheckcif2ChildCheckcifXmlTask(storageRoot, primaryKey, childKey);
		boolean success = task.runTask();
		assertFalse(success);
	}

	@Test
	public void testRunTaskForNonExistingChildKey() {
		File storageRoot = new File(fixturesRoot, "storage_root");
		int primaryKey = 1;
		int childKey = 
			99;
		ChildCheckcif2ChildCheckcifXmlTask task = new ChildCheckcif2ChildCheckcifXmlTask(storageRoot, primaryKey, childKey);
		boolean success = task.runTask();
		assertFalse(success);
	}

	@Test
	public void testRunTaskForNonExistingChildCifXml() {
		File storageRoot = new File(fixturesRoot, "storage_root");
		int primaryKey = 1;
		int childKey = 3;
		ChildCheckcif2ChildCheckcifXmlTask task = new ChildCheckcif2ChildCheckcifXmlTask(storageRoot, primaryKey, childKey);
		boolean success = task.runTask();
		assertFalse(success);
	}

	@Test
	public void testRunTaskForInvalidChildCifXml() {
		File storageRoot = new File(fixturesRoot, "storage_root");
		int primaryKey = 99;
		int childKey = 4;
		ChildCheckcif2ChildCheckcifXmlTask task = new ChildCheckcif2ChildCheckcifXmlTask(storageRoot, primaryKey, childKey);
		boolean success = task.runTask();
		assertFalse(success);
	}

}
