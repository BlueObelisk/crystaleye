package ned24.sandbox.crystaleye.nmrshiftdb.etc;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import uk.ac.cam.ch.crystaleye.FileListing;

public class Test {

	public static void main(String[] args) {
		String path = "e:/gaussian/cml/removed-files/second-protocol";
		String p = "e:/gaussian/cml/second-protocol";
		
		List<String> list = new ArrayList<String>();
		for (File file : new File(p).listFiles()) {
			if (file.getAbsolutePath().endsWith(".cml.xml")) {
				list.add(file.getName());
			}
		}
		try {
			List<File> fileList = FileListing.byMime(new File(path), ".cml.xml");
			for (File file : fileList) {
				list.add(file.getName());
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for (File file : new File("e:/gaussian/cml/second-protocol_mod1").listFiles()) {
			if (!list.contains(file.getName())) {
				System.out.println(file.getAbsolutePath());
			}
		}
	}

}
