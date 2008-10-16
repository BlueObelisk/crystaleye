package wwmm.crystaleye.site;

import static wwmm.crystaleye.CrystalEyeConstants.COMPLETE_CML_MIME_REGEX;
import static wwmm.crystaleye.CrystalEyeConstants.SMILESLIST;
import static wwmm.crystaleye.CrystalEyeConstants.WEBPAGE;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import nu.xom.Nodes;

import org.xmlcml.cml.base.CMLBuilder;
import org.xmlcml.cml.base.CMLConstants;
import org.xmlcml.cml.element.CMLCml;
import org.xmlcml.cml.element.CMLIdentifier;
import org.xmlcml.cml.element.CMLMolecule;

import wwmm.crystaleye.AbstractManager;
import wwmm.crystaleye.commandline.Execute;
import wwmm.crystaleye.properties.SiteProperties;
import wwmm.crystaleye.util.CrystalEyeUtils;
import wwmm.crystaleye.util.Utils;

public class SmilesListManager extends AbstractManager implements CMLConstants {

	private SiteProperties properties;

	public SmilesListManager(File propertiesFile) {
		this.setProperties(propertiesFile);
	}

	public SmilesListManager(String propertiesPath) {
		this(new File(propertiesPath));
	}

	private void setProperties(File propertiesFile) {
		properties = new SiteProperties(propertiesFile);
	}

	public void execute() {
		String[] publisherAbbreviations = properties.getPublisherAbbreviations();
		boolean newIssue = false;
		for (String publisherAbbreviation : publisherAbbreviations) {
			String[] journalAbbreviations = properties.getPublisherJournalAbbreviations(publisherAbbreviation);
			for (String journalAbbreviation : journalAbbreviations) {
				String downloadLogPath = properties.getDownloadLogPath();
				List<IssueDate> unprocessedDates = this.getUnprocessedDates(downloadLogPath, publisherAbbreviation, journalAbbreviation, SMILESLIST, WEBPAGE);
				if (unprocessedDates.size() != 0) {
					for (IssueDate date : unprocessedDates) {
						newIssue = true;
						String summaryWriteDir = properties.getSummaryWriteDir();
						String year = date.getYear();
						String issueNum = date.getIssue();
						String issueWriteDir = Utils.convertFileSeparators(summaryWriteDir+File.separator+
								publisherAbbreviation+File.separator+journalAbbreviation+File.separator+
								year+File.separator+issueNum);
						this.process(issueWriteDir);
						updateProps(downloadLogPath, publisherAbbreviation, journalAbbreviation, year, issueNum, SMILESLIST);
					}
				} else {
					System.out.println("No dates to process at this time for "+publisherAbbreviation+" journal "+journalAbbreviation);
				}
			}
		}
		if (newIssue) {
			updateIndex();
		}
	}
	
	private void updateIndex() {
		String smilesListPath = properties.getSmilesListPath();
		String command = "babel "+smilesListPath+" -ofs";
		Execute.run(command);
	}

	public void process(String issueWriteDir) {
		// get current contents of SMILES file
		String smilesListPath = properties.getSmilesListPath();
		File outFile = new File(smilesListPath);
		StringBuffer sb = new StringBuffer();

		List<File> fileList = new ArrayList<File>();
		if (new File(issueWriteDir).exists()) {
			fileList = CrystalEyeUtils.getSummaryDirFileList(issueWriteDir, "[^\\._]*_[^\\.]*"+COMPLETE_CML_MIME_REGEX);
			if (fileList.size() > 0) {
				for (File cmlFile : fileList) {
					try {
						CMLCml cml = (CMLCml)new CMLBuilder().build(new BufferedReader(new FileReader(cmlFile))).getRootElement();
						CMLMolecule molecule = (CMLMolecule)cml.getFirstCMLChild(CMLMolecule.TAG);
						String cmlId = cml.getId();
						Nodes smilesNodes = molecule.query("./"+CMLIdentifier.NS+"[@convention='daylight:smiles']", CML_XPATH);
						if (smilesNodes.size() > 0) {
							String smiles = smilesNodes.get(0).getValue();
							smiles = smiles.replaceAll("/", "");
							smiles = smiles.replaceAll("\\\\", "");
							if (!smiles.trim().equals("") && !smiles.trim().equals(" ")) {
								sb.append(smiles+" "+cmlId+"\n");
							}
						} else {
							System.err.println("Could not find SMILES in file "+cmlFile.getAbsolutePath());
						}
					} catch (Exception e) {
						e.printStackTrace();
					} catch (OutOfMemoryError o) {
						System.err.println("SKIPPING: out of memory error processing: "+cmlFile.getAbsolutePath());
					}
				}
			}
		}

		if (!outFile.exists()) {
			Utils.writeText(sb.toString(), smilesListPath);
		} else {
			Utils.appendToFile(outFile, sb.toString());
		}
	}
	
	public static void main(String[] args) {
		SmilesListManager d = new SmilesListManager("e:/crystaleye-test2/docs/cif-flow-props.txt");
		//SmilesListManager d = new SmilesListManager("e:/data-test/docs/cif-flow-props.txt");
		d.execute();
	}
}
