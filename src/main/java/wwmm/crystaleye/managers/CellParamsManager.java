package wwmm.crystaleye.managers;

import static org.xmlcml.cml.base.CMLConstants.CML_XPATH;
import static wwmm.crystaleye.CrystalEyeConstants.CELLPARAMS;
import static wwmm.crystaleye.CrystalEyeConstants.COMPLETE_CML_MIME_REGEX;
import static wwmm.crystaleye.CrystalEyeConstants.WEBPAGE;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import nu.xom.Nodes;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.xmlcml.cml.element.CMLCml;
import org.xmlcml.cml.element.CMLCrystal;
import org.xmlcml.cml.element.CMLMolecule;

import wwmm.crystaleye.AbstractManager;
import wwmm.crystaleye.CrystalEyeJournals;
import wwmm.crystaleye.IssueDate;
import wwmm.crystaleye.JournalDetails;
import wwmm.crystaleye.util.CrystalEyeUtils;
import wwmm.crystaleye.util.Utils;

public class CellParamsManager extends AbstractManager {

	private static final Logger LOG = Logger.getLogger(CellParamsManager.class);

	private CellParamsManager() {
		;
	}

	public CellParamsManager(File propertiesFile) {
		this.setProperties(propertiesFile);
	}

	public void execute() {
		String processLogPath = properties.getProcessLogPath();
		for (JournalDetails journalDetails : new CrystalEyeJournals().getDetails()) {
			String publisherAbbreviation = journalDetails.getPublisherAbbreviation();
			String journalAbbreviation = journalDetails.getJournalAbbreviation();
			List<IssueDate> unprocessedDates = this.getUnprocessedDates(processLogPath, publisherAbbreviation, journalAbbreviation, CELLPARAMS, WEBPAGE);
			if (unprocessedDates.size() != 0) {
				for (IssueDate date : unprocessedDates) {
					String summaryWriteDir = properties.getSummaryDir();
					String year = date.getYear();
					String issueNum = date.getIssue();
					String issueWriteDir = FilenameUtils.separatorsToUnix(summaryWriteDir+"/"+
							publisherAbbreviation+"/"+journalAbbreviation+"/"+
							year+"/"+issueNum);
					this.process(issueWriteDir);
					updateProcessLog(processLogPath, publisherAbbreviation, journalAbbreviation, year, issueNum, CELLPARAMS);
				}
			} else {
				LOG.info("No dates to process at this time for "+publisherAbbreviation+" journal "+journalAbbreviation);
			}
		}
	}

	public void process(String issueWriteDir) {		
		List<File> fileList = new ArrayList<File>();
		StringBuilder sb = new StringBuilder();
		if (new File(issueWriteDir).exists()) {
			fileList = CrystalEyeUtils.getSummaryDirFileList(issueWriteDir, "[^\\._]*_[^\\.]*"+COMPLETE_CML_MIME_REGEX);
			if (fileList.size() > 0) {
				for (File cmlFile : fileList ) {
					CMLCml cml = null;
					try {
						cml = (CMLCml)Utils.parseCml(cmlFile).getRootElement();
					} catch(Exception e) {
						LOG.warn("Error parsing CML: "+e.getMessage());
					}
					if (cml == null){
						continue;
					}
					CMLMolecule molecule = (CMLMolecule)cml.getFirstCMLChild(CMLMolecule.TAG);
					CMLCrystal crystal = (CMLCrystal)molecule.getFirstCMLChild(CMLCrystal.TAG);

					Nodes lengthANodes = crystal.query(".//cml:scalar[@dictRef='iucr:_cell_length_a']", CML_XPATH);
					String lengthA = "";
					if (lengthANodes.size() == 1) {
						lengthA  = lengthANodes.get(0).getValue();
					} else {
						throw new RuntimeException("Could not find lengthA node.");
					}

					Nodes lengthBNodes = crystal.query(".//cml:scalar[@dictRef='iucr:_cell_length_b']", CML_XPATH);
					String lengthB = "";
					if (lengthBNodes.size() == 1) {
						lengthB  = lengthBNodes.get(0).getValue();
					} else {
						throw new RuntimeException("Could not find lengthB node.");
					}

					Nodes lengthCNodes = crystal.query(".//cml:scalar[@dictRef='iucr:_cell_length_c']", CML_XPATH);
					String lengthC = "";
					if (lengthCNodes.size() == 1) {
						lengthC  = lengthCNodes.get(0).getValue();
					} else {
						throw new RuntimeException("Could not find lengthC node.");
					}

					Nodes angleANodes = crystal.query(".//cml:scalar[@dictRef='iucr:_cell_angle_alpha']", CML_XPATH);
					String angleA = "";
					if (angleANodes.size() == 1) {
						angleA  = angleANodes.get(0).getValue();
					} else {
						throw new RuntimeException("Could not find angleA node.");
					}

					Nodes angleBNodes = crystal.query(".//cml:scalar[@dictRef='iucr:_cell_angle_beta']", CML_XPATH);
					String angleB = "";
					if (angleBNodes.size() == 1) {
						angleB  = angleBNodes.get(0).getValue();
					} else {
						throw new RuntimeException("Could not find angleB node.");
					}

					Nodes angleGNodes = crystal.query(".//cml:scalar[@dictRef='iucr:_cell_angle_gamma']", CML_XPATH);
					String angleG = "";
					if (angleGNodes.size() == 1) {
						angleG  = angleGNodes.get(0).getValue();
					} else {
						throw new RuntimeException("Could not find angleG node.");
					}
					sb.append(lengthA+","+lengthB+","+lengthC+","+angleA+","+angleB+","+angleG+","+cml.getId()+"\n");
				}
			}
		}

		File cellParamsFile = new File(properties.getCellParamsFilePath());
		if (!cellParamsFile.exists()) {
			Utils.writeText(cellParamsFile, sb.toString());
		} else {
			Utils.appendToFile(cellParamsFile, sb.toString());
		}
	}

	public static void main(String[] args) {
		File propsFile = new File("e:/crystaleye-new/docs/cif-flow-props.txt");
		CellParamsManager d = new CellParamsManager(propsFile);
		d.execute();
	}
}
