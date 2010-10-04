package wwmm.crystaleye.tools;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.xmlcml.cml.base.CMLConstants;
import org.xmlcml.molutil.ChemicalElement;

import wwmm.atomarchiver.AtomArchiveFeed;
import wwmm.crystaleye.CrystalEyeJournals;
import wwmm.crystaleye.JournalDetails;

public class FeedCreationTool implements CMLConstants {

	private File feedDir;
	private String feedDirUrl;

	public FeedCreationTool(File feedDir, String feedDirUrl) {
		this.feedDir = feedDir;
		this.feedDirUrl = feedDirUrl;
	}

	public void createJournalRssFeeds() {
		for (JournalDetails journalDetails : new CrystalEyeJournals().getDetails()) {
			String publisherTitle = journalDetails.getPublisherTitle();
			String publisherAbbreviation = journalDetails.getPublisherAbbreviation();
			String journalTitle = journalDetails.getJournalTitle();
			String journalAbbreviation = journalDetails.getJournalAbbreviation();

			String feedTitle = "CrystalEye: "+publisherTitle+", "+journalTitle;
			String feedDescription = "CrystalEye summary of "+publisherTitle+", "+journalTitle+".";
			String feedAuthor = "Chris Talbot";

			AtomArchiveFeed feed = new AtomArchiveFeed();
			File feedFile = new File(feedDir+"/journal/"+publisherAbbreviation+"/"+journalAbbreviation+"/feed.xml");
			String feedUrl = feedDirUrl+"/journal/"+publisherAbbreviation+"/"+journalAbbreviation+"/feed.xml";
			feed.initFeedWithRandomUuidAsId(feedFile, feedUrl, feedTitle, feedDescription, feedAuthor);
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
			File feedFile = new File(feedDir+"/atoms/"+symbol+"/feed.xml");
			String feedUrl = feedDirUrl+"/atoms/"+symbol+"/feed.xml";
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
				File feedFile = new File(feedDir+"/bonds/"+bondStr+"/feed.xml");
				String feedUrl = feedDirUrl+"/bonds/"+bondStr+"/feed.xml";
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
			File feedFile = new File(feedDir+"/class/"+clas+"/feed.xml");
			String feedUrl = feedDirUrl+"/class/"+clas+"/feed.xml";
			feed.initFeedWithRandomUuidAsId(feedFile, feedUrl, feedTitle, feedDescription, feedAuthor);
		}
	}

	public static void main(String[] args) {
		/*
		FeedCreationTool c = new FeedCreationTool("e:/crystaleye-new/docs/cif-flow-props.txt");

		c.createJournalRssFeeds();
		c.createAtomsRssFeeds();
		c.createBondRssFeeds();
		c.createClassRssFeeds();
		 */
	}
}
