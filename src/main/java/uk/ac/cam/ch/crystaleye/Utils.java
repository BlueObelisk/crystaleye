package uk.ac.cam.ch.crystaleye;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


/**
 * The class is used to define many usual routines
 *
 * @author Nick Day <ned24@cam.ac.uk>
 */
public class Utils {

	public static final int EOF = -1;

	private static String separator = "/";

	public Utils() {
		separator = System.getProperty("file.separator");
	}
	
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
	
	public static int getFileSizeInBytes(File file) {
		InputStream in;
		int total = 0;
		try {
			in = new FileInputStream(file);
			total = 0;
			while (in.read() != -1)
				total++;
		} catch (FileNotFoundException e1) {
			throw new RuntimeException("Could not find file "+file.getAbsolutePath());
		} catch (IOException e) {
			throw new RuntimeException("Exception while reading file "+file.getAbsolutePath());
		}
		return total;
	}

	public static String getFileNameWithMimes(String filePath) {
		String fileSep = "/";
		if (filePath.contains("\\")) {
			fileSep = "\\";
		}
		int idx = filePath.lastIndexOf(fileSep);
		String nameWithMime = filePath.substring(idx+1, filePath.length());
		return nameWithMime;
	}

	public static String getMimeSet(String filePath) {
		filePath = Utils.getFileNameWithMimes(filePath);
		int idx = filePath.indexOf(".");
		return filePath.substring(idx);
	}

	public static String getPathMinusMimeSet(File file) {
		String path = file.getAbsolutePath();
		String parent = file.getParent();
		String fileName = path.substring(path.lastIndexOf(File.separator)+1);
		String fileId = fileName.substring(0,fileName.indexOf("."));
		return parent+File.separator+fileId;
	}

	public static String getPathMinusMimeSet(String path) {		
		return getPathMinusMimeSet(new File(path));
	}

	public static String convertFileSeparators(String filePath) {
		if ("/".equalsIgnoreCase(File.separator)) {
			filePath = filePath.replaceAll("\\\\", File.separator);
		} else if ("\\".equalsIgnoreCase(File.separator)) {
			filePath = filePath.replaceAll("/", "\\\\");
		}
		return filePath;
	}

	public static void getFileFromURL(String u, String outputFile) throws IOException {
		File file = new File(outputFile);
		File parent = file.getParentFile();
		if (!parent.exists()) {
			parent.mkdirs();
		}
		URL url  = new URL(u);
		// Copy resource to local file, use remote file
		// if no local file name specified
		InputStream is = url.openStream();
		FileOutputStream fos=null;

		fos = new FileOutputStream(outputFile);
		int oneChar, count=0;
		while ((oneChar=is.read()) != -1) {
			fos.write(oneChar);
			count++;
		}
		is.close();
		fos.close();
	}

	/**
	 * Delete a directory
	 */
	public static boolean delDir(String dirName) {
		File d = new File(dirName);

		// Make sure the file of dir exists and isn't write protected
		if (!d.exists())
			System.err.println("Delete: No such directory: " + dirName);
		if (!d.canWrite())
			System.err.println("Delete: write protected: " + dirName);

		String[] files = d.list();
		for (int i = 0; i < files.length; i++) {
			String filePath = dirName + separator + files[i];
			System.out.println(filePath);
			File f = new File(filePath);
			if (f.isDirectory()) {
				delDir(filePath);
			} else {
				f.delete();
			}
		}

		return d.delete();
	}

	/**
	 * Copy one file to another directory
	 */
	public static void copyFile(String inputFileName, String outputFileName) {
		File inputFile = new File(inputFileName);
		File outputFile = new File(outputFileName);
		FileInputStream in = null;
		FileOutputStream out = null;
		try {
			in = new FileInputStream(inputFile);
			out = new FileOutputStream(outputFile);
			int c;
			while ((c = in.read()) != -1)
				out.write(c);

			in.close();
			out.close();
		} catch (IOException e) {
			System.err.println("IOException:" + e.toString());
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					throw new RuntimeException("Could not close file input stream: "+in);
				}
			}
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					throw new RuntimeException("Could not close file output stream: "+out);
				}
			}
		}
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

	/**
	 * Put a string in to a file
	 */
	public static void string2File(String content, String fileName) {
		try {
			File file = new File(fileName);
			FileWriter out = new FileWriter(file);
			out.write(content);
			out.close();
		} catch (IOException e) {
			System.err.println("Unhandled exception:" + e.toString());
		}
	}

	/**
	 * Get the content of a file and put it in a byte array
	 */
	public static String file2String(String fileName) {
		String content = "";
		try {
			File file = new File(fileName);
			FileInputStream insr = new FileInputStream(file);

			byte[] fileBuffer = new byte[(int) file.length()];
			insr.read(fileBuffer);
			insr.close();
			content = new String(fileBuffer);
		} catch (Exception e) {
			System.err.println("Unhandled exception:");
			e.printStackTrace();
		}

		return content;
	}

	/**
	 * Put a byte array in to a file
	 */
	public static void byteArray2File(byte[] content, String fileName) {
		try {
			FileOutputStream ostr = new FileOutputStream(new File(fileName));

			BufferedOutputStream bstr = new BufferedOutputStream( ostr );

			bstr.write( content, 0, content.length);
			bstr.close();
		} catch (FileNotFoundException fnfe) {
			System.err.println("Fatal: " + fnfe);
		} catch (IOException ioe) {
			System.err.println("Fatal: " + ioe);
		}
	}

	/**
	 * Get the content of a file and put it in a string
	 */
	public static byte[] file2ByteArray(String fileName) {
		byte[] content = null;
		try {
			File file = new File(fileName);
			FileInputStream insr = new FileInputStream(file);

			content = new byte[(int) file.length()];
			insr.read(content);
			insr.close();
		} catch (Exception e) {
			System.err.println("Unhandled exception:");
			e.printStackTrace();
		}

		return content;
	}
}