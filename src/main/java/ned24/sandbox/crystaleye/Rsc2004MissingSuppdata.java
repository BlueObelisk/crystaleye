package ned24.sandbox.crystaleye;

import static uk.ac.cam.ch.crystaleye.CrystalEyeConstants.RSC_DOI_PREFIX;
import static uk.ac.cam.ch.crystaleye.CrystalEyeConstants.X_XHTML;

import java.io.File;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;
import uk.ac.cam.ch.crystaleye.IOUtils;

public class Rsc2004MissingSuppdata {

	String year = "2004";
	String volume = "0";

	String HOMEPAGE_PREFIX = "http://www.rsc.org";
	String PUBLISHER_ABBREVIATION = "rsc";

	String downloadDir = "e:/data-test/data";

	public Rsc2004MissingSuppdata() {
		;
	}

	public void run() {
		for (int i = 14; i <= 23; i++) {
			getIssue("cc", year, i);
			getIssue("dt", year, i);
		}
		getIssue("dt", year, 1);
		getIssue("dt", year, 2);
		getIssue("jm", year, 9);
		getIssue("jm", year, 14);
		getIssue("jm", year, 24);
		for (int i = 7; i <= 11; i++) {
			getIssue("nj", year, i);
		}
		getIssue("ob", year, 1);
		getIssue("ob", year, 13);
		getIssue("ob", year, 14);
		getIssue("ob", year, 15);
		getIssue("ob", year, 18);
		getIssue("ob", year, 19);
		getIssue("ob", year, 20);
		getIssue("ob", year, 22);
		getIssue("ob", year, 23);
	}

	public void setVolume(String journalAbbreviation) {
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

	public void getIssue(String journalAbbreviation, String year, int issue) {
		setVolume(journalAbbreviation);
		String url = "http://rsc.org/Publishing/Journals/"+journalAbbreviation.toLowerCase()+"/article.asp?Journal="+journalAbbreviation+"81&VolumeYear="+year+volume+"&Volume="+volume+"&JournalCode="+journalAbbreviation+"&MasterJournalCode="+journalAbbreviation+"&SubYear="+year+"&type=Issue&Issue="+issue+"&x=11&y=5";
		System.out.println("fetching url: "+url);
		Document doc = IOUtils.parseWebPageMinusComments(url);
		Nodes articleLinks = doc.query("//x:a[contains(@href,'/Publishing/Journals/"+journalAbbreviation.toUpperCase()+"/article.asp?doi=') and preceding-sibling::x:strong[contains(text(),'DOI:')]]", X_XHTML);
		if (articleLinks.size() > 0) {
			System.out.println("Reading "+journalAbbreviation.toUpperCase()+" issue "+issue+" at "+url);
		}
		for (int i = 0; i < articleLinks.size(); i++) {
			String urlPostfix = ((Element)articleLinks.get(i)).getAttributeValue("href");
			String articleId = urlPostfix.substring(urlPostfix.length()-8);
			String suppdataUrl = "http://pubs.rsc.org/suppdata/"+journalAbbreviation.toUpperCase()+"/b4/"+articleId+"/index.sht";
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
				String pathMinusMime = downloadDir+File.separator+PUBLISHER_ABBREVIATION+File.separator+journalAbbreviation+File.separator+year+File.separator+issue+File.separator+cifId+File.separator+cifId+"sup"+cifLinkNum;
				String cifPath = pathMinusMime+".cif";
				String doiPath = pathMinusMime+".doi";
				String doi = RSC_DOI_PREFIX+"/"+articleId;
				IOUtils.writeText(cif, cifPath);
				IOUtils.writeText(doi, doiPath);
			}
		}
	}

	protected void sleep() {
		int maxTime = Integer.valueOf(5000);
		try {
			Thread.sleep(((int) (maxTime * Math.random())));
		} catch (InterruptedException e) {
			System.err.println("Sleep interrupted.");
		}
	}

	public static void main(String[] args) {
		Rsc2004MissingSuppdata rsc = new Rsc2004MissingSuppdata();
		rsc.run();
	}
}
