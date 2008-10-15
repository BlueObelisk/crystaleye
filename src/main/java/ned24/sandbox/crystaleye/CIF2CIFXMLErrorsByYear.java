package ned24.sandbox.crystaleye;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;

import org.xmlcml.cif.CIF;
import org.xmlcml.cif.CIFParser;

import wwmm.crystaleye.FileListing;
import wwmm.crystaleye.util.Utils;

public class CIF2CIFXMLErrorsByYear {

	public static void main(String[] args) {
		String inPath = "e:/cif2cifxml-errors";

		int numFiles = 0;
		int numErrors = 0;
		StringBuilder sb = new StringBuilder();
		for (File pubFile : new File(inPath).listFiles()) {
			for (File journalFile : pubFile.listFiles()) {
				for (File yearFile : journalFile.listFiles()) {
					List<File> cifList;
					// delimiter error
					int syntactic = 0;
					// loop item error
					int notDivisibleByNames = 0;
					// illegal char
					int illegalCharacter = 0;
					try {
						cifList = FileListing.byMime(yearFile, ".cif");
						System.out.println(cifList.size());
						for (File cifFile : cifList) {
							numFiles++;
							System.out.println(cifFile.getAbsolutePath());
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
								Utils.writePrettyXML(cif.getDocument(), "e:/cif2cifxml-test/"+cifxmlname);
								syntactic++;
								numErrors++;
							} catch (Exception e) { 
								String m = e.getMessage();
								if (m != null) {
									if (m.contains("Cannot fix line") || m.contains("not fix errors") || 
											m.contains("by zero") || m.contains("should never throw: null")) {
										syntactic++;
										numErrors++;
									} else if (m.contains("not divisible by names")) {
										notDivisibleByNames++;
										numErrors++;
									} else if (m.contains("not allowed in XML content")) {
										illegalCharacter++;
										numErrors++;
									} else {
										syntactic++;
										numErrors++;
									}
								} else {
									syntactic++;
									numErrors++;
								}
							}
							System.out.println(numFiles+"/"+numErrors);
						}
					} catch (FileNotFoundException e1) {
						e1.printStackTrace();
					}
					sb.append(pubFile.getName()+","+journalFile.getName()+","+
							yearFile.getName()+","+syntactic+":"+notDivisibleByNames+
							":"+illegalCharacter+"\n");
				}
				sb.append("--------------------------------------------------\n");
			}
			sb.append("================================================================\n");
		}
		Utils.writeText(sb.toString(), "e:/cif2cifxml-errors-byyear.txt");
	}
	
}
