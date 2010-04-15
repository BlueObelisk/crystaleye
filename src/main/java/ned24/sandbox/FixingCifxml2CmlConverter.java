package ned24.sandbox;

import java.io.File;

import org.xmlcml.cml.converters.Command;
import org.xmlcml.cml.converters.cif.CIF2CIFXMLConverter;
import org.xmlcml.cml.converters.cif.CIFXML2CMLConverter;
import org.xmlcml.cml.converters.cif.RawCML2CompleteCMLConverter;

public class FixingCifxml2CmlConverter {
	
	public static void main(String[] args) {
		String inpath = "c:/workspace/test.cif";
		String outpath = "c:/workspace/test.cif.xml";
		
		CIF2CIFXMLConverter converter = new CIF2CIFXMLConverter();
		converter.setCommand(new Command());
		try {
			converter.convert(new File(inpath), new File(outpath));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		String inpath2 = "c:/workspace/test.cif.xml";
		String outpath2 = "c:/workspace/test.cif.cml";
		
		CIFXML2CMLConverter converter2 = new CIFXML2CMLConverter();
		converter2.setCommand(new Command());
		try {
			converter2.convert(new File(inpath2), new File(outpath2));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		String inpath3 = "c:/workspace/test.cif.cml";
		String outpath3 = "c:/workspace/test.complete.cml";
		
		RawCML2CompleteCMLConverter converter3 = new RawCML2CompleteCMLConverter();
		converter3.setCommand(new Command());
		try {
			converter3.convert(new File(inpath3), new File(outpath3));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
