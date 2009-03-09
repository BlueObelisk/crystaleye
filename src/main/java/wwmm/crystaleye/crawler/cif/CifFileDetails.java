package wwmm.crystaleye.crawler.cif;

import org.apache.commons.httpclient.URI;

import wwmm.crystaleye.crawler.SupplementaryFileDetails;

/**
 * <p>
 * A class intended only to extend SupplementaryFileDetails so 
 * it can be used to indicate that a particular instance refers
 * to a CIF.
 * </p>
 * 
 * @author Nick Day
 * @version 0.1
 */
public class CifFileDetails extends SupplementaryFileDetails {

	public CifFileDetails(URI uri, String filename, String linkText, String contentType) {
		super(uri, filename, linkText, contentType);
	}	
	
}
