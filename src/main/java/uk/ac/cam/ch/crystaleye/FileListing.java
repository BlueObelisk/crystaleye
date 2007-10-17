package uk.ac.cam.ch.crystaleye;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author javapractices.com
 * @author Alex Wong
 */
public final class FileListing {

	/**
	 * Demonstrate use.
	 */
	public static void main(String[] aArguments) throws FileNotFoundException {

		File tempDir = new File("k:/taverna/data/cif");
		List files = FileListing.byRegex( tempDir, "[^\\._]*\\.cif" );

		//print out all file names, and display the order of File.compareTo
		System.out.println(files.size());
	}

	/**
	 * Recursively walk a directory tree and return a List of all
	 * Files found that have the given MIME type; the List is sorted using File.compareTo.
	 *
	 * @param aStartingDir is a valid directory, which can be read.
	 */
	static public List<File> byMime(File aStartingDir, String mime) throws FileNotFoundException{
		validateDirectory(aStartingDir);
		List<File> result = new ArrayList<File>();

		File[] filesAndDirs = aStartingDir.listFiles();
		List<File> filesDirs = Arrays.asList(filesAndDirs);
		Iterator filesIter = filesDirs.iterator();
		File file = null;
		while ( filesIter.hasNext() ) {
			file = (File)filesIter.next();
			if (file.isFile() && file.getName().endsWith(mime)) {
				result.add(file);
			}
			//result.add(file); //always add, even if directory
			if (!file.isFile()) {
				//must be a directory
				//recursive call!
				List<File> deeperList = byMime(file, mime);
				result.addAll(deeperList);
			}

		}
		Collections.sort(result);
		return result;
	}

	/**
	 * Recursively walk a directory tree and return a List of all Files found that 
	 * match the given regex; the List is sorted using File.compareTo.
	 *
	 * @param aStartingDir is a valid directory, which can be read.
	 */
	static public List<File> byRegex(File aStartingDir, String regex) throws FileNotFoundException{
		validateDirectory(aStartingDir);
		List<File> result = new ArrayList<File>();

		File[] filesAndDirs = aStartingDir.listFiles();
		List<File> filesDirs = Arrays.asList(filesAndDirs);
		Iterator filesIter = filesDirs.iterator();
		File file = null;
		while ( filesIter.hasNext() ) {
			file = (File)filesIter.next();
			if (file.isFile() && file.getName().matches(regex)) {
				result.add(file);
			}
			//result.add(file); //always add, even if directory
			if (!file.isFile()) {
				//must be a directory
				//recursive call!
				List<File> deeperList = byRegex(file, regex);
				result.addAll(deeperList);
			}

		}
		Collections.sort(result);
		return result;
	}

	/**
	 * Directory is valid if it exists, does not represent a file, and can be read.
	 */
	static private void validateDirectory (File aDirectory) throws FileNotFoundException {
		if (aDirectory == null) {
			throw new IllegalArgumentException("Directory should not be null.");
		}
		if (!aDirectory.exists()) {
			throw new FileNotFoundException("Directory does not exist: " + aDirectory);
		}
		if (!aDirectory.isDirectory()) {
			throw new IllegalArgumentException("Is not a directory: " + aDirectory);
		}
		if (!aDirectory.canRead()) {
			throw new IllegalArgumentException("Directory cannot be read: " + aDirectory);
		}
	}
} 