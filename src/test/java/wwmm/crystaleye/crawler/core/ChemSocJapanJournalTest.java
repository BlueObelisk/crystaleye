package wwmm.crystaleye.crawler.core;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import wwmm.crystaleye.crawler.core.ChemSocJapanJournal;

public class ChemSocJapanJournalTest {
	
	@Test
	public void checkJournalsHaveAllParamatersSet() {
		for (ChemSocJapanJournal journal : ChemSocJapanJournal.values()) {
			String abbreviation = journal.getAbbreviation();
			assertNotNull("Journal "+journal.toString()+" has a NULL abbreviation, must be set to a string.", abbreviation);
			String fullTitle = journal.getFullTitle();
			assertNotNull("Journal "+journal.toString()+" has a NULL title, must be set to a string.", fullTitle);
		}
	}

}
