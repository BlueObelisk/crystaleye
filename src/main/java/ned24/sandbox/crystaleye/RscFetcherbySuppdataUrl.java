package ned24.sandbox.crystaleye;

import static wwmm.crystaleye.CrystalEyeConstants.RSC_DOI_PREFIX;
import static wwmm.crystaleye.CrystalEyeConstants.X_XHTML;

import java.io.File;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;
import wwmm.crystaleye.util.HttpUtils;
import wwmm.crystaleye.util.Utils;

public class RscFetcherbySuppdataUrl {

	String year = null;
	String volume = "0";

	String HOMEPAGE_PREFIX = "http://www.rsc.org";
	String PUBLISHER_ABBREVIATION = "rsc";

	String downloadDir = "e:/rsc-test/data";

	public RscFetcherbySuppdataUrl() {
		;
	}

	public void run() {
		year = "2007";
		for (int i = 1; i <= 48; i++) {
			getIssue("dt", year, i);
		}

		/*
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
		 */
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
		} else {
			volume = "0";
		}
	}

	public void getIssue(String journalAbbreviation, String year, int issue) {
		setVolume(journalAbbreviation);
		String url = "http://rsc.org/Publishing/Journals/"+journalAbbreviation.toLowerCase()+"/article.asp?Journal="+journalAbbreviation+"81&VolumeYear="+year+volume+"&Volume="+volume+"&JournalCode="+journalAbbreviation+"&MasterJournalCode="+journalAbbreviation+"&SubYear="+year+"&type=Issue&Issue="+issue+"&x=11&y=5";
		System.out.println("fetching url: "+url);
		Document doc = HttpUtils.getWebpageMinusCommentsAsXML(url);
		Nodes articleLinks = doc.query("//x:a[contains(@href,'/Publishing/Journals/"+journalAbbreviation.toUpperCase()+"/article.asp?doi=') and preceding-sibling::x:strong[contains(text(),'DOI:')]]", X_XHTML);
		if (articleLinks.size() > 0) {
			System.out.println("Reading "+journalAbbreviation.toUpperCase()+" issue "+issue+" at "+url);
		}
		for (int i = 0; i < articleLinks.size(); i++) {
			String urlPostfix = ((Element)articleLinks.get(i)).getAttributeValue("href");
			String articleId = urlPostfix.substring(urlPostfix.length()-8);
			System.out.println(articleId);
			String suppdataYear = articleId.substring(0,2);
			String suppdataUrl = "http://pubs.rsc.org/suppdata/"+journalAbbreviation.toUpperCase()+"/"+suppdataYear+"/"+articleId+"/index.sht";
			System.out.println(suppdataUrl);
			Document suppdataDoc = HttpUtils.getWebpageMinusCommentsAsXML(suppdataUrl);
			Nodes cifLinks = suppdataDoc.query("//x:a[text()='Crystal structure data'] | //x:a[text()='Crystal Structure Data'] | //x:a[text()='Crystal Structure data'] | //x:a[text()='Crystal data'] | //x:a[text()='Crystal Data'] | //x:a[text()='Crystallographic Data'] | //x:a[text()='Crystallographic data']", X_XHTML);
			int cifLinkNum = 0;
			for (int k = 0; k < cifLinks.size(); k++) {
				System.out.println("yeah!");
				cifLinkNum++;
				sleep();
				String cifFileName = ((Element)cifLinks.get(k)).getAttributeValue("href");
				int idx = suppdataUrl.lastIndexOf("/");
				String parent = suppdataUrl.substring(0, idx);
				int idx1 = cifFileName.indexOf(".");
				String cifId = cifFileName.substring(0, idx1);
				String cifLink = parent+"/"+cifFileName;

				String cif = HttpUtils.fetchWebPage(cifLink);
				String pathMinusMime = downloadDir+File.separator+PUBLISHER_ABBREVIATION+File.separator+journalAbbreviation+File.separator+year+File.separator+issue+File.separator+cifId+File.separator+articleId;
				String cifPath = pathMinusMime+"sup"+cifLinkNum+".cif";
				String doiPath = pathMinusMime+".doi";
				String doi = RSC_DOI_PREFIX+"/"+articleId;
				Utils.writeText(cif, cifPath);
				Utils.writeText(doi, doiPath);
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
		RscFetcherbySuppdataUrl rsc = new RscFetcherbySuppdataUrl();
		rsc.run();
	}
}
