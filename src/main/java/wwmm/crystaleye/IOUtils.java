package wwmm.crystaleye;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Serializer;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.xmlcml.cml.base.CMLBuilder;

public class IOUtils {

	private static final Logger LOG = Logger.getLogger(IOUtils.class);

	public static void appendToFile(File file, String content) {
		try {
			FileWriter fw = new FileWriter(file, true);
			fw.write(content);
			fw.close();
		} catch(IOException e) {
			LOG.warn("IOException: "+e.getMessage());
		}
	}

	public static void writeText(File file, String content) { 
		try {
			FileUtils.writeStringToFile(file, content);
		} catch (IOException e) {
			throw new RuntimeException("Exception writing to file: "+file, e);
		}
	}

	public static void writeXML(Document doc, String fileName)  {
		File writeFile = new File(fileName).getParentFile();
		if (!writeFile.exists()) {
			writeFile.mkdirs();
		}
		try {
			Serializer serializer = new Serializer(new FileOutputStream(fileName));
			serializer.setIndent(2);
			serializer.write(doc);
		} catch (IOException e) {
			throw new RuntimeException("Could not write XML file to "+fileName);
		}
	}

	public static Document parseXml(String filePath) {
		return parseXml(new File(filePath));
	}

	public static Document parseXml(File file) {
		try {
			return new Builder().build(file);
		} catch (Exception e) {
			throw new RuntimeException("Exception parsing XML file ("+file+"), due to: "+e.getMessage(), e);
		}
	}
	
	public static Document parseXml(Reader reader) {
		Document doc;
		BufferedReader br = null;
		try {
			br = new BufferedReader(reader);
			doc = new Builder().build(br);
		} catch (Exception e) {
			throw new RuntimeException("Exception parsing XML due to: "+e.getMessage(), e);
		} finally {
			org.apache.commons.io.IOUtils.closeQuietly(br);
		}
		return doc;
	}
	
	public static Document parseUnvalidatedXml(File file) {
		org.apache.xerces.parsers.SAXParser xmlReader =  new org.apache.xerces.parsers.SAXParser() ;
		try {
			xmlReader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			return new Builder(xmlReader).build(file);
		} catch (Exception e) {
			throw new RuntimeException("Exception whilse parsing XML, due to: "+e.getMessage(), e);
		}
	}

	public static Document parseCml(String filePath) {
		return parseCml(new File(filePath));
	}

	public static Document parseCml(File file) {
		try {
			return new CMLBuilder().build(file);
		} catch (Exception e) {
			throw new RuntimeException("Exception parsing CML file due to: "+e.getMessage(), e);
		}
	}

}
