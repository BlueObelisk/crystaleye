package wwmm.crystaleye.crawlers;

public class ArticleReference {

	private String journal;
	private String year;
	private String volume;
	private String number;
	private String pages;
	private boolean hasBeenPublished;
	
	public ArticleReference() {
		;
	}

	public String getJournal() {
		return journal;
	}

	public void setJournal(String journal) {
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

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public String getPages() {
		return pages;
	}

	public void setPages(String pages) {
		this.pages = pages;
	}

	public boolean hasBeenPublished() {
		return hasBeenPublished;
	}

	public void setHasBeenPublished(boolean hasBeenPublished) {
		this.hasBeenPublished = hasBeenPublished;
	}
	
}
