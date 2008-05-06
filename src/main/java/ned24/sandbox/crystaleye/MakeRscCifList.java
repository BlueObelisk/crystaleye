package ned24.sandbox.crystaleye;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import uk.ac.cam.ch.crystaleye.FileListing;
import uk.ac.cam.ch.crystaleye.IOUtils;

public class MakeRscCifList {

	public static void main(String[] args) {
		String path = "e:/rsc-test2/data";
		File file = new File(path);

		StringBuilder sb = new StringBuilder();
		for (File publisherFile : file.listFiles()) {
			for(File journalFile : publisherFile.listFiles()) {
				for (File yearFile : journalFile.listFiles()) {
					for (File issueFile : yearFile.listFiles()) {
						try {
							List<File> cifList = FileListing.byMime(yearFile, ".cif");
							for (File cifFile : cifList) {
								String name = cifFile.getName();
								int idx = name.indexOf(".");
								String id = name.substring(0,idx);
								sb.append(publisherFile.getName()+","+journalFile.getName()+","
										+yearFile.getName()+","+issueFile.getName()+","+id+"\n");
							}
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
		IOUtils.writeText(sb.toString(), "e:/rsc-data2-ciflist.txt");
	}

}
