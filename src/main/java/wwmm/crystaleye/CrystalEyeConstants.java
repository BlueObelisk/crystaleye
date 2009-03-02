package wwmm.crystaleye;

import nu.xom.XPathContext;

public interface CrystalEyeConstants {
	
	public static final String CRYSTALEYE_HOME_URL = "http://wwmm.ch.cam.ac.uk/crystaleye";
	
	public static final String CRYSTALEYE_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";	
	
	// DOI prefixes for publishers
	public static final String CHEMSOCJAPAN_DOI_PREFIX = "10.1246";
	public static final String RSC_DOI_PREFIX = "10.1039";
	public static final String ACS_DOI_PREFIX = "10.1021";
	public static final String ELSEVIER_DOI_PREFIX = "10.1016";
	
	// general namespaces
	public static final String CC_NS = "http://journals.iucr.org/services/cif";
	public static final String NED24_NS = "http://wwmm.ch.cam.ac.uk/ned24/";
	public static final String XHTML_NS = "http://www.w3.org/1999/xhtml";
	public static final String XLINK_NS = "http://www.w3.org/1999/xlink";
	public static final String SVG_NS = "http://www.w3.org/2000/svg";
	
	// RSS/Atom namespaces
	public static final String ATOM_1_NS = "http://www.w3.org/2005/Atom";
	public static final String DC_NS = "http://purl.org/dc/elements/1.1/";
	public static final String RDF_NS = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	
	// contexts for use in XPath queries
	public static final XPathContext X_CC = new XPathContext("c", CC_NS);
	public static final XPathContext X_NED24 = new XPathContext("n", NED24_NS);
	public static final XPathContext X_XHTML = new XPathContext("x", XHTML_NS);
	public static final XPathContext X_SVG = new XPathContext("svg", SVG_NS);
	// for RSS/Atom
	public static final XPathContext X_ATOM1 = new XPathContext("atom1", ATOM_1_NS);
	public static final XPathContext X_DC = new XPathContext("dc", DC_NS);
	public static final XPathContext X_RDF = new XPathContext("rdf", RDF_NS);
	
	// MIME types used in CrystalEye
	public static final String COMPLETE_CML_MIME = ".complete.cml.xml";
	public static final String CIF_HTML_SUMMARY_MIME = ".cif.summary.html";
	public static final String RAW_CML_MIME = ".raw.cml.xml";
	public static final String CSV_MIME = ".csv";
	public static final String SVG_MIME = ".svg";
	public static final String HTML_MIME = ".html";
	public static final String CIF_MIME = ".cif";
	public static final String DOI_MIME = ".doi";
	public static final String DATE_MIME = ".date";
	public static final String TITLE_MIME = ".title";
	public static final String PNG_MIME = ".png";
	public static final String SMALL_PNG_MIME = ".small.png";
	public static final String PLATON_MIME = ".platon.jpeg";
	
	// flag dictRefs
	public static final String POLYMERIC_FLAG_DICTREF = "ned24:isPolymeric";
	public static final String NO_BONDS_OR_CHARGES_FLAG_DICTREF = "ned24:noBondsOrChargesSet";
	
	// names of the different types of RSS/Atom feeds generated
	// by CrystalEye
	public static final String RSS_ALL_DIR_NAME = "all";
	public static final String RSS_JOURNAL_DIR_NAME = "journal";
	public static final String RSS_ATOMS_DIR_NAME = "atoms";
	public static final String RSS_BOND_DIR_NAME = "bonds";
	public static final String RSS_CLASS_DIR_NAME = "class";
	
	public static final String RSS_DESC_VALUE_PREFIX = "CrystalEye summary of ";
	public static final String CMLRSS_DESC_VALUE_PREFIX = "CrystalEye CMLRSS summary of ";
	public static final String RSS_DIR_NAME = "rss";
	public static final String CMLRSS_DIR_NAME = "cmlrss";
	
	// default RSS/Atom feed name
	public static final String FEED_FILE_NAME = "feed.xml";
	
	// miscellaneous
	public static final int MAX_CIF_SIZE_IN_BYTES = 2621440;
	public static final String NEWLINE = System.getProperty("line.separator");
}
