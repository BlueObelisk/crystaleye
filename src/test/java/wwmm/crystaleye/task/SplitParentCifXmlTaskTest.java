package wwmm.crystaleye.task;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.List;

import nu.xom.Document;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xmlcml.cif.CIF;
import org.xmlcml.cif.CIFDataBlock;
import org.xmlcml.cif.CIFException;

import wwmm.crystaleye.Utils;
import wwmm.crystaleye.model.crystaleye.ChildCifXmlFileDAO;

public class SplitParentCifXmlTaskTest {

	private static File fixturesRoot;
	private static String foldername = "splitparentcifxml";

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
	public void testRunTaskForCifWith1Structure() {
		File storageRoot = new File(fixturesRoot, "storage_root");
		int primaryKey = 1;
		SplitParentCifXmlTask task = new SplitParentCifXmlTask(storageRoot, primaryKey);
		File expectedFile = new File(storageRoot, "1/1/1"+ChildCifXmlFileDAO.getFileExtension());
		assertTrue(!expectedFile.exists());
		boolean success = task.runTask();
		assertTrue(success);
		assertTrue(expectedFile.exists());
		try {
			Document doc = Utils.parseXml(expectedFile);
			new CIF(doc, true);
		} catch (Exception e) {
			fail("Should not have thrown error parsing file: "+expectedFile);
		}
	}
	
	@Test
	public void testRunTaskForCifWithMultipleStructures() throws CIFException {
		File storageRoot = new File(fixturesRoot, "storage_root");
		int primaryKey = 4;
		SplitParentCifXmlTask task = new SplitParentCifXmlTask(storageRoot, primaryKey);
		File primaryKeyFile = new File(storageRoot, ""+primaryKey);
		boolean success = task.runTask();
		assertTrue(success);
		int folderCount = getChildFolderCount(primaryKeyFile);
		assertEquals(13, folderCount);
		File childCifXml7 = new File(primaryKeyFile, "7/7.cif.xml");
		assertTrue(childCifXml7.exists());
		CIF cif7 = new CIF(Utils.parseXml(childCifXml7), true);
		List<CIFDataBlock> dbList7 = cif7.getDataBlockList();
		assertEquals(2, dbList7.size());
		assertEquals("PhtBu2PHC6F4BF(C6F5)2", dbList7.get(0).getId());
		assertEquals("global", dbList7.get(1).getId());
		File childCifXml10 = new File(primaryKeyFile, "10/10.cif.xml");
		assertTrue(childCifXml10.exists());
		CIF cif10 = new CIF(Utils.parseXml(childCifXml10), true);
		List<CIFDataBlock> dbList10 = cif10.getDataBlockList();
		assertEquals(2, dbList10.size());
		assertEquals("Ph3P-C6F4-BF(C6F5)2.C6H5Br", dbList10.get(0).getId());
		assertEquals("global", dbList10.get(1).getId());
	}
	
	private int getChildFolderCount(File file) {
		int count = 0;
		for (File f : file.listFiles()) {
			System.out.println(f);
			if (f.isDirectory() && !f.getName().contains("svn")) {
				count++;
			}
		}
		return count;
	}

	@Test
	public void testRunTaskForNonExistingPrimaryKey() {
		File storageRoot = new File(fixturesRoot, "storage_root");
		int primaryKey = 99;
		SplitParentCifXmlTask task = new SplitParentCifXmlTask(storageRoot, primaryKey);
		boolean success = task.runTask();
		assertFalse(success);
	}

	@Test
	public void testRunTaskForExisintPrimaryKeyButNonExistantCifXml() {
		File storageRoot = new File(fixturesRoot, "storage_root");
		int primaryKey = 2;
		SplitParentCifXmlTask task = new SplitParentCifXmlTask(storageRoot, primaryKey);
		boolean success = task.runTask();
		assertFalse(success);
	}

	@Test
	public void testRunTaskForInvalidCifXml() {
		File storageRoot = new File(fixturesRoot, "storage_root");
		int primaryKey = 3;
		SplitParentCifXmlTask task = new SplitParentCifXmlTask(storageRoot, primaryKey);
		boolean success = task.runTask();
		assertFalse(success);
	}
	
}
