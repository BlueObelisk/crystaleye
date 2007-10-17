package ned24.sandbox.crystaleye.nmrshiftdb;

import java.io.File;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;

import org.xmlcml.cml.base.CMLConstants;

import uk.ac.cam.ch.crystaleye.IOUtils;

public class CopySpectraFrom2dTo3d implements CMLConstants {

	public static void main(String[] args) {
		String path2d = "e:/nmrshiftdb/2d/mols";
		String path3d = "e:/nmrshiftdb/3d/mols";
		
		for (File file : new File(path2d).listFiles()) {
			String name2d = file.getName();
			File file3d = new File(path3d+File.separator+name2d);
			if (file3d.exists()) {
				Document doc2d = IOUtils.parseCmlFile(file);
				Document doc3d = IOUtils.parseCmlFile(file3d);
				Nodes nodes = doc2d.query(".//cml:spectrum", X_CML);
				for (int i = 0; i < nodes.size(); i++) {
					System.out.println("yep");
					Element spectrum = (Element)nodes.get(i);
					doc3d.getRootElement().appendChild(spectrum.copy());
				}
				System.out.println(file3d.getAbsolutePath());
				IOUtils.writePrettyXML(doc3d, file3d.getAbsolutePath());
				//break;
			}
		}
	}
}
