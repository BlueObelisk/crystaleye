package ned24.sandbox;

import java.io.File;
import java.io.IOException;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.xmlcml.cml.base.CMLBuilder;
import org.xmlcml.cml.converters.cmllite.CML2CMLLiteConverter;

import wwmm.crystaleye.util.Utils;

public class CMLLiteTest {
	
	public static void main(String[] args) throws ValidityException, ParsingException, IOException {
		String inpath = "c:/workspace/test.cml";
		String outpath = "c:/workspace/test.lite.cml";
		CML2CMLLiteConverter cmlLiteConverter = new CML2CMLLiteConverter();
		Element rootElement = new CMLBuilder().build(new File(inpath)).getRootElement();
		rootElement = cmlLiteConverter.convertToXML(rootElement);
		Utils.writeXML(new File(outpath), new Document(rootElement));
	}

}
