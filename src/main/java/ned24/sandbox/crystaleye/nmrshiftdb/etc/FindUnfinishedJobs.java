package ned24.sandbox.crystaleye.nmrshiftdb.etc;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import uk.ac.cam.ch.crystaleye.FileListing;

public class FindUnfinishedJobs {

	public static void main(String[] args) {
		String p1 = "e:/gaussian/second-protocol-out";
		String p2 = "e:/gaussian/second-protocol";
		
		List<File> fileList = null;
		try {
			fileList = FileListing.byRegex(new File(p2), ".*");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
				
		List<String> idList = new ArrayList<String>();
		for (File file : new File(p1).listFiles()) {
			if (file.getAbsolutePath().endsWith(".gjf")) {
				String name = file.getName();
				name = name.substring(0,name.length()-4);
				idList.add(name);
			}
		}
		
		List<File> deleteList = new ArrayList<File>();
		for (File file : fileList) {
			String id = file.getName();
			int idx = id.indexOf(".");
			id = id.substring(0,idx);
			if (idList.contains(id)) {
				deleteList.add(file);
			}
		}
		
		for (File file : deleteList) {
			System.out.println(file.getAbsolutePath());
			file.delete();
		}
	}
}
