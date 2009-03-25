package wwmm.crystaleye.task;

import java.io.File;

import wwmm.crystaleye.model.crystaleye.CifFileDAO;

/**
 * <p>
 * This class provides functionality for the 'splitting-up' of CIFs.
 * This means that any CIF containing data on more than one structure,
 * (i.e. having more than one data containing datablock) will be split
 * up into smaller CIFs containing information on only one structure 
 * each.  The unit of currency in CrystalEye is the structure, not the
 * CIF, so doing this is essential. 
 * </p>
 * 
 * <p>
 * For instance, if a CIF has four datablocks, one being the 'global'
 * block containing the CIF metadata and the other three being 'data'
 * blocks containing structural data then this CIF will be split into
 * three smaller CIFs, each containing the 'global' datablock and the 
 * 'data' datablock for one structure, e.g.
 * 
 * <pre>
 * -CIF-----------
 * data_global
 * data_structure1
 * data_structure2
 * data_structure3
 * ---------------
 * </pre>
 * 
 * will be converted into:
 * 
 * <pre>
 * -CIF-----------
 * data_global
 * data_structure1
 * ---------------
 * -CIF-----------
 * data_global
 * data_structure2
 * ---------------
 * -CIF-----------
 * data_global
 * data_structure3
 * ---------------
 * </pre>
 * </p>
 * 
 * @author Nick Day
 * @version 0.1
 *
 */
public class SplitupCifTask {
	
	File cifFile;
	
	public SplitupCifTask(File storageRoot, int primaryKey) {
		CifFileDAO cifFileDao = new CifFileDAO(storageRoot);
		cifFile = cifFileDao.getCifFileFromKey(primaryKey);
	}
	
	

}
