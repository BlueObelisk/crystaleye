package wwmm.crystaleye.crawlers;

import java.util.List;

import nu.xom.Document;

public abstract class IssueCrawler extends Crawler {

	abstract public IssueDetails getCurrentIssueDetails();
	
	abstract public Document getCurrentIssueDocument();
	
	abstract public List<DOI> getCurrentIssueDOIs();
	
	abstract public List<DOI> getDOIs(IssueDetails issueDetails);
	
	abstract public List<ArticleDetails> getDetailsForArticles(IssueDetails details);
	
}
