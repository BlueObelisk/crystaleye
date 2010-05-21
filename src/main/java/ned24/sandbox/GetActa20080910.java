package ned24.sandbox;

import static wwmm.pubcrawler.core.CrawlerConstants.CIF_CONTENT_TYPE;

import java.io.File;
import java.util.List;

import org.apache.commons.httpclient.URI;

import wwmm.crystaleye.util.Utils;
import wwmm.pubcrawler.BasicHttpClient;
import wwmm.pubcrawler.core.ActaJournal;
import wwmm.pubcrawler.core.ArticleDescription;
import wwmm.pubcrawler.core.ArticleReference;
import wwmm.pubcrawler.core.DOI;
import wwmm.pubcrawler.core.IssueDescription;
import wwmm.pubcrawler.core.SupplementaryResourceDescription;
import wwmm.pubcrawler.impl.ActaCifIssueCrawler;
import wwmm.pubcrawler.impl.CifIssueCrawler;

public class GetActa20080910 {

	private static BasicHttpClient httpClient = new BasicHttpClient();

	public static void main(String[] args) {
		for (ActaJournal actaJournal : ActaJournal.values()) {
			for (int i = 2008; i < 2011; i++) {
				for (int j = 1; j < 13; j++) {
					String year = ""+i;
					String issue = j+"-00";
					if (issue.length() == 4) {
						issue = "0"+issue;
					}
					executeCrawler(new ActaCifIssueCrawler(actaJournal), "acta", actaJournal.getAbbreviation(), year, issue);
				}
			}
		}
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
		return "e:/crystaleye-2010/"+publisher+"/"+journal+"/"+year+"/"+issue+"/"+fileId+"/"+fileId+extension;
	}

}
