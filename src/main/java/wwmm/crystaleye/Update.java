package wwmm.crystaleye;

import java.io.File;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;

import wwmm.crystaleye.managers.BondLengthsManager;
import wwmm.crystaleye.managers.CML2FooManager;
import wwmm.crystaleye.managers.CellParamsManager;
import wwmm.crystaleye.managers.Cif2CmlManager;
import wwmm.crystaleye.managers.Cml2RdfManager;
import wwmm.crystaleye.managers.DoiListManager;
import wwmm.crystaleye.managers.FeedManager;
import wwmm.crystaleye.managers.FetchManager;
import wwmm.crystaleye.managers.SmilesListManager;
import wwmm.crystaleye.managers.WebpageManager;

public class Update {
	
	private static final Logger LOG = Logger.getLogger(Update.class);

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
		options.addOption("cml2rdf", false, "Run cml2rdf manager");
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
			System.err.println("Problem parsing command line, due to: "+e.getMessage());
			return;
		}

		String propsPath = cl.getOptionValue("p");
		if (propsPath == null) {
			System.err.println("No properties path set.  Set one with the -p option.");
			return;
		}
		File propsFile = new File(propsPath);
		if (args.length == 2) {
			runall(propsFile);
		} else {
			if (cl.hasOption("all")) {
				runall(propsFile);
			}
			if (cl.hasOption("fetch")) {
				FetchManager fetch = new FetchManager(propsFile);
				fetch.run();
			}
			if (cl.hasOption("cif2cml")) {
				Cif2CmlManager cif2Cml = new Cif2CmlManager(propsFile);
				cif2Cml.execute();
			}
			if (cl.hasOption("cml2foo")) {
				CML2FooManager cml2Foo = new CML2FooManager(propsFile);
				cml2Foo.execute();
			}
			if (cl.hasOption("cml2rdf")) {
				Cml2RdfManager cml2rdf = new Cml2RdfManager(propsFile);
				cml2rdf.execute();
			}
			if (cl.hasOption("webpage")) {
				WebpageManager webpage = new WebpageManager(propsFile);
				webpage.execute();
			}
			if (cl.hasOption("doilist")) {
				DoiListManager dois = new DoiListManager(propsFile);
				dois.execute();
			}
			if (cl.hasOption("cellparams")) {
				CellParamsManager cell = new CellParamsManager(propsFile);
				cell.execute();
			}
			if (cl.hasOption("bondlengths")) {
				//LOG.info("The bondlengths functionality is currently unavailable.");
				BondLengthsManager bond = new BondLengthsManager(propsFile);
				bond.execute();
			}
			if (cl.hasOption("smiles")) {
				SmilesListManager smi = new SmilesListManager(propsFile);
				smi.execute();
			}
			if (cl.hasOption("rss")) {
				FeedManager rss = new FeedManager(propsFile);
				rss.execute();
			}
		}
	}
	
	private void runall(File propsFile) {
		LOG.info("Starting FetchManager");
		FetchManager fetch = new FetchManager(propsFile);
		fetch.run();
		LOG.info("Starting CIF2CMLManager");
		Cif2CmlManager cif2Cml = new Cif2CmlManager(propsFile);
		cif2Cml.execute();
		LOG.info("Starting CML2FooManager");
		CML2FooManager cml2Foo = new CML2FooManager(propsFile);
		cml2Foo.execute();
		//LOG.info("Starting CML2RDFManager");
		//Cml2RdfManager cml2rdf = new Cml2RdfManager(propsFile);
		//cml2rdf.execute();
		LOG.info("Starting WebpageManager");
		WebpageManager webpage = new WebpageManager(propsFile);
		webpage.execute();
		LOG.info("Starting DoiListManager");
		DoiListManager dois = new DoiListManager(propsFile);
		dois.execute();
		//LOG.info("Starting CellParamsManager");
		//CellParamsManager cell = new CellParamsManager(propsFile);
		//cell.execute();
		//LOG.info("Starting BondLengthsManager");
		//BondLengthsManager bond = new BondLengthsManager(propsFile);
		//bond.execute();
		
		LOG.info("Starting SmilesManager");
		SmilesListManager smi = new SmilesListManager(propsFile);
		smi.execute();
		LOG.info("Starting RSSManager");
		FeedManager rss = new FeedManager(propsFile);
		rss.execute();
	}

}
