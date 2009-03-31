package wwmm.crystaleye.task;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import wwmm.crystaleye.model.crystaleye.ParentCifXmlFileDAO;
import static org.junit.Assert.*;

public class SplitParentCifXmlTaskTest {

	private static File fixturesRoot;
	private static String foldername = "splitparentcifxml";

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
		// FIXME - uncomment this.
		//FileUtils.forceDelete(fixturesRoot);
	}

	@Test
	public void testRunTaskForCifWith1Structure() {
		File storageRoot = new File(fixturesRoot, "storage_root");
		int primaryKey = 1;
		SplitParentCifXmlTask task = new SplitParentCifXmlTask(storageRoot, primaryKey);
		File expectedFile = new File(storageRoot, "1/1/1"+ParentCifXmlFileDAO.getFileExtension());
		assertTrue(!expectedFile.exists());
		boolean success = task.runTask();
		assertTrue(success);
		assertTrue(expectedFile.exists());
	}
	
	@Test
	public void testRunTaskForNonExistingPrimaryKey() {
		
	}
	
	@Test
	public void testRunTaskForExisintPrimaryKeyButNonExistantCifXml() {
		
	}
	
	@Test
	public void testRunTaskForInvalidCifXml() {
		
	}
	
	

}
