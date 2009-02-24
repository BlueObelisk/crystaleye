package wwmm.crystaleye.crawlers;

/**
 * <p>
 * The <code>ArticleReference</code> class provides 
 * data items that would usually be used in a 
 * bibliographic reference to a published journal
 * article.  Is a simple container class where the only
 * methods are to allow getting and setting of instance
 * variables.
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

	/**
	 * Get the title of the journal the article was 
	 * published in.
	 * 
	 * @return title of the journal the article was 
	 * published in.
	 */
	public String getJournalTitle() {
		return journalTitle;
	}

	/**
	 * Set the title of the journal the article was 
	 * published in.
	 * 
	 * @param journalTitle - the title of the journal the
	 * article was published in.
	 */
	public void setJournalTitle(String journalTitle) {
		this.journalTitle = journalTitle;
	}

	/**
	 * Get the year the article was published.
	 * 
	 * @return year the article was published.
	 */
	public String getYear() {
		return year;
	}

	/**
	 * Set the year the article was published.
	 * 
	 * @param year the article was published.
	 */
	public void setYear(String year) {
		this.year = year;
	}

	/**
	 * Get the volume of the journal the article was
	 * published in.
	 * 
	 * @return String of the volume of the journal the 
	 * article was published in.
	 */
	public String getVolume() {
		return volume;
	}

	/**
	 * Set the volume of the journal the article was 
	 * published in.
	 * 
	 * @param volume of the journal the article was 
	 * published in.
	 */
	public void setVolume(String volume) {
		this.volume = volume;
	}

	/**
	 * Get the identifier of the issue the article
	 * was published in.
	 * 
	 * @return the identifier of the issue the article
	 * was published in.
	 */
	public String getNumber() {
		return number;
	}

	/**
	 * Set the identifier of the issue the article
	 * was published in.
	 * 
	 * @param number - the identifier of the issue the 
	 * article was published in.
	 */
	public void setNumber(String number) {
		this.number = number;
	}

	/**
	 * Get the pages of the issue that the article is
	 * found on.
	 * 
	 * @return the pages of the issue that the article is
	 * found on.
	 */
	public String getPages() {
		return pages;
	}

	/**
	 * Set the pages of the issue that the article is
	 * found on.
	 * 
	 * @param pages of the issue that the article is 
	 * found on.
	 */
	public void setPages(String pages) {
		this.pages = pages;
	}
	
}
