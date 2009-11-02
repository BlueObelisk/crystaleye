package wwmm.crystaleye.fetch;

import java.io.File;

import org.apache.log4j.Logger;

import wwmm.crystaleye.CrystalEyeProperties;
import wwmm.crystaleye.util.WebUtils;

public abstract class Fetcher {
	
	private static final Logger LOG = Logger.getLogger(Fetcher.class);
	
	CrystalEyeProperties properties;
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
		properties = new CrystalEyeProperties(propertiesFile);
	}
	
	protected void sleep() {
		String sleepMax = properties.getSleepMax();
		int maxTime = Integer.valueOf(sleepMax);
		try {
			Thread.sleep(((int)(maxTime*Math.random())));
		} catch (InterruptedException e) {
			LOG.warn("Sleep interrupted.");
		}
	}

	protected String getWebPage(String url) {
		return WebUtils.fetchWebPage(url);
	}
}
