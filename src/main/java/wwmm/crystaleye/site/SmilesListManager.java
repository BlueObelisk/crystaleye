package wwmm.crystaleye.site;

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
import org.apache.commons.lang.SystemUtils;
import org.apache.log4j.Logger;
import org.xmlcml.cml.base.CMLBuilder;
import org.xmlcml.cml.element.CMLCml;
import org.xmlcml.cml.element.CMLIdentifier;
import org.xmlcml.cml.element.CMLMolecule;

import wwmm.crystaleye.AbstractManager;
import wwmm.crystaleye.CrystalEyeProperties;
import wwmm.crystaleye.CrystalEyeUtils;
import wwmm.crystaleye.IOUtils;
import wwmm.crystaleye.IssueDate;
import wwmm.crystaleye.tools.Execute;

public class SmilesListManager extends AbstractManager {
	
	private static final Logger LOG = Logger.getLogger(SmilesListManager.class);

	private CrystalEyeProperties properties;

	public SmilesListManager(File propertiesFile) {
		this.setProperties(propertiesFile);
	}

	public SmilesListManager(String propertiesPath) {
		this(new File(propertiesPath));
	}

	private void setProperties(File propertiesFile) {
		properties = new CrystalEyeProperties(propertiesFile);
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
						String issueWriteDir = FilenameUtils.separatorsToUnix(summaryWriteDir+"/"+
								publisherAbbreviation+"/"+journalAbbreviation+"/"+
								year+"/"+issueNum);
						this.process(issueWriteDir);
						updateProps(downloadLogPath, publisherAbbreviation, journalAbbreviation, year, issueNum, SMILESLIST);
					}
				} else {
					LOG.info("No dates to process at this time for "+publisherAbbreviation+" journal "+journalAbbreviation);
				}
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
			Execute.run(command);
		} else {
			String[] command = {"babel", smilesListPath, "-ofs"};
			Execute.run(command);
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
							if (!smiles.trim().equals("") && !smiles.trim().equals(" ")) {
								sb.append(smiles+" "+cmlId+"\n");
							}
						} else {
							System.err.println("Could not find SMILES in file "+cmlFile.getAbsolutePath());
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
			IOUtils.writeText(new File(smilesListPath), sb.toString());
		} else {
			IOUtils.appendToFile(outFile, sb.toString());
		}
	}
	
	public static void main(String[] args) {
		SmilesListManager d = new SmilesListManager("c:/workspace/crystaleye-trunk-data/docs/cif-flow-props.txt");
		d.execute();
	}
}
