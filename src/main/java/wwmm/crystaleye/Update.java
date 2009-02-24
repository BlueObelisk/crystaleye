package wwmm.crystaleye;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.xmlcml.cml.base.CMLConstants;

import wwmm.crystaleye.tasks.Cif2CmlManager;

public class Update implements CMLConstants {

	// SiteProperties is currently not being used 
	//private SiteProperties properties;

	public static void main(String[] args) {
		Update launcher = new Update();
		launcher.run(args);
	}

	public void run(String[] args) {
		Options options = new Options();
		Option option = null;

		OptionBuilder.withArgName("PROPERTIES PATH");
		OptionBuilder.hasArg();
		OptionBuilder.withDescription("Path to properties file");
		option = OptionBuilder.create("p");
		options.addOption(option);

		CommandLineParser parser = new BasicParser();
		CommandLine commandLine = null;

		try {
			commandLine = parser.parse(options, args);
		}
		catch(ParseException e) {
			printUsage();
			return;
		}

		String propsPath = commandLine.getOptionValue("p");
		//properties = new SiteProperties(new File(propsPath));

		if (propsPath != null) {
//			FetchManager fetch = new FetchManager(propsPath);
//			fetch.run();
			
			Cif2CmlManager cif2Cml = new Cif2CmlManager(propsPath);
			cif2Cml.execute();
			
//			CML2FooManager cml2Foo = new CML2FooManager(propsPath);			
//			cml2Foo.execute();
			
//			WebpageManager webpage = new WebpageManager(propsPath);
//			webpage.execute();

			// Possibly not needed.
//			DoiListManager dois = new DoiListManager(propsPath);
//			dois.execute();
			
			// Needed for unit cell dimension search. (later)
//			CellParamsManager cell = new CellParamsManager(propsPath);
//			cell.execute();
			
			// Not needed
//			BondLengthsManager bond = new BondLengthsManager(propsPath);
//			bond.execute();
			
			// Needed for substructure search (later)
//			SmilesListManager smi = new SmilesListManager(propsPath);
//			smi.execute();
			
			// Needed to create atom archived feeds.
//			AtomPubManager ap = new AtomPubManager(propsPath);
//			ap.execute();
			
			// before updating the RSS feeds, go through them all and delete any entries over x days old
			// new RSSArchiver().deleteOldFeedEntries("xml", properties.getRssWriteDir(), 14);
			// Legacy, don't bother with this. Not needed			
//			RssManager rss = new RssManager(propsPath);
//			rss.execute();
		} else {
			printUsage();
		}
	}

	private void printUsage() {
		
		//TODO: Change this to the correct class or jar name 
		//The following assumes that Update.class is run from cifmanager.jar
		System.out.println("Usage: cifmanager -p [PATH]");
		System.out.println("Execute CIF manager");
		System.out.println();
		System.out.println("  -p, --path to properties file");
		System.out.println();
	}
}
