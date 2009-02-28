package wwmm.crystaleye.tasks;

import java.util.ArrayList;
import java.util.List;

import wwmm.crystaleye.crawler.AcsIssueCrawler;
import wwmm.crystaleye.crawler.AcsJournal;
import wwmm.crystaleye.crawler.ActaIssueCrawler;
import wwmm.crystaleye.crawler.ActaJournal;
import wwmm.crystaleye.crawler.ArticleDetails;
import wwmm.crystaleye.crawler.ChemSocJapanIssueCrawler;
import wwmm.crystaleye.crawler.ChemSocJapanJournal;
import wwmm.crystaleye.crawler.IssueCrawler;
import wwmm.crystaleye.crawler.RscIssueCrawler;
import wwmm.crystaleye.crawler.RscJournal;

public class CrawlerTask {

	public void getCIFs() {
		for (IssueCrawler crawler : getIssueCrawlers()) {
			List<ArticleDetails> adList = crawler.getDetailsForCurrentArticles();
			
		}
	}
	
	private List<IssueCrawler> getIssueCrawlers() {
		List<IssueCrawler> icList = new ArrayList<IssueCrawler>();
		for (AcsJournal journal : AcsJournal.values()) {
			AcsIssueCrawler crawler = new AcsIssueCrawler(journal);
			icList.add(crawler);
		}
		for (ActaJournal journal : ActaJournal.values()) {
			ActaIssueCrawler crawler = new ActaIssueCrawler(journal);
			icList.add(crawler);
		}
		for (ChemSocJapanJournal journal : ChemSocJapanJournal.values()) {
			ChemSocJapanIssueCrawler crawler = new ChemSocJapanIssueCrawler(journal);
			icList.add(crawler);
		}
		for (RscJournal journal : RscJournal.values()) {
			RscIssueCrawler crawler = new RscIssueCrawler(journal);
			icList.add(crawler);
		}
		return icList;
	}

}
