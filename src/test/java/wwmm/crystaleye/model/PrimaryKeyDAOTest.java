package wwmm.crystaleye.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class PrimaryKeyDAOTest {
	
	private static File fixturesRoot;
	private static String foldername = "primarykeydao";
	
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
	public void testGetFileFromKeyThatExists() {
		File storageRoot = new File(fixturesRoot, "storage_root_with_countfile_at_2");
		PrimaryKeyDAO pkd = new PrimaryKeyDAO(storageRoot);
		int key = 1;
		File file = pkd.getFileFromKey(key);
		assertNotNull(file);
		assertEquals(new File(storageRoot, ""+key), file);
	}
	
	@Test
	public void testGetFileFromKeyThatDoesNotExist() {
		File storageRoot = new File(fixturesRoot, "storage_root_with_countfile_at_2");
		PrimaryKeyDAO pkd = new PrimaryKeyDAO(storageRoot);
		int key = 999;
		File file = pkd.getFileFromKey(key);
		assertEquals(null, file);
	}
	
	@Test
	public void testCountFileNotAnInteger() {
		File storageRoot = new File(fixturesRoot, "storage_root_with_countfile_not_an_int");
		File countFile = new File(storageRoot, PrimaryKeyDAO.KEY_COUNT_FILENAME);
		// first assert countfile exists 
		assertTrue(countFile.exists());
		PrimaryKeyDAO pkd = new PrimaryKeyDAO(storageRoot);
		// now, as countfile does not contain an int, inserting
		// a primary key should fail
		try {
			pkd.insertPrimaryKey();
			fail("Inserting a primary key should have failed, " +
			"as the count file does not contain an int");
		} catch (RuntimeException e ) {
			assertTrue("Failed as expected.", true);
		}
	}

	@Test
	public void testInsert9Keys() throws IOException {
		File storageRoot = new File(fixturesRoot, "storage_root_with_countfile_at_2");
		File countFile = new File(storageRoot, PrimaryKeyDAO.KEY_COUNT_FILENAME);
		PrimaryKeyDAO pkd = new PrimaryKeyDAO(storageRoot);
		for (int i = 0; i < 9; i++) {
			pkd.insertPrimaryKey();
		}
		// assert that 10 key folders now exist
		for (int i = 1; i < 11; i++) {
			File keyFile = new File(storageRoot, ""+i);
			assertTrue(keyFile.exists());
		}
		String count = FileUtils.readFileToString(countFile);
		assertEquals("11", count);
		
		// perform cleanup
		for (int i = 2; i < 11; i++) {
			File keyFile = new File(storageRoot, ""+i);
			FileUtils.forceDelete(keyFile);
		}
		FileUtils.writeStringToFile(countFile, "2");
	}

	@Test
	public void testInsertKeyWithCountfileAt13() throws IOException {
		File storageRoot = new File(fixturesRoot, "storage_root_with_countfile_at_13");
		File countFile = new File(storageRoot, PrimaryKeyDAO.KEY_COUNT_FILENAME);
		PrimaryKeyDAO pkd = new PrimaryKeyDAO(storageRoot);
		pkd.insertPrimaryKey();
		// count should now be at '14' and folder for '13' should have
		// been added to storageRoot
		String count2 = FileUtils.readFileToString(countFile);
		assertEquals("14", count2);
		File key13File = new File(storageRoot, "13");
		assertTrue(key13File.exists());

		// perform clean up
		FileUtils.forceDelete(key13File);
		FileUtils.writeStringToFile(countFile, "13");
	}

	@Test
	public void testInsertKeyWhenNoCountfileBut3Keys() throws IOException {
		File storageRoot = new File(fixturesRoot, "storage_root_with_no_countfile_but_3keys");
		File countFile = new File(storageRoot, PrimaryKeyDAO.KEY_COUNT_FILENAME);
		PrimaryKeyDAO pkd = new PrimaryKeyDAO(storageRoot);
		pkd.insertPrimaryKey();
		// assert that countfile has been created
		assertTrue(countFile.exists());
		// assert that next primary key will be 5
		String count = FileUtils.readFileToString(countFile);
		assertEquals("5", count);
		// assert that there is one folder for each of the 4 primary keys
		for (int i = 1; i <= 4; i++) {
			File keyFile = new File(storageRoot, ""+i);
			assertTrue(keyFile.exists());
		}

		// perform clean up
		File key4 = new File(storageRoot, "4");
		FileUtils.forceDelete(key4);
		FileUtils.forceDelete(countFile);
	}

	@Test
	public void testInsertKeyWhenNoCountfileAndNoKeys() throws IOException {
		File storageRoot = new File(fixturesRoot, "storage_root_with_no_countfile_or_keys");
		File countFile = new File(storageRoot, PrimaryKeyDAO.KEY_COUNT_FILENAME);
		// countfile should not exist
		assertTrue(!countFile.exists());
		PrimaryKeyDAO pkd = new PrimaryKeyDAO(storageRoot);
		pkd.insertPrimaryKey();
		// countfile should now exist with a value of 2
		// (as 1 has just been inserted.
		assertTrue(countFile.exists());
		String count = FileUtils.readFileToString(countFile);
		assertEquals("2", count);
		File firstKeyFile = new File(storageRoot, "1");
		assertTrue(firstKeyFile.exists());
		// assert no children have been created for key file
		assertEquals(0, firstKeyFile.listFiles().length);

		// perform cleanup
		FileUtils.forceDelete(countFile);
		FileUtils.forceDelete(firstKeyFile);
	}

}
