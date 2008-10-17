package wwmm.crystaleye.crawlers;

public class IssueDetails {

	String year;
	String issueId;
	
	public IssueDetails(String year, String issueId) {
		this.year = year;
		this.issueId = issueId;
	}
	
	public void setYear(String year) {
		this.year = year;
	}
	
	public void setIssueId(String issueId) {
		this.issueId = issueId;
	}
	
	public String getYear() {
		return year;
	}
	
	public String getIssueId() {
		return issueId;
	}
	
}
