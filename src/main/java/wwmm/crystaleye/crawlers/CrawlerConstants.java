package wwmm.crystaleye.crawlers;

import nu.xom.XPathContext;

public interface CrawlerConstants {

	public static final String DOI_SITE_URL = "http://dx.doi.org";
	public static final String ACTA_HOMEPAGE_URL = "http://journals.iucr.org";
	public static final String ACS_HOMEPAGE_URL = "http://pubs.acs.org";
	public static final String CHEMSOCJAPAN_HOMEPAGE_URL = "http://www.jstage.jst.go.jp";
	//public static final String ELSEVIER_HOMEPAGE_URL = "";
	public static final String RSC_HOMEPAGE_URL = "http://pubs.rsc.org";
	
	public static final String DC_NS = "http://purl.org/dc/elements/1.1/";
	public static final String RSS_1_NS = "http://purl.org/rss/1.0/";
	
	public static XPathContext X_DC = new XPathContext("dc", DC_NS);
	public static XPathContext X_RSS1 = new XPathContext("rss1", RSS_1_NS);
	
}
