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

public class RscRssCrawler extends Crawler {

	private RscJournal journal;
	private Date lastCrawledDate;

	private static final Logger LOG = Logger.getLogger(RscRssCrawler.class);
	
	public RscRssCrawler(RscJournal journal) {
		this.journal = journal;
	}

	public RscRssCrawler(RscJournal journal, Date lastCrawledDate) {
		this.journal = journal;
		this.lastCrawledDate = lastCrawledDate;
	}

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

	private URI createFeedURI() {
		String feedUrl = RSC_HOMEPAGE_URL+"/publishing/journals/rssfeed.asp?FeedType=LatestArticles&JournalCode="+journal.getAbbreviation();
		return createURI(feedUrl);
	}

	/**
	 * Main method only for demonstration of class use. Does not require
	 * any arguments.
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
