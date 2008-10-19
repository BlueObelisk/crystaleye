package ned24.sandbox.crystaleye;

import java.io.File;

import nu.xom.Nodes;

import org.xmlcml.cml.base.CMLConstants;
import org.xmlcml.cml.element.CMLCml;
import org.xmlcml.cml.element.CMLCrystal;
import org.xmlcml.cml.element.CMLMolecule;

import wwmm.crystaleye.util.Utils;

public class CreateCODCellParamsFile implements CMLConstants {

	String dir;
	String outPath;

	private CreateCODCellParamsFile() {
		;
	}

	public CreateCODCellParamsFile(String dir, String outPath) {
		this.dir = dir;
		this.outPath = outPath;
	}

	public void createFile() {
		File file = new File(dir);

		StringBuilder sb = new StringBuilder();

		for (File fil : file.listFiles()) {
			if (!fil.isDirectory()) {
				continue;
			}
			for (File f : fil.listFiles()) {
				if (f.getAbsolutePath().endsWith(".raw.cml.xml")) {
					System.out.println(f.getAbsolutePath());
					CMLCml cml = null;
					try {
						cml = (CMLCml)Utils.parseCml(f).getRootElement();
					} catch (Exception e) {
						System.err.println("Error reading CML: "+f.getAbsolutePath());
						continue;
					}
					CMLMolecule molecule = (CMLMolecule)cml.getFirstCMLChild(CMLMolecule.TAG);
					CMLCrystal crystal = (CMLCrystal)molecule.getFirstCMLChild(CMLCrystal.TAG);

					Nodes lengthANodes = crystal.query(".//cml:scalar[@dictRef='iucr:_cell_length_a']", CML_XPATH);
					String lengthA = "";
					if (lengthANodes.size() == 1) {
						lengthA  = lengthANodes.get(0).getValue();
					} else {
						System.err.println("Could not find lengthA node.");
						continue;
					}

					Nodes lengthBNodes = crystal.query(".//cml:scalar[@dictRef='iucr:_cell_length_b']", CML_XPATH);
					String lengthB = "";
					if (lengthBNodes.size() == 1) {
						lengthB  = lengthBNodes.get(0).getValue();
					} else {
						System.err.println("Could not find lengthB node.");
						continue;
					}

					Nodes lengthCNodes = crystal.query(".//cml:scalar[@dictRef='iucr:_cell_length_c']", CML_XPATH);
					String lengthC = "";
					if (lengthCNodes.size() == 1) {
						lengthC  = lengthCNodes.get(0).getValue();
					} else {
						System.err.println("Could not find lengthC node.");
						continue;
					}

					Nodes angleANodes = crystal.query(".//cml:scalar[@dictRef='iucr:_cell_angle_alpha']", CML_XPATH);
					String angleA = "";
					if (angleANodes.size() == 1) {
						angleA  = angleANodes.get(0).getValue();
					} else {
						System.err.println("Could not find angleA node.");
						continue;
					}

					Nodes angleBNodes = crystal.query(".//cml:scalar[@dictRef='iucr:_cell_angle_beta']", CML_XPATH);
					String angleB = "";
					if (angleBNodes.size() == 1) {
						angleB  = angleBNodes.get(0).getValue();
					} else {
						System.err.println("Could not find angleB node.");
						continue;
					}

					Nodes angleGNodes = crystal.query(".//cml:scalar[@dictRef='iucr:_cell_angle_gamma']", CML_XPATH);
					String angleG = "";
					if (angleGNodes.size() == 1) {
						angleG  = angleGNodes.get(0).getValue();
					} else {
						System.err.println("Could not find angleG node.");
						continue;
					}
					String name = f.getName();
					int idx = name.indexOf(".raw.cml.xml");
					name = name.substring(0,idx);
					String id = "crystallographynet_cod_2007_08-11_"+name;
					System.out.println(id);
					sb.append(lengthA+","+lengthB+","+lengthC+","+angleA+","+angleB+","+angleG+","+id+"\n");
				}
			}
		}
		Utils.writeText(sb.toString(), outPath);
	}

	public static void main(String[] args) {
		CreateCODCellParamsFile c = new CreateCODCellParamsFile("C:\\Documents and Settings\\Nick Day\\crystallographyopendatabase", "e:/data-test/cell-params.txt");
		c.createFile();
	}
}
