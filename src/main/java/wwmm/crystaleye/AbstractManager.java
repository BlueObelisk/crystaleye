package wwmm.crystaleye;

import static wwmm.crystaleye.CrystalEyeConstants.VALUE;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Nodes;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import wwmm.crystaleye.util.Utils;


public abstract class AbstractManager {
	
	protected CrystalEyeProperties properties;
	
	private static final Logger LOG = Logger.getLogger(AbstractManager.class);
	
	protected void setProperties(File propertiesFile) {
		properties = new CrystalEyeProperties(propertiesFile);
	}

	public void updateProcessLog(String downloadLogPath, String publisherAbbreviation, String journalAbbreviation, String year, String issueNum, String managerTag) {
		String issueCode = publisherAbbreviation+"_"+journalAbbreviation+"_"+year+"_"+issueNum;
		File logFile = new File(downloadLogPath);
		String logTempPath = downloadLogPath+".temp";
		File logTempFile = new File(logTempPath);
		Document doc = Utils.parseXml(logFile);
		Nodes procNodes = doc.query("//publisher[@abbreviation='"+publisherAbbreviation+"']/journal[@abbreviation='"+journalAbbreviation+"']/year[@id='"+year+"']/issue[@id='"+issueNum+"']/"+managerTag);
		if (procNodes.size() != 0){
			Element proc = (Element) procNodes.get(0);
			proc.getAttribute("value").setValue("true");
			Utils.writeXML(logTempFile, doc);
			try {
				FileUtils.copyFile(logTempFile, logFile);
				FileUtils.forceDelete(logTempFile);
			} catch (IOException e) {
				LOG.info("Problem moving log temp file to proper location: "+e.getMessage());
			}
			LOG.info("Updated "+downloadLogPath+" - "+managerTag+"=true ("+issueCode+")");
		} else {
			throw new RuntimeException("Attempted to update "+downloadLogPath+" but could not locate element ("+issueCode+")");
		}
	}

	public List<IssueDate> getUnprocessedDates(String processLogPath, String publisherAbbreviation, 
			String journalAbbreviation, String managerTag, String previousManagerTag) {
		List<IssueDate> outputList = new ArrayList<IssueDate>();

		ProcessLog processLog = new ProcessLog(processLogPath);
		Document processLogContents = processLog.getContents();
		Nodes issues = processLogContents.query("//publisher[@abbreviation='"+publisherAbbreviation+"']/journal[@abbreviation='"+journalAbbreviation+"']/descendant::issue");
		if (issues.size() > 0) {
			for (int i = 0; i < issues.size(); i++) {
				Element issueElement = (Element) issues.get(i);
				Elements managerElements = issueElement.getChildElements(managerTag);
				if (managerElements.size() == 0) {
					String issue = issueElement.getAttributeValue("id");
					Element yearNode = (Element)issueElement.getParent();
					String year = yearNode.getAttributeValue("id");
					throw new RuntimeException("No '"+managerTag+"' element found for "+publisherAbbreviation+" journal "+journalAbbreviation.toUpperCase()+" year "+year+", issue "+issue);
				} else if (managerElements.size() == 1) {
					String value = managerElements.get(0).getAttributeValue(VALUE);
					if ("true".equalsIgnoreCase(value)) {
						continue;
					} else if ("false".equalsIgnoreCase(value)) {
						boolean start = false;
						if (previousManagerTag == null) {
							start = true;
						} else {
							Elements previousManagerElements = issueElement.getChildElements(previousManagerTag);
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
							LOG.info("CIFs from "+publisherAbbreviation+"/"+journalAbbreviation+"/"+year+"/"+iss+" have yet to be processed through '"+managerTag+"' manager");
							outputList.add(new IssueDate(year, iss));
						}
					} else {
						throw new IllegalStateException("Invalid '"+managerTag+"' value.");
					}
				} else {
					throw new IllegalStateException("Should only be one "+managerTag+" element in "+processLogPath+".");
				}
			}
		} else {
			LOG.info("No dates to check in the props file!");
		}
		return outputList;
	}

}
