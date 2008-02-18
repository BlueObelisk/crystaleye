package uk.ac.cam.ch.crystaleye.properties;

import static uk.ac.cam.ch.crystaleye.CrystalEyeConstants.PUBLISHER_ABBREVIATIONS;

import java.io.File;

import uk.ac.cam.ch.crystaleye.CrystalEyeRuntimeException;

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
			throw new CrystalEyeRuntimeException("Could not find entry for "+publisherAbbreviations+" in properties file.");
		}
	}

	public String[] getPublisherAbbreviations() {
		return publisherAbbreviations;
	}
}
