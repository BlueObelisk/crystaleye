package ned24.sandbox.crystaleye;

import java.io.File;

import org.xmlcml.cml.legacy2cml.cif.CIFConverter;

public class CIFConvertMany {

	public static void main(String[] args) {
		String cifDict = "e:/legacy2cml/dict/cifCoreDict.xml";
		String spaceGroupXml = "e:/legacy2cml/cryst/spaceGroup.xml";
		String inPath = "E:\\crystaleye-test2\\data\\acs\\cgdefu\\2001";
		File inFolder = new File(inPath);
		
		for (File issue : inFolder.listFiles()) {
			for (File article : issue.listFiles()) {
				File[] fileList = article.listFiles();
				for (File file : fileList) {
					if (file.getAbsolutePath().endsWith(".cif")) {
						String infile = file.getAbsolutePath();
						String outfile = infile+".out";
						
						String[] args0 = {"-INFILE", infile, 
								"-OUTFILE", outfile, 
								"-SKIPERRORS", 
								"-SKIPHEADER", 
								"-NOGLOBAL", 
								"-SPACEGROUP", spaceGroupXml,
								"-DICT", cifDict
						};
						CIFConverter cifConverter = new CIFConverter();
						try {
							cifConverter.runCommands(args0);
						} catch (Exception e) {
							e.printStackTrace();
							System.err.println("CIFConverter EXCEPTION... "+e);
						}
					}
				}
				for (File file : fileList) {
					if (file.getAbsolutePath().endsWith(".out")) {
						file.deleteOnExit();
					}
				}
			}
		}
	}
}
