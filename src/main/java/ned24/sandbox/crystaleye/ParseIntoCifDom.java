package ned24.sandbox.crystaleye;

import java.io.FileWriter;
import java.io.IOException;

import nu.xom.Document;

import org.xmlcml.cif.CIF;
import org.xmlcml.cif.CIFException;
import org.xmlcml.cif.CIFParser;

public class ParseIntoCifDom {
	public static void main(String[] args) {
		String filename = "E:/ned24/colloquia/Papers/cifdom/normal/example.cif";
		try {
			CIFParser parser = new CIFParser();
			parser.setSkipErrors(true);
			Document doc = parser.parse(filename);
			CIF cif = (CIF) doc.getRootElement();
			cif.processSu(true);
			cif.debug();
			String outfile = "E:/ned24/colloquia/Papers/cifdom/normal/example.cif.xml";
			FileWriter fw = new FileWriter(outfile);
			cif.writeXML(fw);
			fw.close();
		} catch (CIFException e) {
			System.err.println("Could not parse CIF file: "+filename);
		} catch (IOException e) {
			System.err.println("Could not find file: "+filename);
		}
	}
}