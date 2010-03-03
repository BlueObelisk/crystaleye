package wwmm.crystaleye.managers;

import static wwmm.pubcrawler.core.CrawlerConstants.CIF_CONTENT_TYPE;

import java.io.File;
import java.net.URI;
import java.util.List;

import org.apache.log4j.Logger;

import wwmm.crystaleye.CrystalEyeProperties;
import wwmm.crystaleye.DownloadLog;
import wwmm.crystaleye.util.Utils;
import wwmm.pubcrawler.core.AcsJournal;
import wwmm.pubcrawler.core.ActaJournal;
import wwmm.pubcrawler.core.ArticleDetails;
import wwmm.pubcrawler.core.ArticleReference;
import wwmm.pubcrawler.core.ChemSocJapanJournal;
import wwmm.pubcrawler.core.CrawlerHttpClient;
import wwmm.pubcrawler.core.RscJournal;
import wwmm.pubcrawler.core.SupplementaryResourceDetails;
import wwmm.pubcrawler.impl.AcsCifIssueCrawler;
import wwmm.pubcrawler.impl.ActaCifIssueCrawler;
import wwmm.pubcrawler.impl.ChemSocJapanCifIssueCrawler;
import wwmm.pubcrawler.impl.CifIssueCrawler;
import wwmm.pubcrawler.impl.RscCifIssueCrawler;

public class FetchManager {

	private static final Logger LOG = Logger.getLogger(FetchManager.class);

	private String writeDirPath;
	private String downloadLogPath;
	private static final CrawlerHttpClient httpClient = new CrawlerHttpClient();

	private FetchManager() {
		;
	}

	public FetchManager(File propsFile) {
		CrystalEyeProperties properties = new CrystalEyeProperties(propsFile);
		writeDirPath = properties.getWriteDir();
		downloadLogPath = properties.getDownloadLogPath();
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
		List<ArticleDetails> detailsList = crawler.getDetailsForCurrentArticles();
		String year = null;
		String issueNum = null;
		for (ArticleDetails articleDetails : detailsList) {
			ArticleReference ref = articleDetails.getReference();
			year = ref.getYear();
			issueNum = ref.getNumber();
			LOG.debug("supp resources: "+articleDetails.getSupplementaryResources().size());
			for (SupplementaryResourceDetails suppDetails : articleDetails.getSupplementaryResources()) {
				LOG.debug("content type: "+suppDetails.getContentType());
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
		LOG.debug("details list size: "+detailsList.size());
		if (detailsList.size() > 0) {
			if (year == null || issueNum == null) {
				throw new IllegalStateException("Should never reach here - year and issue should already have been set.");
			}
			new DownloadLog(downloadLogPath).updateLog(publisher, journal, year, issueNum);
		}
	}

	private String createOutfilePath(String publisher, String journal, ArticleReference ref, SupplementaryResourceDetails suppDetails, String extension) {
		String fileId = suppDetails.getFileId();
		return writeDirPath+"/"+publisher+"/"+journal+"/"+ref.getYear()+"/"+ref.getNumber()+"/"+fileId+"/"+fileId+extension;
	}

	public static void main(String[] args) {
		String propsFilepath = "e:/crystaleye-new/docs/cif-flow-props.txt";
		FetchManager fetcher = new FetchManager(new File(propsFilepath));
		fetcher.run();
	}

}
