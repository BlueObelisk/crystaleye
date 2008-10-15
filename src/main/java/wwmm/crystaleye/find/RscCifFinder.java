package wwmm.crystaleye.find;

import static wwmm.crystaleye.CrystalEyeConstants.RSC_DOI_PREFIX;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Node;

import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.log4j.Logger;

import wwmm.crystaleye.util.HttpUtils;
import wwmm.crystaleye.util.Utils;

public class RscCifFinder extends JournalCifFinder{

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
	private final String SITE_PREFIX = "http://www.rsc.org";
	private static final Logger LOG = Logger.getLogger(RscJournal.class);

	public RscCifFinder(RscJournal journal) {
		this.journal = journal;
	}

	protected IssueDetails getCurrentIssueDetails() throws Exception {
		String url = "http://rsc.org/Publishing/Journals/"
			+journal.getAbbreviation().toUpperCase()+"/Article.asp?Type=CurrentIssue";
		URI uri = new URI(url, false);
		Document doc = HttpUtils.getWebpageMinusCommentsAsXML(uri);
		List<Node> journalInfo = Utils.queryHTML(doc, "//x:h3[contains(text(),'Contents')]");
		int size = journalInfo.size();
		if (size != 1) {
			throw new Exception("Expected to find 1 element containing"+
					"the year/issue information but found "+size+" at: "+uri.toString());
		}
		String info = journalInfo.get(0).getValue().trim();
		Pattern pattern = Pattern.compile("[^\\d]*(\\d+),[^\\d]*(\\d+)$");
		Matcher matcher = pattern.matcher(info);
		if (!matcher.find() || matcher.groupCount() != 2) {
			throw new Exception("Could not extract the year/issue information " +
					"from :"+uri.toString());
		}
		String issueNum = matcher.group(1);
		String year = matcher.group(2);
		return new IssueDetails(year, issueNum);
	}

	public List<PublisherCifDetails> findCifs(IssueDetails issueDetails) throws Exception {
		return findCifs(issueDetails.getYear(), issueDetails.getIssueId());
	}

	public List<PublisherCifDetails> findCifs(String year, String issueId) throws Exception {
		List<PublisherCifDetails> pcdList = new ArrayList<PublisherCifDetails>();
		String journalAbbreviation = journal.getAbbreviation();
		String issueUrl = "http://rsc.org/Publishing/Journals/"+journalAbbreviation+
		"/article.asp?Journal="+journalAbbreviation+"81&VolumeYear="+year+volume+"&Volume="+volume+
		"&JournalCode="+journalAbbreviation+"&MasterJournalCode="+journalAbbreviation+"&SubYear="+year+
		"&type=Issue&Issue="+issueId+"&x=11&y=5";
		URI issueUri = new URI(issueUrl, false);
		LOG.debug("Starting to find CIFs from: "+issueUri.toString());
		Document issueDoc = HttpUtils.getWebpageMinusCommentsAsXML(issueUri);
		List<Node> articleLinks = Utils.queryHTML(issueDoc, "//x:a[contains(@href,'/Publishing/Journals/"
				+journal.getAbbreviation().toUpperCase()+
		"/article.asp?doi=') and preceding-sibling::x:strong[contains(text(),'DOI:')]]");
		sleep();
		for (Node articleLink : articleLinks) {
			String articleUrl = SITE_PREFIX
			+ ((Element)articleLink).getAttributeValue("href");
			String ss = "?doi=";
			int ssidx = articleUrl.lastIndexOf(ss);
			String articleId = articleUrl.substring(ssidx+ss.length());
			URI articleUri = new URI(articleUrl, false);
			Document articleDoc = HttpUtils.getWebpageMinusCommentsAsXML(articleUri);

			String title = null;
			List<Node> titleNodes = Utils.queryHTML(articleDoc, ".//x:span[@style='font-size:150%;']");
			if (titleNodes.size() > 0) {
				title = titleNodes.get(0).getValue();
			}

			sleep();
			List<Node> suppLinks = Utils.queryHTML(articleDoc, "//x:a[contains(text(),'Electronic supplementary information')]");
			for (Node suppLink : suppLinks) {
				String link = ((Element)suppLink).getAttributeValue("href");
				String suppUrl = SITE_PREFIX+link;
				URI suppUri = new URI(suppUrl, false);
				articleDoc = HttpUtils.getWebpageMinusCommentsAsXML(suppUri);
				sleep();
				List<Node> cifLinks = Utils.queryHTML(articleDoc, "//x:a[text()='Crystal structure data'] | //x:a[text()='Crystal Structure Data'] | //x:a[text()='Crystal Structure data'] | //x:a[text()='Crystal data'] | //x:a[text()='Crystal Data'] | //x:a[text()='Crystallographic Data'] | //x:a[text()='Crystallographic data']");
				for (Node cifLink : cifLinks) {
					String cifId = ((Element)cifLink).getAttributeValue("href");
					String urlMiddle = link.substring(0, link.lastIndexOf("/"));
					String cifUrl = SITE_PREFIX+urlMiddle+"/"+cifId;
					URI cifUri = new URI(cifUrl, false);
					LOG.debug("Found CIF at "+cifUri.toString());
					PublisherCifDetails pcd = new PublisherCifDetails(cifUri, RSC_DOI_PREFIX+"/"+articleId, title);
					pcdList.add(pcd);
					sleep();
				}
			}
		}
		LOG.debug("Finished finding CIFs from " + issueUri);
		return pcdList;
	}

	public static void main(String[] args) throws Exception  {
		for (RscJournal journal : RscJournal.values()) {
			RscCifFinder acf = new RscCifFinder(journal);
			IssueDetails details = acf.getCurrentIssueDetails();
			System.out.println(journal.getAbbreviation()+"::"+details.getYear()+"/"+details.getIssueId());
		}
	}

}
