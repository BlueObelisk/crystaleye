package wwmm.crystaleye.crawler.cif;

import wwmm.crystaleye.crawler.AcsIssueCrawler;
import wwmm.crystaleye.crawler.SupplementaryFileDetails;

public class AcsCifIssueCrawler extends CifIssueCrawler {

	public AcsCifIssueCrawler(AcsIssueCrawler crawler) {
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
		String filename = sfd.getFilename();
		if (filename.endsWith(".cif")) {
			return true;
		} else {
			return false;
		}
	}
	
}
