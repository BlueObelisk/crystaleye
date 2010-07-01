package ned24.sandbox;

import static wwmm.pubcrawler.core.CrawlerConstants.CIF_CONTENT_TYPE;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.URI;

import wwmm.crystaleye.util.Utils;
import wwmm.pubcrawler.BasicHttpClient;
import wwmm.pubcrawler.core.AcsJournal;
import wwmm.pubcrawler.core.ArticleDescription;
import wwmm.pubcrawler.core.ArticleReference;
import wwmm.pubcrawler.core.DOI;
import wwmm.pubcrawler.core.IssueDescription;
import wwmm.pubcrawler.core.RscJournal;
import wwmm.pubcrawler.core.SupplementaryResourceDescription;
import wwmm.pubcrawler.impl.AcsCifIssueCrawler;
import wwmm.pubcrawler.impl.CifIssueCrawler;
import wwmm.pubcrawler.impl.RscCifIssueCrawler;

public class GetAcs20080910 {

	private static BasicHttpClient httpClient = new BasicHttpClient();

	public static void main(String[] args) {
		/*
		for (int j = 20; j < 24; j++) {
			String issue = ""+j;
			while (true) {
				try {
					executeCrawler(new AcsCifIssueCrawler(AcsJournal.JOURNAL_OF_THE_AMERICAN_CHEMICAL_SOCIETY), "acs", "jacsat", "2010", issue);
				} catch (Exception e) {
					e.printStackTrace();
					continue;
				}
				break;
			}
		}
		 */

		executeCrawler(new AcsCifIssueCrawler(AcsJournal.ORGANIC_LETTERS), "acs", "orlef7", "2001", "15");
	}

	private static void executeCrawler(CifIssueCrawler crawler, String publisher, String journal, String year, String issue) {
		List<DOI> dois = null;
		try {
			dois = crawler.getDois(new IssueDescription(year, issue));
		} catch (Exception e) {
			return;
		}
		List<DOI> filteredDois = new ArrayList<DOI>(dois.size());
		for (DOI doi : dois) {
			if (!doi.toString().contains("ol016147s")) {
				filteredDois.add(doi);
			}
		}
		List<ArticleDescription> cifArticlesDetails = crawler.getArticleDescriptions(filteredDois);
		for (ArticleDescription articleDetails : cifArticlesDetails) {
			String doiStr = articleDetails.getDoi().toString();
			String doiPostfix = doiStr.substring(doiStr.lastIndexOf("/")+1);
			for (SupplementaryResourceDescription suppDetails : articleDetails.getSupplementaryResources()) {
				int count = 1;
				if (suppDetails.getContentType().contains(CIF_CONTENT_TYPE)) {
					String cifPath = createOutfilePath(publisher, journal, year, issue, doiPostfix, "sup"+count+".cif");
					String cifUri = suppDetails.getURL();
					httpClient.writeResourceToFile(cifUri, new File(cifPath));
					String datePath = createOutfilePath(publisher, journal, year, issue, doiPostfix, ".date");
					Utils.writeDateStamp(datePath);
					String doiPath = createOutfilePath(publisher, journal, year, issue, doiPostfix, ".doi");
					Utils.writeText(new File(doiPath), articleDetails.getDoi().toString());
					count++;
				}
			}
		}
	}

	private static String createOutfilePath(String publisher, String journal, String year, String issue, String fileId, String extension) {
		return "e:/crystaleye-2010-2/"+publisher+"/"+journal+"/"+year+"/"+issue+"/"+fileId+"/"+fileId+extension;
	}

}
