package wwmm.crystaleye.find;

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

public class ChemSocJapanCifFinder extends JournalCifFinder {

	public enum ChemSocJapanJournal {
		CHEMISTRY_LETTERS("chem-lett", "Chemistry Letters");

		private final String abbreviation;
		private final String fullTitle;

		ChemSocJapanJournal(String abbreviation, String fullTitle) {
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

	public ChemSocJapanJournal journal;
	private static final String SITE_PREFIX = "http://www.jstage.jst.go.jp";
	private static final Logger LOG = Logger.getLogger(ChemSocJapanCifFinder.class);

	public ChemSocJapanCifFinder(ChemSocJapanJournal journal) {
		this.journal = journal;
	}

	protected IssueDetails getCurrentIssueDetails() throws Exception {
		String url = "http://www.csj.jp/journals/"+journal.getAbbreviation()+"/cl-cont/newissue.html";
		URI issueUri = new URI(url, false);
		Document doc = HttpUtils.getWebpageMinusCommentsAsXML(issueUri);
		List<Node> journalInfo = Utils.queryHTML(doc, "//x:span[@class='augr']");
		int size = journalInfo.size();
		if (size != 1) {
			throw new Exception("Expected to find 1 element containing" +
					"the year/issue information but found "+size+" at: "+issueUri.toString());
		}
		String info = journalInfo.get(0).getValue();
		Pattern pattern = Pattern.compile("[^,]*,\\s+\\w+\\.\\s+(\\d+)\\s+\\([^,]*,\\s+(\\d\\d\\d\\d)\\)");
		Matcher matcher = pattern.matcher(info);
		if (!matcher.find() || matcher.groupCount() != 2) {
			throw new Exception("Could not extract the year/issue information " +
					"from :"+issueUri.toString());
		}
		String year = matcher.group(2);
		String issueNum = matcher.group(1);
		return new IssueDetails(year, issueNum);
	}
	
	public List<PublisherCifDetails> findCifs(IssueDetails issueDetails) throws Exception {
		return findCifs(issueDetails.getYear(), issueDetails.getIssueId());
	}

	public List<PublisherCifDetails> findCifs(String year, String issueId) throws Exception {
		List<PublisherCifDetails> pcdList = new ArrayList<PublisherCifDetails>();
		String url = "http://www.chemistry.or.jp/journals/chem-lett/cl-cont/cl"+year+"-"+issueId+".html";
		URI issueUri = new URI(url, false);
		LOG.debug("Started to find CIFs from "+journal.getFullTitle()+", year "+year+", issue "+issueId+".");
		LOG.debug("Starting at the issue homepage: "+issueUri.toString());
		Document doc = HttpUtils.getWebpageMinusCommentsAsXML(issueUri);
		List<Node> abstractPageLinks = Utils.queryHTML(doc, "//x:a[contains(text(),'Supporting Information')]");
		sleep();
		for (Node abstractPageLink : abstractPageLinks) {
			String abstractPageUrl = ((Element)abstractPageLink).getAttributeValue("href");
			URI abstractPageUri = new URI(abstractPageUrl, false);
			Document abstractPage = HttpUtils.getWebpageAsXML(abstractPageUri);
			List<Node> suppPageLinks = Utils.queryHTML(abstractPage, "//x:a[contains(text(),'Supplementary Materials')]");
			sleep();
			String suppPageUrl = SITE_PREFIX+((Element)suppPageLinks.get(0)).getAttributeValue("href");
			URI suppPageUri = new URI(suppPageUrl, false);
			Document suppPage = HttpUtils.getWebpageAsXML(suppPageUri);
			List<Node> crystRows = Utils.queryHTML(suppPage, "//x:tr[x:td[contains(text(),'cif')]] | //x:tr[x:td[contains(text(),'CIF')]]");
			sleep();
			for (Node crystRow : crystRows) {
				List<Node> cifLinks = Utils.queryHTML(crystRow, ".//x:a[contains(@href,'appendix')]");
				if (cifLinks.size() == 0) {
					continue;
				}
				String cifUrl = SITE_PREFIX+((Element)cifLinks.get(0)).getAttributeValue("href");
				URI cifUri = new URI(cifUrl, false);
				LOG.debug("Found CIF at "+cifUri.toString());
				List<Node> doiElements = Utils.queryHTML(abstractPage, "//*[contains(text(),'doi:10.1246')]");
				String doi = null;
				if (doiElements.size() > 0) {
					doi = ((Element)doiElements.get(0)).getValue().substring(4).trim();
				}
				List<Node> titleNodes = Utils.queryHTML(abstractPage, ".//x:font[@size='+1']");
				String title = null;
				if (titleNodes.size() > 0) {
					title = titleNodes.get(0).getValue();
				}
				PublisherCifDetails pcd = new PublisherCifDetails(cifUri, doi, title);
				pcdList.add(pcd);
				sleep();
			}
		}
		LOG.debug("Finished finding CIFs from " + issueUri);
		return pcdList;
	}
	
	public static void main(String[] args) throws Exception  {
		for (ChemSocJapanJournal journal : ChemSocJapanJournal.values()) {
			ChemSocJapanCifFinder acf = new ChemSocJapanCifFinder(journal);
			List<PublisherCifDetails> pcdList = acf.findCifs("2008", "7");
			for (PublisherCifDetails pcd : pcdList) {
				System.out.println(pcd.getDoi());
			}
			break;
		}
	}

}
