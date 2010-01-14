package ned24.sandbox;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.DirectoryWalker;
import org.apache.commons.io.FileUtils;
import org.xmlcml.molutil.ChemicalElement;

import wwmm.crystaleye.fetch.AcsJournal;
import wwmm.crystaleye.fetch.ActaJournal;
import wwmm.crystaleye.fetch.ChemSocJapanJournal;
import wwmm.crystaleye.fetch.NatureJournal;
import wwmm.crystaleye.fetch.RscJournal;
import wwmm.crystaleye.util.FreemarkerUtils;
import freemarker.template.SimpleHash;
import freemarker.template.SimpleSequence;

public class SiteCreation {

	private File outDir;
	
	public SiteCreation(File outDir) {
		this.outDir = outDir;
	}

	public void createWebsite() {
		copySiteFiles();
		createFeedPages();
		createSummaryPages();
		createFaqPages();
		createGreaseMonkeyPages();
		try {
			new SvnFileCleaner().clean(outDir);
		} catch (IOException e) {
			throw new RuntimeException("Problem whilst removing SVN files: "+e.getMessage(), e);
		}
	}
	
	private void createFeedPages() {
		File feedHomePage = new File(outDir, "feed/index.html");
		SimpleHash feedHomeMap = new SimpleHash();
		feedHomeMap.put("pageTitle", "CrystalEye: RSS feeds");
		feedHomeMap.put("pathToRoot", "../");
		feedHomeMap.put("currentMenuSelected", "feeds");
		FreemarkerUtils.writeHtmlTemplate("feed-homepage.ftl", feedHomePage, feedHomeMap);
		
		createAllFeedPages();
		createAtomsFeedPages();
		createBondsFeedPages();
		createClassFeedPages();
		createJournalFeedPages();
	}
	
	private void createJournalFeedPages() {
		File journalFeedPage = new File(outDir, "feed/journal/index.html");
		SimpleHash journalFeedMap = new SimpleHash();
		journalFeedMap.put("pageTitle", "CrystalEye: RSS feeds");
		journalFeedMap.put("pathToRoot", "../../");
		journalFeedMap.put("currentMenuSelected", "feeds");
		SimpleSequence publishers = new SimpleSequence();
		journalFeedMap.put("publishers", publishers);
		
		SimpleHash acsMap = new SimpleHash();
		publishers.add(acsMap);
		SimpleSequence acsJournals = new SimpleSequence();
		acsMap.put("journals", acsJournals);
		acsMap.put("title", "American Chemical Society");
		acsMap.put("abbreviation", "acs");
		for (AcsJournal acsJournal : AcsJournal.values()) {
			SimpleHash journal = new SimpleHash();
			acsJournals.add(journal);
			journal.put("abbreviation", acsJournal.getAbbreviation());
			journal.put("title", acsJournal.getFullTitle());
		}
		
		SimpleHash actaMap = new SimpleHash();
		publishers.add(actaMap);
		SimpleSequence actaJournals = new SimpleSequence();
		actaMap.put("journals", actaJournals);
		actaMap.put("title", "Acta Crystallographica");
		actaMap.put("abbreviation", "acta");
		for (ActaJournal actaJournal : ActaJournal.values()) {
			SimpleHash journal = new SimpleHash();
			actaJournals.add(journal);
			journal.put("abbreviation", actaJournal.getAbbreviation());
			journal.put("title", actaJournal.getFullTitle());
		}
		
		SimpleHash csjMap = new SimpleHash();
		publishers.add(csjMap);
		SimpleSequence csjJournals = new SimpleSequence();
		csjMap.put("journals", csjJournals);
		csjMap.put("title", "Chemical Society of Japan");
		csjMap.put("abbreviation", "chemSocJapan");
		for (ChemSocJapanJournal csjJournal : ChemSocJapanJournal.values()) {
			SimpleHash journal = new SimpleHash();
			csjJournals.add(journal);
			journal.put("abbreviation", csjJournal.getAbbreviation());
			journal.put("title", csjJournal.getFullTitle());
		}
		
		SimpleHash natureMap = new SimpleHash();
		publishers.add(natureMap);
		SimpleSequence natureJournals = new SimpleSequence();
		natureMap.put("journals", natureJournals);
		natureMap.put("title", "Nature");
		natureMap.put("abbreviation", "nature");
		for (NatureJournal natureJournal : NatureJournal.values()) {
			SimpleHash journal = new SimpleHash();
			natureJournals.add(journal);
			journal.put("abbreviation", natureJournal.getAbbreviation());
			journal.put("title", natureJournal.getFullTitle());
		}
		
		SimpleHash rscMap = new SimpleHash();
		publishers.add(rscMap);
		SimpleSequence rscJournals = new SimpleSequence();
		rscMap.put("journals", rscJournals);
		rscMap.put("title", "Royal Society of Chemistry");
		rscMap.put("abbreviation", "rsc");
		for (RscJournal rscJournal : RscJournal.values()) {
			SimpleHash journal = new SimpleHash();
			rscJournals.add(journal);
			journal.put("abbreviation", rscJournal.getAbbreviation());
			journal.put("title", rscJournal.getFullTitle());
		}
		
		FreemarkerUtils.writeHtmlTemplate("feed-journal-index.ftl", journalFeedPage, journalFeedMap);
	}
	
