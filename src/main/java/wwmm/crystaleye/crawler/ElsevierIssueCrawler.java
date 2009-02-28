package wwmm.crystaleye.crawler;

import static wwmm.crystaleye.CrystalEyeConstants.X_XHTML;
import static wwmm.crystaleye.crawler.CrawlerConstants.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Node;
import nu.xom.Nodes;

import org.apache.commons.httpclient.URI;
import org.apache.log4j.Logger;

import wwmm.crystaleye.Utils;

/**
 * <p>
 * The <code>ElsevierIssueCrawler</code> class provides a method for 
 * obtaining information about all articles from a particular issue of 
 * a journal published by Elsevier.
 * </p>
 * 
 * @author Nick Day
 * @version 1.1
 * 
 */
public class ElsevierIssueCrawler extends IssueCrawler {

	private ElsevierJournal journal;

	private static final Logger LOG = Logger.getLogger(ElsevierIssueCrawler.class);

	/**
	 * <p>
	 * Creates an instance of the ElsevierIssueCrawler class and
	 * specifies the journal of the issue to be crawled.
	 * </p>
	 * 
	 * @param doi of the article to be crawled.
	 */
	public ElsevierIssueCrawler(ElsevierJournal journal) {
		this.journal = journal;
	}

	/**
	 * <p>
	 * Gets information to identify the last published issue of a
	 * the provided <code>ElsevierJournal</code>.
	 * </p>
	 * 
	 * @return the year and issue identifier.
	 * 
	 */
	@Override
	public IssueDetails getCurrentIssueDetails() {
		Document doc = getCurrentIssueHtml();
		return getIssueDetails(doc);
	}

	/**
	 * Gets the year and issue identifier for the issue at
	 * the provided URI.
	 * 
	 * @param issueUri - uri of the issue to be crawled.
	 * 
	 * @return IssueDetails containing the year and issue identifier
	 * of the issue at the provided issue URI.
	 */
	private IssueDetails getIssueDetails(Document issueHtml) {
		Nodes nds = issueHtml.query("./x:html/x:head/x:title", X_XHTML);
		if (nds.size() != 1) {
			throw new CrawlerRuntimeException("Expected to find 1 element containing" +
					" the year/issue information but found "+nds.size()+".");
		}
		String title = nds.get(0).getValue().trim();
		Pattern pattern = Pattern.compile("ScienceDirect - "+journal.getFullTitle()+
		", Volume \\d+, Issue (\\d+), Pages \\d+-\\d+ \\(\\d+ \\w+ (\\d{4})\\)");
		Matcher matcher = pattern.matcher(title);
		if (!matcher.find() || matcher.groupCount() != 2) {
			throw new CrawlerRuntimeException("Could not extract the year/issue information.");
		}
		String year = matcher.group(2);
		String issueId = matcher.group(1);
		LOG.debug("Found latest issue details for Elsevier journal "+journal.getFullTitle()+": year="+year+", issue="+issueId+".");
		return new IssueDetails(year, issueId);
	}

	/**
	 * <p>
	 * Gets the HTML of the table of contents of the last 
	 * published issue of the provided journal.
	 * </p>
	 * 
	 * @return HTML of the issue table of contents.
	 * 
	 */
	public Document getCurrentIssueHtml() {
		String url = ELSEVIER_JOURNAL_URL_PREFIX+"/science/journal/"+journal.getAbbreviation();
		URI uri = createURI(url);
		return httpClient.getResourceHTML(uri);
	}

	/**
	 * <p>
	 * Gets the DOIs of all of the articles from the last 
	 * published issue of the provided journal.
	 * </p> 
	 * 
	 * @return a list of the DOIs of the articles.
	 * 
	 */
	@Override
	public List<DOI> getCurrentIssueDOIs() {
		IssueDetails details = getCurrentIssueDetails();
		return getDOIs(details);
	}

	/**
	 * <p>
	 * Gets the DOIs of all articles in the issue defined
	 * by the <code>ElsevierJournal</code> and the provided	year and 
	 * issue identifier (wrapped in the <code>issueDetails</code>
	 * parameter.
	 * </p>
	 * 
	 * @param issueDetails - contains the year and issue
	 * identifier of the issue to be crawled.
	 * 
	 * @return a list of the DOIs of the articles for the issue.
	 * 
	 */
	public List<DOI> getDOIs(IssueDetails details) {
		List<DOI> dois = new ArrayList<DOI>();
		// don't know how to create the issue TOC url from the year 
		// and IssueId alone, so we go to the current issue page
		// and follow the links from there.
		URI issueUri = getIssueTocUriFromCurrentIssueToc(details);
		List<URI> articleUriList = getArticleUris(issueUri);
		for (URI articleUri : articleUriList) {
			DOI doi = getArticleDoi(articleUri);
			dois.add(doi);
		}
		LOG.debug("Finished finding issue DOIs.");
		return dois;
	}
	
	private DOI getArticleDoi(URI articleUri) {
		Document articleDoc = httpClient.getResourceHTMLMinusComments(articleUri);
		Nodes doiNds = articleDoc.query(".//x:a[contains(@href,'"+DOI_SITE_URL+"')]", X_XHTML);
		if (doiNds.size() != 1) {
			throw new IllegalStateException("Expected 1 node, found: "+doiNds.size());
		}
		String doi = ((Element)doiNds.get(0)).getAttributeValue("href");
		return new DOI(doi);
	}
	
