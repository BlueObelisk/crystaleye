package wwmm.crystaleye.model.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;

import nu.xom.Document;
import nu.xom.Element;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xmlcml.cml.element.CMLCml;
import org.xmlcml.cml.element.CMLMolecule;

import wwmm.crystaleye.Utils;

public class ChildDerivedCmlFileDAOTest {
	
	private static File fixturesRoot;
	private static String foldername = "childderivedcmlfiledao";
	
	private static final Logger LOG = Logger.getLogger(ParentCifFileDAOTest.class);

	/**
	 * <p>
	 * The tests for DAO classes will create various folders and files.  
	 * We don't want to alter the fixtures in ./src/main/resources, 
	 * so the folder is copied over to the ./target directory and 
	 * the tests are run on that.  The directory is removed by the 
	 * method tagged with @AfterClass.
	 * </p>
	 */
	@BeforeClass
	public static void setUpTestStorageRoot() throws IOException {
		File target = new File("./target");
		File fixturesSrc = new File("./src/test/resources/model/"+foldername);
		fixturesRoot = new File(target, foldername);
		FileUtils.deleteDirectory(fixturesRoot);
		LOG.debug("Creating directory at: "+fixturesRoot.getAbsolutePath());
		FileUtils.copyDirectory(fixturesSrc, fixturesRoot);
	}

	/**
	 * <p>
	 * Remove the directory that was copied over to the ./target folder
	 * in <code>setUpTestStorageRoot</code>.
	 * </p>
	 */
	@AfterClass
	public static void removeTestStorageRoot() throws IOException {
		LOG.debug("Deleting directory at: "+fixturesRoot.getAbsolutePath());
		FileUtils.deleteDirectory(fixturesRoot);
	}
	
	@Test
	public void testGetCml() {
		File storageRoot = new File(fixturesRoot, "storage_root");
		ChildDerivedCmlFileDAO dao = new ChildDerivedCmlFileDAO(storageRoot);
		int primaryKey = 1;
		int childKey = 1;
		CMLCml cml = dao.getCml(primaryKey, childKey);
		assertNotNull(cml);
		String cmlStr = cml.toXML();
		String expectedCmlStr = "<cml xmlns=\"http://www.xml-cml.org/schema\" id=\"theCmlId\" />";
		assertEquals(expectedCmlStr, cmlStr);
	}
	
	@Test
	public void testGetDocument() {
		File storageRoot = new File(fixturesRoot, "storage_root");
		ChildDerivedCmlFileDAO dao = new ChildDerivedCmlFileDAO(storageRoot);
		int primaryKey = 1;
		int childKey = 1;
		Document doc = dao.getDocument(primaryKey, childKey);
		assertNotNull(doc);
		String docStr = doc.toXML();
		String expectedDocStr = "<?xml version=\"1.0\"?>\n<cml xmlns=\"http://www.xml-cml.org/schema\" id=\"theCmlId\" />\n";
		assertEquals(expectedDocStr, docStr);
	}
	
	@Test
	public void testGetContainerMolecule() {
		File storageRoot = new File(fixturesRoot, "storage_root");
		ChildDerivedCmlFileDAO dao = new ChildDerivedCmlFileDAO(storageRoot);
		int primaryKey = 1;
		int childKey = 2;
		CMLMolecule containerMolecule = dao.getContainerMolecule(primaryKey, childKey);
		assertNotNull(containerMolecule);
		String molStr = containerMolecule.toXML();
		String expectedMolStr = "<molecule id=\"container\">\n"+
			"<molecule id=\"sub1\" />\n"+
			"<molecule id=\"sub2\" />\n"+
			"</molecule>";
		assertEquals(expectedMolStr, molStr);
	}
	
