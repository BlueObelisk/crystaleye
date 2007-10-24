package ned24.sandbox.crystaleye.nmrshiftdb.etc;

import java.io.File;

import uk.ac.cam.ch.crystaleye.Utils;

public class FindNimag {

	public static void main(String[] args) {
		String path = "e:/gaussian/outputs/second-protocol_freq/1";
		for (File file : new File(path).listFiles()) {
			if (file.getAbsolutePath().endsWith(".out")) {
				String s = Utils.file2String(file.getAbsolutePath());
				s = s.replaceAll("\n", "");
				s = s.replaceAll("\\s", "");
				if (!s.contains("NImag=0")) {
					System.out.println(file.getAbsolutePath());
				}
			}
		}
	}
}
