package ned24.sandbox;

import org.xmlcml.cml.converters.cif.CIFXML2CMLConverter;

import java.io.File;

public class TestCif2Cml {
	
	public static void main(String[] args) {
		String inpath = "c:/workspace/lensfield-cif-example/data/wj9904/wj9904-cif.xml";
		String outpath = "c:/workspace/lensfield-cif-example/data/wj9904/test.cml";
		
		CIFXML2CMLConverter converter = new CIFXML2CMLConverter();
		converter.convert(new File(inpath), new File(outpath));
		/*
		String dirPath = "E:\\crystaleye-new\\new\\acta\\e\\2009\\06-00";
		String[] exts = {"cif"};
		Collection<File> cifFiles = FileUtils.listFiles(new File(dirPath), exts, true);
		RawCml2CompleteCmlTool cmlTool = new RawCml2CompleteCmlTool();
		for (File cifFile : cifFiles) {
			cmlTool.convert(cifFile);
		}
		*/
	}

}
