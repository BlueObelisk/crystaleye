package wwmm.crystaleye;

import static wwmm.crystaleye.CrystalEyeConstants.VALUE;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Nodes;

import org.apache.log4j.Logger;


public abstract class AbstractManager {
	
	private static final Logger LOG = Logger.getLogger(AbstractManager.class);

	public void updateProps(String downloadLogPath, String publisherAbbreviation, String journalAbbreviation, String year, String issueNum, String managerTag) {
		String issueCode = publisherAbbreviation+"_"+journalAbbreviation+"_"+year+"_"+issueNum;
		File propsPath = new File(downloadLogPath);
		Document doc = IOUtils.parseXml(propsPath);
		Nodes procNodes = doc.query("//publisher[@abbreviation='"+publisherAbbreviation+"']/journal[@abbreviation='"+journalAbbreviation+"']/year[@id='"+year+"']/issue[@id='"+issueNum+"']/"+managerTag);
		if (procNodes.size() != 0){
			Element proc = (Element) procNodes.get(0);
			proc.getAttribute("value").setValue("true");
			IOUtils.writeXML(doc, propsPath.getAbsolutePath());
			LOG.info("Updated "+downloadLogPath+" - "+managerTag+"=true ("+issueCode+")");
		} else {
			throw new RuntimeException("Attempted to update "+downloadLogPath+" but could not locate element ("+issueCode+")");
		}
	}

	public List<IssueDate> getUnprocessedDates(String downloadLogPath, String publisherAbbreviation, 
			String journalAbbreviation, String managerTag, String previousManagerTag) {
		List<IssueDate> outputList = new ArrayList<IssueDate>();

		File logFile = new File(downloadLogPath);
		Document doc = IOUtils.parseXml(logFile);
		Nodes issues = doc.query("//publisher[@abbreviation='"+publisherAbbreviation+"']/journal[@abbreviation='"+journalAbbreviation+"']/descendant::issue");
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
					throw new IllegalStateException("Should only be one "+managerTag+" element in "+downloadLogPath+".");
				}
			}
		} else {
			LOG.info("No dates to check in the props file!");
		}
		return outputList;
	}

}
