package ned24.sandbox.crystaleye;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import org.xmlcml.cml.legacy2cml.cif.CIFConverter;

import wwmm.crystaleye.FileListing;
import wwmm.crystaleye.util.XmlIOUtils;

public class CIFXML2CML {

	public static void main(String[] args) {
		String inPath = "e:/cifxml2cml-errors";
		File inFile = new File(inPath);

		List<File> cifList;
		try {
			cifList = FileListing.byMime(inFile, "");
			String outFolder = "e:/cifxml2cml-output/";

			StringBuilder sb = new StringBuilder();
			for (File cif : cifList) {
				String path = cif.getAbsolutePath();
				sb.append("-----------------------------------------------------------------\n");
				sb.append("=====: "+path+"\n");
				String outfile = outFolder+cif.getName();

				String cifDict = "e:/legacy2cml/dict/cifCoreDict.xml";
				String spaceGroupXml = "e:/legacy2cml/cryst/spaceGroup.xml";
				String[] args0 = {"-INFILE", path, 
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
					sb.append(e.getMessage()+"\n");
					sb.append(e.getCause()+"\n");
					System.err.println("CIFConverter EXCEPTION... "+e);
				}
			}
			XmlIOUtils.writeText(sb.toString(), "e:/cifxml2cml-errormessages.txt");
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
	}
}
