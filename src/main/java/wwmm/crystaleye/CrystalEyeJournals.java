package wwmm.crystaleye;

import wwmm.pubcrawler.core.Journal;
import wwmm.pubcrawler.journal.acs.AcsJournalIndex;
import wwmm.pubcrawler.journal.acta.ActaJournalIndex;
import wwmm.pubcrawler.journal.chemsocjapan.ChemSocJapanJournalIndex;
import wwmm.pubcrawler.journal.rsc.RscJournalIndex;

import java.util.ArrayList;
import java.util.List;

public class CrystalEyeJournals {
	
	public List<JournalDetails> getDetails() {
		List<JournalDetails> journalDetails = new ArrayList<JournalDetails>(100);
		for (Journal journal : AcsJournalIndex.getIndex().values()) {
			JournalDetails details = new JournalDetails();
			details.setPublisherTitle("The American Chemical Society");
			details.setPublisherAbbreviation("acs");
			details.setJournalTitle(journal.getFullTitle());
			details.setJournalAbbreviation(journal.getAbbreviation());
			journalDetails.add(details);
		}
		for (Journal journal : ActaJournalIndex.getIndex().values()) {
			JournalDetails details = new JournalDetails();
			details.setPublisherTitle("Acta Crystallographica");
			details.setPublisherAbbreviation("acta");
			details.setJournalTitle(journal.getFullTitle());
			details.setJournalAbbreviation(journal.getAbbreviation());
			journalDetails.add(details);
		}
		for (Journal journal : RscJournalIndex.getIndex().values()) {
			JournalDetails details = new JournalDetails();
			details.setPublisherTitle("The Royal Society of Chemistry");
			details.setPublisherAbbreviation("rsc");
			details.setJournalTitle(journal.getFullTitle());
			details.setJournalAbbreviation(journal.getAbbreviation());
			journalDetails.add(details);
		}
		for (Journal journal : ChemSocJapanJournalIndex.getIndex().values()) {
			JournalDetails details = new JournalDetails();
			details.setPublisherTitle("The Chemical Society of Japan");
			details.setPublisherAbbreviation("chemSocJapan");
			details.setJournalTitle(journal.getFullTitle());
			details.setJournalAbbreviation(journal.getAbbreviation());
			journalDetails.add(details);
		}
		return journalDetails;
	}

}
