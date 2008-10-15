package wwmm.crystaleye.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.ParsingException;
import nu.xom.Serializer;
import nu.xom.ValidityException;

import org.apache.commons.io.IOUtils;
import org.xmlcml.cml.base.CMLBuilder;

import wwmm.crystaleye.CrystalEyeRuntimeException;

public class Utils {
	
	public static double round(double val, int places) {
		long factor = (long)Math.pow(10,places);

		// Shift the decimal the correct number of places
		// to the right.
		val = val * factor;

		// Round to the nearest integer.
		long tmp = Math.round(val);

		// Shift the decimal the correct number of places
		// back to the left.
		return (double)tmp / factor;
	}

	public static String convertFileSeparators(String filePath) {
		if ("/".equalsIgnoreCase(File.separator)) {
			filePath = filePath.replaceAll("\\\\", File.separator);
		} else if ("\\".equalsIgnoreCase(File.separator)) {
			filePath = filePath.replaceAll("/", "\\\\");
		}
		return filePath;
	}

	/**
	 * Create a zip file for many files
	 */
	public static void zipFiles(String[] fileNames, String outFileName) {
		// Create a buffer for reading the files
		byte[] buf = new byte[1024];

		try {
			// Create the ZIP file
			ZipOutputStream out = new ZipOutputStream(new FileOutputStream(
					outFileName));

			// Compress the files
			for (int i = 0; i < fileNames.length; i++) {
				FileInputStream in = new FileInputStream(fileNames[i]);

				// Add ZIP entry to output stream.
				out.putNextEntry(new ZipEntry(fileNames[i]));

				// Transfer bytes from the file to the ZIP file
				int len;
				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}

				// Complete the entry
				out.closeEntry();
				in.close();
			}

			// Complete the ZIP file
			out.close();
		} catch (IOException e) {
			System.err.println("IOException:" + e.toString());
		}
	}

	public static void appendToFile(File file, String content) throws IOException {
		FileWriter fw = null;
		try {
			fw = new FileWriter(file, true);
			fw.write(content);
		} finally {
			org.apache.commons.io.IOUtils.closeQuietly(fw);
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
			return Utils.parseXmlFile(new FileReader(file));
		} catch (FileNotFoundException e) {
			throw new CrystalEyeRuntimeException("Could not find file "
					+ file.getAbsolutePath(), e);
		}
	}

	public static Document parseXmlFile(Reader reader) {
		return Utils.parseXmlFile(new Builder(), reader);
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