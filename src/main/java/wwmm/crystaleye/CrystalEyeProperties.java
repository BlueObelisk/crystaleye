package wwmm.crystaleye;

import static wwmm.crystaleye.CrystalEyeConstants.ATOM_PUB_FEED_MAX_ENTRIES;
import static wwmm.crystaleye.CrystalEyeConstants.ATOM_PUB_ROOT_DIR;
import static wwmm.crystaleye.CrystalEyeConstants.ATOM_PUB_ROOT_URL;
import static wwmm.crystaleye.CrystalEyeConstants.BOND_LENGTHS_DIR;
import static wwmm.crystaleye.CrystalEyeConstants.CELL_PARAMS_FILE;
import static wwmm.crystaleye.CrystalEyeConstants.CIF_DICT;
import static wwmm.crystaleye.CrystalEyeConstants.DOI_INDEX_PATH;
import static wwmm.crystaleye.CrystalEyeConstants.DOI_LIST_PATH;
import static wwmm.crystaleye.CrystalEyeConstants.DOWNLOAD_LOG_PATH;
import static wwmm.crystaleye.CrystalEyeConstants.PUBLISHER_ABBREVIATIONS;
import static wwmm.crystaleye.CrystalEyeConstants.RSS_ROOT_FEEDS_DIR;
import static wwmm.crystaleye.CrystalEyeConstants.RSS_WRITE_DIR;
import static wwmm.crystaleye.CrystalEyeConstants.SLEEP_MAX;
import static wwmm.crystaleye.CrystalEyeConstants.SMILES_INDEX_PATH;
import static wwmm.crystaleye.CrystalEyeConstants.SMILES_LIST_PATH;
import static wwmm.crystaleye.CrystalEyeConstants.SPACEGROUP_XML;
import static wwmm.crystaleye.CrystalEyeConstants.SPLITCIF_REGEX;
import static wwmm.crystaleye.CrystalEyeConstants.SUMMARY_WRITE_DIR;
import static wwmm.crystaleye.CrystalEyeConstants.UUID_TO_URL_FILE_PATH;
import static wwmm.crystaleye.CrystalEyeConstants.WEB_SUMMARY_WRITE_DIR;
import static wwmm.crystaleye.CrystalEyeConstants.WRITE_DIR;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class CrystalEyeProperties {

	Properties properties;

	// log paths
	private String downloadLogPath;

	// write directories
	private String writeDir;
	private String sleepMax;
	private String[] publisherAbbreviations;
	private String cifDict;

	// miscellaneous
	private String splitCifRegex;
	private String spaceGroupXml;

	// rss
	private String rssWriteDir;
	private String rootWebFeedsDir;

	// website
	private String summaryWriteDir;
	private String webSummaryWriteDir;

	// doi list
	private String doiListPath;
	private String doiIndexPath;

	// bond lengths
	private String bondLengthsDir;

	// cell parameters
	private String cellParamsFilePath;

	// smiles list
	private String smilesListPath;
	private String smilesIndexPath;

	// atom pub
	private String atomPubRootDir;
	private String atomPubRootUrl;
	private String atomPubFeedMaxEntries;
	private String uuidToUrlFilePath;

	private CrystalEyeProperties() {
		;
	}

	public CrystalEyeProperties(File propFile) {
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

		this.sleepMax=properties.getProperty(SLEEP_MAX);
		if (sleepMax == null) {
			throw new RuntimeException("Could not find entry for "+SLEEP_MAX+" in properties file.");
		}

		this.publisherAbbreviations = properties.getProperty(PUBLISHER_ABBREVIATIONS).split(",");
		if (publisherAbbreviations == null) {
			throw new RuntimeException("Could not find entry for "+publisherAbbreviations+" in properties file.");
		}

		this.cifDict=properties.getProperty(CIF_DICT);
		if (cifDict == null) {
			throw new RuntimeException("Could not find entry for "+CIF_DICT+" in properties file.");
		}

		// regex to find CIFs that have been split by CrystalEye
		this.splitCifRegex = properties.getProperty(SPLITCIF_REGEX);
		if (splitCifRegex == null) {
			throw new RuntimeException("Could not find entry for "+SPLITCIF_REGEX+" in properties file.");
		}

		this.spaceGroupXml = properties.getProperty(SPACEGROUP_XML);
		if (spaceGroupXml == null) {
			throw new RuntimeException("Could not find entry for "+SPACEGROUP_XML+" in properties file.");
		}

		// root folder (on the server) for the crystal summary webpages
		this.summaryWriteDir = properties.getProperty(SUMMARY_WRITE_DIR);
		if (summaryWriteDir == null) {
			throw new RuntimeException("Could not find entry for "+SUMMARY_WRITE_DIR+" in properties file.");
		}

		// root folder (as seen by internet browsers) for the crystal summary webpages
		this.webSummaryWriteDir = properties.getProperty(WEB_SUMMARY_WRITE_DIR);
		if (webSummaryWriteDir == null) {
			throw new RuntimeException("Could not find entry for "+WEB_SUMMARY_WRITE_DIR+" in properties file.");
		}

		// root folder (on the server) for the crystal rss feeds
		this.rssWriteDir = properties.getProperty(RSS_WRITE_DIR);
		if (rssWriteDir == null) {
			throw new RuntimeException("Could not find entry for "+RSS_WRITE_DIR+" in properties file.");
		}

		// root folder (as seen by internet browsers) for the crystal rss feeds
		this.rootWebFeedsDir=properties.getProperty(RSS_ROOT_FEEDS_DIR);
		if (rootWebFeedsDir == null) {
			throw new RuntimeException("Could not find entry for "+RSS_ROOT_FEEDS_DIR+" in properties file.");
		}
		
		// file containing list of DOIs against the URLs for the crystal summary page
		this.doiListPath = properties.getProperty(DOI_LIST_PATH);
		if (doiListPath == null) {
			throw new RuntimeException("Could not find entry for "+DOI_LIST_PATH+" in properties file.");
		}
		
		this.doiIndexPath = properties.getProperty(DOI_INDEX_PATH);
		if (doiIndexPath == null) {
			throw new RuntimeException("Could not find entry for "+DOI_INDEX_PATH+" in properties file.");
		}
		
		this.bondLengthsDir = properties.getProperty(BOND_LENGTHS_DIR);
		if (bondLengthsDir == null) {
			throw new RuntimeException("Could not find entry for "+BOND_LENGTHS_DIR+" in properties file.");
		}
			
		// list of cell params already got from other sources - used to figure
		// out which of the COD cifs we don't have from elsewhere
		this.cellParamsFilePath = properties.getProperty(CELL_PARAMS_FILE);
		if (cellParamsFilePath == null) {
			throw new RuntimeException("Could not find entry for "+CELL_PARAMS_FILE+" in properties file.");
		}
		
		this.smilesListPath = properties.getProperty(SMILES_LIST_PATH);
		if (smilesListPath == null) {
			throw new RuntimeException("Could not find entry for "+SMILES_LIST_PATH+" in properties file.");
		}
		
		this.smilesIndexPath = properties.getProperty(SMILES_INDEX_PATH);
		if (smilesIndexPath == null) {
			throw new RuntimeException("Could not find entry for "+SMILES_INDEX_PATH+" in properties file.");
		}
		
		this.atomPubRootDir = properties.getProperty(ATOM_PUB_ROOT_DIR);
		if (atomPubRootDir == null) {
			throw new RuntimeException("Could not find entry for "+ATOM_PUB_ROOT_DIR+" in properties file.");
		}
		
		this.atomPubRootUrl = properties.getProperty(ATOM_PUB_ROOT_URL);
		if (atomPubRootUrl == null) {
			throw new RuntimeException("Could not find entry for "+ATOM_PUB_ROOT_URL+" in properties file.");
		}
		
		this.atomPubFeedMaxEntries = properties.getProperty(ATOM_PUB_FEED_MAX_ENTRIES);
		if (atomPubFeedMaxEntries == null) {
			throw new RuntimeException("Could not find entry for "+ATOM_PUB_FEED_MAX_ENTRIES+" in properties file.");	
		}
		
		this.uuidToUrlFilePath = properties.getProperty(UUID_TO_URL_FILE_PATH);
		if (uuidToUrlFilePath == null) {
			throw new RuntimeException("Could not find entry for "+UUID_TO_URL_FILE_PATH+" in properties file.");
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

	public String getSleepMax() {
		return sleepMax;
	}

	public String[] getPublisherAbbreviations() {
		return publisherAbbreviations;
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
	
	public String getRootWebFeedsDir() {
		return rootWebFeedsDir;
	}

	public String getRssWriteDir() {
		return rssWriteDir;
	}

	public String getSummaryWriteDir() {
		return summaryWriteDir;
	}

	public String getWebSummaryWriteDir() {
		return webSummaryWriteDir;
	}
	
	public String getDoiListPath() {
		return doiListPath;
	}
	
	public String getDoiIndexPath() {
		return doiIndexPath;
	}
	
	public String getBondLengthsDir() {
		return bondLengthsDir;
	}
	
	public String getCellParamsFilePath() {
		return cellParamsFilePath;
	}
	
	public String getSmilesListPath() {
		return smilesListPath;
	}
	
	public String getSmilesIndexPath() {
		return smilesIndexPath;
	}
	
	public String getAtomPubRootDir() {
		return atomPubRootDir;
	}
	
	public String getAtomPubRootUrl() {
		return atomPubRootUrl;
	}
	
	public int getAtomPubFeedMaxEntries() {
		return Integer.valueOf(atomPubFeedMaxEntries);
	}
	
	public String getUuidToUrlFilePath() {
		return uuidToUrlFilePath;
	}
}
