package wwmm.crystaleye.task;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xmlcml.cml.base.CMLBuilder;
import org.xmlcml.cml.element.CMLCml;
import org.xmlcml.cml.element.CMLMolecule;

public class ChildCml2ChildDerivedCmlTaskTest {
	
	private static File fixturesRoot;
	private static String foldername = "cml2derivedcml";

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
		//FileUtils.forceDelete(fixturesRoot);
	}

	@Test
	public void testRunTask() {
		File storageRoot = new File(fixturesRoot, "storage_root");
		int primaryKey = 1;
		int childKey = 2;
		ChildCml2ChildDerivedCmlTask task = new ChildCml2ChildDerivedCmlTask(storageRoot, primaryKey, childKey);
		File expectedFile = new File(storageRoot, "/1/2/2.derived.cml");
		assertFalse(expectedFile.exists());
		boolean success = task.runTask();
		assertTrue(success);
		assertTrue(expectedFile.exists());
		try {
			CMLBuilder builder = new CMLBuilder();
			CMLCml cml = (CMLCml)builder.build(expectedFile).getRootElement();
			CMLMolecule container = (CMLMolecule)cml.getFirstCMLChild(CMLMolecule.TAG);
			assertEquals("container", container.getId());
			List<CMLMolecule> molList = container.getDescendantsOrMolecule();
			assertEquals(2, molList.size());
			assertEquals("moiety_1", molList.get(0).getId());
			assertEquals("moiety_2", molList.get(1).getId());
		} catch (Exception e) {
			fail("CML file should have been parsed correctly: "+expectedFile);
		}
	}
	
	@Test
	public void testRunTaskForNonExisingPrimaryKey() {
		File storageRoot = new File(fixturesRoot, "storage_root");
		int primaryKey = 99;
		int childKey = 1;
		ChildCml2ChildDerivedCmlTask task = new ChildCml2ChildDerivedCmlTask(storageRoot, primaryKey, childKey);
		boolean success = task.runTask();
		assertFalse(success);
	}
	
	@Test
	public void testRunTaskForNonExistingChildKey() {
		File storageRoot = new File(fixturesRoot, "storage_root");
		int primaryKey = 1;
		int childKey = 99;
		ChildCml2ChildDerivedCmlTask task = new ChildCml2ChildDerivedCmlTask(storageRoot, primaryKey, childKey);
		boolean success = task.runTask();
		assertFalse(success);
	}
	
	@Test
	public void testRunTaskForNonExistingChildCml() {
		File storageRoot = new File(fixturesRoot, "storage_root");
		int primaryKey = 1;
		int childKey = 3;
		ChildCml2ChildDerivedCmlTask task = new ChildCml2ChildDerivedCmlTask(storageRoot, primaryKey, childKey);
		boolean success = task.runTask();
		assertFalse(success);
	}
	
	@Test
	public void testRunTaskForInvalidCml() {
		File storageRoot = new File(fixturesRoot, "storage_root");
		int primaryKey = 1;
		int childKey = 4;
		ChildCml2ChildDerivedCmlTask task = new ChildCml2ChildDerivedCmlTask(storageRoot, primaryKey, childKey);
		boolean success = task.runTask();
		assertFalse(success);
	}

}
