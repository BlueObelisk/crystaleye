package wwmm.crystaleye.crawlers;

/**
 * <p>
 * The <code>ActaJournal</code> enum is meant to enumerate useful 
 * details about journals of interest from Acta Crystallographica.
 * </p>
 * 
 * @author Nick Day
 * @version 1.1
 * 
 */
public enum ActaJournal {
	SECTION_A("a", "Section A: Foundations of Crystallography"),
	SECTION_B("b", "Section B: Structural Science"),
	SECTION_C("c", "Section C: Crystal Structure Communications"),
	SECTION_D("d", "Section D: Biological Crystallography"),
	SECTION_E("e", "Section E: Structure Reports"),
	SECTION_F("f", "Section F: Structural Biology and Crystallization Communications"),
	SECTION_J("j", "Section J: Applied Crystallography"),
	SECTION_S("s", "Section S: Synchrotron Radiation");

	private final String abbreviation;
	private final String fullTitle;

	ActaJournal(String abbreviation, String fullTitle) {
		this.abbreviation = abbreviation;
		this.fullTitle = fullTitle;
	}

	/**
	 * <p>
	 * Gets the complete journal title.
	 * </p>
	 * 
	 * @return String of the complete journal title.
	 * 
	 */
	public String getFullTitle() {
		return this.fullTitle;
	}

	/**
	 * <p>
	 * Gets the journal abbreviation (as used by the publisher
	 * on their website).
	 * </p>
	 * 
	 * @return String of the journal abbreviation.
	 * 
	 */
	public String getAbbreviation() {
		return this.abbreviation;
	}
}