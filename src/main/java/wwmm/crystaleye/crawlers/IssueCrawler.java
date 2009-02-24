package wwmm.crystaleye.crawlers;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nu.xom.Document;

/**
 * <p>
 * The <code>IssueCrawler</code> class provides an outline for the 
 * methods that a crawler of a journal issue should implement.
 * </p> 
 * 
 * @author Nick Day
 * @version 1.1
 *
 */
public abstract class IssueCrawler extends Crawler {

	// FIXME - can we provide a superclass for the journal enums
	// (e.g. ActaJournal) here so that implementers are forced to
	// include one?

	/**
	 * <p>
	 * Gets information to identify the last published issue of a
	 * journal (which is defined in the subclass).
	 * </p>
	 * 
	 * @return the year and issue identifier.
	 * 
	 */
	abstract public IssueDetails getCurrentIssueDetails();

	/**
	 * <p>
	 * Gets the HTML of the table of contents of the last 
	 * published issue of the subclass journal.
	 * </p>
	 * 
	 * @return HTML of the issue table of contents as an
	 * XML Document.
	 * 
	 */
	abstract public Document getCurrentIssueHtml();

	/**
	 * <p>
	 * Gets the DOIs of all of the articles from the last 
	 * published issue of the subclass journal.
	 * </p> 
	 * 
	 * @return a list of the DOIs of the articles.
	 * 
	 */
	abstract public List<DOI> getCurrentIssueDOIs();

	/**
	 * <p>
	 * Gets the DOIs of all articles in the issue defined
	 * by the subclass journal and the provided	year and 
	 * issue identifier (wrapped in the <code>issueDetails</code>
	 * parameter.
	 * </p>
	 * 
	 * @param issueDetails - contains the year and issue
	 * identifier of the issue to be crawled.
	 * 
	 * @return a list of the DOIs of the articles for the issue.
	 * 
	 */
	abstract public List<DOI> getDOIs(IssueDetails issueDetails);

	/**
	 * <p>
	 * Gets information describing all articles in the issue defined
	 * by the subclass journal and the provided	year and 
	 * issue identifier (wrapped in the <code>issueDetails</code>
	 * parameter.
	 * </p>
	 * 
	 * @param issueDetails - contains the year and issue
	 * identifier of the issue to be crawled.
	 * 
	 * @return a list where each item contains the details for 
	 * a particular article from the issue.
	 * 
	 */
	abstract public List<ArticleDetails> getDetailsForArticles(IssueDetails details);

	/**
	 * <p>
	 * Simple container class to describe an issue by
	 * the year and its identifier (usually just the
	 * issue number).  
	 * </p>
	 * 
	 * @author Nick Day
	 * @version 1.1
	 */
	protected class IssueDetails {

		String year;
		String issueId;

		public IssueDetails(String year, String issueId) {
			this.year = year;
			validateYear(year);
			this.issueId = issueId;
		}

		public String getYear() {
			return year;
		}

		public String getIssueId() {
			return issueId;
		}

		private void validateYear(String year) {
			Pattern p = Pattern.compile("^\\d{4}$");
			Matcher m = p.matcher(year);
			if (!m.find()) {
				throw new IllegalStateException("Provided year string is invalid ("+year+"), should be of the form YYYY.");
			}
		}

	}

}
