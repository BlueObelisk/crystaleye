package uk.ac.cam.ch.crystaleye;

public class IssueDate {

	private String year;
	private String issue;
	
	private IssueDate() {
		;
	}

	public IssueDate(String year, String issue) {
		this.year = year;
		this.issue = issue;
	}

	public String getYear() {
		return year;
	}

	public String getIssue() {
		return issue;
	}
}
