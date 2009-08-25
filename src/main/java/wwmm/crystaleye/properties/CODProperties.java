package wwmm.crystaleye.properties;

import static wwmm.crystaleye.CrystalEyeConstants.CELL_PARAMS_FILE;
import static wwmm.crystaleye.CrystalEyeConstants.COD_DOWNLOADED_FILES_LIST;

import java.io.File;

import wwmm.crystaleye.CrystalEyeRuntimeException;

public class CODProperties extends ProcessProperties {
	
	String alreadyGotCifFilePath;
	String cellParamsFilePath;

	public CODProperties(File propFile) {
		super(propFile);
	}
	
	protected void setProperties() {
		super.setProperties();
		// list of files already download from COD
		this.alreadyGotCifFilePath = properties.getProperty(COD_DOWNLOADED_FILES_LIST);
		if (alreadyGotCifFilePath == null) {
			throw new CrystalEyeRuntimeException("Could not find entry for "+COD_DOWNLOADED_FILES_LIST+" in properties file.");
		}
		
		// list of cell params already got from other sources - used to figure
		// out which of the COD cifs we don't have from elsewhere
		this.cellParamsFilePath = properties.getProperty(CELL_PARAMS_FILE);
		if (cellParamsFilePath == null) {
			throw new CrystalEyeRuntimeException("Could not find entry for "+CELL_PARAMS_FILE+" in properties file.");
		}
	}

	public String getAlreadyGotCifFilePath() {
		return alreadyGotCifFilePath;
	}
	
	public String getCellParamsFilePath() {
		return cellParamsFilePath;
	}
}
