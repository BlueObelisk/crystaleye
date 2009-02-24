package uk.ac.cam.ch.crystaleye.site;

import static uk.ac.cam.ch.crystaleye.CrystalEyeConstants.CELLPARAMS;
import static uk.ac.cam.ch.crystaleye.CrystalEyeConstants.COMPLETE_CML_MIME_REGEX;
import static uk.ac.cam.ch.crystaleye.CrystalEyeConstants.WEBPAGE;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import nu.xom.Nodes;

import org.xmlcml.cml.base.CMLConstants;
import org.xmlcml.cml.element.CMLCml;
import org.xmlcml.cml.element.CMLCrystal;
import org.xmlcml.cml.element.CMLMolecule;

import uk.ac.cam.ch.crystaleye.AbstractManager;
import uk.ac.cam.ch.crystaleye.CrystalEyeUtils;
import uk.ac.cam.ch.crystaleye.IOUtils;
import uk.ac.cam.ch.crystaleye.IssueDate;
import uk.ac.cam.ch.crystaleye.Utils;
import uk.ac.cam.ch.crystaleye.properties.SiteProperties;

public class CellParamsManager extends AbstractManager implements CMLConstants {

	private SiteProperties properties;

	public CellParamsManager(File propertiesFile) {
		this.setProperties(propertiesFile);
	}

	public CellParamsManager(String propertiesPath) {
		this(new File(propertiesPath));
	}

	private void setProperties(File propertiesFile) {
		properties = new SiteProperties(propertiesFile);
	}

	public void execute() {
		String[] publisherAbbreviations = properties.getPublisherAbbreviations();
		for (String publisherAbbreviation : publisherAbbreviations) {
			String[] journalAbbreviations = properties.getPublisherJournalAbbreviations(publisherAbbreviation);
			for (String journalAbbreviation : journalAbbreviations) {
				String downloadLogPath = properties.getDownloadLogPath();
				List<IssueDate> unprocessedDates = this.getUnprocessedDates(downloadLogPath, publisherAbbreviation, journalAbbreviation, CELLPARAMS, WEBPAGE);
				if (unprocessedDates.size() != 0) {
					for (IssueDate date : unprocessedDates) {
						String summaryWriteDir = properties.getSummaryWriteDir();
						String year = date.getYear();
						String issueNum = date.getIssue();
						String issueWriteDir = Utils.convertFileSeparators(summaryWriteDir+File.separator+
								publisherAbbreviation+File.separator+journalAbbreviation+File.separator+
								year+File.separator+issueNum);
						this.process(issueWriteDir);
						updateProps(downloadLogPath, publisherAbbreviation, journalAbbreviation, year, issueNum, CELLPARAMS);
					}
				} else {
					System.out.println("No dates to process at this time for "+publisherAbbreviation+" journal "+journalAbbreviation);
				}
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
					CMLCml cml = (CMLCml)IOUtils.parseCmlFile(cmlFile).getRootElement();
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
		IOUtils.appendToFile(new File(properties.getCellParamsFilePath()), sb.toString());
	}

	public static void main(String[] args) {
		//CellParamsManager d = new CellParamsManager("e:/crystaleye-test2/docs/cif-flow-props.txt");
		CellParamsManager d = new CellParamsManager("e:/data-test/docs/cif-flow-props.txt");
		d.execute();
	}
}