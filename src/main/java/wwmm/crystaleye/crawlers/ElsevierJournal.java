package wwmm.crystaleye.crawlers;

public enum ElsevierJournal {
	POLYHEDRON("02775387", "Polyhedron");

	private final String abbreviation;
	private final String fullTitle;

	ElsevierJournal(String abbreviation, String fullTitle) {
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