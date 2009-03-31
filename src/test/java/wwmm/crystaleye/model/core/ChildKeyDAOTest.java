package wwmm.crystaleye.model.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class ChildKeyDAOTest {
	
	private static File fixturesRoot;
	private static String foldername = "childkeydao";
	
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
	public void testGetFolderFromKey() {
		File storageRoot = new File(fixturesRoot, "storage_root_for_get_folder");
		ChildKeyDAO pkd = new ChildKeyDAO(storageRoot);
		File keyFolder = pkd.getFolderFromKeys(1, 13);
		File expectedFile = new File(new File(storageRoot, "1"), "13");
		assertEquals(expectedFile, keyFolder);
	}
	
	@Test
	public void testInsertChildKey() {
		File storageRoot = new File(fixturesRoot, "storage_root_for_insert_keys");
		ChildKeyDAO pkd = new ChildKeyDAO(storageRoot);
		for (int i = 1; i < 10; i++) {
			int childKey = pkd.insert(1);
			assertEquals(i, childKey);
			File file = new File(new File(storageRoot, String.valueOf(1)), String.valueOf(i));
			assertTrue(file.exists());
		}
	}
	
	@Test
	public void testInsertChildKeyWhenPrimaryKeyDoesntExist() {
		File storageRoot = new File(fixturesRoot, "storage_root_for_insert_keys");
		ChildKeyDAO pkd = new ChildKeyDAO(storageRoot);
		int childKey = pkd.insert(99);
		assertEquals(-1, childKey);
	}
	
	@Test
	public void testRemoveEmptyKeyFolder() {
		File storageRoot = new File(fixturesRoot, "storage_root_for_delete_keys");
		ChildKeyDAO pkd = new ChildKeyDAO(storageRoot);
		boolean success = pkd.remove(1, 1);
		assertTrue(success);
	}
	
	@Test
	public void testRemoveNonEmptyKeyFolder() {
		File storageRoot = new File(fixturesRoot, "storage_root_for_delete_keys");
		ChildKeyDAO pkd = new ChildKeyDAO(storageRoot);
		boolean success = pkd.remove(1, 2);
		assertTrue(success);
	}
	
	@Test
	public void testRemoveChildKeyThatDoesntExist() {
		File storageRoot = new File(fixturesRoot, "storage_root_for_delete_keys");
		ChildKeyDAO pkd = new ChildKeyDAO(storageRoot);
		boolean success = pkd.remove(1, 99);
		assertFalse(success);
	}
	
	@Test
	public void testRemoveChildKeyWhosePrimaryKeyDoesntExist() {
		File storageRoot = new File(fixturesRoot, "storage_root_for_delete_keys");
		ChildKeyDAO pkd = new ChildKeyDAO(storageRoot);
		boolean success = pkd.remove(2, 1);
		assertFalse(success);
	}

}
