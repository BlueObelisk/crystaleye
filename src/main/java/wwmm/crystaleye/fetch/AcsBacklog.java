package wwmm.crystaleye.fetch;

import static wwmm.crystaleye.CrystalEyeConstants.CIF_MIME;
import static wwmm.crystaleye.CrystalEyeConstants.DATE_MIME;
import static wwmm.crystaleye.CrystalEyeConstants.DOI_MIME;
import static wwmm.crystaleye.CrystalEyeConstants.X_XHTML;

import java.io.File;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;

import org.apache.log4j.Logger;

import wwmm.crystaleye.util.Utils;
import wwmm.crystaleye.util.WebUtils;

public class AcsBacklog extends Fetcher {
	
	private static final Logger LOG = Logger.getLogger(AcsBacklog.class);

	private static final String PUBLISHER_ABBREVIATION = "acs";

	private AcsJournal journal;
	private String year;
	private String issue;
	private String volume = "0";

	public AcsBacklog(String propertiesFile, AcsJournal journal, String year, String issue) {
		super(PUBLISHER_ABBREVIATION, propertiesFile);
		this.journal = journal;
		this.year = year;
		this.issue = issue;
		setVolumeFromYear();
	}

	private void setVolumeFromYear() {
		int vol = Integer.valueOf(year)-journal.getVolumeOffset();
		volume = String.valueOf(vol);
	}

	public void fetch() {
		String writeDir = properties.getWriteDir();
		String issueWriteDir = writeDir+"/"+PUBLISHER_ABBREVIATION
		+"/"+journal.getAbbreviation()+"/"+year
		+"/"+issue;
		String issueUrl = "http://pubs.acs.org/toc/"+journal.getAbbreviation()+"/"+volume+"/"+issue;
		Document doc = WebUtils.parseWebPage(issueUrl);

		if (doc == null) {
			throw new RuntimeException("Couldn't find URL");
		}

		Nodes suppLinks = doc.query(".//x:a[contains(@href,'/doi/suppl/10.1021')]", X_XHTML);
		LOG.debug("supplinks: "+suppLinks.size());
		sleep();
		if (suppLinks.size() > 0) {
			for (int j = 0; j < suppLinks.size(); j++) {
				String suppUrlPostfix = ((Element)suppLinks.get(j)).getAttributeValue("href");
				String suppUrl = "http://pubs.acs.org"+suppUrlPostfix;
				int idx = suppUrl.lastIndexOf("/");
				String cifId = suppUrl.substring(idx+1);
				doc = WebUtils.parseWebPage(suppUrl);
				sleep();

				Nodes cifLinks = doc.query(".//x:a[contains(@href,'.cif')]", X_XHTML);
				if (cifLinks.size() > 0) {
					for (int k = 0; k < cifLinks.size(); k++) {
						String cifUrl = "http://pubs.acs.org"+((Element)cifLinks.get(k)).getAttributeValue("href");
						int suppNum = k+1;
						String cif = getWebPage(cifUrl);
						Nodes doiAnchors = doc.query("//x:a[contains(@href,'dx.doi.org')]", X_XHTML);
						String doi = null;
						if (doiAnchors.size() > 0) {
							doi = ((Element)doiAnchors.get(0)).getValue();
						}
						writeFiles(issueWriteDir, cifId, suppNum, cif, doi);
						sleep();
					}
				}
			}
		}		
		LOG.info("FINISHED FETCHING CIFS FROM " + issueUrl);
	}

	protected void writeFiles(String issueWriteDir, String cifId, int suppNum, String cif, String doi) {
		String pathPrefix = issueWriteDir+"/"+cifId+"/"+cifId;
		LOG.info("Writing cif to: "+pathPrefix+"sup"+suppNum+CIF_MIME);
		Utils.writeText(new File(pathPrefix+"sup"+suppNum+CIF_MIME), cif);
		if (doi != null) {
			Utils.writeText(new File(pathPrefix+DOI_MIME), doi);
		}
		Utils.writeDateStamp(pathPrefix+DATE_MIME);
	}


	/**
	 * ancham 24
bichaw 52 
cmatex 24
iecred 24
inocaj 24
jafcau 24
jacsat 52
jmcmar 24
joceah 24
langd5 24
mamobx 24
orlef7 24
orgnd7 24
	 */
	public static void main(String[] args) {
		String props = "E:\\crystaleye-new\\docs\\cif-flow-props.txt";
		String year = "2008";
		boolean start = false;
		for (AcsJournal journal : AcsJournal.values()) {
			String abb = journal.getAbbreviation();
			if (abb.equals("inocaj") ||
					abb.equals("jafcau") ||
					abb.equals("jmcmar") ||
					abb.equals("joceah") ||
					abb.equals("langd5") ||
					abb.equals("mamobx") ||
					abb.equals("orlef7") ||
					abb.equals("orgnd7")) {
				for (int i = 13; i < 25; i++) {
					AcsBacklog ab = new AcsBacklog(props, journal, "2008", String.valueOf(i));
					ab.fetch();
				}
			}
		}
		
			AcsBacklog ab = new AcsBacklog(props, AcsJournal.CRYSTAL_GROWTH_AND_DESIGN, "2009", "1");
			ab.fetch();
			for (int i = 1; i < 6; i++) {
				 ab = new AcsBacklog(props, AcsJournal.INORGANIC_CHEMISTRY, "2009", String.valueOf(i));
				ab.fetch();
			}
			for (int i = 1; i < 7; i++) {
				ab = new AcsBacklog(props, AcsJournal.JOURNAL_OF_THE_AMERICAN_CHEMICAL_SOCIETY, "2009", String.valueOf(i));
				ab.fetch();
			}
			for (int i = 1; i < 4; i++) {
				ab = new AcsBacklog(props, AcsJournal.THE_JOURNAL_OF_ORGANIC_CHEMISTRY, "2009", String.valueOf(i));
				ab.fetch();
			}
			for (int i = 1; i < 4; i++) {
				ab = new AcsBacklog(props, AcsJournal.ORGANOMETALLICS, "2009", String.valueOf(i));
				ab.fetch();
			}
			for (int i = 1; i < 4; i++) {
				ab = new AcsBacklog(props, AcsJournal.ORGANIC_LETTERS, "2009", String.valueOf(i));
				ab.fetch();
			}
	}
}
