package wwmm.crystaleye.fetch;

import static wwmm.crystaleye.CrystalEyeConstants.CHEMSOCJAPAN_DOI_PREFIX;
import static wwmm.crystaleye.CrystalEyeConstants.X_XHTML;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Node;
import nu.xom.Nodes;
import wwmm.crystaleye.IOUtils;

public class ChemistryLettersBacklog extends JournalFetcher {

	private static final String SITE_PREFIX = "http://www.jstage.jst.go.jp";	
	private static final String PUBLISHER_ABBREVIATION = "chemSocJapan";

	String journalAbbreviation;
	String year;
	String issue;

	public ChemistryLettersBacklog(String journalAbbreviation, String year, String issue) {
		this.publisherAbbr = PUBLISHER_ABBREVIATION;
		setYear(year);
		setIssue(issue);
	}

	public void setYear(String year) {
		this.year = year;
	}

	public void setIssue(String issue) {
		this.issue = issue;
	}

	public void fetchAll() throws IOException {
		String url = "http://www.chemistry.or.jp/journals/chem-lett/cl-cont/cl"+this.year+"-"+this.issue+".html";
		System.out.println("Fetching CIFs from "+url);
		Document doc = IOUtils.parseWebPage(url);

		//System.out.println(doc.toXML());
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
					System.out.println("supp page url: "+suppPageUrl);
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
								String cifWriteDir = downloadDir.getCanonicalPath()+File.separator+PUBLISHER_ABBREVIATION+File.separator+journalAbbreviation+File.separator+this.year+File.separator+this.issue+File.separator+cifId;
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
		System.out.println("FINISHED FETCHING CIFS FROM "+url);
	}

	public static void main(String[] args) {
		try {
			// this line just to initialise
			ChemistryLettersBacklog ab = new ChemistryLettersBacklog("chem-lett", "2006", "12");
			Properties props = new Properties();
			props.load(new FileInputStream(
					"e:/data-test2/docs/cif-flow-props.txt"));
			for (int i = 12; i < 13; i++) {
				ab = new ChemistryLettersBacklog("orlef7", "2007", String.valueOf(i));
				ab.setDownloadDir(new File(props.getProperty("write.dir")));
				ab.fetchAll();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
