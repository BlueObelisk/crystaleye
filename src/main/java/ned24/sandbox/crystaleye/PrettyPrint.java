package ned24.sandbox.crystaleye;

import java.io.File;

import nu.xom.Document;
import wwmm.crystaleye.util.Utils;

public class PrettyPrint {
	public static void main(String[] args) {
		String path = "e:/nmrshiftdb";
		for (File file : new File(path).listFiles()) {
			System.out.println(file.getAbsolutePath());
			Document doc = Utils.parseXml(file);
			Utils.writePrettyXML(doc, file.getAbsolutePath());
		}
	}
}
