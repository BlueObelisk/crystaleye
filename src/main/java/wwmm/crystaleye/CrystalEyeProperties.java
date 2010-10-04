package wwmm.crystaleye;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class CrystalEyeProperties {

	private Properties properties;
	
	private static final String CIF_DIR_NAME = "cif.dir";
	private static final String WEB_DIR_NAME = "web.dir";
	private static final String WEB_DIR_URL_NAME = "web.dir.url";

	private CrystalEyeProperties() {
		;
	}

	public CrystalEyeProperties(File propFile) {
		if (propFile.exists()) {
			properties = new Properties();
			try {
				properties.load(new FileInputStream(propFile));
			} catch (IOException e) {
				throw new RuntimeException("Could not read properties file: "+propFile.getAbsolutePath(), e);
			}
		} else {
			throw new RuntimeException("Could not find file "+propFile.getAbsolutePath());
		}
	}
	
	public String getCifDir() {
		return properties.getProperty(CIF_DIR_NAME);
	}
	
	public String getWebDir() {
		return properties.getProperty(WEB_DIR_NAME);
	}
	
	public String getWebDirUrl() {
		return properties.getProperty(WEB_DIR_URL_NAME);
	}
	
	public String getDocDir() {
		return getWebDir()+"/docs";
	}

	public String getProcessLogPath() {
		return getDocDir()+"/process-log.xml";
	}

	public String getFeedDir() {
		return getWebDir()+"/feed";
	}
	
	public String getFeedDirUrl() {
		return getWebDirUrl()+"/feed";
	}

	public String getSummaryDir() {
		return getWebDir()+"/summary";
	}

	public String getWebSummaryWriteDir() {
		return getWebDirUrl()+"/summary";
	}
	
	public String getDoiDir() {
		return getWebDir()+"/doi";
	}
	
	public String getDoiListPath() {
		return getDoiDir()+"/doilist.txt";
	}
	
	public String getDoiIndexPath() {
		return getDoiDir()+"/doi-index.txt";
	}
	
	public String getBondLengthsDir() {
		return getWebDir()+"/bond-lengths";
	}
	
	public String getCellParamsFilePath() {
		return getDocDir()+"/cell-params.txt";
	}
	
	public String getSmilesDir() {
		return getWebDir()+"/smiles";
	}
	
	public String getSmilesListPath() {
		return getSmilesDir()+"/smiles.smi";
	}
	
	public String getSmilesIndexPath() {
		return getSmilesDir()+"/smiles.fs";
	}
	
}
