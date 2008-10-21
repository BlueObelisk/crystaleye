package wwmm.crystaleye.crawlers;

import static wwmm.crystaleye.CrystalEyeConstants.X_XHTML;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Node;
import nu.xom.Nodes;

import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.log4j.Logger;

import wwmm.crystaleye.util.Utils;

public class ActaCrawler extends JournalCrawler {

	public enum ActaJournal {
		SECTION_A("a", "Section A: Foundations of Crystallography"),
		SECTION_B("b", "Section B: Structural Science"),
		SECTION_C("c", "Section C: Crystal Structure Communications"),
		SECTION_D("d", "Section D: Biological Crystallography"),
		SECTION_E("e", "Section E: Structure Reports"),
		SECTION_F("f", "Section F: Structural Biology and Crystallization Communications"),
		SECTION_J("j", "Section J: Applied Crystallography"),
		SECTION_S("s", "Section S: Synchrotron Radiation");

		private final String abbreviation;
		private final String fullTitle;

		ActaJournal(String abbreviation, String fullTitle) {
			this.abbreviation = abbreviation;
			this.fullTitle = fullTitle;
		}

		public String getFullTitle() {
			return this.fullTitle;
		}

		public String getAbbreviation() {
			return this.abbreviation;
		}
	}

	public ActaJournal journal;
	private static final Logger LOG = Logger.getLogger(ActaCrawler.class);

	public ActaCrawler(ActaJournal journal) {
		this.journal = journal;
	}

	public IssueDetails getCurrentIssueDetails() {
		Document doc = getCurrentIssueDocument();
		List<Node> currentIssueLink = Utils.queryHTML(doc, "//x:a[contains(@target,'_parent')]");
		Node current = currentIssueLink.get(0);
		if (((Element) current).getValue().contains("preparation")) {
			current = currentIssueLink.get(1);
		}
		String info = ((Element)current).getAttributeValue("href");
		Pattern pattern = Pattern.compile("\\.\\./issues/(\\d\\d\\d\\d)/(\\d\\d/\\d\\d)/issconts.html");
		Matcher matcher = pattern.matcher(info);
		if (!matcher.find() || matcher.groupCount() != 2) {
			throw new RuntimeException("Could not extract the year/issue information " +
					"from current issue for Acta journal, "+journal.getFullTitle()+".");
		}
		String year = matcher.group(1);
		String issueId = matcher.group(2).replaceAll("/", "-");
		LOG.debug("Found latest issue details for Acta journal "+journal.getFullTitle()+": year="+year+", issue="+issueId+".");
		return new IssueDetails(year, issueId);
	}

	public Document getCurrentIssueDocument() {
		String url = "http://journals.iucr.org/"+journal.getAbbreviation()+"/contents/backissuesbdy.html";
		URI issueUri;
		try {
			issueUri = new URI(url, false);
		} catch (URIException e) {
			throw new RuntimeException("Problem creating the issue URI.", e);
		}
		return httpClient.getWebpageDocument(issueUri);
	}

	public List<URI> getCurrentIssueDOIs() {
		IssueDetails details = getCurrentIssueDetails();
		return getIssueDOIs(details);
	}

	public List<URI> getIssueDOIs(String year, String issueId) {
		List<URI> dois = new ArrayList<URI>();
		String url = "http://journals.iucr.org/"+journal.getAbbreviation()+"/issues/"
		+year+"/"+issueId.replaceAll("-", "/")+"/isscontsbdy.html";
		URI issueUri = null;;
		try {
			issueUri = new URI(url, false);
		} catch (URIException e) {
			throw new RuntimeException("Problem creating the issue URI.", e);
		}
		LOG.debug("Started to find article DOIs from "+journal.getFullTitle()+", year "+year+", issue "+issueId+".");
		LOG.debug(issueUri);
		Document issueDoc = httpClient.getWebpageDocument(issueUri);
		List<Node> doiNodes = Utils.queryHTML(issueDoc, ".//x:a[contains(@href,'http://dx.doi.org/10.1107/')]/@href");
		for (Node doiNode : doiNodes) {
			String doi = ((Attribute)doiNode).getValue();
			try {
				dois.add(new URI(doi, false));
			} catch (URIException e) {
				throw new RuntimeException("Problem creating the article DOI.", e);
			}
		}
		LOG.debug("Finished finding issue DOIs.");
		return dois;
	}

	public List<URI> getIssueDOIs(IssueDetails id) {
		return getIssueDOIs(id.getYear(), id.getIssueId());
	}

	public List<ArticleDetails> getIssueArticleDetails(String year, String issueId) {
		List<URI> dois = getIssueDOIs(year, issueId);
		List<ArticleDetails> adList = new ArrayList<ArticleDetails>(dois.size());
		for (URI doi : dois) {
			ArticleDetails ad = getArticleDetails(doi);
			adList.add(ad);
			// FIXME - remove this break
			break;
		}
		return adList;
	}
	
	public List<ArticleDetails> getIssueArticleDetails(IssueDetails id) {
		return getIssueArticleDetails(id.getYear(), id.getIssueId());
	}
	
	private ArticleDetails getArticleDetails(URI doi) {
		ArticleDetails ad = new ArticleDetails();
		Document abstractPageDoc = httpClient.getWebpageDocument(doi);
		
		// get the article title
		Nodes titleNds = abstractPageDoc.query(".//x:div[@class='bibline']/following-sibling::x:h3[1]", X_XHTML);
		if (titleNds.size() != 1) {
			throw new RuntimeException("Could not find article title at: "+doi);
		}
		String title = titleNds.get(0).toXML();
		System.out.println(title);
		
		// get the bibliographic reference
		Nodes bibNds = abstractPageDoc.query(".//x:div[@class='bibline']", X_XHTML);
		if (bibNds.size() != 1) {
			throw new RuntimeException("Could not find bibdata at: "+doi);
		}
		String bibline = bibNds.get(0).getValue();
		Pattern pattern = Pattern.compile("([^\\.]+)\\.\\s+\\((\\d+\\))\\.\\s*(\\w+),\\s*(\\w+\\-\\w+).*");
		Matcher matcher = pattern.matcher(bibline);
		if (!matcher.find() || matcher.groupCount() != 4) {
			throw new RuntimeException("Could not find bibdata at: "+doi);
		}
		String journalAbbreviation = matcher.group(1);
		String year = matcher.group(2);
		String volume = matcher.group(3);
		String pages = matcher.group(4);
		ArticleReference ref = new ArticleReference(journalAbbreviation, year, volume, pages);
		
		// get the author string
		Nodes authorNds = abstractPageDoc.query(".//x:div[@class='bibline']/following-sibling::x:h3[2]", X_XHTML);
		if (authorNds.size() != 1) {
			throw new RuntimeException("Could not find author name text at: "+doi);
		}
		String authors = authorNds.get(0).getValue();
		
		// get the supplementary file URIs
		
		return ad;
	}

	public static void main(String[] args) {
		for (ActaJournal journal : ActaJournal.values()) {
			if (!journal.getAbbreviation().equals("c")) {
				continue;
			}
			ActaCrawler acf = new ActaCrawler(journal);
			IssueDetails details = acf.getCurrentIssueDetails();
			List<ArticleDetails> adList = acf.getIssueArticleDetails(details);
			for (ArticleDetails ad : adList) {
				
			}
			break;
		}
	}

}
