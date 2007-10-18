package ned24.sandbox.crystaleye.nmrshiftdb;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class GetMod1Inputs {

	public static void main(String[] args) {
		String modFolder = "e:/gaussian/inputs/second-protocol_mod1/1/";
		
		List<File> list = new ArrayList<File>();
		for (File file : new File(modFolder).listFiles()) {
			String path = file.getAbsolutePath();
			if (path.endsWith(".sh") || path.endsWith(".condor.sh")) {
				String name = file.getName();
				int idx = name.indexOf(".");
				name = name.substring(0,idx);
				File parent = file.getParentFile();
				File n = new File(parent.getAbsolutePath()+File.separator+name+".gjf");
				if (!n.exists()) {
					list.add(file);
				}
			}
		}
		
		for (File file : list) {
			file.delete();
		}
	}

}