	@Test
	public void testGetChildMolecules() {
		File storageRoot = new File(fixturesRoot, "storage_root");
		ChildDerivedCmlFileDAO dao = new ChildDerivedCmlFileDAO(storageRoot);
		int primaryKey = 1;
		int childKey = 2;
		List<CMLMolecule> molList = dao.getChildMolecules(primaryKey, childKey);
		assertEquals(2, molList.size());
		assertEquals("<molecule id=\"sub1\" />", molList.get(0).toXML());
		assertEquals("<molecule id=\"sub2\" />", molList.get(1).toXML());
		
		int primaryKey2 = 1;
		int childKey2 = 3;
		List<CMLMolecule> molList2 = dao.getChildMolecules(primaryKey2, childKey2);
		assertEquals(1, molList2.size());
		assertEquals("<molecule id=\"container\" />", molList2.get(0).toXML());
	}
	
	@Test
	public void testInsertInchi() {
		File storageRoot = new File(fixturesRoot, "storage_root");
		ChildDerivedCmlFileDAO dao = new ChildDerivedCmlFileDAO(storageRoot);
		int primaryKey = 1;
		int childKey = 6;
		String inchi = "inchi=1/C6H6/c1-2-4-6-5-3-1/h1-6H";
		String moleculeId = "this_is_an_id";
		boolean success = dao.insertInchi(primaryKey, childKey, moleculeId, inchi);
		assertTrue(success);
		File cmlFile = new File(storageRoot, "1/6/6.derived.cml");
		String cmlStr = Utils.parseCml(cmlFile).toXML();
		String expectedCmlStr = "<?xml version=\"1.0\"?>\n"+ 
			"<cml xmlns=\"http://www.xml-cml.org/schema\" id=\"theCmlId\">\n"+
			"  <molecule id=\"this_is_an_id\">\n"+
			"    <identifier convention=\"iupac:inchi\">inchi=1/C6H6/c1-2-4-6-5-3-1/h1-6H</identifier>\n"+
			"  </molecule>\n"+
			"</cml>\n";
		assertEquals(expectedCmlStr, cmlStr);
	}
	
	@Test
	public void testInsertSmiles() {
		File storageRoot = new File(fixturesRoot, "storage_root");
		ChildDerivedCmlFileDAO dao = new ChildDerivedCmlFileDAO(storageRoot);
		int primaryKey = 1;
		int childKey = 4;
		String inchi = "c1ccccc1";
		String moleculeId = "this_is_an_id";
		boolean success = dao.insertSmiles(primaryKey, childKey, moleculeId, inchi);
		assertTrue(success);
		File cmlFile = new File(storageRoot, "1/4/4.derived.cml");
		String cmlStr = Utils.parseCml(cmlFile).toXML();
		String expectedCmlStr = "<?xml version=\"1.0\"?>\n"+ 
			"<cml xmlns=\"http://www.xml-cml.org/schema\" id=\"theCmlId\">\n"+
			"  <molecule id=\"this_is_an_id\">\n"+
			"    <identifier convention=\"openbabel:smiles\">c1ccccc1</identifier>\n"+
			"  </molecule>\n"+
			"</cml>\n";
		assertEquals(expectedCmlStr, cmlStr);
	}
	
	@Test
	public void testInsertCheckcifXml() {
		File storageRoot = new File(fixturesRoot, "storage_root");
		ChildDerivedCmlFileDAO dao = new ChildDerivedCmlFileDAO(storageRoot);
		int primaryKey = 1;
		int childKey = 5;
		Element checkcifXmlRoot = new Element("checkcif");
		boolean success = dao.insertCheckcifXml(primaryKey, childKey, checkcifXmlRoot);
		assertTrue(success);
		File cmlFile = new File(storageRoot, "1/5/5.derived.cml");
		String cmlStr = Utils.parseCml(cmlFile).toXML();
		String expectedCmlStr = "<?xml version=\"1.0\"?>\n"+
			"<cml xmlns=\"http://www.xml-cml.org/schema\" id=\"this_is_an_id\">\n"+
			"  <checkcif xmlns=\"\" />\n"+
			"</cml>\n";
		assertEquals(expectedCmlStr, cmlStr);
	}

}
