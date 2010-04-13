package ned24.sandbox;

import java.io.File;

import org.xmlcml.cml.converters.Command;
import org.xmlcml.cml.converters.cif.CIFXML2CMLConverter;

public class FixingCifxml2CmlConverter {
	
	public static void main(String[] args) {
		String inpath = "c:/workspace/test.cif.xml";
		String outpath = "c:/workspace/test.cif.cml";
		
		CIFXML2CMLConverter converter = new CIFXML2CMLConverter();
		converter.setCommand(new Command());
		try {
			converter.convert(new File(inpath), new File(outpath));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
