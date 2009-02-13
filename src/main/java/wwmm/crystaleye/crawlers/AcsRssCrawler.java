package wwmm.crystaleye.crawlers;

import static wwmm.crystaleye.crawlers.CrawlerConstants.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;

import org.apache.commons.httpclient.URI;
import org.apache.log4j.Logger;

public class AcsRssCrawler extends Crawler {

	private AcsJournal journal;
	private Date lastCrawledDate;

	private static final Logger LOG = Logger.getLogger(AcsRssCrawler.class);
	
	public AcsRssCrawler(AcsJournal journal) {
		this.journal = journal;
	}

	public AcsRssCrawler(AcsJournal journal, Date lastCrawledDate) {
		this.journal = journal;
		this.lastCrawledDate = lastCrawledDate;
	}

	public List<ArticleDetails> getNewArticleDetails() {
		URI feedUri = createFeedURI();
		Document feedDoc = httpClient.getWebpageXML(feedUri);
		List<Element> entries = getFeedEntries(feedDoc);
		List<ArticleDetails> adList = new ArrayList<ArticleDetails>();
		for (Element entry : entries) {
			URI entryLink = getEntryLink(entry);
			Date entryDate = getEntryDate(entry);
			if (needToCrawlArticle(entryDate)) {
				DOI doi = createDOI(entryLink);
				ArticleDetails ad = new AcsArticleCrawler(doi).getDetails();
				adList.add(ad);
			}
		}
		return adList;
	}
	
	private DOI createDOI(URI entryLink) {
		Pattern p = Pattern.compile(ACS_HOMEPAGE_URL+"/doi/abs/(10.1021/.{9})\\?.*");
		Matcher matcher = p.matcher(entryLink.toString());
		String doiPostfix = null;
		if (matcher.find() && matcher.groupCount() == 1) {
			doiPostfix = matcher.group(1);
		} else {
			throw new RuntimeException("Could not extract DOI from <link> URI, "+
					entryLink.toString()+"element, crawler may need rewriting.");
		}
		String doiStr = DOI_SITE_URL+"/"+doiPostfix;
		return new DOI(doiStr);
	}
	
	private boolean needToCrawlArticle(Date entryDate) {
		if (lastCrawledDate == null) {
			return true;
		} else {
			if (entryDate.before(lastCrawledDate)) {
				return false;
			} else {
				return true;
			}
		}
	}
	
	private URI getEntryLink(Element entry) {
		Nodes nds = entry.query("./link");
		if (nds.size() != 1) {
			throw new IllegalStateException("Expected to find 1 link element in this entry, found "+nds.size()+":\n"+entry.toXML());
		}
		String url = ((Element)nds.get(0)).getValue();
		return createURI(url);
	}
	
	private Date getEntryDate(Element entry) {
		Nodes nds = entry.query("./pubDate");
		if (nds.size() != 1) {
			throw new IllegalStateException("Expected to find 1 date element in this entry, found "+nds.size()+":\n"+entry.toXML());
		}
		String dateStr = ((Element)nds.get(0)).getValue();
		SimpleDateFormat sdf = new SimpleDateFormat("EEE',' dd MMM yyyy HH:mm:ss 'GMT'");
		Date date = null;
		try {
			date = sdf.parse(dateStr);
		} catch (ParseException e) {
			throw new RuntimeException("Could not parse date string "+dateStr+
					", looks like the crawler may need rewriting...");
		}
		return date;		
	}
	
	private List<Element> getFeedEntries(Document feedDoc) {
		Nodes nds = feedDoc.query("./rss/channel/item");
		if (nds.size() == 0) {
			throw new RuntimeException("Could not find any entries in RSS feed for "+journal.getFullTitle());
		}
		List<Element> entries = new ArrayList<Element>(nds.size());
		for (int i = 0; i < nds.size(); i++) {
			Element entry = (Element)nds.get(i);
			entries.add(entry);
		}
		return entries;
	}

	private URI createFeedURI() {
		String feedUrl = ACS_HOMEPAGE_URL+"/action/showFeed?ui=0&mi=r41k3s&ai=54r&jc="
		+journal.getAbbreviation()+"&type=etoc&feed=rss";
		return createURI(feedUrl);
	}

	/**
	 * Main method only for demonstration of class use. Does not require
	 * any arguments.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		for (AcsJournal journal : AcsJournal.values()) {
			if (!journal.getAbbreviation().equals("cgdefu")) {
				continue;
			}
			AcsRssCrawler acf = new AcsRssCrawler(journal);
			List<ArticleDetails> details = acf.getNewArticleDetails();
			for (ArticleDetails ad : details) {
				System.out.println(ad.toString());
			}
			break;
		}
	}
}
