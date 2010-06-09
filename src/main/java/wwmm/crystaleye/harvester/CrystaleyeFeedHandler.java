package wwmm.crystaleye.harvester;

import java.io.File;

import org.apache.log4j.Logger;

import uk.ac.cam.ch.atomxom.feeds.FeedHandler;
import uk.ac.cam.ch.atomxom.model.AtomEntry;
import uk.ac.cam.ch.atomxom.model.AtomFeed;

public class CrystaleyeFeedHandler implements FeedHandler {

	private static final Logger LOG = Logger.getLogger(CrystaleyeFeedHandler.class);

	private int maxEntriesToFetch = 500;
	private int numFetchedEntries = 0;
	private EntryHandler entryHandler;

	public CrystaleyeFeedHandler(File cacheDir, EntryHandler entryHandler) {
		this.entryHandler = entryHandler;
	}

	public void setMaxEntriesToFetch(int maxEntries) {
		this.maxEntriesToFetch = maxEntries;
	}

	public void startFeed(String url) {}

	public void startDocument(AtomFeed feed) {}

	public void nextEntry(AtomEntry entry) {
		if (numFetchedEntries >= maxEntriesToFetch) {
			throw new FinishedFetchingException("Reached the maximum number of entries to fetch.");
		}
		boolean success = false; 
		try {
			success = entryHandler.handle(entry);
		} catch (Exception e) {
			LOG.warn("Problem handling entry: "+e.getMessage());
			return;
		}
		if (success) {
			numFetchedEntries++;
			LOG.info("Harvested entry with ID: "+entry.getId());
		}
	}

	public void endDocument() { }

	public void endFeed() { }

}
