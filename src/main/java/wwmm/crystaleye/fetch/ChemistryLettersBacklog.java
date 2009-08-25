package wwmm.crystaleye.fetch;

import static wwmm.crystaleye.CrystalEyeConstants.CHEMSOCJAPAN_DOI_PREFIX;
import static wwmm.crystaleye.CrystalEyeConstants.X_XHTML;

import java.io.File;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Node;
import nu.xom.Nodes;

import org.apache.log4j.Logger;

import wwmm.crystaleye.IOUtils;

public class ChemistryLettersBacklog extends Fetcher {
	
	private static final Logger LOG = Logger.getLogger(ChemistryLettersBacklog.class);

	private static final String SITE_PREFIX = "http://www.jstage.jst.go.jp";	
	private static final String PUBLISHER_ABBREVIATION = "chemSocJapan";

	String journalAbbreviation;
	String year;
	String issue;

	public ChemistryLettersBacklog(String propertiesFile, String journalAbbreviation, String year, String issue) {
		super(PUBLISHER_ABBREVIATION, propertiesFile);
		setYear(year);
		setIssue(issue);
	}

	public void setYear(String year) {
		this.year = year;
	}

	public void setIssue(String issue) {
		this.issue = issue;
	}

	public void fetch() {
		String writeDir = properties.getWriteDir();
		String url = "http://www.chemistry.or.jp/journals/chem-lett/cl-cont/cl"+this.year+"-"+this.issue+".html";
		LOG.info("Fetching CIFs from "+url);
		Document doc = IOUtils.parseWebPage(url);

		Nodes abstractPageLinks = doc.query("//x:a[contains(@href ,'n=li_s')]", X_XHTML);
		sleep();
		if (abstractPageLinks.size() > 0) {
			for (int i = 0; i < abstractPageLinks.size(); i++) {
				String abstractPageLink = ((Element)abstractPageLinks.get(i)).getAttributeValue("href");
				Document abstractPage = IOUtils.parseWebPage(abstractPageLink);
				Nodes suppPageLinks = abstractPage.query("//x:a[contains(text(),'Supplementary Materials')]", X_XHTML);
				sleep();
				if (suppPageLinks.size() > 0) {
					String suppPageUrl = SITE_PREFIX+((Element)suppPageLinks.get(0)).getAttributeValue("href");
					Document suppPage = IOUtils.parseWebPage(suppPageUrl);
					Nodes crystRows = suppPage.query("//x:tr[x:td[contains(text(),'cif')]] | //x:tr[x:td[contains(text(),'CIF')]]", X_XHTML);
					sleep();
					if (crystRows.size() > 0) {
						for (int j = 0; j < crystRows.size(); j++) {
							Node crystRow = crystRows.get(j);
							Nodes cifLinks = crystRow.query(".//x:a[contains(@href,'appendix')]", X_XHTML);
							if (cifLinks.size() > 0) {
								String cifLink = SITE_PREFIX+((Element)cifLinks.get(0)).getAttributeValue("href");
								String cif = IOUtils.fetchWebPage(cifLink);
								String cifId = new File(suppPageUrl).getParentFile().getName().replaceAll("_", "-");
								String cifWriteDir = writeDir+File.separator+PUBLISHER_ABBREVIATION+File.separator+journalAbbreviation+File.separator+this.year+File.separator+this.issue+File.separator+cifId;
								Nodes doiElements = abstractPage.query("//*[contains(text(),'doi:"+CHEMSOCJAPAN_DOI_PREFIX+"')]", X_XHTML);
								int suppNum = j+1;
								if (doiElements.size() > 0) {
									String doi = ((Element)doiElements.get(0)).getValue().substring(4).trim();
									IOUtils.writeText(doi, cifWriteDir+File.separator+cifId+"sup"+suppNum+".doi");
								}
								IOUtils.writeText(cif, cifWriteDir+File.separator+cifId+"sup"+suppNum+".cif");
								sleep();
							}
						}
					}
				}
			}
		}
		LOG.info("FINISHED FETCHING CIFS FROM "+url);
	}

	public static void main(String[] args) {
		ChemistryLettersBacklog cl = new ChemistryLettersBacklog("e:/data-test/docs/cif-flow-props.txt", 
				"chem-lett", "2006", "12");
		cl.fetch();
	}
}
