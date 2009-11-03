package wwmm.crystaleye.fetch;

import static wwmm.crystaleye.CrystalEyeConstants.*;

import java.io.File;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;

import org.apache.log4j.Logger;

import wwmm.crystaleye.CrystalEyeConstants;
import wwmm.crystaleye.util.Utils;
import wwmm.crystaleye.util.WebUtils;


public class RscBacklog extends Fetcher {

	private static final Logger LOG = Logger.getLogger(RscBacklog.class);

	private static final String HOMEPAGE_PREFIX = "http://www.rsc.org";
	private static final String PUBLISHER_ABBREVIATION = "rsc";

	String journalAbbreviation;
	String year;
	String issue;
	String volume = "0";

	public RscBacklog(String propertiesFile, String journalAbbreviation, String year, String issue) {
		super(PUBLISHER_ABBREVIATION, propertiesFile);
		setYear(year);
		setJournalAbbreviation(journalAbbreviation);
		setIssue(issue);
	}

	public void setJournalAbbreviation(String journalAbbreviation) {
		this.journalAbbreviation = journalAbbreviation;
		if ("ob".equalsIgnoreCase(journalAbbreviation)) {
			volume = String.valueOf((Integer.parseInt(year)-2003)+1);
		} else if ("ce".equalsIgnoreCase(journalAbbreviation)) {
			volume = String.valueOf((Integer.parseInt(year)-1999)+1);
		} else if ("nj".equalsIgnoreCase(journalAbbreviation)) {
			volume = String.valueOf((Integer.parseInt(year)-1998)+22);
		} else if ("jm".equalsIgnoreCase(journalAbbreviation)) {
			volume = String.valueOf((Integer.parseInt(year)-1991)+1);
		} else if ("cp".equalsIgnoreCase(journalAbbreviation)) {
			volume = String.valueOf((Integer.parseInt(year)-1999)+1);
		} else if ("fd".equalsIgnoreCase(journalAbbreviation)) {
			volume = String.valueOf((Integer.parseInt(year)-1991)+92);
		} else if ("gc".equalsIgnoreCase(journalAbbreviation)) {
			volume = String.valueOf((Integer.parseInt(year)-1999)+1);
		} else if ("em".equalsIgnoreCase(journalAbbreviation)) {
			volume = String.valueOf((Integer.parseInt(year)-1999)+1);
		} else if ("np".equalsIgnoreCase(journalAbbreviation)) {
			volume = String.valueOf((Integer.parseInt(year)-1984)+1);
		}
	}

	public void setYear(String year) {
		this.year = year;
	}

	public void setIssue(String issue) {
		this.issue = issue;
	}

	public void fetch() {
		String writeDir = properties.getWriteDir();
		String url = "http://rsc.org/Publishing/Journals/"+journalAbbreviation.toLowerCase()+"/article.asp?Journal="+journalAbbreviation+"81&VolumeYear="+year+volume+"&Volume="+volume+"&JournalCode="+journalAbbreviation+"&MasterJournalCode="+journalAbbreviation+"&SubYear="+year+"&type=Issue&Issue="+issue+"&x=11&y=5";
		Document doc = WebUtils.parseWebPageAndRemoveComments(url);
		Nodes articleLinks = doc.query("//x:a[contains(@href,'/Publishing/Journals/"+journalAbbreviation.toUpperCase()+"/article.asp?doi=') and preceding-sibling::x:strong[contains(text(),'DOI:')]]", X_XHTML);
		if (articleLinks.size() > 0) {
			LOG.info("Reading "+journalAbbreviation.toUpperCase()+" issue "+issue+" at "+url);
		}
		for (int i = 0; i < articleLinks.size(); i++) {
			String articleUrl = HOMEPAGE_PREFIX+((Element)articleLinks.get(i)).getAttributeValue("href");
			Document articleDoc = WebUtils.parseWebPageAndRemoveComments(articleUrl);
			Nodes suppdataLinks = articleDoc.query("//x:a[contains(text(),'Electronic supplementary information')]", X_XHTML);
			for (int j = 0; j < suppdataLinks.size(); j++) {
				sleep();
				String suppdataUrl = HOMEPAGE_PREFIX+((Element)suppdataLinks.get(j)).getAttributeValue("href");
				Document suppdataDoc = WebUtils.parseWebPageAndRemoveComments(suppdataUrl);
				Nodes cifLinks = suppdataDoc.query("//x:a[text()='Crystal structure data'] | //x:a[text()='Crystal Structure Data'] | //x:a[text()='Crystal Structure data'] | //x:a[text()='Crystal data'] | //x:a[text()='Crystal Data'] | //x:a[text()='Crystallographic Data'] | //x:a[text()='Crystallographic data']", X_XHTML);
				int cifLinkNum = 0;
				for (int k = 0; k < cifLinks.size(); k++) {
					cifLinkNum++;
					sleep();
					String cifFileName = ((Element)cifLinks.get(k)).getAttributeValue("href");
					int idx = suppdataUrl.lastIndexOf("/");
					String parent = suppdataUrl.substring(0, idx);
					int idx1 = cifFileName.indexOf(".");
					String cifId = cifFileName.substring(0, idx1);
					String cifLink = parent+"/"+cifFileName;

					String cif = WebUtils.fetchWebPage(cifLink);
					String pathMinusMime = writeDir+"/"+PUBLISHER_ABBREVIATION+"/"+journalAbbreviation+"/"+year+"/"+issue+"/"+cifId+"/"+cifId+"sup"+cifLinkNum;
					String cifPath = pathMinusMime+".cif";
					Utils.writeText(new File(cifPath), cif);
					String doiPath = pathMinusMime+".doi";
					Utils.writeText(new File(doiPath), CrystalEyeConstants.RSC_DOI_PREFIX+"/"+cifId.toLowerCase());
				}
			}
		}
	}

	public static void main(String[] args) {
		String props = "e:/crystaleye-new/docs/cif-flow-props.txt";

		for (int i = 1; i < 44; i++) {
			RscBacklog ore = new RscBacklog(props, "dt", "2009", ""+i);
			ore.fetch();
		}
		for (int i = 1; i < 44; i++) {
			RscBacklog ore = new RscBacklog(props, "cc", "2009", ""+i);
			ore.fetch();
		}
		for (int i = 1; i < 12; i++) {
			RscBacklog ore = new RscBacklog(props, "ce", "2009", ""+i);
			ore.fetch();
		}
		for (int i = 1; i < 44; i++) {
			RscBacklog ore = new RscBacklog(props, "cp", "2009", ""+i);
			ore.fetch();
		}	
		for (int i = 1; i < 10; i++) {
			RscBacklog ore = new RscBacklog(props, "gc", "2009", ""+i);
			ore.fetch();
		}
		for (int i = 1; i < 44; i++) {
			RscBacklog ore = new RscBacklog(props, "jm", "2009", ""+i);
			ore.fetch();
		}
		for (int i = 1; i < 12; i++) {
			RscBacklog ore = new RscBacklog(props, "nj", "2009", ""+i);
			ore.fetch();
		}
		for (int i = 1; i < 23; i++) {
			RscBacklog ore = new RscBacklog(props, "ob", "2009", ""+i);
			ore.fetch();
		}
		

	}
}
