package ned24.sandbox.crystaleye;

import java.io.FileWriter;

import org.xmlcml.cif.CIF;
import org.xmlcml.cif.CIFDataBlock;
import org.xmlcml.cif.CIFItem;
import org.xmlcml.cif.CIFParser;

public class SimpleCifEditor {
	public static void main(String[] args) {
		String filename = "C:/path/to/your/cif/file.cif";
		try {
			String tempItemName = "_cell_measurement_temperature";
			double newTemp = 205.0;
			double newSu = 1.0;
			int dps = 1;
			
			CIF cif = (CIF) new CIFParser().parse(filename).getRootElement();
			for (CIFDataBlock block : cif.getDataBlockList()) {
				CIFItem temp = block.getChildItem(tempItemName);
				if (temp == null) {
					temp = new CIFItem(tempItemName, newTemp, newSu, dps);
					block.appendChild(temp);
				} else {
					temp.setValueAndSu(newTemp, newSu, dps);
				}
			}
			cif.writeCIF(new FileWriter(filename));
		} catch (Exception e) {
			//
		}
	}
}
