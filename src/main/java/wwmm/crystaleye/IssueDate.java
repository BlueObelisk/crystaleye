package wwmm.crystaleye;

/**
 * Encapsulates the year and date of a journal as a workaround for 
 * the AbstractManager.getUnprocessedDates() method
 * @author ned24
 *
 */

public class IssueDate {

	private String year;
	private String issue;
	
	private IssueDate() {
		;
	}

	/**
	 * 
	 * @param year
	 * @param issue
	 */
	public IssueDate(String year, String issue) {
		this.year = year;
		this.issue = issue;
	}

	/**
	 * Returns the year of this IssueDate
	 * @return the year
	 */
	public String getYear() {
		return year;
	}

	/**
	 * Returns the year of this IssueDate
	 * @return the issue
	 */
	public String getIssue() {
		return issue;
	}
}
