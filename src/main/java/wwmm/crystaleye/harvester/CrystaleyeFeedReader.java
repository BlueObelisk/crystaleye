package wwmm.crystaleye.harvester;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

import uk.ac.cam.ch.atomxom.feeds.FeedCache;

public class CrystaleyeFeedReader extends FeedCache {
	
	private static final Logger LOG = Logger.getLogger(CrystaleyeFeedReader.class);
	
	protected CrystaleyeFeedHandler feedHandler;
	private String feedUrl;
	
	public void setFeedUrl(String feedUrl) {
		this.feedUrl = feedUrl;
	}
	
	public CrystaleyeFeedReader(File dataDirectory, int maxEntriesToFetch, EntryHandler entryHandler) {
		if (dataDirectory == null) {
			throw new IllegalArgumentException("Data directory must not be null.");
		} else if (entryHandler == null) {
			throw new IllegalArgumentException("Entry handler must not be null.");
		} else if (!dataDirectory.exists()) {
			throw new IllegalStateException("Data directory does not exist ("+dataDirectory+"), please create it or choose an alternative.");
		} else if (!dataDirectory.isDirectory()) {
			throw new IllegalArgumentException("Data directory must be a directory, not a file: "+dataDirectory);
		} else if (maxEntriesToFetch < 1) {
			throw new IllegalArgumentException("Cannot set number of entries to fetch to less than 1.");
		} 
		File cacheDir = new File(dataDirectory, "feed_cache");
		this.feedHandler = new CrystaleyeFeedHandler(cacheDir, entryHandler);
		this.feedHandler.setMaxEntriesToFetch(maxEntriesToFetch);
		this.setHandler(feedHandler);
		this.setCacheDir(cacheDir);
	}

	public void readFeed() throws IOException {
		if (feedUrl == null) {
			throw new IllegalStateException("Implementing subclass must set feedUrl.");
		}
		try {
			super.poll(feedUrl);
		} catch (FinishedFetchingException e) {
			LOG.info("Finished fetching entries from feed.");
		}
	}

}
