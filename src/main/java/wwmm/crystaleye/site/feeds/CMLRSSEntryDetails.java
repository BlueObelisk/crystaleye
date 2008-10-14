package wwmm.crystaleye.site.feeds;


public class CMLRSSEntryDetails {
	
	String feedPath;
	String title;
	String author;
	String id;
	String summary;
	String htmlLink;
	String cmlLink;
	String cmlFilePath;
	String link;
	
	private CMLRSSEntryDetails() {
		;
	}

	public CMLRSSEntryDetails(String title, String author, String id, String summary, String htmlLink, String cmlLink, String cmlFilePath, String link) {
		super();
		this.title = title;
		this.author = author;
		this.id = id;
		this.summary = summary;
		this.htmlLink = htmlLink;
		this.cmlLink = cmlLink;
		this.cmlFilePath = cmlFilePath;
		this.link = link;
	}

	public String getAuthor() {
		return author;
	}

	public String getCmlFilePath() {
		return cmlFilePath;
	}

	public String getCmlLink() {
		return cmlLink;
	}

	public String getFeedPath() {
		return feedPath;
	}

	public String getHtmlLink() {
		return htmlLink;
	}

	public String getId() {
		return id;
	}

	public String getSummary() {
		return summary;
	}

	public String getTitle() {
		return title;
	}
	public String getLink() {
		return link;
	}
}
