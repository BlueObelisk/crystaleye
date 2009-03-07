package wwmm.crystaleye.crawler.cif;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.URIException;

import wwmm.crystaleye.crawler.ActaIssueCrawler;
import wwmm.crystaleye.crawler.SupplementaryFileDetails;

public class ActaCifIssueCrawler extends CifIssueCrawler {

	public ActaCifIssueCrawler(ActaIssueCrawler crawler) {
		super(crawler);
	}

	/**
	 * <p>
	 * Method of finding whether a supplementary file refers to a CIF.
	 * This is an Acta Crystallographica specific implementation.  Well, 
	 * I guess that should be obvious from the classname.
	 * </p>
	 * 
	 */
	@Override
	protected boolean isCifFile(SupplementaryFileDetails sfd) {
		Pattern pattern = Pattern.compile("http://scripts.iucr.org/cgi-bin/sendcif\\?.{6}sup\\d+");
		Matcher matcher = null;
		try {
			matcher = pattern.matcher(sfd.getURI().getURI());
		} catch (URIException e) {
			throw new RuntimeException("Error getting URI string.", e);
		}
		if (matcher.find()) {
			return true;
		}
		return false;
	}
	
}
