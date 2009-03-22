package wwmm.crystaleye.crawler;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import wwmm.crystaleye.crawler.ActaJournal;

public class ActaJournalTest {
	
	@Test
	public void checkJournalsHaveAllParamatersSet() {
		for (ActaJournal journal : ActaJournal.values()) {
			String abbreviation = journal.getAbbreviation();
			assertNotNull("Journal "+journal.toString()+" has a NULL abbreviation, must be set to a string.", abbreviation);
			String fullTitle = journal.getFullTitle();
			assertNotNull("Journal "+journal.toString()+" has a NULL title, must be set to a string.", fullTitle);
		}
	}

}
