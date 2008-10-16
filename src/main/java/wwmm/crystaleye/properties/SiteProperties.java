package wwmm.crystaleye.properties;

import static wwmm.crystaleye.CrystalEyeConstants.ATOM_PUB_FEED_MAX_ENTRIES;
import static wwmm.crystaleye.CrystalEyeConstants.ATOM_PUB_ROOT_DIR;
import static wwmm.crystaleye.CrystalEyeConstants.ATOM_PUB_ROOT_URL;
import static wwmm.crystaleye.CrystalEyeConstants.BOND_LENGTHS_DIR;
import static wwmm.crystaleye.CrystalEyeConstants.CELL_PARAMS_FILE;
import static wwmm.crystaleye.CrystalEyeConstants.DOI_LIST_PATH;
import static wwmm.crystaleye.CrystalEyeConstants.RSS_FEED_TYPES;
import static wwmm.crystaleye.CrystalEyeConstants.RSS_ROOT_FEEDS_DIR;
import static wwmm.crystaleye.CrystalEyeConstants.RSS_WRITE_DIR;
import static wwmm.crystaleye.CrystalEyeConstants.SMILES_INDEX_PATH;
import static wwmm.crystaleye.CrystalEyeConstants.SMILES_LIST_PATH;
import static wwmm.crystaleye.CrystalEyeConstants.SUMMARY_WRITE_DIR;
import static wwmm.crystaleye.CrystalEyeConstants.UUID_TO_URL_FILE_PATH;
import static wwmm.crystaleye.CrystalEyeConstants.WEB_SUMMARY_WRITE_DIR;

import java.io.File;

public class SiteProperties extends ManyPublisherProperties {

	// rss
	private String rssWriteDir;
	private String rootWebFeedsDir;
	private String[] feedTypes;
	private String[] urlSafeFeedTypes;

	// website
	private String summaryWriteDir;
	private String webSummaryWriteDir;
	
	// doi list
	private String doiListPath;
	
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

	public SiteProperties(File propFile) {
		super(propFile);
	}

	protected void setProperties() {
		super.setProperties();
		// the types of rss feed that are to be created
		this.feedTypes = properties.getProperty(RSS_FEED_TYPES).split(",");
		if (feedTypes == null) {
			throw new CrystalEyeRuntimeException("Could not find entry for "+feedTypes+" in properties file.");
		}	
		// rss feed types contain '.' so need to remove them to make the name url safe
		urlSafeFeedTypes = new String[feedTypes.length];
		for (int i = 0; i < feedTypes.length; i++) {
			urlSafeFeedTypes[i] = feedTypes[i].replaceAll("\\.", "");
		}

		// root folder (on the server) for the crystal summary webpages
		this.summaryWriteDir = properties.getProperty(SUMMARY_WRITE_DIR);
		if (summaryWriteDir == null) {
			throw new CrystalEyeRuntimeException("Could not find entry for "+SUMMARY_WRITE_DIR+" in properties file.");
		}

		// root folder (as seen by internet browsers) for the crystal summary webpages
		this.webSummaryWriteDir = properties.getProperty(WEB_SUMMARY_WRITE_DIR);
		if (webSummaryWriteDir == null) {
			throw new CrystalEyeRuntimeException("Could not find entry for "+WEB_SUMMARY_WRITE_DIR+" in properties file.");
		}

		// root folder (on the server) for the crystal rss feeds
		this.rssWriteDir = properties.getProperty(RSS_WRITE_DIR);
		if (rssWriteDir == null) {
			throw new CrystalEyeRuntimeException("Could not find entry for "+RSS_WRITE_DIR+" in properties file.");
		}

		// root folder (as seen by internet browsers) for the crystal rss feeds
		this.rootWebFeedsDir=properties.getProperty(RSS_ROOT_FEEDS_DIR);
		if (rootWebFeedsDir == null) {
			throw new CrystalEyeRuntimeException("Could not find entry for "+RSS_ROOT_FEEDS_DIR+" in properties file.");
		}
		
		// file containing list of DOIs against the URLs for the crystal summary page
		this.doiListPath = properties.getProperty(DOI_LIST_PATH);
		if (doiListPath == null) {
			throw new CrystalEyeRuntimeException("Could not find entry for "+DOI_LIST_PATH+" in properties file.");
		}
		
		this.bondLengthsDir = properties.getProperty(BOND_LENGTHS_DIR);
		if (bondLengthsDir == null) {
			throw new CrystalEyeRuntimeException("Could not find entry for "+BOND_LENGTHS_DIR+" in properties file.");
		}
			
		// list of cell params already got from other sources - used to figure
		// out which of the COD cifs we don't have from elsewhere
		this.cellParamsFilePath = properties.getProperty(CELL_PARAMS_FILE);
		if (cellParamsFilePath == null) {
			throw new CrystalEyeRuntimeException("Could not find entry for "+CELL_PARAMS_FILE+" in properties file.");
		}
		
		this.smilesListPath = properties.getProperty(SMILES_LIST_PATH);
		if (smilesListPath == null) {
			throw new CrystalEyeRuntimeException("Could not find entry for "+SMILES_LIST_PATH+" in properties file.");
		}
		
		this.smilesIndexPath = properties.getProperty(SMILES_INDEX_PATH);
		if (smilesIndexPath == null) {
			throw new CrystalEyeRuntimeException("Could not find entry for "+SMILES_INDEX_PATH+" in properties file.");
		}
		
		this.atomPubRootDir = properties.getProperty(ATOM_PUB_ROOT_DIR);
		if (atomPubRootDir == null) {
			throw new CrystalEyeRuntimeException("Could not find entry for "+ATOM_PUB_ROOT_DIR+" in properties file.");
		}
		
		this.atomPubRootUrl = properties.getProperty(ATOM_PUB_ROOT_URL);
		if (atomPubRootUrl == null) {
			throw new CrystalEyeRuntimeException("Could not find entry for "+ATOM_PUB_ROOT_URL+" in properties file.");
		}
		
		this.atomPubFeedMaxEntries = properties.getProperty(ATOM_PUB_FEED_MAX_ENTRIES);
		if (atomPubFeedMaxEntries == null) {
			throw new CrystalEyeRuntimeException("Could not find entry for "+ATOM_PUB_FEED_MAX_ENTRIES+" in properties file.");	
		}
		
		this.uuidToUrlFilePath = properties.getProperty(UUID_TO_URL_FILE_PATH);
		if (uuidToUrlFilePath == null) {
			throw new CrystalEyeRuntimeException("Could not find entry for "+UUID_TO_URL_FILE_PATH+" in properties file.");
		}
	}

	public String[] getFeedTypes() {
		return feedTypes;
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

	public String[] getUrlSafeFeedTypes() {
		return urlSafeFeedTypes;
	}

	public String getWebSummaryWriteDir() {
		return webSummaryWriteDir;
	}
	
	public String getDoiListPath() {
		return doiListPath;
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

