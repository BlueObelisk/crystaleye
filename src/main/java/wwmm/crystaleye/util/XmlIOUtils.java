package wwmm.crystaleye.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.ParsingException;
import nu.xom.Serializer;
import nu.xom.ValidityException;

import org.xmlcml.cml.base.CMLBuilder;

import wwmm.crystaleye.CrystalEyeRuntimeException;

public class XmlIOUtils {

	public static void appendToFile(File file, String content) {
		FileWriter fw = null;
		try {
			fw = new FileWriter(file, true);
			fw.write(content);
		} catch (IOException e) {
			System.err.println("IOException: " + e.getMessage());
		} finally {
			org.apache.commons.io.IOUtils.closeQuietly(fw);
		}
	}

	public static String stream2String(InputStream in) throws IOException {
		StringBuffer out = new StringBuffer();
		byte[] b = new byte[4096];
		for (int n; (n = in.read(b)) != -1;) {
			out.append(new String(b, 0, n));
		}
		return out.toString();
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
				throw new CrystalEyeRuntimeException("Error writing text to "
						+ fileName, e);
			} finally {
				try {
					if (out != null)
						out.close();
				} catch (IOException e) {
					throw new CrystalEyeRuntimeException(
							"Cannot close writer: " + out, e);
				}
			}
		}
	}

	public static void writeXML(Document doc, String fileName) {
		File writeFile = new File(fileName).getParentFile();
		if (!writeFile.exists()) {
			writeFile.mkdirs();
		}
		try {
			Serializer serializer = null;
			serializer = new Serializer(new FileOutputStream(fileName));
			serializer.write(doc);
		} catch (IOException e) {
			throw new RuntimeException("Could not write XML file to "
					+ fileName);
		}
	}

	public static void writePrettyXML(Document doc, String fileName) {
		File writeFile = new File(fileName).getParentFile();
		if (!writeFile.exists()) {
			writeFile.mkdirs();
		}
		Serializer serializer;
		try {
			serializer = new Serializer(new FileOutputStream(fileName));
			serializer.setIndent(2);
			serializer.write(doc);
		} catch (IOException e) {
			throw new RuntimeException("Could not write XML file to "
					+ fileName);
		}
	}

	public static Document parseXmlFile(File file) {
		try {
			return parseXmlFile(new FileReader(file));
		} catch (FileNotFoundException e) {
			throw new CrystalEyeRuntimeException("Could not find file "
					+ file.getAbsolutePath(), e);
		}
	}
	
	public static Document parseXmlFile(Reader reader) {
		return parseXmlFile(new Builder(), reader);
	}

	public static Document parseXmlFile(Builder builder, Reader reader) {
		Document doc;
		try {
			doc = builder.build(new BufferedReader(reader));
		} catch (ValidityException e) {
			throw new CrystalEyeRuntimeException("Invalid XML", e);
		} catch (ParsingException e) {
			throw new CrystalEyeRuntimeException("Could not parse XML", e);
		} catch (UnsupportedEncodingException e) {
			throw new CrystalEyeRuntimeException("Unsupported encoding", e);
		} catch (IOException e) {
			throw new CrystalEyeRuntimeException("Input exception", e);
		}
		return doc;
	}

	public static Document parseCmlFile(File file) {
		Document doc;
		try {
			doc = new CMLBuilder().build(new BufferedReader(
					new FileReader(file)));
		} catch (ValidityException e) {
			throw new CrystalEyeRuntimeException("File at "
					+ file.getAbsolutePath() + " is not valid XML", e);
		} catch (ParsingException e) {
			throw new CrystalEyeRuntimeException("Could not parse file at "
					+ file.getAbsolutePath(), e);
		} catch (UnsupportedEncodingException e) {
			throw new CrystalEyeRuntimeException(
					"File at " + file.getAbsolutePath()
							+ " is in an unsupported encoding", e);
		} catch (FileNotFoundException e) {
			throw new CrystalEyeRuntimeException("File at "
					+ file.getAbsolutePath() + " could not be found", e);
		} catch (IOException e) {
			throw new CrystalEyeRuntimeException("Could read file at "
					+ file.getAbsolutePath(), e);
		}
		return doc;
	}
	
}
