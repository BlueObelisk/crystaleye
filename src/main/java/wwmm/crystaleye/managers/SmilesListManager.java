package wwmm.crystaleye.managers;

import static org.xmlcml.cml.base.CMLConstants.CML_XPATH;
import static wwmm.crystaleye.CrystalEyeConstants.COMPLETE_CML_MIME_REGEX;
import static wwmm.crystaleye.CrystalEyeConstants.SMILESLIST;
import static wwmm.crystaleye.CrystalEyeConstants.WEBPAGE;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import nu.xom.Nodes;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.log4j.Logger;
import org.xmlcml.cml.base.CMLBuilder;
import org.xmlcml.cml.element.CMLCml;
import org.xmlcml.cml.element.CMLIdentifier;
import org.xmlcml.cml.element.CMLMolecule;

import wwmm.crystaleye.AbstractManager;
import wwmm.crystaleye.CrystalEyeJournals;
import wwmm.crystaleye.IssueDate;
import wwmm.crystaleye.JournalDetails;
import wwmm.crystaleye.tools.Execute;
import wwmm.crystaleye.util.CrystalEyeUtils;
import wwmm.crystaleye.util.Utils;

public class SmilesListManager extends AbstractManager {

	private static final Logger LOG = Logger.getLogger(SmilesListManager.class);

	private SmilesListManager() {
		;
	}

	public SmilesListManager(File propertiesFile) {
		this.setProperties(propertiesFile);
	}

	public void execute() {
		String processLogPath = properties.getProcessLogPath();
		boolean newIssue = false;
		for (JournalDetails journalDetails : new CrystalEyeJournals().getDetails()) {
			String publisherAbbreviation = journalDetails.getPublisherAbbreviation();
			String journalAbbreviation = journalDetails.getJournalAbbreviation();
			List<IssueDate> unprocessedDates = this.getUnprocessedDates(processLogPath, publisherAbbreviation, journalAbbreviation, SMILESLIST, WEBPAGE);
			if (unprocessedDates.size() != 0) {
				for (IssueDate date : unprocessedDates) {
					newIssue = true;
					String summaryWriteDir = properties.getSummaryDir();
					String year = date.getYear();
					String issueNum = date.getIssue();
					String issueWriteDir = FilenameUtils.separatorsToUnix(summaryWriteDir+"/"+
							publisherAbbreviation+"/"+journalAbbreviation+"/"+
							year+"/"+issueNum);
					this.process(issueWriteDir);
					updateProcessLog(processLogPath, publisherAbbreviation, journalAbbreviation, year, issueNum, SMILESLIST);
				}
			} else {
				LOG.info("No dates to process at this time for "+publisherAbbreviation+" journal "+journalAbbreviation);
			}
		}
		if (newIssue) {
			updateIndex();
		}
	}


	private void updateIndex() {
		String smilesListPath = properties.getSmilesListPath();
		if (SystemUtils.IS_OS_WINDOWS) {
			String[] command = {"cmd.exe", "/C", "babel", smilesListPath, "-ofs"};
			new Execute().run(command);
		} else {
			String[] command = {"babel", smilesListPath, "-ofs"};
			new Execute().run(command);
		}
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
							if (!StringUtils.isEmpty(smiles)) {
								sb.append(smiles+" "+cmlId+"\n");
							}
						} else {
							LOG.warn("Could not find SMILES in file "+cmlFile.getAbsolutePath());
						}
					} catch (Exception e) {
						LOG.warn("Exception while calculating SMILES, due to: "+e.getMessage());
					} catch (OutOfMemoryError e) {
						LOG.warn("SKIPPING: out of memory error processing: "+cmlFile.getAbsolutePath());
					}
				}
			}
		}

		if (!outFile.exists()) {
			Utils.writeText(new File(smilesListPath), sb.toString());
		} else {
			Utils.appendToFile(outFile, sb.toString());
		}
	}

	public static void main(String[] args) {
		File propsFile = new File("e:/crystaleye-new/docs/cif-flow-props.txt");
		SmilesListManager d = new SmilesListManager(propsFile);
		d.execute();
	}
}
