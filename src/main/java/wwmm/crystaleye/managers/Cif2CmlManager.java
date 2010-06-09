package wwmm.crystaleye.managers;

import static wwmm.crystaleye.CrystalEyeConstants.CIF2CML;
import static wwmm.crystaleye.CrystalEyeConstants.COMPLETE_CML_MIME;
import static wwmm.crystaleye.CrystalEyeConstants.DATE_MIME;
import static wwmm.crystaleye.CrystalEyeConstants.MAX_CIF_SIZE_IN_BYTES;
import static wwmm.crystaleye.CrystalEyeConstants.RAW_CML_MIME;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import nu.xom.Document;
import nu.xom.Nodes;
import nu.xom.XPathContext;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.xmlcml.cml.converters.cif.CIF2CIFXMLConverter;
import org.xmlcml.cml.converters.cif.CIFXML2CMLConverter;
import org.xmlcml.cml.element.CMLCml;
import org.xmlcml.cml.element.CMLMolecule;

import wwmm.crystaleye.AbstractManager;
import wwmm.crystaleye.IssueDate;
import wwmm.crystaleye.tools.CheckCifParser;
import wwmm.crystaleye.tools.CheckCifTool;
import wwmm.crystaleye.tools.RawCml2CompleteCmlTool;
import wwmm.crystaleye.tools.SplitCifTool;
import wwmm.crystaleye.util.CMLUtils;
import wwmm.crystaleye.util.Utils;

public class Cif2CmlManager extends AbstractManager {

	private static final Logger LOG = Logger.getLogger(Cif2CmlManager.class);

	private Cif2CmlManager() {
		;
	}

	public Cif2CmlManager(File propertiesFile) {
		this.setProperties(propertiesFile);
	}

	public void execute() {
		String[] publisherAbbreviations = properties.getPublisherAbbreviations();
		for (String publisherAbbreviation : publisherAbbreviations) {
			String[] journalAbbreviations = properties.getPublisherJournalAbbreviations(publisherAbbreviation);
			for (String journalAbbreviation : journalAbbreviations) {
				String downloadLogPath = properties.getDownloadLogPath();
				List<IssueDate> unprocessedDates = this.getUnprocessedDates(downloadLogPath, publisherAbbreviation, journalAbbreviation, CIF2CML, null);
				if (unprocessedDates.size() != 0) {
					for (IssueDate date : unprocessedDates) {
						String writeDir = properties.getWriteDir();
						String year = date.getYear();
						String issueNum = date.getIssue();
						String issueWriteDir = FilenameUtils.separatorsToUnix(writeDir+"/"+
								publisherAbbreviation+"/"+journalAbbreviation+"/"+
								year+"/"+issueNum);
						this.process(issueWriteDir, publisherAbbreviation, journalAbbreviation, year, issueNum);
						updateProps(downloadLogPath, publisherAbbreviation, journalAbbreviation, year, issueNum, CIF2CML);
					}
				} else {
					LOG.info("No dates to process at this time for "+publisherAbbreviation+" journal "+journalAbbreviation);
				}
			}
		}
	}

	public void process(String issueWriteDir, String publisherAbbreviation, String journalAbbreviation, String year, String issueNum) {
		// go through to the article directories in the issue dir and process all found CIFs
		if (!new File(issueWriteDir).exists()) {
			throw new IllegalStateException("Issue directory at "+issueWriteDir+" should exist.");
		}
		for (File cifFile : getIssueCifFiles(issueWriteDir)) {
			LOG.info("Processing CIF file: "+cifFile);
			handleCif(cifFile, publisherAbbreviation, journalAbbreviation, year, issueNum);
		}
	}

	private void handleCif(File cifFile, String publisherAbbreviation, String journalAbbreviation, String year, String issueNum) {
		// calculate number of bytes in the file - if it is too large then do not try to parse
		if (fileTooLarge(cifFile)) {
			return;
		}

		List<File> splitCifList = null;
		try {
			splitCifList = new SplitCifTool().split(cifFile);
		} catch (Exception e) {
			LOG.warn("Could not split cif file ("+cifFile.getAbsolutePath()+"), due to: "+e.getMessage());
			return;
		}
		for (File splitCifFile : splitCifList) {
			if (isLongFile(splitCifFile)) {
				continue;
			}
			
			try {
				LOG.info("Processing split CIF file: "+splitCifFile);
				String splitCifPath = splitCifFile.getAbsolutePath();

				// parse split CIF to split cmls
				String pathMinusMime = Utils.getPathMinusMimeSet(splitCifFile);
				String suppId = pathMinusMime.substring(pathMinusMime.lastIndexOf(File.separator)+1);
				String articleId = suppId.substring(0,suppId.indexOf("_"));
				articleId = articleId.replaceAll("sup[\\d]*", "");
				String rawCmlPath = pathMinusMime+RAW_CML_MIME;

				// set up and run CIFConverter
				try {
					convertCif2RawCml(splitCifPath, rawCmlPath);
				} catch (Exception e) {
					LOG.warn("Error converting cif to xml: "+splitCifPath+" - "+e.getMessage());
					continue;
				}

				File rawCmlFile = new File(rawCmlPath);
				if (!rawCmlFile.exists()) {
					continue;
				}
				
				CMLCml cml = new RawCml2CompleteCmlTool().convert(rawCmlFile);
				Utils.writeDateStamp(pathMinusMime+DATE_MIME);
				Utils.writeXML(new File(pathMinusMime+COMPLETE_CML_MIME), cml.getDocument());
				String id = publisherAbbreviation+"_"+journalAbbreviation+"_"+year+"_"+issueNum+"_"+suppId;
				setCmlAndParentMolId(cml, id);
				
				calculateCheckCif(splitCifFile, pathMinusMime);
				handleCheckcifs(cml, pathMinusMime);

			} catch (Exception e) {
				LOG.warn("Error whilst processing: "+splitCifFile);
			} catch (OutOfMemoryError e) {
				LOG.warn("OutOfMemory whilst processing: "+splitCifFile);
			}
		}
	}
	
