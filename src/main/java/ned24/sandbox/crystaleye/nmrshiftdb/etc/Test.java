package ned24.sandbox.crystaleye.nmrshiftdb.etc;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Test {

	public static void main(String[] args) {
		String p = "e:/gaussian/cml/second-protocol_nomisassigns";
		String o = "e:/gaussian/cml/second-protocol_mod1nomisassigns";
		
		List<String> fileList = new ArrayList<String>();
		for (File file : new File(o).listFiles()) {
			fileList.add(file.getName());
		}
		
		List<File> delList = new ArrayList<File>();
		for (File file : new File(p).listFiles()) {
			if (!fileList.contains(file.getName())) {
				delList.add(file);
			}
		}
		
		for (File file : delList) {
			file.delete();
		}
	}

}
