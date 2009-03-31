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
import org.xmlcml.cif.CIF;
import org.xmlcml.cif.CIFException;

import wwmm.crystaleye.Utils;
import wwmm.crystaleye.model.crystaleye.ParentCifXmlFileDAO;

public class ParentCif2ParentCifXmlTaskTest {
	
	private static File fixturesRoot;
	private static String foldername = "parentcif2parentcifxml";
	
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
		FileUtils.forceDelete(fixturesRoot);
	}
	
	@Test
	public void testRunTask() {
		File storageRoot = new File(fixturesRoot, "storage_root");
		int primaryKey = 1;
		ParentCif2ParentCifXmlTask task = new ParentCif2ParentCifXmlTask(storageRoot, primaryKey);
		File expectedCifXmlFile = new File(new File(storageRoot, ""+primaryKey), primaryKey+ParentCifXmlFileDAO.getFileExtension());
		assertTrue(!expectedCifXmlFile.exists());
		boolean success = task.runTask();
		assertTrue(success);
		assertTrue(expectedCifXmlFile.exists());
		// parse just to check it has right contents
		Document doc = Utils.parseXml(expectedCifXmlFile);
		try {
			new CIF(doc, true);
		} catch (CIFException e) {
			fail("Parsing the CIF file should not fail: "+expectedCifXmlFile);
		}
	}
	
	@Test
	public void testRunTaskForNonExistantKey() {
		File storageRoot = new File(fixturesRoot, "storage_root");
		int primaryKey = 99;
		ParentCif2ParentCifXmlTask task = new ParentCif2ParentCifXmlTask(storageRoot, primaryKey);
		boolean success = task.runTask();
		assertFalse(success);
	}
	
	@Test
	public void testRunTaskForExisingKeyButNonExistantCIF() {
		File storageRoot = new File(fixturesRoot, "storage_root");
		int primaryKey = 3;
		ParentCif2ParentCifXmlTask task = new ParentCif2ParentCifXmlTask(storageRoot, primaryKey);
		boolean success = task.runTask();
		assertFalse(success);
	}
	
	@Test
	public void testRunTaskForInvalidCif() {
		File storageRoot = new File(fixturesRoot, "storage_root");
		int primaryKey = 2;
		ParentCif2ParentCifXmlTask task = new ParentCif2ParentCifXmlTask(storageRoot, primaryKey);
		boolean success = task.runTask();
		assertFalse(success);
	}

}
