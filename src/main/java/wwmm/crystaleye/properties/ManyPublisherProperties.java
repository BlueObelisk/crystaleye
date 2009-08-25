package wwmm.crystaleye.properties;

import static wwmm.crystaleye.CrystalEyeConstants.PUBLISHER_ABBREVIATIONS;

import java.io.File;

public class ManyPublisherProperties extends StandardProperties {
	
	// publisher information
	private String[] publisherAbbreviations;
	
	public ManyPublisherProperties(File propFile) {
		super(propFile);
	}
	
	protected void setProperties() {
		super.setProperties();
		
		// abbreviations of the names of the publishers which we are fetching cifs from
		this.publisherAbbreviations = properties.getProperty(PUBLISHER_ABBREVIATIONS).split(",");
		if (publisherAbbreviations == null) {
			throw new RuntimeException("Could not find entry for "+publisherAbbreviations+" in properties file.");
		}
	}

	public String[] getPublisherAbbreviations() {
		return publisherAbbreviations;
	}
}
