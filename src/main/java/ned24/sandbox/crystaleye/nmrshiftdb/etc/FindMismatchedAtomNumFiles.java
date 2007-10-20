package ned24.sandbox.crystaleye.nmrshiftdb.etc;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ned24.sandbox.crystaleye.nmrshiftdb.C13SpectraTool;

public class FindMismatchedAtomNumFiles {

	public static void main(String[] args) {
		String path = "e:/test/cml";
		List<File> list = new ArrayList<File>();
		for (File file : new File(path).listFiles()) {
			C13SpectraTool c = new C13SpectraTool(file);
			String solvent = c.getCalculatedSolvent();
			boolean b = c.testSpectraConcordant(solvent);
			if (!b) {
				list.add(file);
			}
		}
		
		for (File file : list) {
			file.delete();
		}
	}
}
