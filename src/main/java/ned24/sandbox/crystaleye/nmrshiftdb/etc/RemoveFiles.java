package ned24.sandbox.crystaleye.nmrshiftdb.etc;

import java.io.File;

import ned24.sandbox.crystaleye.nmrshiftdb.GaussianUtils;


public class RemoveFiles {

	public static void main(String[] args) {
		String gotPath = "e:/gaussian/inputs/second-protocol_freq/1";

		int count = 1;
		String folderName = "second-protocol_freq/1";
		for (File file : new File(gotPath).listFiles()) {
			String name = file.getName();
			int idx = name.indexOf(".");
			name = name.substring(0,idx);
			GaussianUtils.writeCondorSubmitFile(gotPath, folderName, name, count);
			GaussianUtils.writeShFile(gotPath, name, count);
		}
	}
}
