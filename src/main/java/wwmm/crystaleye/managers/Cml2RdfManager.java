package wwmm.crystaleye.managers;

import static wwmm.crystaleye.CrystalEyeConstants.CML2FOO;
import static wwmm.crystaleye.CrystalEyeConstants.CML2RDF;
import static wwmm.crystaleye.CrystalEyeConstants.COMPLETE_CML_MIME_REGEX;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.xmlcml.cml.converters.ConverterCommand;
import org.xmlcml.cml.converters.rdf.cml.CML2OWLRDFConverter;

import wwmm.crystaleye.AbstractManager;
import wwmm.crystaleye.IssueDate;
import wwmm.crystaleye.util.CrystalEyeUtils;

public class Cml2RdfManager extends AbstractManager {
	
	private static final Logger LOG = Logger.getLogger(Cml2RdfManager.class);

	private Cml2RdfManager() {
		;
	}
	
	public Cml2RdfManager(File propertiesFile) {
		this.setProperties(propertiesFile);
	}
	
	public void execute() {
		String[] publisherAbbreviations = properties.getPublisherAbbreviations();
		for (String publisherAbbreviation : publisherAbbreviations) {
			String[] journalAbbreviations = properties.getPublisherJournalAbbreviations(publisherAbbreviation);
			for (String journalAbbreviation : journalAbbreviations) {
				String downloadLogPath = properties.getDownloadLogPath();
				List<IssueDate> unprocessedDates = this.getUnprocessedDates(downloadLogPath, publisherAbbreviation, journalAbbreviation, CML2RDF, CML2FOO);
				if (unprocessedDates.size() != 0) {
					for (IssueDate date : unprocessedDates) {
						String writeDir = properties.getWriteDir();
						String year = date.getYear();
						String issueNum = date.getIssue();
						String issueWriteDir = FilenameUtils.separatorsToUnix(writeDir+"/"+
								publisherAbbreviation+"/"+journalAbbreviation+"/"+
								year+"/"+issueNum);
						this.process(issueWriteDir);
						updateProps(downloadLogPath, publisherAbbreviation, journalAbbreviation, year, issueNum, CML2RDF);
					}
				} else {
					LOG.info("No dates to process at this time for "+publisherAbbreviation+" journal "+journalAbbreviation);
				}
			}
		}
	}
	
	public void process(String issueWriteDir) {
		List<File> fileList = new ArrayList<File>();
		if (new File(issueWriteDir).exists()) {
			fileList = CrystalEyeUtils.getDataDirFileList(issueWriteDir, "[^\\._]*_[^\\.]*"+COMPLETE_CML_MIME_REGEX);
			if (fileList.size() > 0) {
				for (File cmlFile : fileList ) {
					LOG.info("Converting CML to RDF: "+cmlFile.getAbsolutePath());
					CML2OWLRDFConverter converter = new CML2OWLRDFConverter();
					ConverterCommand command = new ConverterCommand();
					command.setAuxfileName("org/xmlcml/cml/converters/rdf/cml/ontologies/cifCore1.owl");
					converter.setCommand(command);
					String filename = cmlFile.getName();
					String basename = filename.substring(0, filename.indexOf("."));
					File outFile = new File(cmlFile.getParentFile(), basename+".cml.rdf");
					converter.convert(cmlFile, outFile);
				}
			}
		}
	}
	
	public static void main(String[] args) {
		File propsFile = new File("e:/crystaleye-new/docs/cif-flow-props.txt");
		Cml2RdfManager manager = new Cml2RdfManager(propsFile);
		manager.execute();
	}

}
