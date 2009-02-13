package wwmm.crystaleye.crawlers;

import static wwmm.crystaleye.CrystalEyeConstants.X_XHTML;
import static wwmm.crystaleye.crawlers.CrawlerConstants.ACS_HOMEPAGE_URL;
import static wwmm.crystaleye.crawlers.CrawlerConstants.DOI_SITE_URL;

import java.util.ArrayList;
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

public class AcsIssueCrawler extends Crawler {

	private AcsJournal journal;
	
	private static final Logger LOG = Logger.getLogger(AcsIssueCrawler.class);

	public AcsIssueCrawler(AcsJournal journal) {
		this.journal = journal;
	}

	public IssueDetails getCurrentIssueDetails() {
		Document doc = getCurrentIssueDocument();
		Nodes journalInfo = doc.query(".//x:div[@id='tocMeta']", X_XHTML);
		int size = journalInfo.size();
		if (size != 1) {
			throw new RuntimeException("Expected to find 1 element containing" +
					" the year/issue information but found "+size+".");
		}
		String info = journalInfo.get(0).getValue().trim();
		Pattern pattern = Pattern.compile("[^,]*,\\s*(\\d+)\\s+Volume\\s+(\\d+),\\s+Issue\\s+(\\d+)\\s+Pages\\s+(\\d+-\\d+).*");
		Matcher matcher = pattern.matcher(info);
		if (!matcher.find() || matcher.groupCount() != 4) {
			throw new RuntimeException("Could not extract the year/issue information.");
		}
		String year = matcher.group(1);
		String issueId = matcher.group(3);
		LOG.debug("Found latest issue details for ACS journal "+journal.getFullTitle()+": year="+year+", issue="+issueId+".");
		return new IssueDetails(year, issueId);
	}
	
	public Document getCurrentIssueDocument() {
		String url = "http://pubs.acs.org/toc/"+journal.getAbbreviation()+"/current";
		URI issueUri = createURI(url);
		return httpClient.getWebpageHTML(issueUri);
	}
	
	public List<DOI> getCurrentIssueDOIs() {
		IssueDetails details = getCurrentIssueDetails();
		return getDOIs(details);
	}

	public List<DOI> getDOIs(String year, String issueId) {
		List<DOI> dois = new ArrayList<DOI>();
		int volume = Integer.valueOf(year)-journal.getVolumeOffset();
		String issueUrl = ACS_HOMEPAGE_URL+"/toc/"+journal.getAbbreviation()+"/"+volume+"/"+issueId;
		URI issueUri = createURI(issueUrl);
		LOG.debug("Started to find DOIs from "+journal.getFullTitle()+", year "+year+", issue "+issueId+".");
		LOG.debug(issueUri.toString());
		Document issueDoc = httpClient.getWebpageHTML(issueUri);
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
		LOG.debug("Starting to find issue article details: "+year+"-"+issueId);
		List<DOI> dois = getDOIs(year, issueId);
		List<ArticleDetails> adList = new ArrayList<ArticleDetails>(dois.size());
		for (DOI doi : dois) {
			ArticleDetails ad = new AcsArticleCrawler(doi).getDetails();
			adList.add(ad);
		}
		LOG.debug("Finished finding issue article details: "+year+"-"+issueId);
		return adList;
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
		for (AcsJournal journal : AcsJournal.values()) {
			if (!journal.getAbbreviation().equals("cgdefu")) {
				continue;
			}
			AcsIssueCrawler acf = new AcsIssueCrawler(journal);
			IssueDetails details = acf.getCurrentIssueDetails();
			List<ArticleDetails> adList = acf.getArticleDetails(details);
			for (ArticleDetails ad : adList) {
				System.out.println(ad.toString());
			}
			break;
		}
	}

}
