package ned24.sandbox;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.xmlcml.cml.base.CMLConstants;
import org.xmlcml.molutil.ChemicalElement;

import wwmm.atomarchiver.AtomArchiveFeed;
import wwmm.crystaleye.CrystalEyeProperties;

public class RSSFeedCreationTool implements CMLConstants {

	private CrystalEyeProperties properties;

	private String[] publisherAbbreviations;
	private String publisherTitle;
	private String[] journalAbbreviations;
	private String journalAbbreviation;
	private String[] journalTitles;
	private String journalTitle;
	private String propertiesPath;
	private String feedWriteDir;

	public RSSFeedCreationTool(String propPath) {
		this.propertiesPath=propPath;
		setProperties();
	}

	public RSSFeedCreationTool(File propertiesFile) {
		this.setProperties(propertiesFile);
	}

	private void setProperties(File propertiesFile) {
		properties = new CrystalEyeProperties(propertiesFile);	
	}

	private void setProperties() {
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(this.propertiesPath));
			this.publisherAbbreviations = properties.getProperty("publisher.abbreviations").split(",");
			this.feedWriteDir = properties.getProperty("rss.write.dir");
		} catch (IOException e) {
			throw new RuntimeException("Could not read properties file: "+this.propertiesPath, e);
		}
	}

	private void setJournalNames(String publisherAbbreviation) {
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(this.propertiesPath));
		} catch (IOException e) {
			throw new RuntimeException("Could not read properties file: "+this.propertiesPath, e);
		}
		this.journalAbbreviations = properties.getProperty(publisherAbbreviation+".journal.abbreviations").split(",");
		this.journalTitles = properties.getProperty(publisherAbbreviation+".journal.full.titles").split(",");
	}

	private void setPublisherTitle(String publisherAbbreviation) {
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(this.propertiesPath));
		} catch (IOException e) {
			throw new RuntimeException("Could not read properties file: "+this.propertiesPath, e);
		}
		this.publisherTitle = properties.getProperty(publisherAbbreviation+".full.title");
	}

	public void createJournalRssFeeds() {
		for (String publisherAbbreviation : publisherAbbreviations) {
			setJournalNames(publisherAbbreviation);
			setPublisherTitle(publisherAbbreviation);
			for (int i = 0; i < journalAbbreviations.length; i++) {
				journalAbbreviation = journalAbbreviations[i];
				journalTitle = journalTitles[i];
				String feedTitle = "CrystalEye: "+this.publisherTitle+", "+this.journalTitle;
				String feedDescription = "CrystalEye summary of "+this.publisherTitle+", "+this.journalTitle+".";
				String feedAuthor = "Chris Talbot";

				AtomArchiveFeed feed = new AtomArchiveFeed();
				File feedFile = new File(feedWriteDir+"/journal/"+publisherAbbreviation+"/"+journalAbbreviation+"/feed.xml");
				String feedUrl = "http://wwmm.ch.cam.ac.uk/crystaleye/feed/journal/"+publisherAbbreviation+"/"+journalAbbreviation+"/feed.xml";
				feed.initFeedWithRandomUuidAsId(feedFile, feedUrl, feedTitle, feedDescription, feedAuthor);
			}
		}
	}

	public void createAtomsRssFeeds() {
		for (int i = 1; i < 105; i++) {
			ChemicalElement ce = ChemicalElement.getElement(i);
			String symbol = ce.getSymbol();
			String feedTitle = "CrystalEye: Structures containing "+symbol;
			String feedDescription = "CrystalEye summary of structures containing "+symbol+".";
			String feedAuthor = "Chris Talbot";
			
			AtomArchiveFeed feed = new AtomArchiveFeed();
			File feedFile = new File(feedWriteDir+"/atoms/"+symbol+"/feed.xml");
			String feedUrl = "http://wwmm.ch.cam.ac.uk/crystaleye/feed/atoms/"+symbol+"/feed.xml";
			feed.initFeedWithRandomUuidAsId(feedFile, feedUrl, feedTitle, feedDescription, feedAuthor);
		}
	}

	public void createBondRssFeeds() {
		for (int i = 1; i < 105; i++) {
			ChemicalElement ce1 = ChemicalElement.getElement(i);
			String symbol1 = ce1.getSymbol();
			for (int j = i; j < 105; j++) {
				ChemicalElement ce2 = ChemicalElement.getElement(j);
				String symbol2 = ce2.getSymbol();
				
				List<String> symbols = new ArrayList<String>(2);
				symbols.add(symbol1);
				symbols.add(symbol2);
				Collections.sort(symbols);
				String bondStr = symbols.get(0)+"-"+symbols.get(1);
				
				String feedTitle = "CrystalEye: Structures containing bonds of "+bondStr;
				String feedDescription = "CrystalEye summary of structures containing bonds of "+bondStr+".";
				String feedAuthor = "Chris Talbot";
				
				AtomArchiveFeed feed = new AtomArchiveFeed();
				File feedFile = new File(feedWriteDir+"/bonds/"+bondStr+"/feed.xml");
				String feedUrl = "http://wwmm.ch.cam.ac.uk/crystaleye/feed/bonds/"+bondStr+"/feed.xml";
				feed.initFeedWithRandomUuidAsId(feedFile, feedUrl, feedTitle, feedDescription, feedAuthor);
			}
		}
	}

	public void createClassRssFeeds() {
		String[] classes = {"organic", "inorganic", "organometallic"};
		for (String clas : classes) {
			String feedTitle = "CrystalEye: "+clas+" structures";
			String feedDescription = "CrystalEye summary of "+clas+" structures.";
			String feedAuthor = "Chris Talbot";
			
			AtomArchiveFeed feed = new AtomArchiveFeed();
			File feedFile = new File(feedWriteDir+"/class/"+clas+"/feed.xml");
			String feedUrl = "http://wwmm.ch.cam.ac.uk/crystaleye/feed/class/"+clas+"/feed.xml";
			feed.initFeedWithRandomUuidAsId(feedFile, feedUrl, feedTitle, feedDescription, feedAuthor);
		}
	}

	public static void main(String[] args) {
		RSSFeedCreationTool c = new RSSFeedCreationTool("e:/crystaleye-new/docs/cif-flow-props.txt");

		c.createJournalRssFeeds();
		c.createAtomsRssFeeds();
		c.createBondRssFeeds();
		c.createClassRssFeeds();
	}
}
