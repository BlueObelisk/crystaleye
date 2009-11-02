package wwmm.crystaleye;

import java.io.File;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;

import wwmm.crystaleye.fetch.FetchManager;
import wwmm.crystaleye.managers.CML2FooManager;
import wwmm.crystaleye.managers.CellParamsManager;
import wwmm.crystaleye.managers.Cif2CmlManager;
import wwmm.crystaleye.managers.Cml2RdfManager;
import wwmm.crystaleye.managers.DoiListManager;
import wwmm.crystaleye.managers.RSSManager;
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
			LOG.warn("Exception parsing command line: "+e.getMessage());
			return;
		}

		String propsPath = cl.getOptionValue("p");
		if (propsPath == null) {
			throw new RuntimeException("No properties path set.");
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
				/*
				BondLengthsManager bond = new BondLengthsManager(propsFile);
				bond.execute();
				*/
			}
			if (cl.hasOption("smiles")) {
				SmilesListManager smi = new SmilesListManager(propsFile);
				smi.execute();
			}
			if (cl.hasOption("rss")) {
				RSSManager rss = new RSSManager(propsFile);
				rss.execute();
			}
		}
	}
	
	private void runall(File propsFile) {
		FetchManager fetch = new FetchManager(propsFile);
		fetch.run();
		Cif2CmlManager cif2Cml = new Cif2CmlManager(propsFile);
		cif2Cml.execute();
		CML2FooManager cml2Foo = new CML2FooManager(propsFile);
		cml2Foo.execute();
		Cml2RdfManager cml2rdf = new Cml2RdfManager(propsFile);
		cml2rdf.execute();
		WebpageManager webpage = new WebpageManager(propsFile);
		webpage.execute();
		DoiListManager dois = new DoiListManager(propsFile);
		dois.execute();
		CellParamsManager cell = new CellParamsManager(propsFile);
		cell.execute();
		/*
		BondLengthsManager bond = new BondLengthsManager(propsFile);
		bond.execute();
		*/
		SmilesListManager smi = new SmilesListManager(propsFile);
		smi.execute();
		RSSManager rss = new RSSManager(propsFile);
		rss.execute();
	}

}
