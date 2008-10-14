package ned24.sandbox.crystaleye;

import java.io.File;

import wwmm.crystaleye.Utils;

public class ClearUnwantedFiles {
	public static void main(String[] args) {
		for (int i = 0 ; i < 1; i++) {
			String dir = "e:\\crystaleye-test\\data";
			File file = new File(dir);
			ClearUnwantedFiles cuf = new ClearUnwantedFiles();
			cuf.clearDataFolder(file);
		}
	}

	public void clearDataFolder(File file) {
		for (File publisherFolder : file.listFiles()) {
			for (File journalFolder : publisherFolder.listFiles()) {
				for (File yearFolder : journalFolder.listFiles()) {
					for (File issueFolder : yearFolder.listFiles()) {
						File[] articleFolders = issueFolder.listFiles();
						for (File articleFolder : articleFolders) {
							File[] s = articleFolder.listFiles();
							for (File fi : s) {
								System.out.println(fi.getAbsolutePath());
								String path = fi.getAbsolutePath();
								if (!(path.endsWith(".doi") || path.endsWith(".cif") || path.endsWith(".checkcif.html"))) {	
									boolean success = false; 
									if (fi.isDirectory()) {
										File[] dirFiles = fi.listFiles();
										for (File dirFile : dirFiles) {
											String newpath = dirFile.getAbsolutePath();
											if(!newpath.endsWith(".checkcif.html") && !newpath.endsWith(".platon.jpeg")) {
												if (dirFile.isDirectory()) {
													Utils.delDir(newpath);
												} else if (dirFile.isFile()) {
													success = dirFile.delete();
												}
											}
										}
									} else if (fi.isFile()) {
										success = fi.delete();
									}
								}
							}
						}
					}
				}
			}
		}
	}

	public void clearIssue(File file) {
		File[] files = file.listFiles();
		for (File f : files) {
			System.out.println(f.getAbsolutePath());
			File[] s = f.listFiles();
			for (File fi : s) {
				String path = fi.getAbsolutePath();
				if (!(path.endsWith(".doi") || path.endsWith(".cif") || path.endsWith(".checkcif.html"))) {	
					boolean success = false; 
					if (fi.isDirectory()) {
						File[] dirFiles = fi.listFiles();
						for (File dirFile : dirFiles) {
							String newpath = dirFile.getAbsolutePath();
							if(!newpath.endsWith(".checkcif.html") && !newpath.endsWith(".platon.jpeg")) {
								if (dirFile.isDirectory()) {
									Utils.delDir(newpath);
								} else if (dirFile.isFile()) {
									success = dirFile.delete();
								}
							}
						}
					} else if (fi.isFile()) {
						success = fi.delete();
					}
				}
			}
		}
	}
}
