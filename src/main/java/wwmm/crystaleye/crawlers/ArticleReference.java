package wwmm.crystaleye.crawlers;

public class ArticleReference {

	private String journal;
	private String year;
	private String volume;
	private String number;
	private String pages;
	private boolean isAsapArticle;
	
	public ArticleReference(String journal, String year,
			String volume, String number, String pages, boolean isAsapArticle) {
		this.journal = journal;
		this.year = year;
		this.volume = volume;
		this.number = number;
		this.pages = pages;
		this.isAsapArticle = isAsapArticle;
	}
	public String getJournal() {
		return journal;
	}
	public void setJournal(String journal) {
		this.journal = journal;
	}
	public boolean isAsapArticle() {
		return isAsapArticle;
	}
	public void setAsapArticle(boolean isAsapArticle) {
		this.isAsapArticle = isAsapArticle;
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