	private boolean isLongFile(File file) {
		String path = file.getAbsolutePath();
		if (path.contains("cg900220g")) {
			return true;
		}
		return false;
	}
	
	private void setCmlAndParentMolId(CMLCml cml, String id) {
		cml.setId(id);
		CMLMolecule molecule = CMLUtils.getFirstParentMolecule(cml);
		if (molecule != null) {
			molecule.setId(id);
		}
	}
	
	private void calculateCheckCif(File cifFile, String pathMinusMime) {
		String calculatedCheckCif = new CheckCifTool().getCheckcifString(cifFile);
		Utils.writeText(new File(pathMinusMime+".calculated.checkcif.html"), calculatedCheckCif);
	}
	
	private void getPlatonImage(Document doc, String pathMinusMime) {
		// get platon from parsed checkcif/store
		Nodes platonLinks = doc.query("//x:checkCif/x:calculated/x:dataBlock/x:platon/x:link", new XPathContext("x", "http://journals.iucr.org/services/cif"));
		if (platonLinks.size() > 0) {
			URL url = null;
			try {
				String imageLink = platonLinks.get(0).getValue();
				String prefix = imageLink.substring(0, imageLink.lastIndexOf(File.separator)+1);
				String file = imageLink.substring(imageLink.lastIndexOf(File.separator)+1);
				url = new URL(prefix+file); 
			} catch (MalformedURLException e) {
				throw new RuntimeException("Platon image has malformed url: "+e.getMessage(), e);
			}
			BufferedImage image = null;
			try {
				image = ImageIO.read(url);
				image = image.getSubimage(14, 15, 590, 443);
				ImageIO.write(image, "jpeg", new File(pathMinusMime+".platon.jpeg"));
			} catch (IOException e) {
				LOG.warn("Could not get PLATON image, due to: "+e.getMessage());
			}
		}	
	}
	
	private void handleCheckcifs(CMLCml cml, String pathMinusMime) {
		String depositedCheckcifPath = pathMinusMime.substring(0,pathMinusMime.lastIndexOf(File.separator));
		String depCCParent = new File(depositedCheckcifPath).getParent();
		depositedCheckcifPath = depCCParent+pathMinusMime.substring(pathMinusMime.lastIndexOf(File.separator),pathMinusMime.lastIndexOf("_"))+".deposited.checkcif.html";
		String calculatedCheckcifPath = pathMinusMime+".calculated.checkcif.html";
		File depositedCheckcif = new File(depositedCheckcifPath);
		File calculatedCheckcif = new File(calculatedCheckcifPath);
		if (depositedCheckcif.exists()) {
			String contents = null;
			try {
				contents = FileUtils.readFileToString(depositedCheckcif);
			} catch (IOException e) {
				throw new RuntimeException("Exception reading file ("+depositedCheckcif+"), due to: "+e.getMessage(), e);
			} 
			Document deposDoc = new CheckCifParser(contents).parsePublished();
			cml.appendChild(deposDoc.getRootElement().copy());
		}
		if (calculatedCheckcif.exists()) {
			String contents = null;
			try {
				contents = FileUtils.readFileToString(calculatedCheckcif);
			} catch (IOException e) {
				throw new RuntimeException("Exception reading file ("+calculatedCheckcif+"), due to: "+e.getMessage(), e);
			}
			Document calcDoc = new CheckCifParser(contents).parseService();
			cml.appendChild(calcDoc.getRootElement().copy());
			this.getPlatonImage(calcDoc, pathMinusMime);	
		}
	}

	/**
	 * <p>
	 * Finds the CIF files associated with the issue directory provided
	 * and returns them as a list.
	 * </p>
	 * 
	 * @param issueDirPath - folder corresponding to a journal issue.
	 * 
	 * @return list of CIF files associated with the provided issue.
	 */
	private List<File> getIssueCifFiles(String issueDirPath) {
		List<File> files = new ArrayList<File>();
		for (File parent : new File(issueDirPath).listFiles()) { 
			for (File file : parent.listFiles()) {
				if (file.getName().matches("[^\\._]*\\.cif")) {
					files.add(file);
				}
			}
		}
		return files;
	}

	private boolean fileTooLarge(File file) {
		boolean tooLarge = false;
		if ((int)file.length() > MAX_CIF_SIZE_IN_BYTES) {
			LOG.warn("CIF file too large to parse.  Skipping: "+file.getAbsolutePath());
			tooLarge = true;
		}
		return tooLarge;
	}

	private void convertCif2RawCml(String infile, String outfile) {
		CIF2CIFXMLConverter conv1 = new CIF2CIFXMLConverter();
		String cifXmlPath = infile+".xml";
		File cifXmlFile = new File(cifXmlPath);
		conv1.convert(new File(infile), cifXmlFile);

		CIFXML2CMLConverter conv2 = new CIFXML2CMLConverter();
		conv2.convert(cifXmlFile, new File(outfile));
	}	

	public static void main(String[] args) {
		File propsFile = new File("c:/workspace/crystaleye-trunk-data/docs/cif-flow-props.txt");
		Cif2CmlManager acta = new Cif2CmlManager(propsFile);
		acta.execute();
	}
}
