/**
 * 
 */
package wwmm.crystaleye.crawlers;

public enum ChemSocJapanJournal {
	CHEMISTRY_LETTERS("chem-lett", "Chemistry Letters");

	private final String abbreviation;
	private final String fullTitle;

	ChemSocJapanJournal(String abbreviation, String fullTitle) {
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