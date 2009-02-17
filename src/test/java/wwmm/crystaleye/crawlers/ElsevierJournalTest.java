package wwmm.crystaleye.crawlers;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class ElsevierJournalTest {
	
	@Test
	public void checkJournalsHaveAllParamatersSet() {
		for (ElsevierJournal journal : ElsevierJournal.values()) {
			String abbreviation = journal.getAbbreviation();
			assertNotNull("Journal "+journal.toString()+" has a NULL abbreviation, must be set to a string.", abbreviation);
			String fullTitle = journal.getFullTitle();
			assertNotNull("Journal "+journal.toString()+" has a NULL title, must be set to a string.", fullTitle);
		}
	}

}