	private List<URI> getArticleUris(URI issueUri) {
		Document issueDoc = httpClient.getResourceHTMLMinusComments(issueUri);
		Nodes nds = issueDoc.query(".//x:a[contains(@href,'ArticleURL')]", X_XHTML);
		List<URI> articleUris = new ArrayList<URI>(nds.size());
		for (int i = 0; i < nds.size(); i++) {
			Element nd = (Element)nds.get(i);
			if (!isArticleLink(nd)) {
				continue;
			}
			String articleUrl = nd.getAttributeValue("href");
			URI articleUri = createURI(articleUrl, true);
			articleUris.add(articleUri);
			
			// FIXME - remove these lines - this was added so that while
			// creating the crawler, it wouldn't run for all articles 
			// in the journal.
			break;
		}
		return articleUris;
	}
	
	private boolean isArticleLink(Element link) {
		String value = link.getValue();
		if (value.contains("Erratum") ||
				value.contains("erratum") ||
				value.contains("Inside Front Cover") ||
				value.contains("Editorial Board Page") ||
				value.contains("contents pages")) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Crawls the current issue table of contents to find the URL for the 
	 * issue that matches the provided year and issueId in the navigation
	 * list on the left of the webpage.
	 * 
	 * @param issueDetails - contains the year and issue
	 * identifier of the issue to be crawled.
	 * 
	 * @return URI of the issue.
	 */
	private URI getIssueTocUriFromCurrentIssueToc(IssueDetails details) {
		URI yearUri = getLastIssueOfYearUri(details.getYear());
		return getIssueTocUri(yearUri, details);
	}

	/**
	 * Gets the URI for the issue that matches the year and
	 * issue identifier provided in <code>details</code>.
	 * 
	 * @param yearUri - URI of the last issue table of contents for 
	 * the provided year.
	 * @param issueDetails - contains the year and issue
	 * identifier of the issue being crawled.
	 * 
	 * @return URI of the table of contents that matches the provided 
	 * year and issue identifier.
	 */
	private URI getIssueTocUri(URI yearUri, IssueDetails details) {
		String year = details.getYear();
		String issueId = details.getIssueId();
		Document lastIssueOfYearToc = httpClient.getResourceHTMLMinusComments(yearUri);
		if (getIssueDetails(lastIssueOfYearToc).getIssueId().equals(issueId)) {
			return yearUri;
		} else {
			int volume = Integer.parseInt(year)-journal.getVolumeOffset();
			Nodes nds = lastIssueOfYearToc.query(".//x:a[contains(.,'Volume "+volume+", Issue "+issueId+"')]", X_XHTML);
			if (nds.size() != 1) {
				throw new IllegalStateException("Expected to find 1 node, found: "+nds.size());
			}
			String issueUrlPostfix = ((Element)nds.get(0)).getAttributeValue("href");
			String issueUrl = ELSEVIER_JOURNAL_URL_PREFIX+issueUrlPostfix;
			return createURI(issueUrl, true);
		}
	}

	/**
	 * Uses the navigation bar on the left-hand side of the current issue
	 * webpage to follow a link to a particular year.  Following this link
	 * provides the table of contents for the last issue in that year.  
	 * Hence the title of the method...
	 * 
	 * @param year - year of the issue to be crawled.
	 * 
	 * @return URI of the last issue of the year provided as the parameter.
	 */
	private URI getLastIssueOfYearUri(String year) {
		String currentYear = getCurrentIssueDetails().getYear();
		String currentIssueUrl = ELSEVIER_JOURNAL_URL_PREFIX+"/science/journal/"+journal.getAbbreviation();
		URI currentIssueUri = createURI(currentIssueUrl);
		if (year.equals(currentYear)) {
			return currentIssueUri;
		} else {
			Document currentIssueToc = httpClient.getResourceHTMLMinusComments(currentIssueUri);
			Nodes nds = currentIssueToc.query(".//x:a[contains(.,'("+year+")')]", X_XHTML);
			if (nds.size() != 1) {
				throw new IllegalStateException("Expected to find one node, found: "+nds.size());
			}
			String yearPostfix = ((Element)nds.get(0)).getAttributeValue("href");
			String yearUrl = ELSEVIER_JOURNAL_URL_PREFIX+yearPostfix;
			return createURI(yearUrl);
		}
	}

	/**
	 * <p>
	 * Gets information describing all articles in the issue 
	 * defined by the <code>ElsevierJournal</code> and the provided	
	 * year and issue identifier (wrapped in the 
	 * <code>issueDetails</code> parameter.
	 * </p>
	 * 
	 * @param issueDetails - contains the year and issue
	 * identifier of the issue to be crawled.
	 * 
	 * @return a list where each item contains the details for 
	 * a particular article from the issue.
	 * 
	 */
	@Override
	public List<ArticleDetails> getDetailsForArticles(IssueDetails details) {
		String year = details.getYear();
		String issueId = details.getIssueId();
		LOG.debug("Starting to find issue article details: "+year+"-"+issueId);
		List<DOI> dois = getDOIs(details);
		List<ArticleDetails> adList = new ArrayList<ArticleDetails>(dois.size());
		for (DOI doi : dois) {
			ArticleDetails ad = new ElsevierArticleCrawler(doi).getDetails();
			adList.add(ad);
		}
		LOG.debug("Finished finding issue article details: "+year+"-"+issueId);
		return adList;
	}

	/**
	 * <p>
	 * Main method only for demonstration of class use. Does not require
	 * any arguments.
	 * </p>
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		for (ElsevierJournal journal : ElsevierJournal.values()) {
			if (!journal.getAbbreviation().equals("02775387")) {
				continue;
			}
			ElsevierIssueCrawler acf = new ElsevierIssueCrawler(journal);
			IssueDetails details = acf.getCurrentIssueDetails();
			List<ArticleDetails> adList = acf.getDetailsForArticles(details);
			break;
		}
	}

}
