package ned24.sandbox.crystaleye.nmrshiftdb.etc;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FindMissingModJobs {

	public static void main(String[] args) {
		String outPath = "e:/gaussian/outputs/second-protocol_mod1/1";
		String otherPath = "e:/test/cml";
		
		List<String> others = new ArrayList<String>();
		for (File otherFile : new File(otherPath).listFiles()) {
			String p = otherFile.getAbsolutePath();
			if (p.endsWith(".xml")) {
				String name = otherFile.getName();
				name = name.substring(0,name.length()-8);
				others.add(name);
			}
		}
		
		List<String> outs = new ArrayList<String>();
		for (File outFile : new File(outPath).listFiles()) {
			String p = outFile.getAbsolutePath();
			if (p.endsWith(".out")) {
				String name = outFile.getName();
				name = name.substring(0,name.length()-4);
				outs.add(name);
			}
		}
		
		System.out.println(others.size());
		System.out.println(outs.size());
		
		for (String name : others) {
			if (!outs.contains(name)) {
				System.out.println(name);
			}
		}
	}
}
