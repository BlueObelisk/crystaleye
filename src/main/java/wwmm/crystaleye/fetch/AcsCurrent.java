package wwmm.crystaleye.fetch;

import static wwmm.crystaleye.CrystalEyeConstants.X_XHTML;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;

import org.apache.log4j.Logger;

import wwmm.crystaleye.IssueDate;
import wwmm.crystaleye.util.WebUtils;

public class AcsCurrent extends CurrentIssueFetcher {
	
	private static final Logger LOG = Logger.getLogger(AcsCurrent.class);
	
	private static final String publisherAbbreviation = "acs";
	
	public AcsCurrent(File propertiesFile) {
		super(publisherAbbreviation, propertiesFile);
	}
	
	public AcsCurrent(String propertiesFile) {
		this(new File(propertiesFile));
	}

	protected IssueDate getCurrentIssueId(String journalAbbreviation) {
		Document doc = getCurrentIssueHtml(journalAbbreviation);
		Nodes journalInfo = doc.query(".//x:div[@id='tocMeta']", X_XHTML);
		int size = journalInfo.size();
		if (size != 1) {
			throw new RuntimeException("Expected to find 1 element containing" +
					" the year/issue information but found "+size+".");
		}
		String info = journalInfo.get(0).getValue().trim();
		Pattern pattern = Pattern.compile("[^,]*,\\s*(\\d+)\\s+Volume\\s+(\\d+),\\s+Issue\\s+(\\d+)\\s+Pages\\s+(\\d+-\\d+).*");
		Matcher matcher = pattern.matcher(info);
		if (!matcher.find() || matcher.groupCount() != 4) {
			throw new RuntimeException("Could not extract the year/issue information.");
		}
		String year = matcher.group(1);
		String issueId = matcher.group(3);
		return new IssueDate(year, issueId);
	}
	
	/**
	 * <p>
	 * Gets the HTML of the table of contents of the last 
	 * published issue of the provided journal.
	 * </p>
	 * 
	 * @return HTML of the issue table of contents.
	 * 
	 */
	public Document getCurrentIssueHtml(String journalAbbreviation) {
		String url = "http://pubs.acs.org/toc/"+journalAbbreviation+"/current";
		return WebUtils.parseWebPage(url);
	}
	
	private String getJournalVolumeFromYear(String journalAbbreviation, String year) {
		String volume = null;
		for (AcsJournal jrnl : AcsJournal.values()) {
			if (jrnl.getAbbreviation().equals(journalAbbreviation)) {
				int i = Integer.parseInt(year)-jrnl.getVolumeOffset();
				volume = String.valueOf(i);
			}
		}
		if (volume == null) {
			throw new RuntimeException("Coudln't match journal abbreviation ("+journalAbbreviation+
					") to one in AcsJournal enum.");
		}
		return volume;
	}

	protected void fetch(String issueWriteDir, String journalAbbreviation, String year, String issueNum) {
		String volume = getJournalVolumeFromYear(journalAbbreviation, year);
		String issueUrl = "http://pubs.acs.org/toc/"+journalAbbreviation+"/"+volume+"/"+issueNum;
		Document doc = WebUtils.parseWebPage(issueUrl);
		Nodes suppLinks = doc.query(".//x:a[contains(@href,'/doi/suppl/10.1021')]", X_XHTML);
		sleep();
		if (suppLinks.size() > 0) {
			for (int j = 0; j < suppLinks.size(); j++) {
				String suppUrlPostfix = ((Element)suppLinks.get(j)).getAttributeValue("href");
				String suppUrl = "http://pubs.acs.org"+suppUrlPostfix;
				int idx = suppUrl.lastIndexOf("/");
				String cifId = suppUrl.substring(idx+1);
				doc = WebUtils.parseWebPage(suppUrl);
				sleep();
				
				Nodes cifLinks = doc.query(".//x:a[contains(@href,'.cif')]", X_XHTML);
				if (cifLinks.size() > 0) {
					for (int k = 0; k < cifLinks.size(); k++) {
						String cifUrl = "http://pubs.acs.org"+((Element)cifLinks.get(k)).getAttributeValue("href");
						int suppNum = k+1;
						String cif = getWebPage(cifUrl);
						Nodes doiAnchors = doc.query("//x:a[contains(@href,'dx.doi.org')]", X_XHTML);
						String doi = null;
						if (doiAnchors.size() > 0) {
							doi = ((Element)doiAnchors.get(0)).getValue();
						}
						writeFiles(issueWriteDir, cifId, suppNum, cif, doi);
						sleep();
					}
				}
			}
		}
		LOG.info("FINISHED FETCHING CIFS FROM "+issueUrl);
	}

	public static void main(String[] args) {
		AcsCurrent acs = new AcsCurrent("E:\\crystaleye-new\\docs\\cif-flow-props.txt");
		acs.execute();
	}
}
