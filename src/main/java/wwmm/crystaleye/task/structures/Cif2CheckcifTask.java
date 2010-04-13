package wwmm.crystaleye.task.structures;

import java.io.File;

import wwmm.crystaleye.model.structures.CheckcifHtmlFileDAO;
import wwmm.crystaleye.model.structures.CifFileDAO;
import wwmm.crystaleye.task.Task;
import wwmm.crystaleye.tools.CheckCifTool;

public class Cif2CheckcifTask extends Task {

	public Cif2CheckcifTask(File storageRoot, int... keys) {
		super(storageRoot, keys);
	}

	@Override
	public void perform() {
		CifFileDAO childCifDao = new CifFileDAO(getDbRoot());
		File cifFile = childCifDao.getAndEnsureFileFromKeys(getKeys());
		CheckCifTool checkCifTool = new CheckCifTool();
		String checkCifStr = checkCifTool.getCheckcifString(cifFile);
		CheckcifHtmlFileDAO checkCifDao = new CheckcifHtmlFileDAO(getDbRoot());
		checkCifDao.insert(checkCifStr, getKeys());
	}
	
}
