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
import wwmm.pubcrawler.core.ArticleReference;
import wwmm.pubcrawler.core.ChemSocJapanJournal;
import wwmm.pubcrawler.core.CrawlerHttpClient;
import wwmm.pubcrawler.core.DOI;
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
		List<DOI> dois = crawler.getDoisForCurrentArticles();
		List<DOI> cifArticleDois = new ArrayList<DOI>();
		StringBuilder sb = new StringBuilder();
		for (DOI doi : dois) {
			if (!doiStrings.contains(doi.getPostfix())) {
				cifArticleDois.add(doi);
				sb.append(doi.getPostfix()+"\n");
			}
		}
		
		List<ArticleDescription> cifArticlesDetails = crawler.getArticleDescriptions(cifArticleDois);
		String year = null;
		String issueNum = null;
		for (ArticleDescription articleDetails : cifArticlesDetails) {
			ArticleReference ref = articleDetails.getReference();
			year = ref.getYear();
			issueNum = ref.getNumber();
			for (SupplementaryResourceDescription suppDetails : articleDetails.getSupplementaryResources()) {
				if (suppDetails.getContentType().contains(CIF_CONTENT_TYPE)) {
					String cifPath = createOutfilePath(publisher, journal, ref, suppDetails, ".cif");
					URI cifUri = suppDetails.getURI();
					httpClient.writeResourceToFile(cifUri, new File(cifPath));
					String datePath = createOutfilePath(publisher, journal, ref, suppDetails, ".date");
					Utils.writeDateStamp(datePath);
					String doiPath = createOutfilePath(publisher, journal, ref, suppDetails, ".doi");
					Utils.writeText(new File(doiPath), articleDetails.getDoi().toString());
					LOG.info("Wrote CIF to "+cifPath+" from the resource at "+cifUri.toString());
				}
			}
		}
		LOG.debug("details list size: "+cifArticlesDetails.size());
		if (cifArticlesDetails.size() > 0) {
			if (year == null || issueNum == null) {
				throw new IllegalStateException("Should never reach here - year and issue should already have been set.");
			}
			new DownloadLog(downloadLogPath).updateLog(publisher, journal, year, issueNum);
			Utils.appendToFile(doiIndexFile, sb.toString());
		}
	}

	private String createOutfilePath(String publisher, String journal, ArticleReference ref, SupplementaryResourceDescription suppDetails, String extension) {
		String fileId = suppDetails.getFileId();
		return writeDirPath+"/"+publisher+"/"+journal+"/"+ref.getYear()+"/"+ref.getNumber()+"/"+fileId+"/"+fileId+extension;
	}

	public static void main(String[] args) {
		String propsFilepath = "e:/crystaleye-new/docs/cif-flow-props.txt";
		FetchManager fetcher = new FetchManager(new File(propsFilepath));
		fetcher.run();
	}

}
