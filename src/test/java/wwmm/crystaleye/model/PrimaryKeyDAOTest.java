package wwmm.crystaleye.model;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

public class PrimaryKeyDAOTest {

	private final String rootPrefix = "./src/test/resources/model/primarykeydao/";

	@Test
	public void testCountFileNotAnInteger() {
		String root = rootPrefix+"storage_root_with_countfile_not_an_int";
		File storageRoot = new File(root);
		File countFile = new File(root, PrimaryKeyDAO.KEY_COUNT_FILENAME);
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
		String root = rootPrefix+"storage_root_with_countfile_at_2";
		File storageRoot = new File(root);
		// assert storage root contains 2 files (count file and folder '1')
		// if this fails, then it is possible that the files created
		// at the end of this method have not been cleaned up properly.
		assertEquals(2, storageRoot.listFiles().length);
		File countFile = new File(root, PrimaryKeyDAO.KEY_COUNT_FILENAME);
		// set count to desired start value
		FileUtils.writeStringToFile(countFile, "2");
		PrimaryKeyDAO pkd = new PrimaryKeyDAO(storageRoot);
		for (int i = 0; i < 9; i++) {
			pkd.insertPrimaryKey();
		}
		assertEquals(11, storageRoot.listFiles().length);
		String count = FileUtils.readFileToString(countFile);
		assertEquals("11", count);
		// perform cleanup
		for (int i = 2; i < 11; i++) {
			File keyFile = new File(storageRoot, ""+i);
			FileUtils.forceDelete(keyFile);
		}
	}

	@Test
	public void testInsertKeyWithCountfileAt13() throws IOException {
		String root = rootPrefix+"storage_root_with_countfile_at_13";
		File storageRoot = new File(root);
		// assert storage root contains 1 file
		// if this fails, then it is possible that the files created
		// at the end of this method have not been cleaned up properly.
		assertEquals(1, storageRoot.listFiles().length);
		File countFile = new File(root, PrimaryKeyDAO.KEY_COUNT_FILENAME);
		// first assert countfile exists and set contents to '13'
		assertTrue(countFile.exists());
		FileUtils.writeStringToFile(countFile, "13");
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
		String root = rootPrefix+"storage_root_with_no_countfile_but_3keys";
		File storageRoot = new File(root);
		// assert storage root contains 3 folders
		// if this fails, then it is possible that the files created
		// at the end of this method have not been cleaned up properly.
		assertEquals(3, storageRoot.listFiles().length);
		File countFile = new File(root, PrimaryKeyDAO.KEY_COUNT_FILENAME);
		// first assert countfile does not exist 
		assertTrue(!countFile.exists());
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
		String root = rootPrefix+"storage_root_with_no_countfile_or_keys";
		File storageRoot = new File(root);
		// assert storage root does not contain any files or folders
		// if this fails, then it is possible that the files created
		// at the end of this method have not been cleaned up properly.
		assertEquals(0, storageRoot.listFiles().length);
		File countFile = new File(root, PrimaryKeyDAO.KEY_COUNT_FILENAME);
		// first assert countfile does not exist 
		assertTrue(!countFile.exists());
		PrimaryKeyDAO pkd = new PrimaryKeyDAO(storageRoot);
		pkd.insertPrimaryKey();
		// countfile should not exist with a value of 2
		// (as 1 has just been inserted.
		assertTrue(countFile.exists());
		String count = FileUtils.readFileToString(countFile);
		assertEquals("2", count);
		// two files should have been created, the countfile, and one 
		// for the first primary key
		assertEquals(2, storageRoot.listFiles().length);
		File firstKeyFile = new File(storageRoot, "1");
		assertTrue(firstKeyFile.exists());
		// assert no children have been created for key file
		assertEquals(0, firstKeyFile.listFiles().length);

		// perform cleanup
		FileUtils.forceDelete(countFile);
		FileUtils.forceDelete(firstKeyFile);
	}

}
