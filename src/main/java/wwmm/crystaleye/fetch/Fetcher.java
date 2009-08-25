package wwmm.crystaleye.fetch;

import java.io.File;

import wwmm.crystaleye.IOUtils;
import wwmm.crystaleye.properties.FetcherProperties;

public abstract class Fetcher {
	
	FetcherProperties properties;
	protected String PUBLISHER_ABBREVIATION;

	private Fetcher() {
		;
	}
	
	protected Fetcher(String publisherAbbreviation, File propertiesFile) {
		this.setProperties(propertiesFile);
		setPublisherAbbreviation(publisherAbbreviation);
	}

	protected Fetcher(String publisherAbbreviation, String propertiesPath) {
		this(publisherAbbreviation, new File(propertiesPath));
	}
	
	private void setPublisherAbbreviation(String publisherAbbreviation) {
		this.PUBLISHER_ABBREVIATION = publisherAbbreviation;
	}

	protected void setProperties(File propertiesFile) {
		properties = new FetcherProperties(propertiesFile);
	}
	
	protected void sleep() {
		String sleepMax = properties.getSleepMax();
		int maxTime = Integer.valueOf(sleepMax);
		try {
			Thread.sleep(((int)(maxTime*Math.random())));
		} catch (InterruptedException e) {
			System.err.println("Sleep interrupted.");
		}
	}

	protected String getWebPage(String url) {
		return IOUtils.fetchWebPage(url);
	}
}
