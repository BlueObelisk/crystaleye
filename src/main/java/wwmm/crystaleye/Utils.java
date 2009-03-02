package wwmm.crystaleye;

import static wwmm.crystaleye.CrystalEyeConstants.X_XHTML;

import java.io.BufferedReader;
import java.io.BufferedWriter;
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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.xmlcml.cml.base.CMLBuilder;

public class Utils {

	public static List<Node> queryHTML(Document doc, String xpath) {
		Node node = doc.getRootElement();
		return queryHTML(node, xpath);
	}

	public static List<Node> queryHTML(Node node, String xpath) {
		Nodes nodes = node.query(xpath, X_XHTML);
		return getNodeListFromNodes(nodes);
	}

	public static List<Node> getNodeListFromNodes(Nodes nodes) {
		List<Node> nodeList = new ArrayList<Node>(nodes.size());
		for (int i = 0; i < nodes.size(); i++) {
			nodeList.add(nodes.get(i));
		}
		return nodeList;
	}

	public static double round(double val, int places) {
		long factor = (long)Math.pow(10,places);
		val = val * factor;
		long tmp = Math.round(val);
		return (double)tmp / factor;
	}

	public static void appendToFile(File file, String content) throws IOException {
		FileWriter fw = null;
		try {
			fw = new FileWriter(file, true);
			fw.write(content);
		} finally {
			IOUtils.closeQuietly(fw);
		}
	}

	public static void writeText(String content, String fileName) {
		if (content == null) {
			throw new IllegalStateException("Content to be written is null.");
		} else if (fileName == null) {
			throw new IllegalStateException("File name is null.");
		} else {
			File parentDir = new File(fileName).getParentFile();
			if (!parentDir.exists()) {
				parentDir.mkdirs();
			}
			BufferedWriter out = null;
			try {
				out = new BufferedWriter(new FileWriter(fileName));
				out.write(content);
				out.close();
			} catch (IOException e) {
				throw new RuntimeException("Error writing text to "
						+ fileName, e);
			} finally {
				IOUtils.closeQuietly(out);
			}
		}
	}

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

	public static Document parseXml(InputStream in) {
		return parseXml(new Builder(), in);
	}

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

	public static Document parseXml(Reader reader) {
		return Utils.parseXml(new Builder(), reader);
	}

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

	public static Document parseXml(File file) {
		try {
			return Utils.parseXml(new FileReader(file));
		} catch (FileNotFoundException e) {
			throw new RuntimeException("Could not find file "+file.getAbsolutePath(), e);
		}
	}

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

	/**
	 * Writes the contents of the provided <code>InputStream</code>
	 * to the location defined by the provided <code>File</code>. 
	 * 
	 * @param in - InputStream containing the contents to be written.
	 * @param file - the location that will be written to.
	 */
	public static void writeInputStreamToFile(InputStream in, File file) {
		try {
			byte[] bytes = IOUtils.toByteArray(in);
			FileUtils.writeByteArrayToFile(file, bytes);
		} catch (IOException e) {
			throw new RuntimeException("Error writing stream to file, "+
					file.getAbsolutePath(), e);
		}		
	}

}