package ned24.sandbox.crystaleye.nmrshiftdb.etc;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import ned24.sandbox.crystaleye.nmrshiftdb.GaussianConstants;
import nu.xom.Element;
import nu.xom.Nodes;

import org.xmlcml.cml.element.CMLMolecule;
import org.xmlcml.cml.element.CMLScalar;

import uk.ac.cam.ch.crystaleye.IOUtils;

public class Test implements GaussianConstants {

	public static void main(String[] args) {
		FindPossibleMisassignments f = new FindPossibleMisassignments(HSR1_HALOGEN_AND_MORGAN_NAME);
		Set<File> fileSet = f.getFileList();
		System.out.println("fileset "+fileSet.size());
		List<String> nameList = new ArrayList<String>();
		for (File file : fileSet) {
			nameList.add(file.getName());
		}
		System.out.println("namelist: "+nameList.size());
		
		String path = "e:/gaussian/cml";
		
		for (File protName : new File(path).listFiles()) {
			if (protName.getName().equals(".svn")) {
				continue;
			}
			List<File> stillPM = new ArrayList<File>();
			List<File> notAnyMore = new ArrayList<File>();
			for (File file : protName.listFiles()) {
				if (file.getName().equals(".svn")) {
					continue;
				}
				//System.out.println(file.getAbsolutePath());
				CMLMolecule molecule = (CMLMolecule)IOUtils.parseCmlFile(file).getRootElement();
				Nodes nodes = molecule.query(".//cml:scalar[contains(@dictRef,'ned24:possibly-misassigned')]", X_CML);
				if (nodes.size() > 0) {
					if (nameList.contains(file.getName())) {
						stillPM.add(file);
					} else {
						System.out.println(file.getName());
						Nodes nodes1 = molecule.query(".//cml:scalar[contains(@dictRef,'ned24')]", X_CML);
						for (int i = 0; i < nodes1.size(); i++) {
							Element el = (Element)nodes1.get(i);
							el.detach();
						}
						CMLScalar s = new CMLScalar();
						s.setDictRef("ned24:not-removed");
						molecule.appendChild(s);
						notAnyMore.add(file);
						IOUtils.writePrettyXML(molecule.getDocument(), file.getAbsolutePath());
					}
				}
			}
			System.out.println("stillpm "+stillPM.size());
			System.out.println("notmore "+notAnyMore.size());
		}
	}
}
