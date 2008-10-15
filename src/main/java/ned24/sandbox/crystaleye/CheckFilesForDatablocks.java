package ned24.sandbox.crystaleye;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import wwmm.crystaleye.FileListing;

public class CheckFilesForDatablocks {

	public static void main(String[] args) {
		String path = "e:/cifxml2cml-errors";
		File file = new File(path);
		
		try {
			List<File> copyList = new ArrayList<File>();
			List<File> cifList = FileListing.byMime(file, ".cif");
			for (File cif : cifList) {
				List<String> lines = FileUtils.readLines(cif);
				int blocks = 0;
				for (String line : lines) {
					if (line.contains("data_")) {
						blocks++;
					}
				}
				if (blocks < 2) {
					System.out.println(cif.getAbsolutePath());
					copyList.add(cif);
				}
			}
			/*
			for (File copy : copyList) {
				String name = copy.getName();
				Utils.copyFile(copy.getAbsolutePath(), "e:/one-block/"+name);
			}
			*/
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
