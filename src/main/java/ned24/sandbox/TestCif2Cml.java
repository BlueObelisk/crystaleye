package ned24.sandbox;

import java.io.File;
import java.util.Collection;

import org.apache.commons.io.FileUtils;

import wwmm.crystaleye.tools.RawCml2CompleteCmlTool;

public class TestCif2Cml {
	
	public static void main(String[] args) {
		String dirPath = "E:\\crystaleye-new\\new\\acta\\e\\2009\\06-00";
		String[] exts = {"cif"};
		Collection<File> cifFiles = FileUtils.listFiles(new File(dirPath), exts, true);
		RawCml2CompleteCmlTool cmlTool = new RawCml2CompleteCmlTool();
		for (File cifFile : cifFiles) {
			cmlTool.convert(cifFile);
		}
	}

}
