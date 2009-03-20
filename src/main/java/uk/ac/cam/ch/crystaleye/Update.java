package uk.ac.cam.ch.crystaleye;

import java.io.File;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.xmlcml.cml.base.CMLConstants;

import uk.ac.cam.ch.crystaleye.fetch.FetchManager;
import uk.ac.cam.ch.crystaleye.process.CML2FooManager;
import uk.ac.cam.ch.crystaleye.process.Cif2CmlManager;
import uk.ac.cam.ch.crystaleye.properties.SiteProperties;
import uk.ac.cam.ch.crystaleye.site.BondLengthsManager;
import uk.ac.cam.ch.crystaleye.site.CellParamsManager;
import uk.ac.cam.ch.crystaleye.site.DoiListManager;
import uk.ac.cam.ch.crystaleye.site.SmilesListManager;
import uk.ac.cam.ch.crystaleye.site.WebpageManager;
import uk.ac.cam.ch.crystaleye.site.feeds.AtomPubManager;
import uk.ac.cam.ch.crystaleye.site.feeds.RssManager;

public class Update implements CMLConstants {

	public static void main(String[] args) {
		Update launcher = new Update();
		launcher.run(args);
	}

	public void run(String[] args) {
		Options options = new Options();
		options.addOption("p", true, "Path to properties file");
		options.addOption("all", false, "Run all manager");
		options.addOption("fetch", false, "Run fetch manager");
		options.addOption("cif2cml", false, "Run cif2cml manager");
		options.addOption("cml2foo", false, "Run cml2foo manager");
		options.addOption("webpage", false, "Run webpage manager");
		options.addOption("doilist", false, "Run doilist manager");
		options.addOption("cellparams", false, "Run cellparams manager");
		options.addOption("bondlengths", false, "Run bondlengths manager");
		options.addOption("smiles", false, "Run smiles manager");
		options.addOption("atompub", false, "Run atompub manager");
		options.addOption("rss", false, "Run rss manager");

		CommandLineParser parser = new BasicParser();
		CommandLine cl = null;
		try {
			cl = parser.parse(options, args);
		} catch(ParseException e) {
			e.printStackTrace();
			return;
		}

		String propsPath = cl.getOptionValue("p");
		if (propsPath == null) {
			throw new RuntimeException("No properties path set.");
		}
		if (args.length == 2) {
			runall(propsPath);
		} else {
			if (cl.hasOption("all")) {
				runall(propsPath);
			}
			if (cl.hasOption("fetch")) {
				FetchManager fetch = new FetchManager(propsPath);
				fetch.run();
			}
			if (cl.hasOption("cif2cml")) {
				Cif2CmlManager cif2Cml = new Cif2CmlManager(propsPath);
				cif2Cml.execute();
			}
			if (cl.hasOption("cml2foo")) {
				CML2FooManager cml2Foo = new CML2FooManager(propsPath);
				cml2Foo.execute();
			}
			if (cl.hasOption("webpage")) {
				WebpageManager webpage = new WebpageManager(propsPath);
				webpage.execute();
			}
			if (cl.hasOption("doilist")) {
				DoiListManager dois = new DoiListManager(propsPath);
				dois.execute();
			}
			if (cl.hasOption("cellparams")) {
				CellParamsManager cell = new CellParamsManager(propsPath);
				cell.execute();
			}
			if (cl.hasOption("bondlengths")) {
				/*
				BondLengthsManager bond = new BondLengthsManager(propsPath);
				bond.execute();
				*/
			}
			if (cl.hasOption("smiles")) {
				SmilesListManager smi = new SmilesListManager(propsPath);
				smi.execute();
			}
			if (cl.hasOption("atompub")) {
				AtomPubManager ap = new AtomPubManager(propsPath);
				ap.execute();
			}
			if (cl.hasOption("rss")) {
				RssManager rss = new RssManager(propsPath);
				rss.execute();
			}
		}
	}
	
	private void runall(String propsPath) {
		FetchManager fetch = new FetchManager(propsPath);
		fetch.run();
		Cif2CmlManager cif2Cml = new Cif2CmlManager(propsPath);
		cif2Cml.execute();
		CML2FooManager cml2Foo = new CML2FooManager(propsPath);
		cml2Foo.execute();
		WebpageManager webpage = new WebpageManager(propsPath);
		webpage.execute();
		DoiListManager dois = new DoiListManager(propsPath);
		dois.execute();
		CellParamsManager cell = new CellParamsManager(propsPath);
		cell.execute();
		/*
		BondLengthsManager bond = new BondLengthsManager(propsPath);
		bond.execute();
		*/
		SmilesListManager smi = new SmilesListManager(propsPath);
		smi.execute();
		AtomPubManager ap = new AtomPubManager(propsPath);
		ap.execute();
		// before updating the RSS feeds, go through them all and delete any entries over x days old
		// new RSSArchiver().deleteOldFeedEntries("xml", properties.getRssWriteDir(), 14);
		RssManager rss = new RssManager(propsPath);
		rss.execute();
	}

}
