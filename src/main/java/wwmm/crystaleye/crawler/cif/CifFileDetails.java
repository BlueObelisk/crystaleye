package wwmm.crystaleye.crawler.cif;

import org.apache.commons.httpclient.URI;

import wwmm.crystaleye.crawler.SupplementaryFileDetails;

/**
 * A class used solely to indicate that a particular 
 * supplementary file is a CIF.
 * 
 * @author Nick Day
 * @version 0.1
 */
public class CifFileDetails extends SupplementaryFileDetails {

	public CifFileDetails(URI uri, String linkText, String contentType) {
		super(uri, linkText, contentType);
	}	
	
}
