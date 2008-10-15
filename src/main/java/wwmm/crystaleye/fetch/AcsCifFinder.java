package wwmm.crystaleye.fetch;

import static wwmm.crystaleye.CrystalEyeConstants.X_XHTML;

import java.io.IOException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;

import wwmm.crystaleye.CrystalEyeRuntimeException;
import wwmm.crystaleye.util.HttpUtils;

public class AcsCifFinder extends JournalCifFinder {
		
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
		
	public AcsCifFinder(AcsJournal journal) {
		this.journal = journal;
	}

	public IssueDetails getCurrentIssueDetails() {
		String url = "http://pubs3.acs.org/acs/journals/toc.page?incoden="+journal.getAbbreviation();
		URI uri = new URI(url, false);
		Document doc = HttpUtils.getWebpageAsXML(uri);
		Nodes journalInfo = doc.query(".//x:div[@id='issueinfo']", X_XHTML);
		if (journalInfo.size() != 0) {
			String info = journalInfo.get(0).getValue().trim();
			Pattern pattern = Pattern.compile("\\s*Vol\\.\\s+\\d+,\\s+No\\.\\s+(\\d+):.*(\\d\\d\\d\\d)");
			Matcher matcher = pattern.matcher(info);
			if (!matcher.find()) {
				throw new CrystalEyeRuntimeException("Could not extract the year/issue information from the 'current-issue' page "+url);
			} else {
				String year = matcher.group(2);
				String issueNum = matcher.group(1);
				return new IssueDetails(year, issueNum);
			}
		} else {
			throw new CrystalEyeRuntimeException("Could not find the year/issue information from the 'current-issue' page "+url);
		}
	}
	
	public void fetch(IssueDetails issueDetails) {
		findCifs(issueDetails.getYear(), issueDetails.getIssueId());
	}
	
	public void findCifs(String year, String issueId) {
		String decade = year.substring(2, 3);
		int volume = Integer.valueOf(year)-journal.getVolumeOffset();
		String issueUrl = "http://pubs3.acs.org/acs/journals/toc.page?incoden="
			+journal.getAbbreviation()+"&indecade="+decade+"&involume="+String.valueOf(volume)+
			"&inissue="+issueId;
		URI issueUri = new URI(issueUrl, false);
		Document doc = HttpUtils.getWebpageAsXML(issueUri);
		Nodes suppLinks = doc.query("//x:a[contains(text(),'Supporting')]", X_XHTML);
		sleep();

		if (suppLinks.size() > 0) {
			for (int j = 0; j < suppLinks.size(); j++) {
				String suppUrl = ((Element)suppLinks.get(j)).getAttributeValue("href");
				URI suppUri = new URI(suppUrl, false);
				doc = HttpUtils.getWebpageAsXML(suppUri);
				sleep();

				Nodes cifLinks = doc.query(".//x:a[contains(@href,'.cif') or contains(@href,'.CIF')]",
						X_XHTML);
				if (cifLinks.size() > 0) {
					for (int k = 0; k < cifLinks.size(); k++) {
						String cifUrl = ((Element)cifLinks.get(k)).getAttributeValue("href");
						int idx = cifUrl.lastIndexOf("/");
						String cifId = cifUrl.substring(0,idx);
						idx = cifId.lastIndexOf("/");
						cifId = cifId.substring(idx+1);
						int suppNum = k+1;
						cifUrl = cifUrl.replaceAll("pubs\\.acs\\.org/", "pubs\\.acs\\.org//");
						
						String doi = null;
						String title = null;
						Nodes doiAnchors = doc.query("//x:a[contains(@href,'dx.doi.org')]", X_XHTML);
						if (doiAnchors.size() > 0) {
							Element doiAnchor = (Element)doiAnchors.get(0);
							doi = doiAnchor.getValue();
							Element parent = (Element)doiAnchor.getParent();
							Nodes titleNodes = parent.query("./x:span[1]", X_XHTML);
							if (titleNodes.size() > 0) {
								title = ((Element)titleNodes.get(0)).getValue();
							}
						}
						
						URL cifURL = new URL(cifUrl);
						writeFiles(issueWriteDir, cifId, suppNum, cifURL, doi, title);
						sleep();
					}
				}
			}
		}
		System.out.println("FINISHED FETCHING CIFS FROM "+issueUrl);
	}

	public static void main(String[] args) throws URIException, CrystalEyeRuntimeException, NullPointerException {
		
		for (AcsJournal journal : AcsJournal.values()) {
			IssueDetails details = new AcsCifFinder(journal).getCurrentIssueDetails();
			System.out.println(details.getYear()+"/"+details.getIssueId());
		}
		
	}
	
}
