package wwmm.crystaleye.crawlers;

import org.apache.log4j.Logger;

public class JournalCrawler {

	int maxSleep = 1000;
	
	private static final Logger LOG = Logger.getLogger(JournalCrawler.class);
	
	protected void sleep() {
		int maxTime = Integer.valueOf(maxSleep);
		try {
			Thread.sleep(((int) (maxTime * Math.random())));
		} catch (InterruptedException e) {
			LOG.debug("Sleep interrupted.");
		}
	}
	
}
