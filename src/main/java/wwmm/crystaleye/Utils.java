package wwmm.crystaleye;

import static wwmm.pubcrawler.CrawlerConstants.X_XHTML;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Node;
import nu.xom.Nodes;
import nu.xom.ParsingException;
import nu.xom.Serializer;
import nu.xom.ValidityException;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.xmlcml.cml.base.CMLBuilder;

/**
 * <p>
 * Utility methods used throughout CrystalEye.
 * </p>
 * 
 * @author Nick Day
 * @version 1.1
 */
public class Utils {

	private static final Logger LOG = Logger.getLogger(Utils.class);

	/**
	 * <p>
	 * The <code>sleep</code> method is called by all methods in this class which 
	 * call a method in the superclass.  A random number between 0 and 
	 * <code>maxSleep</code> is used to determine how long a period the process 
	 * should sleep for before continuing.
	 * </p>
	 */
	public static void sleep(int maxSleep) {
		int maxTime = Integer.valueOf(maxSleep);
		try {
			Thread.sleep(((int) (maxTime * Math.random())));
		} catch (InterruptedException e) {
			LOG.debug("Sleep interrupted.");
		}
	}

	/**
	 * <p>
	 * Convenience method for doing an XPath query and receiving 
	 * back a list of <code>Node</code> rather than the <code>Nodes</code>
	 * returned in the default in XOM.
	 * </p>
	 * 
	 * @param doc you want to query
	 * @param xpath - the XPath String you want to use in the query.
	 * 
	 * @return list of <code>Node</code> that represent the parts of
	 * the queried XML document that matched the provided XPath query.
	 */
	public static List<Node> queryHTML(Document doc, String xpath) {
		Node node = doc.getRootElement();
		return queryHTML(node, xpath);
	}

	/**
	 * <p>
	 * Convenience method for doing an XPath query and receiving 
	 * back a list of <code>Node</code> rather than the <code>Nodes</code>
	 * returned in the default in XOM.
	 * </p>
	 * 
	 * @param node you want to query
	 * @param xpath - the XPath String you want to use in the query.
	 * 
	 * @return list of <code>Node</code> that represent the parts of
	 * the queried XML document that matched the provided XPath query.
	 */
	public static List<Node> queryHTML(Node node, String xpath) {
		Nodes nodes = node.query(xpath, X_XHTML);
		return getNodeListFromNodes(nodes);
	}

	/**
	 * <p>
	 * Convenience method for getting a list of <code>Node</code>
	 * from <code>Nodes</code>.
	 * </p>
	 * 
	 * @param nodes you want converted to a list.
	 * 
	 * @return list of <Node> containing the same XML elemenst as 
	 * the provided <code>Nodes</code>.
	 */
	public static List<Node> getNodeListFromNodes(Nodes nodes) {
		List<Node> nodeList = new ArrayList<Node>(nodes.size());
		for (int i = 0; i < nodes.size(); i++) {
			nodeList.add(nodes.get(i));
		}
		return nodeList;
	}

	/**
	 * <p>
	 * Convenience method for rounding a <code>double</code>.
	 * </p>
	 * 
	 * @param val - the number you want rounded.
	 * @param places - the number of decimal places you want
	 * the provided number rounded to.
	 * 
	 * @return the rounded number as a <code>double</code>.
	 */
	public static double round(double val, int places) {
		long factor = (long)Math.pow(10,places);
		val = val * factor;
		long tmp = Math.round(val);
		return (double)tmp / factor;
	}

	/**
	 * <p>
	 * Convenience method for appending a String to a File.
	 * </p>
	 * 
	 * @param file you want the String appended to.
	 * @param content you want to append to the File.
	 * 
	 * @throws IOException if there is a problem reading or writing
	 * the file.
	 */
	public static void appendToFile(File file, String content) throws IOException {
		FileWriter fw = null;
		try {
			fw = new FileWriter(file, true);
			fw.write(content);
		} finally {
			IOUtils.closeQuietly(fw);
		}
	}

