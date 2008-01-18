package uk.ac.cam.ch.crystaleye.fetch;

import java.io.File;

import uk.ac.cam.ch.crystaleye.IOUtils;

public abstract class JournalFetcher implements Fetcher {

	
	File downloadDir;
	protected String publisherAbbr;
	protected String journalAbbr;
	int maxSleep = 1000;

	public String getPublisherAbbr() {
		return publisherAbbr;
	}

	public void setPublisherAbbr(String publisherAbbr) {
		this.publisherAbbr = publisherAbbr;
	}

	public String getJournalAbbr() {
		return journalAbbr;
	}

	public void setJournalAbbr(String journalAbbr) {
		this.journalAbbr = journalAbbr;
	}

	protected String getWebPage(String url) {
		return IOUtils.fetchWebPage(url);
	}

	public int getMaxSleep() {
		return maxSleep;
	}

	public void setMaxSleep(int maxSleep) {
		this.maxSleep = maxSleep;
	}

	protected void sleep() {
		int maxTime = Integer.valueOf(maxSleep);
		try {
			Thread.sleep(((int) (maxTime * Math.random())));
		} catch (InterruptedException e) {
			System.err.println("Sleep interrupted.");
		}
	}

	public File getDownloadDir() {
		return downloadDir;
	}

	public void setDownloadDir(File downloadDir) {
		this.downloadDir = downloadDir;
	}

}
