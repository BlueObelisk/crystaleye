package wwmm.crystaleye;

import java.util.ArrayList;
import java.util.List;

import wwmm.pubcrawler.core.AcsJournal;
import wwmm.pubcrawler.core.ActaJournal;
import wwmm.pubcrawler.core.ChemSocJapanJournal;
import wwmm.pubcrawler.core.RscJournal;

public class CrystalEyeJournals {
	
	public List<JournalDetails> getDetails() {
		List<JournalDetails> journalDetails = new ArrayList<JournalDetails>(100);
		for (AcsJournal journal : AcsJournal.values()) {
			JournalDetails details = new JournalDetails();
			details.setPublisherTitle("The American Chemical Society");
			details.setPublisherAbbreviation("acs");
			details.setJournalTitle(journal.getFullTitle());
			details.setJournalAbbreviation(journal.getAbbreviation());
			journalDetails.add(details);
		}
		for (ActaJournal journal : ActaJournal.values()) {
			JournalDetails details = new JournalDetails();
			details.setPublisherTitle("Acta Crystallographica");
			details.setPublisherAbbreviation("acta");
			details.setJournalTitle(journal.getFullTitle());
			details.setJournalAbbreviation(journal.getAbbreviation());
			journalDetails.add(details);
		}
		for (RscJournal journal : RscJournal.values()) {
			JournalDetails details = new JournalDetails();
			details.setPublisherTitle("The Royal Society of Chemistry");
			details.setPublisherAbbreviation("rsc");
			details.setJournalTitle(journal.getFullTitle());
			details.setJournalAbbreviation(journal.getAbbreviation());
			journalDetails.add(details);
		}
		for (ChemSocJapanJournal journal : ChemSocJapanJournal.values()) {
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
