package wwmm.crystaleye.crawlers;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.URI;
import org.apache.log4j.Logger;

import wwmm.crystaleye.BasicHttpClient;

public class Crawler {

	int maxSleep = 1000;
	BasicHttpClient httpClient;
	
	private static final Logger LOG = Logger.getLogger(Crawler.class);
	
	public Crawler() {
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
	
	protected String getContentType(URI uri) {
		Header[] headers = httpClient.getHeaders(uri);
		String contentType = null;
		for (Header header : headers) {
			if ("Content-Type".equals(header.getName())) {
				contentType = header.getValue();
			}
		}
		return contentType;
	}
	
}
