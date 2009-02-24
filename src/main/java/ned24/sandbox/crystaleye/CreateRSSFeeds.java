package ned24.sandbox.crystaleye;

import static wwmm.crystaleye.CrystalEyeConstants.CRYSTALEYE_DATE_FORMAT;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import nu.xom.Document;

import org.xmlcml.cml.base.CMLConstants;
import org.xmlcml.molutil.ChemicalElement;

import wwmm.crystaleye.Utils;
import wwmm.crystaleye.properties.SiteProperties;
import wwmm.crystaleye.site.feeds.CMLRSSEntry.FeedType;
import wwmm.crystaleye.templates.feeds.Atom1;
import wwmm.crystaleye.templates.feeds.Rss1;
import wwmm.crystaleye.templates.feeds.Rss2;

import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedOutput;

public class CreateRSSFeeds implements CMLConstants {

	private static final String RSS = "rss";
	private SiteProperties properties;

	String[] publisherAbbreviations;
	String publisherAbbreviation;
	String publisherTitle;
	String[] journalAbbreviations;
	String journalAbbreviation;
	String[] journalTitles;
	String journalTitle;
	String year;
	String issueNum;
	String issueWriteDir;

	private String downloadLogPath;
	private String writeDir;
	private String propertiesPath;

	String splitCifRegex;

	String feedWriteDir;
	String rssArchiveDir;
	String[] feedTypes;
	String summaryWriteDir;

	String rootFeedsDir;

	public CreateRSSFeeds(String propPath) {
		this.propertiesPath=propPath;
		setProperties();
	}

	public CreateRSSFeeds(File propertiesFile) {
		this.setProperties(propertiesFile);
	}

	private void setProperties(File propertiesFile) {
		properties = new SiteProperties(propertiesFile);

		rootFeedsDir = properties.getRssWriteDir();	
	}

	private void setProperties() {
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(this.propertiesPath));
			this.writeDir = properties.getProperty("write.dir");
			this.downloadLogPath = properties.getProperty("download.log.path");
			this.publisherAbbreviations = properties.getProperty("publisher.abbreviations").split(",");
			this.splitCifRegex = properties.getProperty("splitcif.regex");
			this.feedWriteDir = properties.getProperty("rss.write.dir");
			this.feedTypes = properties.getProperty("rss.feed.types").split(",");
			this.summaryWriteDir = properties.getProperty("summary.write.dir");
			this.rssArchiveDir = properties.getProperty("rss.archive.dir");
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

				String[] feedTypes = {"rss_1.0", "rss_2.0", "atom_1.0"};

				Date dNow = new Date();
				SimpleDateFormat formatter = new SimpleDateFormat(CRYSTALEYE_DATE_FORMAT);
				String date = formatter.format(dNow);

