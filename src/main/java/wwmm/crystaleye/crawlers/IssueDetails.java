package wwmm.crystaleye.crawlers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IssueDetails {

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
