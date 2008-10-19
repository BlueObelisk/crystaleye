package wwmm.crystaleye.crawlers;

import org.apache.log4j.Logger;

import wwmm.crystaleye.BasicHttpClient;

public class JournalCrawler {

	int maxSleep = 1000;
	BasicHttpClient httpClient;
	
	private static final Logger LOG = Logger.getLogger(JournalCrawler.class);
	
	public JournalCrawler() {
		httpClient = new BasicHttpClient();
	}
	
	protected void sleep() {
		int maxTime = Integer.valueOf(maxSleep);
		try {
			Thread.sleep(((int) (maxTime * Math.random())));
		} catch (InterruptedException e) {
			LOG.debug("Sleep interrupted.");
		}
	}
	
}
