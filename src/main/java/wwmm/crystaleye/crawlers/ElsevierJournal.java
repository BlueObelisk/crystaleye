package wwmm.crystaleye.crawlers;

/**
 * <p>
 * The <code>ElsevierJournal</code> enum is meant to enumerate useful 
 * details about journals of interest from Elsevier.
 * </p>
 * 
 * @author Nick Day
 * @version 1.1
 * 
 */
public enum ElsevierJournal {
	POLYHEDRON("02775387", "Polyhedron");

	private final String abbreviation;
	private final String fullTitle;

	ElsevierJournal(String abbreviation, String fullTitle) {
		this.abbreviation = abbreviation;
		this.fullTitle = fullTitle;
	}

	/**
	 * Gets the complete journal title.
	 * 
	 * @return String of the complete journal title.
	 * 
	 */
	public String getFullTitle() {
		return this.fullTitle;
	}

	/**
	 * Gets the journal abbreviation (as used by the publisher
	 * on their website).
	 * 
	 * @return String of the journal abbreviation.
	 * 
	 */
	public String getAbbreviation() {
		return this.abbreviation;
	}
}