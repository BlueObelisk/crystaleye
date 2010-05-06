package ned24.sandbox;

import static wwmm.pubcrawler.core.CrawlerConstants.CIF_CONTENT_TYPE;

import java.io.File;
import java.util.List;

import org.apache.commons.httpclient.URI;

import wwmm.crystaleye.util.Utils;
import wwmm.pubcrawler.BasicHttpClient;
import wwmm.pubcrawler.core.AcsJournal;
import wwmm.pubcrawler.core.ArticleDescription;
import wwmm.pubcrawler.core.ArticleReference;
import wwmm.pubcrawler.core.DOI;
import wwmm.pubcrawler.core.IssueDescription;
import wwmm.pubcrawler.core.SupplementaryResourceDescription;
import wwmm.pubcrawler.impl.AcsCifIssueCrawler;
import wwmm.pubcrawler.impl.CifIssueCrawler;

public class GetAcs20080910 {

	private static BasicHttpClient httpClient = new BasicHttpClient();

	public static void main(String[] args) {
		for (AcsJournal acsJournal : AcsJournal.values()) {
			for (int i = 2008; i < 2011; i++) {
				for (int j = 1; j < 53; j++) {
					String year = ""+i;
					String issue = ""+j;
					executeCrawler(new AcsCifIssueCrawler(acsJournal), "acs", acsJournal.getAbbreviation(), year, issue);
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
				if (suppDetails.getContentType().contains(CIF_CONTENT_TYPE)) {
					String cifPath = createOutfilePath(publisher, journal, ref, suppDetails, ".cif");
					URI cifUri = suppDetails.getURI();
					httpClient.writeResourceToFile(cifUri, new File(cifPath));
					String datePath = createOutfilePath(publisher, journal, ref, suppDetails, ".date");
					Utils.writeDateStamp(datePath);
					String doiPath = createOutfilePath(publisher, journal, ref, suppDetails, ".doi");
					Utils.writeText(new File(doiPath), articleDetails.getDoi().toString());
				}
			}
		}
	}

	private static String createOutfilePath(String publisher, String journal, ArticleReference ref, SupplementaryResourceDescription suppDetails, String extension) {
		String fileId = suppDetails.getFileId();
		return "e:/crystaleye-2010/"+publisher+"/"+journal+"/"+ref.getYear()+"/"+ref.getNumber()+"/"+fileId+"/"+fileId+extension;
	}

}
