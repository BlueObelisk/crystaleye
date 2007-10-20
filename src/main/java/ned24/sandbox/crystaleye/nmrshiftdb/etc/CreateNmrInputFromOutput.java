package ned24.sandbox.crystaleye.nmrshiftdb.etc;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ned24.sandbox.crystaleye.nmrshiftdb.GaussianConstants;
import ned24.sandbox.crystaleye.nmrshiftdb.GaussianTemplate;
import ned24.sandbox.crystaleye.nmrshiftdb.GaussianUtils;

import org.xmlcml.cml.element.CMLCml;
import org.xmlcml.cml.element.CMLMolecule;

public class CreateNmrInputFromOutput implements GaussianConstants {

	List<File> fileList;
	
	public CreateNmrInputFromOutput(List<File> fileList) {
		this.fileList = fileList;
	}
	
	public void run() {
		for (File f : fileList) {
			CMLCml cml = GaussianUtils.getFinalCml(f);
			CMLMolecule molecule = (CMLMolecule)cml.getFirstCMLChild(CMLMolecule.TAG);
			String connTable = GaussianUtils.getConnectionTable(molecule);
			String solvent = getSolvent(cml);
			if (solvent == null) {
				throw new RuntimeException("Could not find solvent: "+f.getAbsolutePath());
			}
			String name = getFileName(f);
			GaussianTemplate gau = new GaussianTemplate(name, connTable, solvent);
			String input = gau.getNmrStepInput();
		}
	}
	
	private String getSolvent(CMLCml cml) {
		return null;
	}
	
	private static String getFileName(File file) {
		String name = file.getName();
		int idx = name.indexOf(".");
		return name.substring(0,idx);
	}
	
	public static void main(String[] args) {
		List<File> list = new ArrayList<File>();
		list.add(new File("E:\\gaussian\\outputs\\second-protocol_mod1\\1\\nmrshiftdb2189-1.out"));
		CreateNmrInputFromOutput c = new CreateNmrInputFromOutput(list);
		c.run();
	}
}
