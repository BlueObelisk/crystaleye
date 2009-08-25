package wwmm.crystaleye;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.xmlcml.cml.base.CMLConstants;

import wwmm.crystaleye.fetch.FetchManager;
import wwmm.crystaleye.process.CML2FooManager;
import wwmm.crystaleye.process.Cif2CmlManager;
import wwmm.crystaleye.process.Cml2RdfManager;
import wwmm.crystaleye.site.CellParamsManager;
import wwmm.crystaleye.site.DoiListManager;
import wwmm.crystaleye.site.RSSManager;
import wwmm.crystaleye.site.SmilesListManager;
import wwmm.crystaleye.site.WebpageManager;

public class Update {

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
			if (cl.hasOption("cml2rdf")) {
				Cml2RdfManager cml2rdf = new Cml2RdfManager(propsPath);
				cml2rdf.execute();
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
			if (cl.hasOption("rss")) {
				RSSManager rss = new RSSManager(propsPath);
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
		Cml2RdfManager cml2rdf = new Cml2RdfManager(propsPath);
		cml2rdf.execute();
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
		RSSManager rss = new RSSManager(propsPath);
		rss.execute();
	}

}
