package wwmm.crystaleye.properties;

import static wwmm.crystaleye.CrystalEyeConstants.DOWNLOAD_LOG_PATH;
import static wwmm.crystaleye.CrystalEyeConstants.WRITE_DIR;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public abstract class StandardProperties {

	Properties properties;

	// log paths
	private String downloadLogPath;

	// write directories
	private String writeDir;	

	private StandardProperties() {
		;
	}

	protected StandardProperties(File propFile) {
		if (propFile.exists()) {
			properties = new Properties();
			try {
				properties.load(new FileInputStream(propFile));
				setProperties();
			} catch (IOException e) {
				throw new RuntimeException("Could not read properties file: "+propFile.getAbsolutePath(), e);
			}
		} else {
			throw new RuntimeException("Could not find file "+propFile.getAbsolutePath());
		}
	}

	protected void setProperties() {	
		// path to the file which keeps track of the issues that have been downloaded
		this.downloadLogPath = properties.getProperty(DOWNLOAD_LOG_PATH);
		if (downloadLogPath == null) {
			throw new RuntimeException("Could not find entry for "+DOWNLOAD_LOG_PATH+" in properties file.");
		}

		// root of the directory to which all files are written
		this.writeDir = properties.getProperty(WRITE_DIR);
		if (writeDir == null) {
			throw new RuntimeException("Could not find entry for "+WRITE_DIR+" in properties file.");
		}
	}

	public String getDownloadLogPath() {
		return downloadLogPath;
	}

	public String getWriteDir() {
		return writeDir;
	}

	public String getPublisherTitle(String publisherAbbreviation) {
		String publisherTitle = properties.getProperty(publisherAbbreviation+".full.title");
		if (publisherTitle == null) {
			throw new RuntimeException("No entry for "+publisherAbbreviation+".full.title in properties file.");
		}
		return publisherTitle;
	}

	public String getJournalTitle(String publisherAbbreviation, String journalAbbreviation) {
		int count = 0;
		for (String s : getPublisherJournalAbbreviations(publisherAbbreviation)) {
			if (s.equals(journalAbbreviation)) {
				return getPublisherJournalTitles(publisherAbbreviation)[count];
			}
			count++;
		}
		return null;
	}

	public String[] getPublisherJournalTitles(String publisherAbbreviation) {
		String[] journals = properties.getProperty(publisherAbbreviation+".journal.full.titles").split(",");
		if (journals == null) {
			throw new RuntimeException("No entry for "+publisherAbbreviation+".journal.full.titles in properties file.");
		}
		return journals;
	}

	public String[] getPublisherJournalAbbreviations(String publisherAbbreviation) {
		String[] journalTitles = properties.getProperty(publisherAbbreviation+".journal.abbreviations").split(",");
		if (journalTitles == null) {
			throw new RuntimeException("No entry for "+publisherAbbreviation+".journal.abbreviations in properties file.");
		}
		return journalTitles;
	}
}
