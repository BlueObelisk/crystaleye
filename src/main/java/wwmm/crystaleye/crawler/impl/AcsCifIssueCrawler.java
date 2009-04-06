package wwmm.crystaleye.crawler.impl;

import wwmm.crystaleye.crawler.core.AcsIssueCrawler;
import wwmm.crystaleye.crawler.core.SupplementaryFileDetails;

/**
 * <p>
 * Provides a method of crawling an issue of a journal published
 * by the American Chemical Society, and only returning the details for
 * those articles that have a CIF as supplementary data.
 * </p>
 * 
 * @author Nick Day
 * @version 0.1;
 */
public class AcsCifIssueCrawler extends CifIssueCrawler {

	public AcsCifIssueCrawler(AcsIssueCrawler crawler) {
		super(crawler);
	}
	
	/**
	 * <p>
	 * An American Chemical Society specific method of determining 
	 * whether a supplementary file refers to a CIF.
	 * </p>
	 * 
	 * @return true if the SupplementaryFileDetails described a 
	 * CIF file, false if not.
	 */
	@Override
	protected boolean isCifFile(SupplementaryFileDetails sfd) {
		String filename = sfd.getFileId();
		if (filename.endsWith(".cif")) {
			return true;
		} else {
			return false;
		}
	}
	
}
