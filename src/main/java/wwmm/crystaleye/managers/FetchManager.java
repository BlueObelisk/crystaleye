package wwmm.crystaleye.managers;

import static wwmm.pubcrawler.core.CrawlerConstants.CIF_CONTENT_TYPE;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.URI;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import wwmm.crystaleye.CrystalEyeProperties;
import wwmm.crystaleye.DownloadLog;
import wwmm.crystaleye.util.Utils;
import wwmm.pubcrawler.core.AcsJournal;
import wwmm.pubcrawler.core.ActaJournal;
import wwmm.pubcrawler.core.ArticleDescription;
import wwmm.pubcrawler.core.ChemSocJapanJournal;
import wwmm.pubcrawler.core.CrawlerHttpClient;
import wwmm.pubcrawler.core.DOI;
import wwmm.pubcrawler.core.IssueDescription;
import wwmm.pubcrawler.core.RscJournal;
import wwmm.pubcrawler.core.SupplementaryResourceDescription;
import wwmm.pubcrawler.impl.AcsCifIssueCrawler;
import wwmm.pubcrawler.impl.ActaCifIssueCrawler;
import wwmm.pubcrawler.impl.ChemSocJapanCifIssueCrawler;
import wwmm.pubcrawler.impl.CifIssueCrawler;
import wwmm.pubcrawler.impl.RscCifIssueCrawler;

public class FetchManager {

	private static final Logger LOG = Logger.getLogger(FetchManager.class);

	private String writeDirPath;
	private String downloadLogPath;
	private File doiIndexFile;
	private List<String> doiStrings;
	private static final CrawlerHttpClient httpClient = new CrawlerHttpClient();

	private FetchManager() {
		;
	}

	public FetchManager(File propsFile) {
		CrystalEyeProperties properties = new CrystalEyeProperties(propsFile);
		writeDirPath = properties.getWriteDir();
		downloadLogPath = properties.getDownloadLogPath();
		String doiIndexPath = properties.getDoiIndexPath();
		doiIndexFile = new File(doiIndexPath);
		try {
			if (!doiIndexFile.exists()) {
				doiIndexFile.createNewFile();
			}
			doiStrings = FileUtils.readLines(doiIndexFile);
		} catch (IOException e) {
			throw new RuntimeException("Should never throw!: "+e.getMessage());
		}
	}

	public void run() {
		for (AcsJournal acsJournal : AcsJournal.values()) {
			executeCrawler(new AcsCifIssueCrawler(acsJournal), "acs", acsJournal.getAbbreviation());
		}
		for (ActaJournal actaJournal : ActaJournal.values()) {
			executeCrawler(new ActaCifIssueCrawler(actaJournal), "acta", actaJournal.getAbbreviation());
		}
		for (ChemSocJapanJournal csjJournal : ChemSocJapanJournal.values()) {
			executeCrawler(new ChemSocJapanCifIssueCrawler(csjJournal), "chemSocJapan", csjJournal.getAbbreviation());
		}
		for (RscJournal rscJournal : RscJournal.values()) {
			executeCrawler(new RscCifIssueCrawler(rscJournal), "rsc", rscJournal.getAbbreviation());
		}
	}

	private void executeCrawler(CifIssueCrawler crawler, String publisher, String journal) {
		List<DOI> dois = crawler.getCurrentArticlesDois();
		List<DOI> cifArticleDois = new ArrayList<DOI>();
		StringBuilder sb = new StringBuilder();
		for (DOI doi : dois) {
			if (!doiStrings.contains(doi.getPostfix())) {
				cifArticleDois.add(doi);
				sb.append(doi.getPostfix()+"\n");
			}
		}

		List<ArticleDescription> cifArticlesDetails = crawler.getArticleDescriptions(cifArticleDois);
		LOG.info("CIF articles to fetch: "+cifArticlesDetails.size());
		IssueDescription issueDescription = crawler.getCurrentIssueDescription();
		String year = issueDescription.getYear();
		String issue = issueDescription.getIssueId();
		for (ArticleDescription articleDetails : cifArticlesDetails) {
			int count = 1;
			for (SupplementaryResourceDescription suppDetails : articleDetails.getSupplementaryResources()) {
				if (suppDetails.getContentType().contains(CIF_CONTENT_TYPE)) {
					String doiStr = articleDetails.getDoi().toString();
					String doiPostfix = doiStr.substring(doiStr.lastIndexOf("/")+1);
					String fileId = "";
					String cifExtension = "";
					if (crawler instanceof ActaCifIssueCrawler) {
						fileId = suppDetails.getFileId();
						cifExtension = ".cif";
					} else {
						fileId = doiPostfix;
						cifExtension = "sup"+count+".cif";
					}
					String cifPath = createOutfilePath(publisher, journal, year, issue, fileId, suppDetails, cifExtension);
					String cifUri = suppDetails.getURL();
					httpClient.writeResourceToFile(cifUri, new File(cifPath));
					String datePath = createOutfilePath(publisher, journal, year, issue, fileId, suppDetails, ".date");
					Utils.writeDateStamp(datePath);
					String doiPath = createOutfilePath(publisher, journal, year, issue, fileId, suppDetails, ".doi");
					Utils.writeText(new File(doiPath), doiStr);
					LOG.info("Wrote CIF to "+cifPath+" from the resource at "+cifUri.toString());
					count++;
				}
			}
		}
		if (cifArticlesDetails.size() > 0) {
			if (year == null || issue == null) {
				return;
			} else {
				new DownloadLog(downloadLogPath).updateLog(publisher, journal, year, issue);
			}
		}
		Utils.appendToFile(doiIndexFile, sb.toString());
	}

	private String createOutfilePath(String publisher, String journal, String year, String issue, String filename, SupplementaryResourceDescription suppDetails, String extension) {
		return writeDirPath+"/"+publisher+"/"+journal+"/"+year+"/"+issue+"/"+filename.replaceAll("sup\\d+", "")+"/"+filename+extension;
	}

	public static void main(String[] args) {
		String propsFilepath = "e:/crystaleye-new/docs/cif-flow-props.txt";
		FetchManager fetcher = new FetchManager(new File(propsFilepath));
		fetcher.run();
	}

}
