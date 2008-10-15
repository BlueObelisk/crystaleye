package wwmm.crystaleye.site.feeds;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import wwmm.crystaleye.CrystalEyeRuntimeException;
import wwmm.crystaleye.util.XmlIOUtils;

import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndLinkImpl;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.SyndFeedOutput;
import com.sun.syndication.io.XmlReader;

public class RSSHandler {

	String feedUrl;
	List<SyndEntry> entryList;

	private RSSHandler() {
		;
	}

	public RSSHandler(String feedUrl, List<SyndEntry> entryList) {
		this.feedUrl = feedUrl;
		this.entryList = entryList;
	}

	public RSSHandler(String feedUrl, SyndEntry entry) {
		this.feedUrl = feedUrl;
		entryList = new LinkedList<SyndEntry>();
		entryList.add(entry);
	}

	public void addEntries() {
		if (feedUrl == null || entryList == null) {
			throw new IllegalArgumentException("Must provide a feed path, type and entry details before calling addEntry()");
		}
		SyndFeedInput input = new SyndFeedInput();
		SyndFeed feed = null;
		try {
			feed = input.build(new XmlReader(new File(feedUrl)));
		} catch (IllegalArgumentException e) {
			throw new CrystalEyeRuntimeException("Feed does not exist: "+feedUrl, e);
		} catch (FeedException e) {
			throw new CrystalEyeRuntimeException("FeedException: "+feedUrl, e);
		} catch (IOException e) {
			throw new CrystalEyeRuntimeException("Could not read feed at: "+feedUrl, e);
		}

		List currentEntries = feed.getEntries();
		for (SyndEntry entry : entryList) {
			// make sure the new entry is added to the beginning of the
			// entry list, not the end.
			currentEntries.add(0,entry);
		}
		feed.setEntries(currentEntries);

		SyndFeedOutput output = new SyndFeedOutput();
		try {
			String feedStr = output.outputString(feed);
			XmlIOUtils.writeText(feedStr, feedUrl);
		} catch (FeedException e) {
			throw new CrystalEyeRuntimeException("Error outputting the RSS feed to "+feedUrl, e);
		} 	
	}

	public static SyndEntry createEntry(String entryTitle, String entryLink, List<SyndLinkImpl> otherLinks, String descValue, String author) {		
		SyndEntry entry = new SyndEntryImpl();
		entry.setTitle(entryTitle);
		if (otherLinks != null) {
			entry.setLinks(otherLinks);
			SyndLinkImpl linkImpl = new SyndLinkImpl();
			linkImpl.setHref(entryLink);
			linkImpl.setHreflang("en");
			linkImpl.setRel("self");
			otherLinks.add(linkImpl);
			entry.setLinks(otherLinks);
			entry.setLink(entryLink);
		} else {
			entry.setLink(entryLink);
		}
		entry.setPublishedDate(new Date());
		SyndContent description = new SyndContentImpl();
		description.setType("text/plain");
		description.setValue(descValue);
		entry.setDescription(description);
		entry.setAuthor(author);
		entry.setUri(entryLink);

		return entry;
	}
}
