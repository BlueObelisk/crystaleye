package ned24.sandbox;

import java.io.File;

import nu.xom.Element;
import nu.xom.Nodes;
import nu.xom.Text;

import org.xmlcml.cml.base.CMLConstants;
import org.xmlcml.cml.element.CMLScalar;

import wwmm.crystaleye.CrystalEyeConstants;
import wwmm.crystaleye.util.Utils;

public class FixAcsDois {

	public static void main(String[] args) {
		String path = "e:/crystaleye-new/new/acs/";
		File file = new File(path);
		for (File journalDir : file.listFiles()) {
			for (File yearDir : journalDir.listFiles()) {
				for (File issueDir : yearDir.listFiles()) {
					for (File articleDir : issueDir.listFiles()) {
						String articleId = articleDir.getName();
						for (File articleFile : articleDir.listFiles()) {
							String doi = CrystalEyeConstants.ACS_DOI_PREFIX+"/"+articleId;
							if (articleFile.getPath().endsWith(".doi")) {
								Utils.writeText(articleFile, doi);
							}
							if (articleFile.isDirectory()) {
								for (File f : articleFile.listFiles()) {
									if (f.getPath().endsWith(".complete.cml.xml")) {
										Element root = (Element)Utils.parseCml(f).getRootElement();
										Nodes nodes = root.query(".//cml:scalar[@dictRef='idf:doi']", CMLConstants.CML_XPATH);
										if (nodes.size() !=1) {
											throw new RuntimeException("asd");
										}
										Element doiElement = (Element)nodes.get(0);
										doiElement.detach();
										CMLScalar scalar = new CMLScalar();
										scalar.setDictRef("idf:doi");
										scalar.appendChild(new Text(doi));
										root.appendChild(scalar);
										Utils.writeXML(f, root.getDocument());
									}
								}
							}
						}
					}
				}
			}
		}
	}

}
