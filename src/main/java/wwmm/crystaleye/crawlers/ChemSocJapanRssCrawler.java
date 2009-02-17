package wwmm.crystaleye.crawlers;

import static wwmm.crystaleye.crawlers.CrawlerConstants.DOI_SITE_URL;

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

public class ChemSocJapanRssCrawler extends Crawler {

	private ChemSocJapanJournal journal;
	private Date lastCrawledDate;

	private static final Logger LOG = Logger.getLogger(ChemSocJapanRssCrawler.class);
	
	public ChemSocJapanRssCrawler(ChemSocJapanJournal journal) {
		this.journal = journal;
	}

	public ChemSocJapanRssCrawler(ChemSocJapanJournal journal, Date lastCrawledDate) {
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
				ArticleDetails ad = new ChemSocJapanArticleCrawler(doi).getDetails();
				adList.add(ad);
			}
		}
		return adList;
	}
	
	private DOI getDOI(Element entry) {
		Nodes nds = entry.query("./link");
		if (nds.size() != 1) {
			throw new IllegalStateException("Expected to find 1 link element in this entry, found "+nds.size()+":\n"+entry.toXML());
		}
		String entryLink = ((Element)nds.get(0)).getValue();
		Pattern p = Pattern.compile(".*id=cl/(\\d+\\.\\d+)");
		Matcher matcher = p.matcher(entryLink);
		String doiPostfix = null;
		if (matcher.find() && matcher.groupCount() == 1) {
			doiPostfix = matcher.group(1);
		} else {
			throw new CrawlerRuntimeException("Could not extract DOI from <link> URI, "+
					entryLink.toString()+"element, crawler may need rewriting.");
		}
		String doiStr = DOI_SITE_URL+"/10.1246/cl."+doiPostfix;
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
	
	private Date getEntryDate(Element entry) {
		Nodes nds = entry.query("./pubDate");
		if (nds.size() != 1) {
			throw new IllegalStateException("Expected to find 1 date element in this entry, found "+nds.size()+":\n"+entry.toXML());
		}
		String dateStr = ((Element)nds.get(0)).getValue();
		SimpleDateFormat sdf = new SimpleDateFormat("EEE',' dd MMM yyyy z");
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
		Nodes nds = feedDoc.query("./rss/channel/item");
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
		String feedUrl = "http://www.csj.jp/journals/"+journal.getAbbreviation()+"/cl-cont/rss/"+journal.getAbbreviation().replaceAll("-", "")+".rss";
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
		for (ChemSocJapanJournal journal : ChemSocJapanJournal.values()) {
			if (!journal.getAbbreviation().equals("chem-lett")) {
				continue;
			}
			SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy z");
			Date date = sdf.parse("Sat, 7 Feb 2009 JST");
			ChemSocJapanRssCrawler acf = new ChemSocJapanRssCrawler(journal, date);
			List<ArticleDetails> details = acf.getNewArticleDetails();
			for (ArticleDetails ad : details) {
				System.out.println(ad.toString());
			}
			break;
		}
	}
	
}
