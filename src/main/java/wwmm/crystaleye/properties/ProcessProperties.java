package wwmm.crystaleye.properties;

import static wwmm.crystaleye.CrystalEyeConstants.CIF_DICT;
import static wwmm.crystaleye.CrystalEyeConstants.SPACEGROUP_XML;
import static wwmm.crystaleye.CrystalEyeConstants.SPLITCIF_REGEX;

import java.io.File;

import wwmm.crystaleye.CrystalEyeRuntimeException;

public class ProcessProperties extends ManyPublisherProperties{
		
	// dictionaries
	private String cifDict;
	
	// miscellaneous
	private String splitCifRegex;
	private String spaceGroupXml;

	public ProcessProperties(File propFile) {
		super(propFile);
	}
	
	protected void setProperties() {
		super.setProperties();
		// path to cif dictionary
		this.cifDict=properties.getProperty(CIF_DICT);
		if (cifDict == null) {
			throw new CrystalEyeRuntimeException("Could not find entry for "+CIF_DICT+" in properties file.");
		}
		
		// regex to find CIFs that have been split by CrystalEye
		this.splitCifRegex = properties.getProperty(SPLITCIF_REGEX);
		if (splitCifRegex == null) {
			throw new CrystalEyeRuntimeException("Could not find entry for "+SPLITCIF_REGEX+" in properties file.");
		}
		
		this.spaceGroupXml = properties.getProperty(SPACEGROUP_XML);
		if (spaceGroupXml == null) {
			throw new CrystalEyeRuntimeException("Could not find entry for "+SPACEGROUP_XML+" in properties file.");
		}
	}

	public String getCifDict() {
		return cifDict;
	}

	public String getSplitCifRegex() {
		return splitCifRegex;
	}
	
	public String getSpaceGroupXml() {
		return spaceGroupXml;
	}
}
