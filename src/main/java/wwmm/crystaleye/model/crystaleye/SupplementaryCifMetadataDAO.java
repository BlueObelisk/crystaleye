package wwmm.crystaleye.model.crystaleye;

import java.io.File;

import wwmm.crystaleye.model.core.SecondaryFileDAO;

/**
 * <p>
 * Data-access class for accessing the metadata associated with a
 * CIF that was obtained from published article supplementary data.
 * </p>
 * 
 * @author Nick Day
 * @version 0.1
 */
public class SupplementaryCifMetadataDAO extends SecondaryFileDAO {
	
	public static final String SUPPLEMENTARY_CIF_METADATA_MIME = ".bibliontology.xml";

	public SupplementaryCifMetadataDAO(File storageRoot) {
		super(storageRoot, SUPPLEMENTARY_CIF_METADATA_MIME);
	}

}
