package wwmm.crystaleye.crawlers;

/**
 * <p>
 * The <code>ArticleReference</code> class provides 
 * data items that would usually be used in a 
 * bibliographic reference to a published journal
 * article.
 * </p>
 * 
 * @author Nick Day
 * @version 1.1
 * 
 */
public class ArticleReference {

	private String journalTitle;
	private String year;
	private String volume;
	private String number;
	private String pages;
	
	public ArticleReference() {
		;
	}

	public String getJournalTitle() {
		return journalTitle;
	}

	public void setJournalTitle(String journalTitle) {
		this.journalTitle = journalTitle;
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
	
}
