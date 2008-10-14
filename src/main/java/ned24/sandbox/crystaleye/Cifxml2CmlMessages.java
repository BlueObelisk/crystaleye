package ned24.sandbox.crystaleye;

import java.io.File;

import uk.ac.cam.ch.crystaleye.IOUtils;
import uk.ac.cam.ch.crystaleye.Utils;

public class Cifxml2CmlMessages {

	public static void main(String[] args) {	
		String path = "e:/cifxml2cml-errormessages.txt";
		String content = Utils.file2String(path);
		String[] a = content.split("-----------------------------------------------------------------");
		System.out.println(a.length);

		String[] pubs = {"acs", "acta", "rsc"};
		String[] years = {"2001", "2002", "2003", "2004", "2005", "2006", "2007"};

		StringBuilder sb = new StringBuilder();
		for (String pub : pubs) {
			for (String year : years) {
				int noAtoms = 0;
				int badElement = 0;
				int badSym = 0;
				int noCell =0;
				int badDouble = 0;
				int missingAtomData = 0;
				for (String piece : a) {
					String[] lines = piece.split("\n");
					if (lines.length > 2) {
						String filepath = lines[1];
						filepath = filepath.substring(7);
						String[] parts = filepath.split("\\\\");
						String thisPub = parts[2];
						String thisyear = parts[4];
						if (thisPub.equals(pub) && thisyear.equals(year)) {
							if (piece.contains("no explicit atoms given")) {
								noAtoms++;
							} else if (piece.contains("Bad element")) {
								badElement++;
							} else if (piece.contains("Missing fractional coordinates")) {
								badDouble++;
							} else if (piece.contains("Bad string in symmetry")) {
								badSym++;
							} else if (piece.contains("Must have 3 operators")) {
								badSym++;
							} else if (piece.contains("no cell given")) {
								noCell++;
							} else if (piece.contains("bad double")) {
								badDouble++;
							} else if (piece.contains("Must give _atom")){ 
								missingAtomData++;
							}
						}
					}
				}
				sb.append("==============================================================\n");
				sb.append("~~~~: "+pub+"/"+year+"\n");
				sb.append("no explicit atoms: "+noAtoms+"\n");
				sb.append("      bad element: "+badElement+"\n");
				sb.append("       bad double: "+badDouble+"\n");
				sb.append("  bad sym element: "+badSym+"\n");
				sb.append("          no cell: "+noCell+"\n");
				sb.append("missing atom data: "+missingAtomData+"\n");
			}
		}
		IOUtils.writeText(sb.toString(), "e:/cifxml2cml-errorsbyyear.txt");
	}
}
