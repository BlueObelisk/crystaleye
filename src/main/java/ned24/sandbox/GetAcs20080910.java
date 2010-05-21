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
import wwmm.pubcrawler.impl.RscCifIssueCrawler;

public class GetAcs20080910 {

	private static BasicHttpClient httpClient = new BasicHttpClient();

	public static void main(String[] args) {
		for (AcsJournal acsJournal : AcsJournal.values()) {
			if (acsJournal.equals(AcsJournal.ACCOUNTS_OF_CHEMICAL_RESEARCH) ||
					acsJournal.equals(AcsJournal.ANALYTICAL_CHEMISTRY) ||
					acsJournal.equals(AcsJournal.BIOCHEMISTRY) ||
					acsJournal.equals(AcsJournal.BIOCONJUGATE_CHEMISTRY) ||
					acsJournal.equals(AcsJournal.BIOMACROMOLECULES) ||
					acsJournal.equals(AcsJournal.CHEMICAL_REVIEWS) ||
					acsJournal.equals(AcsJournal.CHEMISTRY_OF_MATERIALS) ||
					acsJournal.equals(AcsJournal.CRYSTAL_GROWTH_AND_DESIGN) ||
					acsJournal.equals(AcsJournal.ENERGY_AND_FUELS) ||
					acsJournal.equals(AcsJournal.INDUSTRIAL_AND_ENGINEERING_CHEMISTRY_RESEARCH) ||
					acsJournal.equals(AcsJournal.INORGANIC_CHEMISTRY) ||
					acsJournal.equals(AcsJournal.JOURNAL_OF_AGRICULTURAL_AND_FOOD_CHEMISTRY) ||
					acsJournal.equals(AcsJournal.JOURNAL_OF_CHEMICAL_AND_ENGINEERING_DATA) ||
					acsJournal.equals(AcsJournal.JOURNAL_OF_THE_AMERICAN_CHEMICAL_SOCIETY) ||
					acsJournal.equals(AcsJournal.JOURNAL_OF_CHEMICAL_INFORMATION_AND_MODELLING) ||
					acsJournal.equals(AcsJournal.JOURNAL_OF_COMBINATORIAL_CHEMISTRY) ||
					acsJournal.equals(AcsJournal.JOURNAL_OF_MEDICINAL_CHEMISTRY)) {
				continue;
			}
			for (int i = 2008; i < 2011; i++) {
				for (int j = 1; j < 53; j++) {
					if (acsJournal.equals(AcsJournal.JOURNAL_OF_NATURAL_PRODUCTS) &&
							2008 == i && j < 2) {
						continue;
					}
					
					String year = ""+i;
					String issue = ""+j;
					while (true) {
						try {
							executeCrawler(new AcsCifIssueCrawler(acsJournal), "acs", acsJournal.getAbbreviation(), year, issue);
						} catch (Exception e) {
							e.printStackTrace();
							continue;
						}
						break;
					}
					
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
				if (suppDetails.getContentType() != null && suppDetails.getContentType().contains(CIF_CONTENT_TYPE)) {
					String cifPath = createOutfilePath(publisher, journal, ref, suppDetails, ".cif");
					String cifUrl = suppDetails.getURL();
					httpClient.writeResourceToFile(cifUrl, new File(cifPath));
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
