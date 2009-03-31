package wwmm.crystaleye.crawler.core;

/**
 * <p>
 * The <code>RscJournal</code> enum is meant to enumerate useful 
 * details about journals of interest from the Royal Society of 
 * Chemistry.
 * </p>
 * 
 * @author Nick Day
 * @version 1.1
 * 
 */
public enum RscJournal {
	ANNUAL_REPORTS_SECTION_A("ic", "Annual Reports Section A"),
	ANNUAL_REPORTS_SECTION_B("oc", "Annual Reports Section B"),
	ANNUAL_REPORTS_SECTION_C("pc", "Annual Reports Section C"),
	CHEMCOMM("cc", "Chemical Communications"),
	CHEMICAL_BIOLOGY_VIRTUAL_JOURNAL("cb", "Chemical Biology Virtual Journal"),
	CRYSTENGCOMM("ce", "CrystEngComm"),
	DALTON_TRANSACTIONS("dt", "Dalton Transactions"),
	GREEN_CHEMISTRY("gc", "Green Chemistry"),
	JOURNAL_OF_MATERIALS_CHEMISTRY("jm", "Journal of Materials Chemistry"),
	JOURNAL_OF_ENVIRONMENTAL_MONITORING("em", "Journal of Environmental Monitoring"),
	NATURAL_PRODUCT_REPORTS("np", "Natural Product Reports"),
	NEW_JOURNAL_OF_CHEMISTRY("nj", "New Journal of Chemistry"),
	ORGANIC_AND_BIOMOLECULAR_CHEMISTRY("ob", "Organic and Biomolecular Chemistry"),
	PCCP("cp", "PCCP");

	private final String abbreviation;
	private final String fullTitle;

	RscJournal(String abbreviation, String fullTitle) {
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