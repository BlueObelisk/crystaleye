/**
 * 
 */
package wwmm.crystaleye.crawlers;

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

	public String getFullTitle() {
		return this.fullTitle;
	}

	public String getAbbreviation() {
		return this.abbreviation;
	}
}