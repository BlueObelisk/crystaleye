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

public class AcsCrawler extends JournalCrawler {

	public enum AcsJournal {
		ACCOUNTS_OF_CHEMICAL_RESEARCH("achre4", "Accounts of Chemical Research", 1967),
		ANALYTICAL_CHEMISTRY("ancham", "Analytical Chemistry", 1928),
		BIOCONJUGATE_CHEMISTRY("bcches", "Bioconjugate Chemistry", 1989),
		BIOCHEMISTRY("bichaw", "Biochemistry", 1961),
		BIOMACROMOLECULES("bomaf6", "Biomacromolecules", 1999),
		CHEMICAL_REVIEWS("chreay", "Chemical Reviews", 1900),
		CHEMISTRY_OF_MATERIALS("cmatex", "Chemistry of Materials", 1988),
		CRYSTAL_GROWTH_AND_DESIGN("cgdefu", "Crystal Growth and Design", 2000),
		ENERGY_AND_FUELS("enfuem", "Energy & Fuels", 1986),
		INDUSTRIAL_AND_ENGINEERING_CHEMISTRY_RESEARCH("iecred", "Industrial & Engineering Chemistry Research", 1961),
		INORGANIC_CHEMISTRY("inocaj", "Inorganic Chemistry", 1961),
		JOURNAL_OF_AGRICULTURAL_AND_FOOD_CHEMISTRY("jafcau", "Journal of Agricultural and Food Chemistry", 1952),
		JOURNAL_OF_CHEMICAL_AND_ENGINEERING_DATA("jceaax", "Journal of Chemical & Engineering Data", 1955),
		JOURNAL_OF_THE_AMERICAN_CHEMICAL_SOCIETY("jacsat", "Journal of the American Chemical Society", 1878),
		JOURNAL_OF_COMBINATORIAL_CHEMISTRY("jcchff", "Journal of Combinatorial Chemistry", 1998),
		JOURNAL_OF_CHEMICAL_INFORMATION_AND_MODELLING("jcisd8", "Journal of Chemical Information and Modelling", 1960),
		JOURNAL_OF_MEDICINAL_CHEMISTRY("jmcmar", "Journal of Medicinal Chemistry", 1957),
		JOURNAL_OF_NATURAL_PRODUCTS("jnprdf", "Journal of Natural Products", 1937),
		THE_JOURNAL_OF_ORGANIC_CHEMISTRY("joceah", "The Journal of Organic Chemistry", 1935),
		LANGMUIR("langd5", "Langmuir", 1984),
		MACROMOLECULES("mamobx", "Macromolecules", 1967),
		MOLECULAR_PHARMACEUTICS("mpohbp", "Molecular Pharmaceutics", 2003),
		ORGANIC_LETTERS("orlef7", "Organic Letters", 1998),
		ORGANIC_PROCESS_RESEARCH_AND_DEVELOPMENT("oprdfk", "Organic Process and Research and Development", 1996),
		ORGANOMETALLICS("orgnd7", "Organometallics", 1981);

		private final String abbreviation;
		private final String fullTitle;
		private final int volumeOffset;

		AcsJournal(String abbreviation, String fullTitle, int volumeOffset) {
			this.abbreviation = abbreviation;
			this.fullTitle = fullTitle;
			this.volumeOffset = volumeOffset;
		}

		public String getFullTitle() {
			return this.fullTitle;
		}

		public String getAbbreviation() {
			return this.abbreviation;
		}

		public int getVolumeOffset() {
			return this.volumeOffset;
		}
	}

	public AcsJournal journal;
	private static final Logger LOG = Logger.getLogger(AcsCrawler.class);

	public AcsCrawler(AcsJournal journal) {
		this.journal = journal;
	}

	public IssueDetails getCurrentIssueDetails() throws Exception {
		Document doc = getCurrentIssueDocument();
		List<Node> journalInfo = Utils.queryHTML(doc, ".//x:div[@id='issueinfo']");
		int size = journalInfo.size();
		if (size != 1) {
			throw new Exception("Expected to find 1 element containing" +
					"the year/issue information but found "+size+".");
		}
		String info = journalInfo.get(0).getValue().trim();
		Pattern pattern = Pattern.compile("\\s*Vol\\.\\s+\\d+,\\s+No\\.\\s+(\\d+):.*(\\d\\d\\d\\d)");
		Matcher matcher = pattern.matcher(info);
		if (!matcher.find() || matcher.groupCount() != 2) {
			throw new Exception("Could not extract the year/issue information.");
		}
		String year = matcher.group(2);
		String issueId = matcher.group(1);
		LOG.debug("Found latest issue details for ACS journal "+journal.getFullTitle()+": year="+year+", issue="+issueId+".");
		return new IssueDetails(year, issueId);
	}
	
	public Document getCurrentIssueDocument() throws Exception {
		String url = "http://pubs3.acs.org/acs/journals/toc.page?incoden="+journal.getAbbreviation();
		URI issueUri = new URI(url, false);
		return HttpUtils.getWebpageAsXML(issueUri);
	}
	
	public List<URI> getCurrentIssueDOIs() throws Exception {
		IssueDetails details = getCurrentIssueDetails();
		return getIssueDOIs(details);
	}

	public List<URI> getIssueDOIs(String year, String issueId) throws Exception {
		List<URI> dois = new ArrayList<URI>();
		String decade = year.substring(2, 3);
		int volume = Integer.valueOf(year)-journal.getVolumeOffset();
		String issueUrl = "http://pubs3.acs.org/acs/journals/toc.page?incoden="
			+journal.getAbbreviation()+"&indecade="+decade+"&involume="+String.valueOf(volume)+
			"&inissue="+issueId;
		URI issueUri = new URI(issueUrl, false);
		LOG.debug("Started to find DOIs from "+journal.getFullTitle()+", year "+year+", issue "+issueId+".");
		LOG.debug(issueUri.toString());
		Document issueDoc = HttpUtils.getWebpageAsXML(issueUri);
		List<Node> doiNodes = Utils.queryHTML(issueDoc, ".//x:a[contains(@href,'http://dx.doi.org/10.1021')]");
		for (Node doiNode : doiNodes) {
			String doi = ((Element)doiNode).getValue();
			dois.add(new URI(doi, false));
		}
		LOG.debug("Finished finding issue DOIs.");
		return dois;
	}
	
	public List<URI> getIssueDOIs(IssueDetails details) throws Exception {
		return getIssueDOIs(details.getYear(), details.getIssueId());
	}

	public static void main(String[] args) throws Exception {
		for (AcsJournal journal : AcsJournal.values()) {
			if (!journal.getAbbreviation().equals("cgdefu")) {
				continue;
			}
			AcsCrawler acf = new AcsCrawler(journal);
			IssueDetails details = acf.getCurrentIssueDetails();
			List<URI> dois = acf.getIssueDOIs(details.getYear(), details.getIssueId());
			for (URI doi : dois) {
				System.out.println(doi);
			}
			break;
		}
	}

}
