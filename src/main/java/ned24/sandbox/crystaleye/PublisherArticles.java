package ned24.sandbox.crystaleye;

import static wwmm.crystaleye.CrystalEyeConstants.X_XHTML;

import java.io.File;

import nu.xom.Document;
import nu.xom.Nodes;
import wwmm.crystaleye.IOUtils;

public class PublisherArticles {

	int startYear = 2001;
	int endYear = 2008;
	String acsStatsFilePath = "e:/acs-article-stats.txt";
	String rscStatsFilePath = "e:/rsc-article-stats.txt";

	public PublisherArticles() {
		;		
	}

	public void run() {
		//getRscArticleDetails();
		//getAcsArticleDetails();
	}
	
	public void getRscArticleDetails() {
		IOUtils.writeText("", rscStatsFilePath);
		String[] journalAbbs = {"cc","ce","cp","dt","gc","jm","nj","ob"};
		for (String journalAbbreviation : journalAbbs) {
			int journalTotal = 0;
			for (int i = startYear; i <= endYear; i++) {
				StringBuilder sb = new StringBuilder();
				int yearTotal = 0;
				String year = String.valueOf(i);
				for (int issue = 1; issue <= 53; issue++) {
					String volume = getRscVolume(journalAbbreviation, year);
					String url = "http://rsc.org/Publishing/Journals/"+journalAbbreviation.toLowerCase()
					+"/article.asp?Journal="+journalAbbreviation+"81&VolumeYear="+
					year+volume+"&Volume="+volume+"&JournalCode="+journalAbbreviation+
					"&MasterJournalCode="+journalAbbreviation+"&SubYear="+year+
					"&type=Issue&Issue="+issue+"&x=11&y=5";
					System.out.println(url);
					Document doc = IOUtils.parseWebPage(url);
					Nodes articleNodes = doc.query(".//x:strong/x:a[contains(@href,'article.asp')]", X_XHTML);
					int articles = articleNodes.size();
					System.out.println(articles);
					if (articles > 0) {
						yearTotal += articles;
						journalTotal += articles;
						sb.append(journalAbbreviation+", "+year+", "+issue+", "+articles+"\n");
					} else {
						break;
					}
				}
				sb.append("=============================================\n");
				sb.append("TOTAL FOR "+year+" : "+yearTotal+"\n");
				sb.append("=============================================\n");
				IOUtils.appendToFile(new File(rscStatsFilePath), sb.toString());
			}
		}
	}

	public void getAcsArticleDetails() {
		IOUtils.writeText("", acsStatsFilePath);
		String[] journalAbbs = {"cgdefu", "inocaj", "jacsat", "jnprdf", "joceah", "orgnd7", "orlef7"};
		for (String journalAbbreviation : journalAbbs) {
			int journalTotal = 0;
			for (int i = startYear; i <= endYear; i++) {
				StringBuilder sb = new StringBuilder();
				int yearTotal = 0;
				String year = String.valueOf(i);
				for (int issue = 1; issue <= 53; issue++) {
					String decade = year.substring(2, 3);
					String volume = getAcsVolume(journalAbbreviation, year);
					String url = "http://pubs3.acs.org/acs/journals/toc.page?incoden="
						+ journalAbbreviation.toLowerCase() + "&indecade=" + decade
						+ "&involume=" + volume + "&inissue=" + issue;
					System.out.println(url);
					Document doc = IOUtils.parseWebPage(url);
					Nodes articleNodes = doc.query(".//x:table[@border='1' and @width='100%' and @bordercolor='#CCCCCC' and @cellspacing='0' and @cellpadding='5']", X_XHTML);
					int articles = articleNodes.size();
					if (articles > 0) {
						yearTotal += articles;
						journalTotal += articles;
						sb.append(journalAbbreviation+", "+year+", "+issue+", "+articles+"\n");
					} else {
						break;
					}
				}
				sb.append("=============================================\n");
				sb.append("TOTAL FOR "+year+" : "+yearTotal+"\n");
				sb.append("=============================================\n");
				IOUtils.appendToFile(new File(acsStatsFilePath), sb.toString());
			}
		}
	}
	
	public String getRscVolume(String journalAbbreviation, String year) {
		if ("ob".equalsIgnoreCase(journalAbbreviation)) {
			return String.valueOf((Integer.parseInt(year)-2003)+1);
		} else if ("ce".equalsIgnoreCase(journalAbbreviation)) {
			return String.valueOf((Integer.parseInt(year)-1999)+1);
		} else if ("nj".equalsIgnoreCase(journalAbbreviation)) {
			return String.valueOf((Integer.parseInt(year)-1998)+22);
		} else if ("jm".equalsIgnoreCase(journalAbbreviation)) {
			return String.valueOf((Integer.parseInt(year)-1991)+1);
		} else if ("cp".equalsIgnoreCase(journalAbbreviation)) {
			return String.valueOf((Integer.parseInt(year)-1999)+1);
		} else if ("fd".equalsIgnoreCase(journalAbbreviation)) {
			return String.valueOf((Integer.parseInt(year)-1991)+92);
		} else if ("gc".equalsIgnoreCase(journalAbbreviation)) {
			return String.valueOf((Integer.parseInt(year)-1999)+1);
		} else if ("em".equalsIgnoreCase(journalAbbreviation)) {
			return String.valueOf((Integer.parseInt(year)-1999)+1);
		} else if ("np".equalsIgnoreCase(journalAbbreviation)) {
			return String.valueOf((Integer.parseInt(year)-1984)+1);
		} else if ("cc".equalsIgnoreCase(journalAbbreviation) ||
				"dt".equalsIgnoreCase(journalAbbreviation)) {
			return "0";
		} else {
			throw new RuntimeException("Unknown RSC journal abbreviation: "+journalAbbreviation);
		}
	}

	public String getAcsVolume(String journalAbbreviation, String year) {
		if ("cgdefu".equalsIgnoreCase(journalAbbreviation)) {
			return  String.valueOf((Integer.parseInt(year) - 2000));
		} else if ("inocaj".equalsIgnoreCase(journalAbbreviation)) {
			return  String.valueOf((Integer.parseInt(year) - 1961));
		} else if ("jacsat".equalsIgnoreCase(journalAbbreviation)) {
			return  String.valueOf((Integer.parseInt(year) - 1878));
		} else if ("jnprdf".equalsIgnoreCase(journalAbbreviation)) {
			return  String.valueOf((Integer.parseInt(year) - 1937));
		} else if ("joceah".equalsIgnoreCase(journalAbbreviation)) {
			return  String.valueOf((Integer.parseInt(year) - 1935));
		} else if ("orlef7".equalsIgnoreCase(journalAbbreviation)) {
			return  String.valueOf((Integer.parseInt(year) - 1998));
		} else if ("orgnd7".equalsIgnoreCase(journalAbbreviation)) {
			return  String.valueOf((Integer.parseInt(year) - 1981));
		} else {
			throw new RuntimeException("Unknown ACS journal abbreviation: "+journalAbbreviation);
		}
	}

	public static void main(String[] args) {
		PublisherArticles pub = new PublisherArticles();
		pub.run();
	}

}
