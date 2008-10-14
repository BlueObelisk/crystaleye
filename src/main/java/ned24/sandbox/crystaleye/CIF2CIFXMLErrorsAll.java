package ned24.sandbox.crystaleye;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;

import org.xmlcml.cif.CIF;
import org.xmlcml.cif.CIFParser;

import wwmm.crystaleye.FileListing;
import wwmm.crystaleye.IOUtils;

public class CIF2CIFXMLErrorsAll {

	public static void main(String[] args) {
		String inPath = "e:/cifxml2cml-errors";
		File inFile = new File(inPath);

		List<File> cifList;
		try {
			cifList = FileListing.byMime(inFile, ".cif");
			
			System.out.println(cifList.size());
			for (File cifFile : cifList) {
				System.out.println(cifFile.getAbsolutePath());
				StringBuilder sb = new StringBuilder();
				sb.append("-----------------------------------------------------------------\n");
				sb.append("=====: "+cifFile.getAbsolutePath()+"\n");
				CIFParser parser = new CIFParser();
				parser.setSkipHeader(true);
				parser.setSkipErrors(true);
				parser.setCheckDuplicates(true);
				parser.setBlockIdsAsIntegers(false);

				try {
					CIF cif = (CIF) parser.parse(new BufferedReader(new FileReader(cifFile))).getRootElement();
					File parent = cifFile.getParentFile();
					String cifname = cifFile.getName();
					String cifxmlname = cifname+".xml";
					IOUtils.writePrettyXML(cif.getDocument(), cifFile.getAbsolutePath()+".xml");
				} catch (Exception e) {
					sb.append(e.getMessage()+"\n");
					System.out.println(e.getMessage());
				}
				//IOUtils.appendToFile(new File("e:/cif2cifxml-messages.txt"), sb.toString());
			}
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
	}
	
}
