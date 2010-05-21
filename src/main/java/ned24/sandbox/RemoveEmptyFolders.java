package ned24.sandbox;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

public class RemoveEmptyFolders {
	
	public static void main(String[] args) throws IOException {
		String dirPath = "e:/crystaleye-2010/acta/";
		File dir = new File(dirPath);
		List<File> filesToRemove = new ArrayList<File>();
		for (File journalDir : dir.listFiles()) {
			if (journalDir.equals("cmatex")) {
				break;
			}
			for (File yearDir : journalDir.listFiles()) {
				for (File issueDir : yearDir.listFiles()) {
					for (File articleDir : issueDir.listFiles()) {
						if (articleDir.listFiles().length == 0) {
							System.out.println(articleDir);
							filesToRemove.add(articleDir);
						}
					}
				}
			}
		}
		for (File file : filesToRemove) {
			System.out.println("deleting: "+file);
			FileUtils.deleteDirectory(file);
		}
	}

}
