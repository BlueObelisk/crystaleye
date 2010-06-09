package ned24.sandbox;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import wwmm.crystaleye.tools.MoietyCml2RdfTool;

public class CreateMoietyN3Files {
	
	private static int MAX_FILES_PER_UPDATE = 100000;
	
	public static void main(String[] args) throws ValidityException, ParsingException, IOException {
		//String path = "C:/workspace/crystaleye-trunk-data/www/crystaleye/summary";
		String path = "/var/crystaleye/www/crystaleye/summary/";
		
		File dataDir = new File(path);
		
		int count = 0;
		for (File publisherDir : dataDir.listFiles()) {
			if (!publisherDir.isDirectory()) {
				continue;
			}
			for (File journalDir : publisherDir.listFiles()) {
				if (!publisherDir.isDirectory()) {
					continue;
				}
				for (File yearDir : journalDir.listFiles()) {
					if (!yearDir.isDirectory()) {
						continue;
					}
					for (File issueDir : yearDir.listFiles()) {
						if (!issueDir.isDirectory()) {
							continue;
						}
						// so that OutOfMemoryErrors aren't thrown.
						if (count > MAX_FILES_PER_UPDATE) {
							break;
						}
						Collection files = FileUtils.listFiles(issueDir, null, true);
						List<File> moietyFiles = new ArrayList<File>();
						for (Object obj : files) {
							// so that OutOfMemoryErrors aren't thrown.
							if (count > MAX_FILES_PER_UPDATE) {
								break;
							}
							File file = (File)obj;
							String filepath = getFilepath(file);
							if (FilenameUtils.wildcardMatch(filepath, "*moieties/*/*.complete.cml.xml") &&
									!filepath.contains("/fragments/")) {
								moietyFiles.add(file);
								count++;
							}
						}
						MoietyCml2RdfTool tool = new MoietyCml2RdfTool();
						for (File moietyFile : moietyFiles) {
							String moietyPath = FilenameUtils.separatorsToUnix(moietyFile.getAbsolutePath());
							String n3Path = moietyPath.replaceAll(".complete.cml.xml", ".n3");
							File n3File = new File(n3Path);
							String summaryUrl = "http://wwmm.ch.cam.ac.uk/crystaleye/summary/";
							String moietyUrl = moietyPath.replaceAll(path, summaryUrl);
							String n3Url = n3Path.replaceAll(path, summaryUrl);
							System.out.println("Converting: "+moietyFile);
							tool.convert(moietyFile, n3File, moietyUrl, n3Url);
						}
					}
				}
			}
		}
	}
	
	private static String getFilepath(File file) {
		return FilenameUtils.separatorsToUnix(file.getAbsolutePath());
	}

}
