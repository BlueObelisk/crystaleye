package wwmm.crystaleye.crawlers;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Node;

import org.apache.commons.httpclient.URI;
import org.apache.log4j.Logger;

import wwmm.crystaleye.util.HttpUtils;
import wwmm.crystaleye.util.Utils;

public class RscCrawler extends JournalCrawler{

	public enum RscJournal {
		CHEMCOMM("cc", "Chemical Communications"),
		CRYSTENGCOMM("ce", "CrystEngComm"),
		PCCP("cp", "PCCP"),
		DALTON_TRANSACTIONS("dt", "Dalton Transactions"),
		GREEN_CHEMISTRY("gc", "Green Chemistry"),
		JOURNAL_OF_MATERIALS_CHEMISTRY("jm", "Journal of Materials Chemistry"),
		NEW_JOURNAL_OF_CHEMISTRY("nj", "New Journal of Chemistry"),
		ORGANIC_AND_BIOMOLECULAR_CHEMISTRY("ob", "Organic and Biomolecular Chemistry");

		private final String abbreviation;
		private final String fullTitle;

		RscJournal(String abbreviation, String fullTitle) {
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

	public RscJournal journal;
	private String volume = "0";
	private static final Logger LOG = Logger.getLogger(RscCrawler.class);

	public RscCrawler(RscJournal journal) {
		this.journal = journal;
	}

	protected IssueDetails getCurrentIssueDetails() throws Exception {
		Document doc = getCurrentIssueDocument();
		List<Node> journalInfo = Utils.queryHTML(doc, "//x:h3[contains(text(),'Contents')]");
		int size = journalInfo.size();
		if (size != 1) {
			throw new Exception("Expected to find 1 element containing"+
					"the year/issue information but found "+size+".");
		}
		String info = journalInfo.get(0).getValue().trim();
		Pattern pattern = Pattern.compile("[^\\d]*(\\d+),[^\\d]*(\\d+)$");
		Matcher matcher = pattern.matcher(info);
		if (!matcher.find() || matcher.groupCount() != 2) {
			throw new Exception("Could not extract the year/issue information.");
		}
		String issueNum = matcher.group(1);
		String year = matcher.group(2);
		return new IssueDetails(year, issueNum);
	}

	public Document getCurrentIssueDocument() throws Exception {
		String url = "http://rsc.org/Publishing/Journals/"
			+journal.getAbbreviation().toUpperCase()+"/Article.asp?Type=CurrentIssue";
		URI uri = new URI(url, false);
		return HttpUtils.getWebpageMinusCommentsAsXML(uri);
	}

	public List<URI> getCurrentIssueDOIs() throws Exception {
		IssueDetails details = getCurrentIssueDetails();
		return getIssueDOIs(details);
	}

	public List<URI> getIssueDOIs(String year, String issueId) throws Exception {
		String journalAbbreviation = journal.getAbbreviation();
		String issueUrl = "http://rsc.org/Publishing/Journals/"+journalAbbreviation
		+"/article.asp?Journal="+journalAbbreviation+"81&VolumeYear="+year+volume+"&Volume="+volume
		+"&JournalCode="+journalAbbreviation+"&MasterJournalCode="+journalAbbreviation+"&SubYear="+year
		+"&type=Issue&Issue="+issueId+"&x=11&y=5";
		URI issueUri = new URI(issueUrl, false);
		LOG.debug("Started to find DOIs from "+journal.getFullTitle()+", year "+year+", issue "+issueId+".");
		Document issueDoc = HttpUtils.getWebpageMinusCommentsAsXML(issueUri);
		List<Node> doiNodes = Utils.queryHTML(issueDoc, ".//x:a[contains(@title,'DOI:10.1039')]");
		List<URI> dois = new ArrayList<URI>();
		for (Node doiNode : doiNodes) {
			String doi = ((Element)doiNode).getValue();
			dois.add(new URI(doi, false));
		}
		LOG.debug("Finished finding DOIs.");
		return dois;
	}

	public List<URI> getIssueDOIs(IssueDetails details) throws Exception {
		return getIssueDOIs(details.getYear(), details.getIssueId());
	}

	public static void main(String[] args) throws Exception {
		for (RscJournal journal : RscJournal.values()) {
			if (!journal.getAbbreviation().equals("cc")) {
				continue;
			}
			RscCrawler acf = new RscCrawler(journal);
			IssueDetails details = acf.getCurrentIssueDetails();
			List<URI> dois = acf.getIssueDOIs(details.getYear(), details.getIssueId());
			for (URI doi : dois) {
				System.out.println(doi);
			}
			break;
		}
	}

}