				for (String feedType : feedTypes) {
					SyndFeed feed = new SyndFeedImpl();
					feed.setTitle(feedTitle);
					feed.setLink("http://wwmm.ch.cam.ac.uk/crystaleye/feed/journal/"+publisherAbbreviation+"/"+journalAbbreviation+"/rss/"+feedType.replaceAll("\\.", "")+"/feed.xml");
					feed.setUri("http://wwmm.ch.cam.ac.uk/crystaleye/feed/journal/"+publisherAbbreviation+"/"+journalAbbreviation+"/rss/"+feedType.replaceAll("\\.", "")+"/feed.xml");
					feed.setDescription(feedDescription);
					feed.setAuthor(feedAuthor);
					try {
						feed.setPublishedDate(formatter.parse(date));
					} catch (ParseException e1) {
						e1.printStackTrace();
					}

					feed.setFeedType(feedType);

					String feedStr = "";
					SyndFeedOutput output = new SyndFeedOutput();
					try {
						feedStr = output.outputString(feed);
					} catch (FeedException e) {
						e.printStackTrace();
					}
					String outStr = feedWriteDir+"/journal/"+publisherAbbreviation+"/"+journalAbbreviation+"/rss/"+feedType.replaceAll("\\.", "")+"/feed.xml";

					Utils.writeText(feedStr, outStr);
				}
			}
		}
	}

	public void createAllRssFeeds() {
		String feedTitle = "CrystalEye: All Structures";
		String feedDescription = "All structures passing through the CrystalEye system.";
		String feedAuthor = "Chris Talbot";

		String[] feedTypes = {"rss_1.0", "rss_2.0", "atom_1.0"};

		Date dNow = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat(CRYSTALEYE_DATE_FORMAT);
		String date = formatter.format(dNow);

		for (String feedType : feedTypes) {
			SyndFeed feed = new SyndFeedImpl();
			feed.setTitle(feedTitle);
			feed.setLink("http://wwmm.ch.cam.ac.uk/crystaleye/feed/all/rss/"+feedType.replaceAll("\\.", "")+"/feed.xml");
			feed.setUri("http://wwmm.ch.cam.ac.uk/crystaleye/feed/all/rss/"+feedType.replaceAll("\\.", "")+"/feed.xml");
			feed.setDescription(feedDescription);
			feed.setAuthor(feedAuthor);
			try {
				feed.setPublishedDate(formatter.parse(date));
			} catch (ParseException e1) {
				e1.printStackTrace();
			}

			feed.setFeedType(feedType);

			String feedStr = "";
			SyndFeedOutput output = new SyndFeedOutput();
			try {
				feedStr = output.outputString(feed);
			} catch (FeedException e) {
				e.printStackTrace();
			}
			String outStr = feedWriteDir+"/all/rss/"+feedType.replaceAll("\\.", "")+"/feed.xml";

			Utils.writeText(feedStr, outStr);
		}
	}

	public void createAtomsRssFeeds() {
		for (int i = 1; i < 105; i++) {
			ChemicalElement ce = ChemicalElement.getElement(i);
			String symbol = ce.getSymbol();
			String feedTitle = "CrystalEye: Structures containing "+symbol;
			String feedDescription = "CrystalEye summary of structures containing "+symbol+".";
			String feedAuthor = "Chris Talbot";

			String[] feedTypes = {"rss_1.0", "rss_2.0", "atom_1.0"};

			Date dNow = new Date();
			SimpleDateFormat formatter = new SimpleDateFormat(CRYSTALEYE_DATE_FORMAT);
			String date = formatter.format(dNow);

			for (String feedType : feedTypes) {
				SyndFeed feed = new SyndFeedImpl();
				feed.setTitle(feedTitle);
				feed.setLink("http://wwmm.ch.cam.ac.uk/crystaleye/feed/atoms/"+symbol+"/rss/"+feedType.replaceAll("\\.", "")+"/feed.xml");
				feed.setUri("http://wwmm.ch.cam.ac.uk/crystaleye/feed/atoms/"+symbol+"/rss/"+feedType.replaceAll("\\.", "")+"/feed.xml");
				feed.setDescription(feedDescription);
				feed.setAuthor(feedAuthor);
				try {
					feed.setPublishedDate(formatter.parse(date));
				} catch (ParseException e1) {
					e1.printStackTrace();
				}

				feed.setFeedType(feedType);

				String feedStr = "";
				SyndFeedOutput output = new SyndFeedOutput();
				try {
					feedStr = output.outputString(feed);
				} catch (FeedException e) {
					e.printStackTrace();
				}
				String outStr = feedWriteDir+"/atoms/"+symbol+"/rss/"+feedType.replaceAll("\\.", "")+"/feed.xml";

				Utils.writeText(feedStr, outStr);
			}
		}
	}

	public void createBondRssFeeds() {
		for (int i = 1; i < 105; i++) {
			ChemicalElement ce1 = ChemicalElement.getElement(i);
			String symbol1 = ce1.getSymbol();
			for (int j = i; j < 105; j++) {
				ChemicalElement ce2 = ChemicalElement.getElement(j);
				String symbol2 = ce2.getSymbol();

				String feedTitle = "CrystalEye: Structures containing bonds of "+symbol1+"-"+symbol2;
				String feedDescription = "CrystalEye summary of structures containing bonds of "+symbol1+"-"+symbol2+".";
				String feedAuthor = "Chris Talbot";

				String[] feedTypes = {"rss_1.0", "rss_2.0", "atom_1.0"};

				Date dNow = new Date();
				SimpleDateFormat formatter = new SimpleDateFormat(CRYSTALEYE_DATE_FORMAT);
				String date = formatter.format(dNow);

				for (String feedType : feedTypes) {
					SyndFeed feed = new SyndFeedImpl();
					feed.setTitle(feedTitle);
					feed.setLink("http://wwmm.ch.cam.ac.uk/crystaleye/feed/bonds/"+symbol1+"-"+symbol2+"/rss/"+feedType.replaceAll("\\.", "")+"/feed.xml");
					feed.setUri("http://wwmm.ch.cam.ac.uk/crystaleye/feed/bonds/"+symbol1+"-"+symbol2+"/rss/"+feedType.replaceAll("\\.", "")+"/feed.xml");
					feed.setDescription(feedDescription);
					feed.setAuthor(feedAuthor);
					try {
						feed.setPublishedDate(formatter.parse(date));
					} catch (ParseException e1) {
						e1.printStackTrace();
					}

					feed.setFeedType(feedType);

					String feedStr = "";
					SyndFeedOutput output = new SyndFeedOutput();
					try {
						feedStr = output.outputString(feed);
					} catch (FeedException e) {
						e.printStackTrace();
					}
					String outStr = feedWriteDir+"/bonds/"+symbol1+"-"+symbol2+"/rss/"+feedType.replaceAll("\\.", "")+"/feed.xml";

					Utils.writeText(feedStr, outStr);
				}
			}
		}
	}

	public void createClassRssFeeds() {
		String[] classes = {"organic", "inorganic", "organometallic"};
		for (String clas : classes) {
			String feedTitle = "CrystalEye: "+clas+" structures";
			String feedDescription = "CrystalEye summary of "+clas+" structures.";
			String feedAuthor = "Chris Talbot";

			String[] feedTypes = {"rss_1.0", "rss_2.0", "atom_1.0"};

			Date dNow = new Date();
			SimpleDateFormat formatter = new SimpleDateFormat(CRYSTALEYE_DATE_FORMAT);
			String date = formatter.format(dNow);

			for (String feedType : feedTypes) {
				SyndFeed feed = new SyndFeedImpl();
				feed.setTitle(feedTitle);
				feed.setLink("http://wwmm.ch.cam.ac.uk/crystaleye/feed/class/"+clas+"/rss/"+feedType.replaceAll("\\.", "")+"/feed.xml");
				feed.setUri("http://wwmm.ch.cam.ac.uk/crystaleye/feed/class/"+clas+"/rss/"+feedType.replaceAll("\\.", "")+"/feed.xml");
				feed.setDescription(feedDescription);
				feed.setAuthor(feedAuthor);
				try {
					feed.setPublishedDate(formatter.parse(date));
				} catch (ParseException e1) {
					e1.printStackTrace();
				}

				feed.setFeedType(feedType);

				String feedStr = "";
				SyndFeedOutput output = new SyndFeedOutput();
				try {
					feedStr = output.outputString(feed);
				} catch (FeedException e) {
					e.printStackTrace();
				}
				String outStr = feedWriteDir+"/class/"+clas+"/rss/"+feedType.replaceAll("\\.", "")+"/feed.xml";

				Utils.writeText(feedStr, outStr);
			}
		}
	}

	public void createJournalCmlrssFeeds() {
		for (String publisherAbbreviation : publisherAbbreviations) {
			setJournalNames(publisherAbbreviation);
			setPublisherTitle(publisherAbbreviation);
			for (int i = 0; i < journalAbbreviations.length; i++) {
				journalAbbreviation = journalAbbreviations[i];
				journalTitle = journalTitles[i];

				String feedTitle = "CrystalEye CMLRSS: "+this.publisherTitle+", "+this.journalTitle;
				String feedDescription = "CrystalEye CMLRSS summary of "+this.publisherTitle+", "+this.journalTitle+", "+year+", "+issueNum;
				String feedAuthor = "Chris Talbot";

				String[] feedTypes = {"rss_1.0", "rss_2.0", "atom_0.3", "atom_1.0"};

				for (String feedType : feedTypes) {
					String cmlRssUrl = "http://wwmm.ch.cam.ac.uk/crystaleye/feed/journal/"+publisherAbbreviation+"/"+journalAbbreviation+"/cmlrss/"+feedType.replaceAll("\\.", "")+"/feed.xml";
					Document feedDoc = null;
					FeedType type = getFeedType(feedType);
					try {
						if (type.equals(FeedType.ATOM_1)) {
							feedDoc = new Atom1(feedTitle, feedDescription, feedAuthor, cmlRssUrl).getFeed();
						} else if (type.equals(FeedType.RSS_1)) {
							feedDoc = new Rss1(feedTitle, feedDescription, feedAuthor, cmlRssUrl).getFeed();
						} else if (type.equals(FeedType.RSS_2)) {
							feedDoc = new Rss2(feedTitle, feedDescription, feedAuthor, cmlRssUrl).getFeed();
						}
					} catch (Exception e) {
						e.printStackTrace();
						throw new RuntimeException("Problem parsing feed.");
					}

					String outStr = feedWriteDir+"/journal/"+publisherAbbreviation+"/"+journalAbbreviation+"/cmlrss/"+feedType.replaceAll("\\.", "")+"/feed.xml";	
					Utils.writeXML(feedDoc, outStr);
				}
			}
		}
	}

	public void createAtomsCmlrssFeeds() {
		for (int i = 1; i < 105; i++) {
			ChemicalElement ce = ChemicalElement.getElement(i);
			String symbol = ce.getSymbol();
			String feedTitle = "CrystalEye CMLRSS: Structures containing "+symbol;
			String feedDescription = "CrystalEye CMLRSS summary of structures containing "+symbol+".";
			String feedAuthor = "Chris Talbot";

			String[] feedTypes = {"rss_1.0", "rss_2.0", "atom_1.0"};

			for (String feedType : feedTypes) {
				String cmlRssUrl = "http://wwmm.ch.cam.ac.uk/crystaleye/feed/atoms/"+symbol+"/cmlrss/"+feedType.replaceAll("\\.", "")+"/feed.xml";
				Document feedDoc = null;
				FeedType type = getFeedType(feedType);
				try {
					if (type.equals(FeedType.ATOM_1)) {
						feedDoc = new Atom1(feedTitle, feedDescription, feedAuthor, cmlRssUrl).getFeed();
					} else if (type.equals(FeedType.RSS_1)) {
						feedDoc = new Rss1(feedTitle, feedDescription, feedAuthor, cmlRssUrl).getFeed();
					} else if (type.equals(FeedType.RSS_2)) {
						feedDoc = new Rss2(feedTitle, feedDescription, feedAuthor, cmlRssUrl).getFeed();
					}
				} catch (Exception e) {
					e.printStackTrace();
					throw new RuntimeException("Problem parsing feed.");
				}

				String outStr = feedWriteDir+"/atoms/"+symbol+"/cmlrss/"+feedType.replaceAll("\\.", "")+"/feed.xml";
				Utils.writeXML(feedDoc, outStr);
			}
		}
	}

	public void createBondCmlrssFeeds() {
		for (int i = 1; i < 105; i++) {
			ChemicalElement ce1 = ChemicalElement.getElement(i);
			String symbol1 = ce1.getSymbol();
			for (int j = i; j < 105; j++) {
				ChemicalElement ce2 = ChemicalElement.getElement(j);
				String symbol2 = ce2.getSymbol();

				String feedTitle = "CrystalEye CMLRSS: Structures containing bonds of "+symbol1+"-"+symbol2;
				String feedDescription = "CrystalEye CMLRSS summary of structures containing bonds of "+symbol1+"-"+symbol2+".";
				String feedAuthor = "Chris Talbot";

				String[] feedTypes = {"rss_1.0", "rss_2.0", "atom_1.0"};

				for (String feedType : feedTypes) {
					String cmlRssUrl = "http://wwmm.ch.cam.ac.uk/crystaleye/feed/bonds/"+symbol1+"-"+symbol2+"/cmlrss/"+feedType.replaceAll("\\.", "")+"/feed.xml";
					Document feedDoc = null;
					FeedType type = getFeedType(feedType);
					try {
						if (type.equals(FeedType.ATOM_1)) {
							feedDoc = new Atom1(feedTitle, feedDescription, feedAuthor, cmlRssUrl).getFeed();
						} else if (type.equals(FeedType.RSS_1)) {
							feedDoc = new Rss1(feedTitle, feedDescription, feedAuthor, cmlRssUrl).getFeed();
						} else if (type.equals(FeedType.RSS_2)) {
							feedDoc = new Rss2(feedTitle, feedDescription, feedAuthor, cmlRssUrl).getFeed();
						}
					} catch (Exception e) {
						e.printStackTrace();
						throw new RuntimeException("Problem parsing feed.");
					}

					String outStr = feedWriteDir+"/bonds/"+symbol1+"-"+symbol2+"/cmlrss/"+feedType.replaceAll("\\.", "")+"/feed.xml";
					Utils.writeXML(feedDoc, outStr);
				}
			}
		}
	}

	public void createClassCmlrssFeeds() {
		String[] classes = {"organic", "inorganic", "organometallic"};
		for (String clas : classes) {
			String feedTitle = "CrystalEye CMLRSS: "+clas+" structures";
			String feedDescription = "CrystalEye CMLRSS summary of "+clas+" structures.";
			String feedAuthor = "Chris Talbot";

			String[] feedTypes = {"rss_1.0", "rss_2.0", "atom_1.0"};

			for (String feedType : feedTypes) {
				String cmlRssUrl = "http://wwmm.ch.cam.ac.uk/crystaleye/feed/class/"+clas+"/cmlrss/"+feedType.replaceAll("\\.", "")+"/feed.xml";
				Document feedDoc = null;
				FeedType type = getFeedType(feedType);
				try {
					if (type.equals(FeedType.ATOM_1)) {
						feedDoc = new Atom1(feedTitle, feedDescription, feedAuthor, cmlRssUrl).getFeed();
					} else if (type.equals(FeedType.RSS_1)) {
						feedDoc = new Rss1(feedTitle, feedDescription, feedAuthor, cmlRssUrl).getFeed();
					} else if (type.equals(FeedType.RSS_2)) {
						feedDoc = new Rss2(feedTitle, feedDescription, feedAuthor, cmlRssUrl).getFeed();
					}
				} catch (Exception e) {
					e.printStackTrace();
					throw new RuntimeException("Problem parsing feed.");
				}

				String outStr = feedWriteDir+"/class/"+clas+"/cmlrss/"+feedType.replaceAll("\\.", "")+"/feed.xml";
				Utils.writeXML(feedDoc, outStr);
			}
		}
	}

	private FeedType getFeedType(String feedType) {
		if ("rss_1.0".equals(feedType)) {
			return FeedType.RSS_1;
		} else if ("rss_2.0".equals(feedType)) {
			return FeedType.RSS_2;
		} else if ("atom_1.0".equals(feedType)) {
			return FeedType.ATOM_1;
		} else {
			throw new RuntimeException("RSS type "+feedType+" not supported.");
		}
	}

	public static void main(String[] args) {
		CreateRSSFeeds c = new CreateRSSFeeds("e:/crystaleye-data/docs/cif-flow-props.txt");
		c.createAllRssFeeds();

		c.createJournalRssFeeds();
		c.createAtomsRssFeeds();
		c.createBondRssFeeds();
		c.createClassRssFeeds();

		c.createAtomsCmlrssFeeds();
		c.createBondCmlrssFeeds();
		c.createClassCmlrssFeeds();
	}
}
