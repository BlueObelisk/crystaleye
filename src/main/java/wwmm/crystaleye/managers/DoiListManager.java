package wwmm.crystaleye.managers;

import static org.xmlcml.cml.base.CMLConstants.CML_XPATH;
import static wwmm.crystaleye.CrystalEyeConstants.COMPLETE_CML_MIME_REGEX;
import static wwmm.crystaleye.CrystalEyeConstants.CRYSTALEYE_HOME_URL;
import static wwmm.crystaleye.CrystalEyeConstants.DOILIST;
import static wwmm.crystaleye.CrystalEyeConstants.WEBPAGE;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import nu.xom.Nodes;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.xmlcml.cml.base.CMLBuilder;
import org.xmlcml.cml.base.CMLElement;
import org.xmlcml.cml.element.CMLScalar;

import wwmm.crystaleye.AbstractManager;
import wwmm.crystaleye.CrystalEyeProperties;
import wwmm.crystaleye.CrystalEyeUtils;
import wwmm.crystaleye.IOUtils;
import wwmm.crystaleye.IssueDate;

public class DoiListManager extends AbstractManager {

	private static final Logger LOG = Logger.getLogger(DoiListManager.class);
	
	private CrystalEyeProperties properties;

	public DoiListManager(File propertiesFile) {
		this.setProperties(propertiesFile);
	}

	public DoiListManager(String propertiesPath) {
		this(new File(propertiesPath));
	}

	private void setProperties(File propertiesFile) {
		properties = new CrystalEyeProperties(propertiesFile);
	}

	public void execute() {
		String[] publisherAbbreviations = properties.getPublisherAbbreviations();
		for (String publisherAbbreviation : publisherAbbreviations) {
			String[] journalAbbreviations = properties.getPublisherJournalAbbreviations(publisherAbbreviation);
			for (String journalAbbreviation : journalAbbreviations) {
				String downloadLogPath = properties.getDownloadLogPath();
				List<IssueDate> unprocessedDates = this.getUnprocessedDates(downloadLogPath, publisherAbbreviation, journalAbbreviation, DOILIST, WEBPAGE);
				if (unprocessedDates.size() != 0) {
					for (IssueDate date : unprocessedDates) {
						String summaryWriteDir = properties.getSummaryWriteDir();
						String year = date.getYear();
						String issueNum = date.getIssue();
						String issueWriteDir = FilenameUtils.separatorsToUnix(summaryWriteDir+"/"+
								publisherAbbreviation+"/"+journalAbbreviation+"/"+
								year+"/"+issueNum);
						this.process(issueWriteDir);
						updateProps(downloadLogPath, publisherAbbreviation, journalAbbreviation, year, issueNum, DOILIST);
					}
				} else {
					LOG.info("No dates to process at this time for "+publisherAbbreviation+" journal "+journalAbbreviation);
				}
			}
		}
	}

	public void process(String issueWriteDir) {
		// get current contents of DOI file
		String doiListPath = properties.getDoiListPath();
		File outFile = new File(doiListPath);
		StringBuffer sb = new StringBuffer();

		List<File> fileList = new ArrayList<File>();
		if (new File(issueWriteDir).exists()) {
			fileList = CrystalEyeUtils.getSummaryDirFileList(issueWriteDir, "[^\\._]*_[^\\.]*"+COMPLETE_CML_MIME_REGEX);
			if (fileList.size() > 0) {
				for (File cmlFile : fileList) {
					try {
						CMLElement cml = (CMLElement)new CMLBuilder().build(new BufferedReader(new FileReader(cmlFile))).getRootElement();
						Nodes doiNodes = cml.query(".//"+CMLScalar.NS+"[@dictRef='idf:doi']", CML_XPATH);
						if (doiNodes.size() > 0) {
							String doi = doiNodes.get(0).getValue();
							String path = cmlFile.getAbsolutePath();
							String[] parts = null;
							if (File.separator.equals("/")) {
								parts = path.split("/");
							} else {
								parts = path.split("\\\\");
							}
							int k = parts.length-1;
							String cifSummaryUrl = CRYSTALEYE_HOME_URL+"/summary/"+parts[k-7]+"/"+
							parts[k-6]+"/"+parts[k-5]+"/"+parts[k-4]+"/"+parts[k-3]+"/"+parts[k-2]+"/"+parts[k-1]+"/"+
							parts[k-1]+".cif.summary.html";
							sb.append(doi+"="+cifSummaryUrl+"\n");
						} else {
							LOG.warn("Could not find DOI in file "+cmlFile.getAbsolutePath());
						}
					} catch (Exception e) {
						LOG.warn("Exception whilst updating DOI list file ("+doiListPath+"): "+e.getMessage());
					} catch (OutOfMemoryError o) {
						LOG.warn("Out of memory error processing: "+cmlFile.getAbsolutePath());
					}
				}
			}
		}

		if (!outFile.exists()) {
			IOUtils.writeText(new File(doiListPath), sb.toString());
		} else {
			IOUtils.appendToFile(outFile, sb.toString());
		}
	}

	public static void main(String[] args) {
		DoiListManager d = new DoiListManager("c:/workspace/crystaleye-trunk-data/docs/cif-flow-props.txt");
		d.execute();
	}
}
