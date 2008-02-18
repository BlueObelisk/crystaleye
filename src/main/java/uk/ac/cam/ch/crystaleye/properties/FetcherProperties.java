package uk.ac.cam.ch.crystaleye.properties;

import static uk.ac.cam.ch.crystaleye.CrystalEyeConstants.SLEEP_MAX;

import java.io.File;

import uk.ac.cam.ch.crystaleye.CrystalEyeRuntimeException;

public class FetcherProperties extends StandardProperties {
	
	// sleeping (zzz)
	private String sleepMax;
	
	public FetcherProperties(File propFile) {
		super(propFile);
	}
	
	protected void setProperties() {
		super.setProperties();
		// maximum time to sleep between webpage fetches
		this.sleepMax=properties.getProperty(SLEEP_MAX);
		if (sleepMax == null) {
			throw new CrystalEyeRuntimeException("Could not find entry for "+SLEEP_MAX+" in properties file.");
		}
	}

	public String getSleepMax() {
		return sleepMax;
	}
}
