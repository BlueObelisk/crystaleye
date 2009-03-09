package wwmm.crystaleye.crawler.cif;

import wwmm.crystaleye.crawler.ChemSocJapanIssueCrawler;
import wwmm.crystaleye.crawler.SupplementaryFileDetails;

public class ChemSocJapanCifIssueCrawler extends CifIssueCrawler {
	
	public ChemSocJapanCifIssueCrawler(ChemSocJapanIssueCrawler crawler) {
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
		String linkText = sfd.getLinkText();
		if (linkText.contains("CIF")) {
			return true;
		} else {
			return false;
		}
	}


}
