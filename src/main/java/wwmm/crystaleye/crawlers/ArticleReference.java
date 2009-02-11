package wwmm.crystaleye.crawlers;

public class ArticleReference {

	private String journal;
	private String year;
	private String volume;
	private String number;
	private String pages;
	
	public ArticleReference(String journal, String year,
			String volume, String number, String pages) {
		this.journal = journal;
		this.year = year;
		this.volume = volume;
		this.number = number;
		this.pages = pages;
	}
	public String getNumber() {
		return number;
	}
	public void setNumber(String number) {
		this.number = number;
	}
	public String getJournalAbbreviation() {
		return journal;
	}
	public void setJournalAbbreviation(String journal) {
		this.journal = journal;
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