	private void createClassFeedPages() {
		File classFeedPage = new File(outDir, "feed/class/index.html");
		SimpleHash classFeedMap = new SimpleHash();
		classFeedMap.put("pageTitle", "CrystalEye: RSS feeds");
		classFeedMap.put("pathToRoot", "../../");
		classFeedMap.put("currentMenuSelected", "feeds");
		FreemarkerUtils.writeHtmlTemplate("feed-class-index.ftl", classFeedPage, classFeedMap);
	}
	
	private void createAtomsFeedPages() {
		File atomsFeedPage = new File(outDir, "feed/atoms/index.html");
		SimpleHash atomsFeedMap = new SimpleHash();
		atomsFeedMap.put("pageTitle", "CrystalEye: RSS feeds");
		atomsFeedMap.put("pathToRoot", "../../");
		atomsFeedMap.put("currentMenuSelected", "feeds");
		SimpleSequence atoms = new SimpleSequence();
		atomsFeedMap.put("atoms", atoms);
		for (int i = 1; i < 105; i++) {
			SimpleHash atom = new SimpleHash();
			ChemicalElement ce = ChemicalElement.getElement(i);
			atom.put("symbol", ce.getSymbol());
			atoms.add(atom);
		}
		FreemarkerUtils.writeHtmlTemplate("feed-atoms-index.ftl", atomsFeedPage, atomsFeedMap);
	}
	
	private void createBondsFeedPages() {
		createBondsIndex();
		createBondPairsIndex();
	}
	
	private void createBondsIndex() {
		File bondsFeedPage = new File(outDir, "feed/bonds/index.html");
		SimpleHash bondsFeedMap = new SimpleHash();
		bondsFeedMap.put("pageTitle", "CrystalEye: RSS feeds");
		bondsFeedMap.put("pathToRoot", "../../");
		bondsFeedMap.put("currentMenuSelected", "feeds");
		SimpleSequence atoms = new SimpleSequence();
		bondsFeedMap.put("atoms", atoms);
		for (int i = 1; i < 105; i++) {
			SimpleHash atom = new SimpleHash();
			ChemicalElement ce = ChemicalElement.getElement(i);
			atom.put("symbol", ce.getSymbol());
			atoms.add(atom);
		}
		FreemarkerUtils.writeHtmlTemplate("feed-bonds-index.ftl", bondsFeedPage, bondsFeedMap);
	}
	
