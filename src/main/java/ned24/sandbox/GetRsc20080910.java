package ned24.sandbox;

import static wwmm.pubcrawler.core.CrawlerConstants.CIF_CONTENT_TYPE;

import java.io.File;
import java.util.List;

import wwmm.crystaleye.util.Utils;
import wwmm.pubcrawler.BasicHttpClient;
import wwmm.pubcrawler.core.ArticleDescription;
import wwmm.pubcrawler.core.ArticleReference;
import wwmm.pubcrawler.core.DOI;
import wwmm.pubcrawler.core.IssueDescription;
import wwmm.pubcrawler.core.RscJournal;
import wwmm.pubcrawler.core.SupplementaryResourceDescription;
import wwmm.pubcrawler.impl.CifIssueCrawler;
import wwmm.pubcrawler.impl.RscCifIssueCrawler;

public class GetRsc20080910 {

	private static BasicHttpClient httpClient = new BasicHttpClient();

	public static void main(String[] args) {
		

		for (int j = 1; j < 5; j++) {
			String issue = ""+j;
			while (true) {
				try {
					executeCrawler(new RscCifIssueCrawler(RscJournal.JOURNAL_OF_MATERIALS_CHEMISTRY), "rsc", "jm", "2010", issue);
				} catch (Exception e) {
					e.printStackTrace();
					continue;
				}
				break;
			}
		}
		for (int j = 18; j < 24; j++) {
			String issue = ""+j;
			while (true) {
				try {
					executeCrawler(new RscCifIssueCrawler(RscJournal.JOURNAL_OF_MATERIALS_CHEMISTRY), "rsc", "jm", "2010", issue);
				} catch (Exception e) {
					e.printStackTrace();
					continue;
				}
				break;
			}
		} 
		
		for (int j = 18; j < 22; j++) {
			String issue = ""+j;
			while (true) {
				try {
					executeCrawler(new RscCifIssueCrawler(RscJournal.CHEMCOMM), "rsc", "cc", "2010", issue);
				} catch (Exception e) {
					e.printStackTrace();
					continue;
				}
				break;
			}
		}
		executeCrawler(new RscCifIssueCrawler(RscJournal.CHEMCOMM), "rsc", "cc", "2010", "23");
		executeCrawler(new RscCifIssueCrawler(RscJournal.CHEMCOMM), "rsc", "cc", "2010", "24");
		
		executeCrawler(new RscCifIssueCrawler(RscJournal.CRYSTENGCOMM), "rsc", "ce", "2010", "5");
		
		for (int j = 18; j < 27; j++) {
			String issue = ""+j;
			while (true) {
				try {
					executeCrawler(new RscCifIssueCrawler(RscJournal.DALTON_TRANSACTIONS), "rsc", "dt", "2010", issue);
				} catch (Exception e) {
					e.printStackTrace();
					continue;
				}
				break;
			}
		}
		executeCrawler(new RscCifIssueCrawler(RscJournal.DALTON_TRANSACTIONS), "rsc", "dt", "2010", "3");
		executeCrawler(new RscCifIssueCrawler(RscJournal.DALTON_TRANSACTIONS), "rsc", "dt", "2010", "4");
		executeCrawler(new RscCifIssueCrawler(RscJournal.DALTON_TRANSACTIONS), "rsc", "dt", "2010", "13");
		
		executeCrawler(new RscCifIssueCrawler(RscJournal.ORGANIC_AND_BIOMOLECULAR_CHEMISTRY), "rsc", "ob", "2010", "11");
		executeCrawler(new RscCifIssueCrawler(RscJournal.ORGANIC_AND_BIOMOLECULAR_CHEMISTRY), "rsc", "ob", "2010", "12");
	}

	private static void executeCrawler(CifIssueCrawler crawler, String publisher, String journal, String year, String issue) {
		List<DOI> dois = null;
		try {
			dois = crawler.getDois(new IssueDescription(year, issue));
		} catch (Exception e) {
			return;
		}
		List<ArticleDescription> cifArticlesDetails = crawler.getArticleDescriptions(dois);
		for (ArticleDescription articleDetails : cifArticlesDetails) {
			ArticleReference ref = articleDetails.getReference();
			for (SupplementaryResourceDescription suppDetails : articleDetails.getSupplementaryResources()) {
				int count = 1;
				if (suppDetails.getContentType().contains(CIF_CONTENT_TYPE)) {
					String cifPath = createOutfilePath(publisher, journal, year, issue, suppDetails, "sup"+count+".cif");
					String cifUri = suppDetails.getURL();
					httpClient.writeResourceToFile(cifUri, new File(cifPath));
					String datePath = createOutfilePath(publisher, journal, year, issue, suppDetails, ".date");
					Utils.writeDateStamp(datePath);
					String doiPath = createOutfilePath(publisher, journal, year, issue, suppDetails, ".doi");
					Utils.writeText(new File(doiPath), articleDetails.getDoi().toString());
					count++;
				}
			}
		}
	}

	private static String createOutfilePath(String publisher, String journal, String year, String issue, SupplementaryResourceDescription suppDetails, String extension) {
		String fileId = suppDetails.getFileId();
		return "e:/crystaleye-2010-2/"+publisher+"/"+journal+"/"+year+"/"+issue+"/"+fileId+"/"+fileId+extension;
	}

}