	/**
	 * <p>
	 * Writes the provided XML out to the file defined by the
	 * provided filename.
	 * </p>
	 * 
	 * @param doc - the XML to be written.
	 * @param fileName of the file to be written to.
	 */
	public static void writeXML(Document doc, String fileName) {
		File writeFile = new File(fileName).getParentFile();
		if (!writeFile.exists()) {
			writeFile.mkdirs();
		}
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(fileName);
			Serializer serializer = null;
			serializer = new Serializer(fos);
			serializer.write(doc);
		} catch (IOException e) {
			throw new RuntimeException("Could not write XML file to "+fileName);
		} finally {
			IOUtils.closeQuietly(fos);
		}
	}

	/**
	 * <p>
	 * Writes the provided XML out to the file defined by the
	 * provided filename complete with indentations to make the
	 * XML more human readable.
	 * </p>
	 * 
	 * @param doc - the XML to be written.
	 * @param fileName of the file to be written to.
	 */
	public static void writePrettyXML(Document doc, String fileName) {
		File writeFile = new File(fileName).getParentFile();
		if (!writeFile.exists()) {
			writeFile.mkdirs();
		}
		Serializer serializer;
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(fileName);
			serializer = new Serializer(fos);
			serializer.setIndent(2);
			serializer.write(doc);
		} catch (IOException e) {
			throw new RuntimeException("Could not write XML file to "+fileName);
		} finally {
			IOUtils.closeQuietly(fos);
		}
	}

	/**
	 * <p>
	 * Writes the provided XML out to the provided OutputStream complete 
	 * with indentations to make the XML more human readable.
	 * </p>
	 * 
	 * @param doc to be written to the OutputStream.
	 * @param out - the OutputStream to be written to.
	 * 
	 * @throws IOException if there is a problem writing the XML to the
	 * OutputStream. 
	 */
	public static void writePrettyXML(Document doc, OutputStream out) throws IOException {
		Serializer serializer = null;
		serializer = new Serializer(out);
		serializer.setIndent(2);
		serializer.write(doc);
	}
	
	/**
	 * <p>
	 * Converts an XML document to a pretty XML String.
	 * </p>
	 * 
	 * @param doc to be converted.
	 * 
	 * @return String form of the provided XML document.
	 */
	public static String toPrettyXMLString(Document doc) {
		OutputStream baos = null;
		Serializer serializer = null;
		try {
			baos = new ByteArrayOutputStream();
			serializer = new Serializer(baos);
			serializer.setIndent(2);
			serializer.write(doc);
		} catch (IOException e) {
			throw new RuntimeException("Could not write XML file to ByteArrayOutputStream.");
		} finally {
			IOUtils.closeQuietly(baos);
		}
		return baos.toString();
	}

	/**
	 * <p>
	 * Parses the contents of an <code>InputStream</code> into an
	 * XML document.
	 * </p>
	 * 
	 * @param in - the InputStream you want converted to XML.
	 * 
	 * @return XML document representing the contents of the 
	 * provided InputStream.
	 */
	public static Document parseXml(InputStream in) {
		return parseXml(new Builder(), in);
	}

	/**
	 * <p>
	 * Parses the contents of an <code>InputStream</code> into an
	 * XML document using a specified XML builder.
	 * </p>
	 * 
	 * @param builder - the XML builder you want the parser to use
	 * in creating the XML document.
	 * @param in - the InputStream you want converted to XML.
	 * 
	 * @return XML document representing the contents of the 
	 * provided InputStream.
	 */
	public static Document parseXml(Builder builder, InputStream in) {
		Document doc;
		try {
			doc = builder.build(in);
		} catch (ValidityException e) {
			throw new RuntimeException("Invalid XML", e);
		} catch (ParsingException e) {
			throw new RuntimeException("Could not parse XML", e);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Unsupported encoding", e);
		} catch (IOException e) {
			throw new RuntimeException("Input exception", e);
		}
		return doc;
	}

	/**
	 * <p>
	 * Parses the contents of a <code>Reader</code> into an
	 * XML document.
	 * </p>
	 * 
	 * @param reader - the Reader you want converted to XML.
	 * 
	 * @return XML document representing the contents of the 
	 * provided Reader.
	 */
	public static Document parseXml(Reader reader) {
		return Utils.parseXml(new Builder(), reader);
	}

	/**
	 * <p>
	 * Parses the contents of a <code>Reader</code> into an
	 * XML document using a specified XML builder.
	 * </p>
	 * 
	 * @param builder - the XML builder you want the parser to use
	 * in creating the XML document.
	 * @param reader - the Reader you want converted to XML.
	 * 
	 * @return XML document representing the contents of the 
	 * provided Reader.
	 */
	public static Document parseXml(Builder builder, Reader reader) {
		Document doc;
		try {
			doc = builder.build(reader);
		} catch (ValidityException e) {
			throw new RuntimeException("Invalid XML", e);
		} catch (ParsingException e) {
			throw new RuntimeException("Could not parse XML", e);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Unsupported encoding", e);
		} catch (IOException e) {
			throw new RuntimeException("Input exception", e);
		}
		return doc;
	}

	/**
	 * <p>
	 * Parses the contents of the provided <code>File</code> 
	 * into an XML document.
	 * </p>
	 * 
	 * @param file you want to be parsed.
	 * 
	 * @return XML document containing the contents of the file.
	 */
	public static Document parseXml(File file) {
		try {
			return Utils.parseXml(new FileReader(file));
		} catch (FileNotFoundException e) {
			throw new RuntimeException("Could not find file "+file.getAbsolutePath(), e);
		}
	}

	/**
	 * <p>
	 * Parses the contents of the provided <code>File</code> 
	 * into a CML document.
	 * </p>
	 * 
	 * @param file you want to be parsed.
	 * 
	 * @return CML document containing the contents of the file.
	 */
	public static Document parseCml(File file) {
		Document doc;
		try {
			doc = new CMLBuilder().build(new BufferedReader(
					new FileReader(file)));
		} catch (ValidityException e) {
			throw new RuntimeException("File at "
					+ file.getAbsolutePath() + " is not valid XML", e);
		} catch (ParsingException e) {
			throw new RuntimeException("Could not parse file at "
					+ file.getAbsolutePath(), e);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(
					"File at " + file.getAbsolutePath()
					+ " is in an unsupported encoding", e);
		} catch (FileNotFoundException e) {
			throw new RuntimeException("File at "
					+ file.getAbsolutePath() + " could not be found", e);
		} catch (IOException e) {
			throw new RuntimeException("Could read file at "
					+ file.getAbsolutePath(), e);
		}
		return doc;
	}

}