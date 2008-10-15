package wwmm.crystaleye;

import static wwmm.crystaleye.CrystalEyeConstants.VALUE;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Nodes;
import wwmm.crystaleye.properties.StandardProperties;
import wwmm.crystaleye.util.Utils;

public abstract class AbstractManager {

	public void updateProps(String downloadLogPath, 
							String publisherAbbreviation,
							String journalAbbreviation,
							String year,
							String issueNum,
							String managerTag) {
		
		String issueCode = publisherAbbreviation+"_"+journalAbbreviation+"_"+year+"_"+issueNum;
		File propsPath = new File(downloadLogPath);
		Document doc = Utils.parseXmlFile(propsPath);
		Nodes procNodes = doc.query("//publisher[@abbreviation='"+publisherAbbreviation+
							"']/journal[@abbreviation='"+journalAbbreviation+"']/year[@id='"+year+
							"']/issue[@id='"+issueNum+"']/"+managerTag);
		
		if (procNodes.size() != 0){
			Element proc = (Element) procNodes.get(0);
			proc.getAttribute("value").setValue("true");
			Utils.writePrettyXML(doc, propsPath.getAbsolutePath());
			System.out.println("Updated "+downloadLogPath+" - "+managerTag+"=true ("+issueCode+")");
		} else {
			throw new CrystalEyeRuntimeException("Attempted to update "+downloadLogPath+
												" but could not locate element ("+issueCode+")");
		}
	}

	/**
	 * 
	 * @param downloadLogPath the path to the xml file of the download log
	 * @param publisherAbbreviation abbreviation for publisher from properties file
	 * @param journalAbbreviation abbreviation for journal from properties file
	 * @param managerTag name of element in download log it needs to check
	 * @param dependantManagerTag the tag of the process that needs to be run first
	 * @return
	 */
	public List<IssueDate> getUnprocessedDates( String downloadLogPath,
												String publisherAbbreviation,
												String journalAbbreviation,
												String managerTag,
												String dependantManagerTag) {
		
		List<IssueDate> outputList = new ArrayList<IssueDate>();

		File logFile = new File(downloadLogPath);
		Document doc = Utils.parseXmlFile(logFile);
		Nodes issues = doc.query("//publisher[@abbreviation='"+publisherAbbreviation+
									"']/journal[@abbreviation='"+journalAbbreviation+
									"']/descendant::issue");
		
		if (issues.size() > 0) {
			
			for (int i = 0; i < issues.size(); i++) {
				
				Element issueElement = (Element) issues.get(i);
				Elements managerElements = issueElement.getChildElements(managerTag);
				if (managerElements.size() == 0) {
					
					String issue = issueElement.getAttributeValue("id");
					Element yearNode = (Element)issueElement.getParent();
					String year = yearNode.getAttributeValue("id");
					throw new CrystalEyeRuntimeException("No '"+managerTag+
							"' element found for "+publisherAbbreviation+" journal "+
							journalAbbreviation.toUpperCase()+" year "+year+", issue "+issue);
					
				} else if (managerElements.size() == 1) {
					
					String value = managerElements.get(0).getAttributeValue(VALUE);
					if ("true".equalsIgnoreCase(value)) {
						continue;
					} else if ("false".equalsIgnoreCase(value)) {
						boolean start = false;
						if (dependantManagerTag == null) {
							start = true;
						} else {
							Elements previousManagerElements 
								= issueElement.getChildElements(dependantManagerTag);
							if (previousManagerElements.size() > 0) {
								String procValue = previousManagerElements.get(0).getAttributeValue(VALUE);
								if ("true".equalsIgnoreCase(procValue)) {
									start = true;
								}
							}
						}
						if (start) {
							String iss = issueElement.getAttributeValue("id");
							Element yearNode = (Element)issueElement.getParent();
							String year = yearNode.getAttributeValue("id");
							System.out.println("CIFs from "+publisherAbbreviation+"/"+
												journalAbbreviation+"/"+year+"/"+iss+
												" have yet to be processed through '"+
												managerTag+"' manager");
							outputList.add(new IssueDate(year, iss));
						}
					} else {
						throw new IllegalStateException("Invalid '"+managerTag+"' value.");
					}
				} else {
					throw new IllegalStateException("Should only be one "+managerTag+
							" element in "+downloadLogPath+".");
				}
			}
		} else {
			System.out.println("No dates to check in the props file!");
		}
		return outputList;
	}

	/**
	 * Nested iterations through each publisher, journal, year, issue
	 */
	public void execute() {
		
	}

	/**
	 * Nested iterations through each publisher, journal, year, issue
	 * @param writeDir the path to the root of the repository
	 */
	public void execute(String writeDir, StandardProperties properties) {
		//For all publishers
/*		String[] publisherAbbreviations = properties.getPublisherAbbreviations();
		for (String pubAbbrev : publisherAbbreviations) {
			
			//For all journals
			String[] journalAbbreviations = properties.getPublisherJournalAbbreviations(pubAbbrev);
			for (String journAbbrev : journalAbbreviations) {
								
				//Get path to download log file
				String downloadLogPath = properties.getDownloadLogPath();
				
				//Get unprocessed date from download log
				List<IssueDate> unprocessedDates = this.getUnprocessedDates(downloadLogPath, pubAbbrev, journAbbrev, CIF2CML, null);
				if (unprocessedDates.size() != 0) {
					for (IssueDate date : unprocessedDates) {
						String writeDir = properties.getWriteDir();
						String year = date.getYear();
						String issueNum = date.getIssue();
						String issueWriteDir = Utils.convertFileSeparators(writeDir+File.separator+
								pubAbbrev+File.separator+journAbbrev+File.separator+
								year+File.separator+issueNum);
						this.process(issueWriteDir, pubAbbrev, journAbbrev, year, issueNum);
						updateProps(downloadLogPath, pubAbbrev, journAbbrev, year, issueNum, CIF2CML);
					}
				} else {
					System.out.println("["+CIF2CML+"] No dates to process at this time for "+pubAbbrev+" journal "+journAbbrev);
				}
			}
		}*/

	}
	
	
	/**
	 * 
	 * @param issueWriteDir
	 * @param publisherAbbreviation
	 * @param journalAbbreviation
	 * @param year
	 * @param issueNum
	 */
	public void process(String issueWriteDir,
			String publisherAbbreviation,
			String journalAbbreviation,
			String year,
			String issueNum) {
	}
	
}
