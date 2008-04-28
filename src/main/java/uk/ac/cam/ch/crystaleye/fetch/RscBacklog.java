package uk.ac.cam.ch.crystaleye.fetch;

import static uk.ac.cam.ch.crystaleye.CrystalEyeConstants.RSC_DOI_PREFIX;
import static uk.ac.cam.ch.crystaleye.CrystalEyeConstants.X_XHTML;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;
import uk.ac.cam.ch.crystaleye.IOUtils;


public class RscBacklog extends JournalFetcher {

	private static final String HOMEPAGE_PREFIX = "http://www.rsc.org";
	private static final String PUBLISHER_ABBREVIATION = "rsc";

	String journalAbbreviation;
	String year;
	String issue;
	String volume = "0";

	public RscBacklog(String journalAbbreviation, String year, String issue) {
		publisherAbbr = PUBLISHER_ABBREVIATION;
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

	public void fetchAll() {
		String url = "http://rsc.org/Publishing/Journals/"+journalAbbreviation.toLowerCase()+"/article.asp?Journal="+journalAbbreviation+"81&VolumeYear="+year+volume+"&Volume="+volume+"&JournalCode="+journalAbbreviation+"&MasterJournalCode="+journalAbbreviation+"&SubYear="+year+"&type=Issue&Issue="+issue+"&x=11&y=5";
		System.out.println("fetching url: "+url);
		Document doc = IOUtils.parseWebPageMinusComments(url);
		Nodes articleLinks = doc.query("//x:a[contains(@href,'/Publishing/Journals/"+journalAbbreviation.toUpperCase()+"/article.asp?doi=') and preceding-sibling::x:strong[contains(text(),'DOI:')]]", X_XHTML);
		System.out.println(articleLinks.size());
		if (articleLinks.size() > 0) {
			System.out.println("Reading "+journalAbbreviation.toUpperCase()+" issue "+issue+" at "+url);
		}
		for (int i = 0; i < articleLinks.size(); i++) {
			String articleUrl = HOMEPAGE_PREFIX+((Element)articleLinks.get(i)).getAttributeValue("href");
			Document articleDoc = IOUtils.parseWebPageMinusComments(articleUrl);
			Nodes suppdataLinks = articleDoc.query("//x:a[contains(text(),'Electronic supplementary information')]", X_XHTML);
			for (int j = 0; j < suppdataLinks.size(); j++) {
				sleep();
				String suppdataUrl = HOMEPAGE_PREFIX+((Element)suppdataLinks.get(j)).getAttributeValue("href");
				Document suppdataDoc = IOUtils.parseWebPageMinusComments(suppdataUrl);
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

					String cif = IOUtils.fetchWebPage(cifLink);
					String pathMinusMime = downloadDir+File.separator+PUBLISHER_ABBREVIATION+File.separator+journalAbbreviation+File.separator+year+File.separator+issue+File.separator+cifId+File.separator+cifId;
					String cifPath = pathMinusMime+"sup"+cifLinkNum+".cif";
					String doiPath = pathMinusMime+".doi";
					String doi = RSC_DOI_PREFIX+"/"+cifId;
					IOUtils.writeText(cif, cifPath);
					IOUtils.writeText(doi, doiPath);
				}
			}
		}
	}

	public static void main(String[] args) {
		Properties props;
		try {
			props = IOUtils.loadProperties("E:\\data-test\\docs\\cif-flow-props.txt");
			RscBacklog ore = new RscBacklog( "gc", "2007", "11");
			ore.setDownloadDir(new File(props.getProperty("write.dir")));
			ore.fetchAll();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