	private void createBondPairsIndex() {
		for (int i = 1; i < 105; i++) {
			SimpleHash bondsFeedMap = new SimpleHash();
			bondsFeedMap.put("pageTitle", "CrystalEye: RSS feeds");
			bondsFeedMap.put("pathToRoot", "../../");
			bondsFeedMap.put("currentMenuSelected", "feeds");
			SimpleSequence bonds = new SimpleSequence();
			bondsFeedMap.put("bonds", bonds);
			ChemicalElement ce1 = ChemicalElement.getElement(i);
			String symbol1 = ce1.getSymbol();
			File bondsFeedPage = new File(outDir, "feed/bonds/"+symbol1+"-index.html");
			for (int j = 1; j < 105; j++) {
				SimpleHash bond = new SimpleHash();
				ChemicalElement ce2 = ChemicalElement.getElement(j);
				bond.put("symbol", ce1.getSymbol()+"-"+ce2.getSymbol());
				bonds.add(bond);
			}
			FreemarkerUtils.writeHtmlTemplate("feed-bondpairs-index.ftl", bondsFeedPage, bondsFeedMap);
		}
	}
	
	private void createAllFeedPages() {
		File allFeedPage = new File(outDir, "feed/all/index.html");
		SimpleHash allFeedMap = new SimpleHash();
		allFeedMap.put("pageTitle", "CrystalEye: RSS feeds");
		allFeedMap.put("pathToRoot", "../../");
		allFeedMap.put("currentMenuSelected", "feeds");
		FreemarkerUtils.writeHtmlTemplate("feed-all-index.ftl", allFeedPage, allFeedMap);
	}
	
	private void createSummaryPages() {
		File summaryHomePage = new File(outDir, "summary/index.html");
		SimpleHash summaryHomeMap = new SimpleHash();
		summaryHomeMap.put("pageTitle", "CrystalEye: Browse structures");
		summaryHomeMap.put("pathToRoot", "../");
		summaryHomeMap.put("currentMenuSelected", "summary");
		FreemarkerUtils.writeHtmlTemplate("journal-index.ftl", summaryHomePage, summaryHomeMap);
		
		File summaryNotesPage = new File(outDir, "summary/notes.html");
		SimpleHash summaryNotesMap = new SimpleHash();
		summaryNotesMap.put("pageTitle", "CrystalEye: Points for browsing structures");
		summaryNotesMap.put("pathToRoot", "../");
		summaryNotesMap.put("currentMenuSelected", "summary");
		FreemarkerUtils.writeHtmlTemplate("browsing-notes.ftl", summaryNotesPage, summaryNotesMap);
	}
	
	private void createFaqPages() {
		File faqHomePage = new File(outDir, "faq/index.html");
		SimpleHash map = new SimpleHash();
		map.put("pageTitle", "CrystalEye: Frequently Asked Questions");
		map.put("pathToRoot", "../");
		map.put("currentMenuSelected", "faq");
		FreemarkerUtils.writeHtmlTemplate("faq.ftl", faqHomePage, map);
	}
	
	private void createGreaseMonkeyPages() {
		File gmHomePage = new File(outDir, "gm/index.html");
		SimpleHash map = new SimpleHash();
		map.put("pageTitle", "CrystalEye: Greasemonkey");
		map.put("pathToRoot", "../");
		map.put("currentMenuSelected", "greasemonkey");
		FreemarkerUtils.writeHtmlTemplate("greasemonkey.ftl", gmHomePage, map);
	}

	private void copySiteFiles() {
		File siteFilesDir = new File("./src/main/resources/website");
		try {
			FileUtils.copyDirectory(siteFilesDir, outDir);
		} catch (IOException e) {
			throw new RuntimeException("Couldn't copy website files: "+e.getMessage(), e);
		}
	}

	public static void main(String[] args) {
		File outDir = new File("c:/workspace/crystaleye-website-test");
		SiteCreation sc = new SiteCreation(outDir);
		sc.createWebsite();
	}

	
	public static class SvnFileCleaner extends DirectoryWalker {

		public SvnFileCleaner() {
			super();
		}

		public List clean(File startDirectory) throws IOException {
			List results = new ArrayList();
			walk(startDirectory, results);
			return results;
		}

		protected boolean handleDirectory(File directory, int depth, Collection results) {
			// delete svn directories and then skip
			if (".svn".equals(directory.getName())) {
				try {
					FileUtils.deleteDirectory(directory);
				} catch (IOException e) {
					throw new RuntimeException("Problem deleting directory: "+e.getMessage(), e);
				}
				return false;
			} else {
				return true;
			}

		}
	}


}
