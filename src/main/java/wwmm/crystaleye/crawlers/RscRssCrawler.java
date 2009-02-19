package wwmm.crystaleye.crawlers;

import static wwmm.crystaleye.crawlers.CrawlerConstants.DOI_SITE_URL;
import static wwmm.crystaleye.crawlers.CrawlerConstants.RSC_HOMEPAGE_URL;
import static wwmm.crystaleye.crawlers.CrawlerConstants.X_DC;
import static wwmm.crystaleye.crawlers.CrawlerConstants.X_RSS1;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;

import org.apache.commons.httpclient.URI;
import org.apache.log4j.Logger;

/**
 * <p>
 * The <code>RscRssCrawler</code> class provides a method for 
 * crawling RSS feeds published by the Royal Society of
 * Chemistry. 
 * </p>
 * 
 * @author Nick Day
 * @version 1.1
 * 
 */
public class RscRssCrawler extends Crawler {

	private RscJournal journal;
	private Date lastCrawledDate;

	private static final Logger LOG = Logger.getLogger(RscRssCrawler.class);
	
	/**
	 * <p>
	 * Creates an instance of the RscRssCrawler class ready to 
	 * crawl the feed for the provided <code>RscJournal</code>.
	 * By using this constructor, all articles in the feed will
	 * be returned on executing <code>getNewArticleDetails</code>.
	 * </p>
	 * 
	 * @param journal - the journals RSS feed to be crawled.
	 * 
	 */
	public RscRssCrawler(RscJournal journal) {
		this.journal = journal;
	}

	/**
	 * <p>
	 * Creates an instance of the RscRssCrawler class ready to 
	 * crawl the feed for the provided <code>RscJournal</code>.
	 * On executing <code>getNewArticleDetails</code>, only those
	 * article included in the feed after the provided 
	 * <code>lastCrawledDate</code> will be returned.
	 * </p>
	 * 
	 * @param journal - the journals RSS feed to be crawled.
	 * 
	 */
	public RscRssCrawler(RscJournal journal, Date lastCrawledDate) {
		this.journal = journal;
		this.lastCrawledDate = lastCrawledDate;
	}

	/**
	 * <p>
	 * Returns information about the new articles to be published
	 * in the RSS feed.  If a <code>lastCrawledDate</code> was
	 * provided in the constructor, then only information about
	 * articles published after that date will be returned. If no
	 * <code>lastCrawledDate</code> was provided, then information
	 * about all article in the feed will be returned.
	 * </p>
	 * 
	 * @return a list where each item provides information about a
	 * separate article.
	 * 
	 */
	public List<ArticleDetails> getNewArticleDetails() {
		URI feedUri = createFeedURI();
		Document feedDoc = httpClient.getResourceXML(feedUri);
		List<Element> entries = getFeedEntries(feedDoc);
		List<ArticleDetails> adList = new ArrayList<ArticleDetails>();
		for (Element entry : entries) {
			Date entryDate = getEntryDate(entry);
			if (needToCrawlArticle(entryDate)) {
				DOI doi = getDOI(entry);
				ArticleDetails ad = new RscArticleCrawler(doi).getDetails();
				adList.add(ad);
			}
		}
		return adList;
	}
	
	/**
	 * <p>
	 * Gets the article DOI from the RSS entry.
	 * </p>
	 * 
	 * @param entry - the RSS entry that is being crawled.
	 * 
	 * @return DOI for the article described by the RSS entry.
	 * 
	 */
	private DOI getDOI(Element entry) {
		Nodes nds = entry.query("./dc:identifier", X_DC);
		if (nds.size() == 0) {
			throw new CrawlerRuntimeException("Could not get DOI from entry:\n"+entry.toXML());
		}
		String value = ((Element)nds.get(0)).getValue();
		String doiPrefix = value.replaceAll("DOI", "").trim();
		String doi = DOI_SITE_URL+"/"+doiPrefix;
		return new DOI(doi);
	}
	
	/**
	 * <p>
	 * If a <code>lastCrawledDate</code> was provided in the 
	 * constructor, then then entry for each article is checked
	 * here to make sure it was published afterwards.
	 * </p>
	 * 
	 * @param entryDate - the date of the current RSS entry.
	 * 
	 * @return boolean whether or not the current article needs
	 * to be scraped.
	 * 
	 */
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
	
	/**
	 * <p>
	 * Gets the published date from the provided RSS entry.
	 * </p> 
	 * 
	 * @param entry - the RSS entry to be scraped.
	 * 
	 * @return Date that the article described by the provided 
	 * RSS entry was published.
	 * 
	 */
	private Date getEntryDate(Element entry) {
		Nodes nds = entry.query("./dc:date", X_DC);
		if (nds.size() != 1) {
			throw new IllegalStateException("Expected to find 1 date element in this entry, found "+nds.size()+":\n"+entry.toXML());
		}
		String dateStr = ((Element)nds.get(0)).getValue();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy'-'MM'-'dd");
		Date date = null;
		try {
			date = sdf.parse(dateStr);
		} catch (ParseException e) {
			throw new CrawlerRuntimeException("Could not parse date string "+dateStr+
					", looks like the crawler may need rewriting...");
		}
		return date;		
	}
	
	/**
	 * <p>
	 * Gets all entries found in the RSS feed being crawled.
	 * </p>
	 * 
	 * @param feedDoc - XML of the RSS feed being crawled.
	 * 
	 * @return list of entries from the crawled RSS feed.
	 * 
	 */
	private List<Element> getFeedEntries(Document feedDoc) {
		Nodes nds = feedDoc.query(".//rss1:item", X_RSS1);
		if (nds.size() == 0) {
			throw new CrawlerRuntimeException("Could not find any entries in RSS feed for "+journal.getFullTitle());
		}
		List<Element> entries = new ArrayList<Element>(nds.size());
		for (int i = 0; i < nds.size(); i++) {
			Element entry = (Element)nds.get(i);
			entries.add(entry);
		}
		return entries;
	}

	/**
	 * <p>
	 * Gets the URI for the RSS feed for the latest published articles
	 * of the provided <code>RscJournal</code>.
	 * </p>
	 * 
	 * @return the RSS feed <code>URI</code>.
	 * 
	 */
	private URI createFeedURI() {
		String feedUrl = RSC_HOMEPAGE_URL+"/publishing/journals/rssfeed.asp?FeedType=LatestArticles&JournalCode="+journal.getAbbreviation();
		return createURI(feedUrl);
	}

	/**
	 * <p>
	 * Main method only for demonstration of class use. Does not require
	 * any arguments.
	 * </p>
	 * 
	 * @param args
	 * @throws ParseException 
	 */
	public static void main(String[] args) throws ParseException {
		for (ActaJournal journal : ActaJournal.values()) {
			if (!journal.getAbbreviation().equals("c")) {
				continue;
			}
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy'-'MM'-'dd");
			Date date = sdf.parse("2008-08-15");
			ActaRssCrawler acf = new ActaRssCrawler(journal, date);
			List<ArticleDetails> details = acf.getNewArticleDetails();
			for (ArticleDetails ad : details) {
				System.out.println(ad.toString());
			}
			break;
		}
	}
	
}
