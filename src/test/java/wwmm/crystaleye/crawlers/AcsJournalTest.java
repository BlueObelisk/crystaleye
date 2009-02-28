package wwmm.crystaleye.crawlers;

import org.junit.Test;

import wwmm.crystaleye.crawler.AcsJournal;
import static org.junit.Assert.*;

public class AcsJournalTest {
	
	@Test
	public void checkJournalsHaveAllParamatersSet() {
		for (AcsJournal journal : AcsJournal.values()) {
			String abbreviation = journal.getAbbreviation();
			assertNotNull("Journal "+journal.toString()+" has a NULL abbreviation, must be set to a string.", abbreviation);
			int volumeOffset = journal.getVolumeOffset();
			assertNotNull("Journal "+journal.toString()+" has a NULL volumeOffset, must be set to a string.", volumeOffset);
			String fullTitle = journal.getFullTitle();
			assertNotNull("Journal "+journal.toString()+" has a NULL title, must be set to a string.", fullTitle);
		}
	}

}
