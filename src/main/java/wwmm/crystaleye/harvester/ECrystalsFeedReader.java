package wwmm.crystaleye.harvester;

import java.io.File;
import java.io.IOException;

public class ECrystalsFeedReader extends CrystaleyeFeedReader {

	public ECrystalsFeedReader(File dataDirectory, int maxEntriesToFetch) {
		super(dataDirectory, maxEntriesToFetch, new ECrystalsFeedEntryHandler(dataDirectory));
		setFeedUrl("http://ecrystals.chem.soton.ac.uk/cgi/latest_tool?output=Atom");
	}

	/**
	 * Main method meant for class use demonstration purposes.
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		String filepath = "c:/workspace/ecrystals-harvesting/";
		File dataDir = new File(filepath);
		ECrystalsFeedReader reader = new ECrystalsFeedReader(dataDir, 1000);
		reader.readFeed();
	}

}
