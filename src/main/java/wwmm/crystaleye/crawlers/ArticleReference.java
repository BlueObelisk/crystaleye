package wwmm.crystaleye.crawlers;

public class ArticleReference {

	private String journalAbbreviation;
	private String year;
	private String volume;
	private String pages;
	
	public ArticleReference(String journalAbbreviation, String year,
			String volume, String pages) {
		this.journalAbbreviation = journalAbbreviation;
		this.year = year;
		this.volume = volume;
		this.pages = pages;
	}
	public String getJournalAbbreviation() {
		return journalAbbreviation;
	}
	public void setJournalAbbreviation(String journalAbbreviation) {
		this.journalAbbreviation = journalAbbreviation;
	}
	public String getYear() {
		return year;
	}
	public void setYear(String year) {
		this.year = year;
	}
	public String getVolume() {
		return volume;
	}
	public void setVolume(String volume) {
		this.volume = volume;
	}
	public String getPages() {
		return pages;
	}
	public void setPages(String pages) {
		this.pages = pages;
	}
	
}
