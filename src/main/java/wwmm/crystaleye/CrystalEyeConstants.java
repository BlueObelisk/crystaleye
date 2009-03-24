package wwmm.crystaleye;

import nu.xom.XPathContext;

public interface CrystalEyeConstants {
	
	// general namespaces and prefixes
	public static final String CRYSTALEYE_NS = "http://wwmm.ch.cam.ac.uk/crystaleye/";
	public static final String CRYSTALEYE_PREFIX = "ce";
	public static final String XHTML_NS = "http://www.w3.org/1999/xhtml";
	public static final String ATOM_1_NS = "http://www.w3.org/2005/Atom";
	public static final String DC_NS = "http://purl.org/dc/elements/1.1/";
	public static final String DC_PREFIX = "dc";
	public static final String RDF_NS = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	public static final String RDF_PREFIX = "rdf";
	public static final String BIBO_NS = "http://purl.org/ontology/bibo/";
	public static final String BIBO_PREFIX = "bibo";
	
	// contexts for use in XPath queries
	public static final XPathContext X_XHTML = new XPathContext("x", XHTML_NS);
	public static final XPathContext X_ATOM1 = new XPathContext("atom1", ATOM_1_NS);
	public static final XPathContext X_DC = new XPathContext(DC_PREFIX, DC_NS);
	public static final XPathContext X_RDF = new XPathContext(RDF_PREFIX, RDF_NS);
	
	public static final String CIF_CONTENT_TYPE = "chemical/x-cif";
	
}
