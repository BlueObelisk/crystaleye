package wwmm.crystaleye.crawler;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.URIException;

/**
 * <p>
 * Wraps an an instance of a class that extends <code>IssueCrawler</code>
 * and only returns details for articles that have a CIF file provided as
 * supplementary information.
 * </p>
 * 
 * @author Nick Day
 * @version 1.1
 */
public class CifIssueCrawler {

	// an instance of a class that extends IssueCrawler
	IssueCrawler issueCrawler;
	
	public CifIssueCrawler(IssueCrawler issueCrawler) {
		this.issueCrawler = issueCrawler;
	}

	/**
	 * <p>
	 * For the current issue of the journal provided in 
	 * <code>issueCrawler</code>, this returns only details of those 
	 * articles which have a CIF file provided as supplementary data.
	 * </p>
	 * 
	 * @return a list containing only details of those articles which 
	 * have a CIF file provided as supplementary data.
	 */
	public List<ArticleDetails> getDetailsForCurrentArticles() {
		IssueDetails issueDetails = issueCrawler.getCurrentIssueDetails();
		return getDetailsForArticles(issueDetails);
	}
	
	/**
	 * <p>
	 * For the issue defined by the provided <code>issueDetails</code>, this
	 * returns only details of those articles which have a CIF file provided
	 * as supplementary data.
	 * </p>
	 * 
	 * @param issueDetails - contains the year and the issue of the
	 * issue to be crawled.
	 * 
	 * @return a list containing only details of those articles which 
	 * have a CIF file provided as supplementary data.
	 */
	public List<ArticleDetails> getDetailsForArticles(IssueDetails issueDetails) {
		List<ArticleDetails> adList = issueCrawler.getDetailsForArticles(issueDetails);
		List<ArticleDetails> cifAdList = new ArrayList<ArticleDetails>();
		for (ArticleDetails ad : adList) {
			if (hasSupplementaryCifFile(ad)) {
				cifAdList.add(ad);
			}
		}
		return cifAdList;
	}
	
	/**
	 * <p>
	 * Returns whether or not the article (described by the
	 * parameter <code>ArticleDetails</code>) has a CIF file
	 * provided as supplementary data.
	 * </p>
	 * 
	 * @param ad - the details of the article to be checked.
	 * 
	 * @return whether the article has a CIF file provided as 
	 * supplementary data.
	 */
	private boolean hasSupplementaryCifFile(ArticleDetails ad) {
		for (SupplementaryFileDetails sdf : ad.getSuppFiles()) {
			try {
				if (sdf.getUri().getURI().endsWith(".cif")) {
					return true;
				}
			} catch (URIException e) {
				throw new RuntimeException("Error getting URI string from: "+sdf.getUri());
			}
		}
		return false;
	}
	
}
