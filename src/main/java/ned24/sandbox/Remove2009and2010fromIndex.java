package ned24.sandbox;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;

import wwmm.crystaleye.util.Utils;

public class Remove2009and2010fromIndex {

	public static void main(String[] args) throws IOException {
		String filepath = "C:\\Documents and Settings\\ned24.UCC\\Desktop\\crysta";

		for (File file : new File(filepath).listFiles()) {
			StringBuilder sb = new StringBuilder();
			File newFile = new File(file.getAbsolutePath()+".2");
			List<String> lines = FileUtils.readLines(file);
			int count = 0;
			for (String line : lines) {
				count++;
				if (line.contains("/2009/") || line.contains("_2009_") ||
						line.contains("/2010/") || line.contains("_2010_")) {
					continue;
				} else {
					sb.append(line+"\n");
				}
				if (count % 5000 == 0) {
					Utils.appendToFile(newFile, sb.toString());
					sb = new StringBuilder();
				}
			}
			Utils.appendToFile(newFile, sb.toString());
			sb = new StringBuilder();
		}

	}

}
