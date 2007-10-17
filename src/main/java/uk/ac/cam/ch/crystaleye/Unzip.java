package uk.ac.cam.ch.crystaleye;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Unzip {

	public static final void copyInputStream(InputStream in, OutputStream out)
	throws IOException
	{
		byte[] buffer = new byte[1024];
		int len;

		while((len = in.read(buffer)) >= 0)
			out.write(buffer, 0, len);

		in.close();
		out.close();
	}

	public static final void main(String[] args) {
		Enumeration entries;
		ZipFile zipFile;

		if(args.length != 1 && args.length != 2) {
			System.err.println("Usage: Unzip zipfile");
			return;
		}

		try {
			zipFile = new ZipFile(args[0]);
			File file = new File(args[0]);
			
			String name = "";
			if (args.length == 2) {
				name = args[1];
			}
			
			String folder = file.getParentFile().getAbsolutePath();
			entries = zipFile.entries();

			while(entries.hasMoreElements()) {
				ZipEntry entry = (ZipEntry)entries.nextElement();

				if (args.length == 2) {
					if (!entry.getName().equals(name)) {
						continue;
					}
				}
				
				if(entry.isDirectory()) {
					// Assume directories are stored parents first then children.
					System.err.println("Extracting directory: " + entry.getName());
					// This is not robust, just for demonstration purposes.
					(new File(entry.getName())).mkdir();
					continue;
				}

				System.err.println("Extracting file: " + entry.getName());
				copyInputStream(zipFile.getInputStream(entry),
						new BufferedOutputStream(new FileOutputStream(folder+File.separator+entry.getName())));
			}

			zipFile.close();
		} catch (IOException ioe) {
			System.err.println("Unhandled exception:");
			ioe.printStackTrace();
			return;
		}
	}

}