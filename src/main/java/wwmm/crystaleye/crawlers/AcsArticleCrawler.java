package wwmm.crystaleye.crawlers;

import static wwmm.crystaleye.CrystalEyeConstants.X_XHTML;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nu.xom.Document;
import nu.xom.Nodes;

import org.apache.commons.httpclient.URI;

public class AcsArticleCrawler extends Crawler {

	private URI doi;
	
	public AcsArticleCrawler(URI doi) {
		this.doi = doi;
	}
	
	public ArticleDetails getDetails() {
		Document abstractPageDoc = httpClient.getWebpageDocument(doi);
		
		String title = getTitle(abstractPageDoc);
		ArticleReference ref = getReference(abstractPageDoc);
		String authors = getAuthors(abstractPageDoc);
		List<SupplementaryFile> suppFiles = getSupplementaryFiles(abstractPageDoc);

		ArticleDetails ad = new ArticleDetails();
		ad.setDoi(doi);
		ad.setTitle(title);
		ad.setReference(ref);
		ad.setAuthors(authors);
		ad.setSuppFiles(suppFiles);
		return ad;
	}
	
	private List<SupplementaryFile> getSupplementaryFiles(Document abstractPageDoc) {
		
		return new ArrayList<SupplementaryFile>();
	}

	private String getAuthors(Document abstractPageDoc) {
		Nodes authorNds = abstractPageDoc.query(".//x:p[./x:font[@size='+2']]/following-sibling::x:p[1]", X_XHTML);
		if (authorNds.size() != 1) {
			throw new RuntimeException("Problem finding authors at: "+doi);
		}
		String authors = authorNds.get(0).getValue();
		authors = authors.replaceAll("\\*", "");
		authors = authors.replaceAll("\\s+", " ");
		authors = authors.trim();
		return authors;
	}

	private ArticleReference getReference(Document abstractPageDoc) {
		Nodes refNds = abstractPageDoc.query(".//x:div[@id='articleNav']/following-sibling::x:p[1]", X_XHTML);
		if (refNds.size() != 1) {
			throw new RuntimeException("Problem finding bibliographic text at: "+doi);
		}
		String bibline = refNds.get(0).getValue().trim();
		Pattern pattern = Pattern.compile("\\s+([^\\,]+),\\s+(\\d+\\s+\\(\\d+\\)),\\s+(\\d+).(\\d+),\\s+(\\d+).*");
		Matcher matcher = pattern.matcher(bibline);
		if (matcher.find() || matcher.groupCount() != 5) {
			throw new RuntimeException("Problem finding bibliographic text at: "+doi);
		}
		// TODO = fill in the details object
		
		
		return new ArticleReference(null, null, null, null);
	}

	private String getTitle(Document abstractPageDoc) {
		Nodes titleNds = abstractPageDoc.query(".//x:p/x:font[@size='+2']/x:b", X_XHTML);
		if (titleNds.size() != 1) {
			throw new RuntimeException("Problem finding title at: "+doi);
		}
		String title = titleNds.get(0).toXML();
		title = title.replaceAll("<b>", "");
		title = title.replaceAll("</b>", "");
		title = title.trim();
		return title;
	}
	
}
