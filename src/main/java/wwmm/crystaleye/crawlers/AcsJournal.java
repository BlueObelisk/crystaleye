package wwmm.crystaleye.crawlers;

public enum AcsJournal {
	ACCOUNTS_OF_CHEMICAL_RESEARCH("achre4", "Accounts of Chemical Research", 1967),
	ANALYTICAL_CHEMISTRY("ancham", "Analytical Chemistry", 1928),
	BIOCONJUGATE_CHEMISTRY("bcches", "Bioconjugate Chemistry", 1989),
	BIOCHEMISTRY("bichaw", "Biochemistry", 1961),
	BIOMACROMOLECULES("bomaf6", "Biomacromolecules", 1999),
	CHEMICAL_REVIEWS("chreay", "Chemical Reviews", 1900),
	CHEMISTRY_OF_MATERIALS("cmatex", "Chemistry of Materials", 1988),
	CRYSTAL_GROWTH_AND_DESIGN("cgdefu", "Crystal Growth and Design", 2000),
	ENERGY_AND_FUELS("enfuem", "Energy & Fuels", 1986),
	INDUSTRIAL_AND_ENGINEERING_CHEMISTRY_RESEARCH("iecred", "Industrial & Engineering Chemistry Research", 1961),
	INORGANIC_CHEMISTRY("inocaj", "Inorganic Chemistry", 1961),
	JOURNAL_OF_AGRICULTURAL_AND_FOOD_CHEMISTRY("jafcau", "Journal of Agricultural and Food Chemistry", 1952),
	JOURNAL_OF_CHEMICAL_AND_ENGINEERING_DATA("jceaax", "Journal of Chemical & Engineering Data", 1955),
	JOURNAL_OF_THE_AMERICAN_CHEMICAL_SOCIETY("jacsat", "Journal of the American Chemical Society", 1878),
	JOURNAL_OF_COMBINATORIAL_CHEMISTRY("jcchff", "Journal of Combinatorial Chemistry", 1998),
	JOURNAL_OF_CHEMICAL_INFORMATION_AND_MODELLING("jcisd8", "Journal of Chemical Information and Modelling", 1960),
	JOURNAL_OF_MEDICINAL_CHEMISTRY("jmcmar", "Journal of Medicinal Chemistry", 1957),
	JOURNAL_OF_NATURAL_PRODUCTS("jnprdf", "Journal of Natural Products", 1937),
	THE_JOURNAL_OF_ORGANIC_CHEMISTRY("joceah", "The Journal of Organic Chemistry", 1935),
	LANGMUIR("langd5", "Langmuir", 1984),
	MACROMOLECULES("mamobx", "Macromolecules", 1967),
	MOLECULAR_PHARMACEUTICS("mpohbp", "Molecular Pharmaceutics", 2003),
	ORGANIC_LETTERS("orlef7", "Organic Letters", 1998),
	ORGANIC_PROCESS_RESEARCH_AND_DEVELOPMENT("oprdfk", "Organic Process and Research and Development", 1996),
	ORGANOMETALLICS("orgnd7", "Organometallics", 1981);

	private final String abbreviation;
	private final String fullTitle;
	private final int volumeOffset;

	AcsJournal(String abbreviation, String fullTitle, int volumeOffset) {
		this.abbreviation = abbreviation;
		this.fullTitle = fullTitle;
		this.volumeOffset = volumeOffset;
	}

	public String getFullTitle() {
		return this.fullTitle;
	}

	public String getAbbreviation() {
		return this.abbreviation;
	}

	public int getVolumeOffset() {
		return this.volumeOffset;
	}
}