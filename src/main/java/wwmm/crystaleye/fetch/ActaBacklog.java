package wwmm.crystaleye.fetch;

import static wwmm.crystaleye.CrystalEyeConstants.X_XHTML;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Node;
import nu.xom.Nodes;

import org.apache.log4j.Logger;

import wwmm.crystaleye.IOUtils;

public class ActaBacklog extends Fetcher {
	
	private static final Logger LOG = Logger.getLogger(ActaBacklog.class);

	private static final String SITE_PREFIX = "http://journals.iucr.org";
	private static final String PUBLISHER_ABBREVIATION = "acta";

    String journalAbbreviation;
    String year;
    String issueNum;
    String issuePart;

    public ActaBacklog(String propertiesFile, String journalAbbreviation, String year, String issueNum, String issuePart) {
    	super(PUBLISHER_ABBREVIATION, propertiesFile);
        setYear(year);
        setJournalAbbreviation(journalAbbreviation);
        setIssueNum(issueNum);
        setIssuePart(issuePart);
    }

    public void setYear(String year) {
        this.year = year;
    }

    public void setIssueNum(String issueNum) {
        this.issueNum= issueNum;
    }
    
    public void setIssuePart(String issuePart) {
        this.issuePart= issuePart;
    }
    
    public void setJournalAbbreviation(String journalAbbreviation) {
        this.journalAbbreviation = journalAbbreviation;
    }
	
	public void fetch() {
		String writeDir = properties.getWriteDir();
		String issueWriteDir = writeDir+File.separator+PUBLISHER_ABBREVIATION+File.separator+journalAbbreviation+
		File.separator+year+File.separator+issueNum+"-"+issuePart;
		Pattern pattern = Pattern.compile("http://scripts.iucr.org/cgi-bin/sendcif\\?(.*)");
		if (writeDir == null || journalAbbreviation == null || year == null || issueNum == null || issuePart == null) {
            throw new IllegalStateException(
                    "Make sure all parameters are set before calling fetch() method.");
        } else {
			String url = "http://journals.iucr.org/"+journalAbbreviation+"/issues/"+year+"/"+issueNum+"/"+issuePart+"/isscontsbdy.html";
			LOG.info("Fetching CIFs from: "+url);
			Document doc = IOUtils.parseWebPage(url);
			Nodes tocEntries = doc.query("//x:div[@class='toc entry']", X_XHTML);
			sleep();
			if (tocEntries.size() > 0) {
				for (int i = 0; i < tocEntries.size(); i++) {
					Node tocEntry = tocEntries.get(i);
					Nodes cifLinks = tocEntry.query(".//x:img[contains(@src,'"+journalAbbreviation+"/graphics/cifborder.gif')]/parent::x:*", X_XHTML);
					if (cifLinks.size() > 0) {
						String cifWriteDir = "";
						String cifId = "";
						for (int j = 0; j < cifLinks.size(); j++) {
							Node cifLink = cifLinks.get(j);
							String cifUrl = ((Element)cifLink).getAttributeValue("href");
							Matcher matcher = pattern.matcher(cifUrl);
							if (matcher.find()) {
								cifId = matcher.group(1);
							} else {
								throw new RuntimeException("Could not find the CIF ID.");
							}
							cifWriteDir = issueWriteDir+File.separator+cifId.substring(0,cifId.length()-4);
							String result = IOUtils.fetchWebPage(cifUrl);
							String cifPath = cifWriteDir+File.separator+cifId+".cif";
							LOG.info("Writing CIF to "+cifPath);
							IOUtils.writeText(result, cifPath);
							sleep();
						}
						Nodes doiNodes = tocEntry.query(".//x:p/x:font[@size='2']", X_XHTML);
						if (doiNodes.size() > 0) {
							String doi = ((Element)doiNodes.get(0)).getValue().substring(4);
							IOUtils.writeText(doi, cifWriteDir+File.separator+cifId.substring(0,cifId.length()-4)+".doi");
						} else {
							System.err.println("Could not find the DOI for this toc entry.");
						}
						Nodes checkCifNodes = tocEntry.query(".//x:img[contains(@src,'/"+journalAbbreviation+"/graphics/checkcifborder.gif')]/parent::x:*", X_XHTML);
						if (checkCifNodes.size() > 0) {
							for (int j = 0; j < checkCifNodes.size(); j++) {
								Node checkCifLink = checkCifNodes.get(j);
								String checkCifUrl = ((Element)checkCifLink).getAttributeValue("href");
								String result = IOUtils.fetchWebPage(SITE_PREFIX+checkCifUrl);
								IOUtils.writeText(result.toString(), cifWriteDir+File.separator+cifId+".deposited.checkcif.html");
								sleep();
							}
						}
					}
				}
			}
			LOG.info("FINISHED FETCHING CIFS FROM "+url);
		}
	}
	
	public static void main(String[] args) {
		String props = "c:/Users/ned24/workspace/crystaleye-trunk-data/docs/cif-flow-props.txt";
		ActaBacklog acta = new ActaBacklog(props, "c","2009", "07", "00");
		acta.fetch();
	}
}
