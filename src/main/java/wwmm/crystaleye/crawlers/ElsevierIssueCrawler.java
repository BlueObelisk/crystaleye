package wwmm.crystaleye.crawlers;

import static wwmm.crystaleye.CrystalEyeConstants.X_XHTML;
import static wwmm.crystaleye.crawlers.CrawlerConstants.*;

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

import wwmm.crystaleye.util.Utils;

public class ElsevierIssueCrawler extends Crawler {

private ElsevierJournal journal;
	
	private static final Logger LOG = Logger.getLogger(ElsevierIssueCrawler.class);

	public ElsevierIssueCrawler(ElsevierJournal journal) {
		this.journal = journal;
	}

	public IssueDetails getCurrentIssueDetails() {
		Document doc = getCurrentIssueDocument();
		Nodes nds = doc.query("./x:html/x:head/x:title", X_XHTML);
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
	
	public Document getCurrentIssueDocument() {
		String url = "http://www.sciencedirect.com/science/journal/"+journal.getAbbreviation();
		URI issueUri = createURI(url);
		return httpClient.getResourceHTMLMinusComments(issueUri);
	}
	
	public List<DOI> getCurrentIssueDOIs() {
		IssueDetails details = getCurrentIssueDetails();
		return getDOIs(details);
	}

	public List<DOI> getDOIs(String year, String issueId) {
		List<DOI> dois = new ArrayList<DOI>();
		int volume = Integer.valueOf(year)-journal.getVolumeOffset();
		String issueUrl = ELSEVIER_JOURNAL_URL_PREFIX+"/toc/"+journal.getAbbreviation()+"/"+volume+"/"+issueId;
		URI issueUri = createURI(issueUrl);
		LOG.debug("Started to find DOIs from "+journal.getFullTitle()+", year "+year+", issue "+issueId+".");
		LOG.debug(issueUri.toString());
		Document issueDoc = httpClient.getResourceHTML(issueUri);
		List<Node> doiNodes = Utils.queryHTML(issueDoc, ".//x:div[@class='DOI']");
		for (Node doiNode : doiNodes) {
			String contents = ((Element)doiNode).getValue();
			String doiPostfix = contents.replaceAll("DOI:", "").trim();
			String doiStr = DOI_SITE_URL+"/"+doiPostfix;
			DOI doi = new DOI(createURI(doiStr)); 
			dois.add(doi);
		}
		LOG.debug("Finished finding issue DOIs.");
		return dois;
	}
	
	public List<DOI> getDOIs(IssueDetails details) {
		return getDOIs(details.getYear(), details.getIssueId());
	}
	
	public List<ArticleDetails> getArticleDetails(String year, String issueId) {
		/*
		LOG.debug("Starting to find issue article details: "+year+"-"+issueId);
		List<DOI> dois = getDOIs(year, issueId);
		List<ArticleDetails> adList = new ArrayList<ArticleDetails>(dois.size());
		for (DOI doi : dois) {
			ArticleDetails ad = new ElsevierArticleCrawler(doi).getDetails();
			adList.add(ad);
		}
		LOG.debug("Finished finding issue article details: "+year+"-"+issueId);
		return adList;
		*/
		return Collections.EMPTY_LIST;
	}
	
	public List<ArticleDetails> getArticleDetails(IssueDetails id) {
		return getArticleDetails(id.getYear(), id.getIssueId());
	}

	/**
	 * Main method only for demonstration of class use. Does not require
	 * any arguments.
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
			List<ArticleDetails> adList = acf.getArticleDetails(details);
			for (ArticleDetails ad : adList) {
				System.out.println(ad.toString());
			}
			break;
		}
	}
	
}
