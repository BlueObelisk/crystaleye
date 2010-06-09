package wwmm.crystaleye.tools;

import static org.xmlcml.cml.base.CMLConstants.CML_XPATH;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import nu.xom.Node;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.xmlcml.cml.base.CMLBuilder;
import org.xmlcml.cml.base.CMLUtil;
import org.xmlcml.cml.element.CMLFormula;
import org.xmlcml.cml.element.CMLIdentifier;
import org.xmlcml.cml.element.CMLMolecule;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

public class MoietyCml2RdfTool {
	
	public static final String CHEMAXIOM_NS = "http://www.polymerinformatics.com/ChemAxiom/ChemDomain.owl#";
	public static final String CML_CORE_RDF_SCHEMA_NS = "http://www.xmlcml.org/rdf-schema#";
	public static final String DC_NS = "http://purl.org/dc/elements/1.1/";
	public static final String DCTERMS_NS = "http://purl.org/dc/terms/";
	public static final String IUCR_RDF_SCHEMA_NS = "http://www.iucr.org/cif-rdf-schema#";
	public static final String IUPAC_RDF_SCHEMA_NS = "http://www.iupac.org/rdf-schema#";
	public static final String OCO_NS = "http://www.openarchives.org/ore/chem/experiments/";
	public static final String OPENBABEL_RDF_SCHEMA_NS = "http://openbabel.org/rdf-schema#";
	public static final String ORE_NS = "http://www.openarchives.org/ore/terms/";
	public static final String RDF_NS = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	public static final String UUID_NS = "urn:uuid:"+UUID.randomUUID().toString()+"#";
	public static final String XSD_NS = "http://www.w3.org/2001/XMLSchema#";

	private Property rdfTypeProperty, cmlIdentifierProperty, cmlValueProperty, cmlFormulaProperty, cmlRepresentedByProperty, 
	oreDescribesProperty, oreIsDescribedByProperty, dcRightsProperty, dctermsFormatProperty, dctermsCreatorProperty, oreAggregatesProperty,
	dctermsSourceProperty;
	private Resource chemMoiety, oreResourceMap, oreAggregation, crystaleyeResource;
	
	protected Model model;

	public MoietyCml2RdfTool() {
		;
	}

	private void initialiseProperties() {
		rdfTypeProperty = createProperty(RDF_NS+"type");
		cmlValueProperty = createProperty(CML_CORE_RDF_SCHEMA_NS+"value");
		cmlIdentifierProperty = createProperty(CML_CORE_RDF_SCHEMA_NS+"identifier");
		cmlFormulaProperty = createProperty(CML_CORE_RDF_SCHEMA_NS+"formula");
		cmlRepresentedByProperty = createProperty(CML_CORE_RDF_SCHEMA_NS+"representedBy");
		oreDescribesProperty = createProperty(ORE_NS+"describes");
		oreIsDescribedByProperty = createProperty(ORE_NS+"isDescribedBy");
		dcRightsProperty = createProperty(DC_NS+"rights");
		dctermsFormatProperty = createProperty(DCTERMS_NS+"format");
		dctermsCreatorProperty = createProperty(DCTERMS_NS+"creator");
		oreAggregatesProperty = createProperty(ORE_NS+"aggregates");
		dctermsSourceProperty = createProperty(DCTERMS_NS+"source");
	}

	private void initialiseResources() {
		chemMoiety = createResource(CHEMAXIOM_NS+"MolecularEntity");
		oreResourceMap = createResource(ORE_NS+"ResourceMap");
		oreAggregation = createResource(ORE_NS+"Aggregation");
		crystaleyeResource = createResource("http://wwmm.ch.cam.ac.uk/crystaleye");
	}

	private void setNamespacePrefixes() {
		setNamespacePrefix("cml", CML_CORE_RDF_SCHEMA_NS);	
		setNamespacePrefix("chem", CHEMAXIOM_NS);
		setNamespacePrefix("xsd", XSD_NS);
		setNamespacePrefix("dc", DC_NS);
		setNamespacePrefix("dcterms", DCTERMS_NS);
		setNamespacePrefix("iupac", IUPAC_RDF_SCHEMA_NS);
		setNamespacePrefix("openbabel", OPENBABEL_RDF_SCHEMA_NS);
		setNamespacePrefix("ore", ORE_NS);
	}
	
	private void init() {
		model = ModelFactory.createDefaultModel();
		initialiseProperties();
		initialiseResources();
		setNamespacePrefixes();
	}

	public void convert(File infile, File outfile, String infileUrl, String outfileUrl) throws ValidityException, ParsingException, IOException {
		init();
		
		CMLMolecule molecule = (CMLMolecule)new CMLBuilder().build(infile).getRootElement();

		String moietyUrl = FilenameUtils.getPathNoEndSeparator(infileUrl);
		String name = FilenameUtils.getName(moietyUrl);
		String pngUrl = moietyUrl+"/"+name+".png";
		Resource moietyAggregation = createResource(moietyUrl);
		Resource cmlResource = createResource(infileUrl);
		cmlResource.addProperty(dctermsFormatProperty, "chemical/x-cml");
		moietyAggregation.addProperty(oreAggregatesProperty, cmlResource);
		Resource pngResource = createResource(pngUrl);
		moietyAggregation.addProperty(oreAggregatesProperty, pngResource);
		pngResource.addProperty(dctermsFormatProperty, "image/png");
		moietyAggregation.addProperty(rdfTypeProperty, chemMoiety);
		moietyAggregation.addProperty(cmlRepresentedByProperty, cmlResource);
		moietyAggregation.addProperty(cmlRepresentedByProperty, pngResource);
		moietyAggregation.addProperty(rdfTypeProperty, oreAggregation);
		addIdentifiers(moietyAggregation, molecule);
		addFormula(moietyAggregation, molecule);

		Resource rem = createResource(outfileUrl);
		rem.addProperty(oreDescribesProperty, moietyUrl);
		rem.addProperty(dcRightsProperty, "To the extent possible under law, the University of Cambridge has waived all copyright and related or neighboring rights to this work. This work is published from United Kingdom.");
		rem.addProperty(rdfTypeProperty, oreResourceMap);
		rem.addProperty(dctermsCreatorProperty, crystaleyeResource);
		moietyAggregation.addProperty(oreIsDescribedByProperty, rem);

		String parentCifUrl = createParentCifUrl(moietyUrl);
		Resource parentCifResource = createResource(parentCifUrl);
		moietyAggregation.addProperty(dctermsSourceProperty, parentCifResource);

		String doi = "info:doi/"+getDoi(molecule);
		if (doi != null) {
			Resource doiResource = createResource(doi);
			parentCifResource.addProperty(dctermsSourceProperty, doiResource);
		}

		FileWriter fw = null;
		try {
			fw = new FileWriter(outfile);
			model.write(fw, "N3");
		} finally {
			IOUtils.closeQuietly(fw);
		}
	}

	private String getDoi(CMLMolecule molecule) {
		List<Node> scalars = CMLUtil.getQueryNodes(molecule, ".//cml:scalar[@dictRef='idf:doi']", CML_XPATH);
		if (scalars.size() > 0) {
			Node nd = scalars.get(0);
			return nd.getValue();
		} else {
			return null;
		}
	}

	private String createParentCifUrl(String moietyUrl) {
		System.out.println(moietyUrl);
		int idx = moietyUrl.indexOf("/moieties");
		String ss = moietyUrl.substring(0, idx);
		String splitCifName = FilenameUtils.getName(ss);
		int uidx = splitCifName.indexOf("_");
		String cifId = splitCifName.substring(0, uidx);
		String url = FilenameUtils.getPathNoEndSeparator(ss)+"/"+cifId+".cif";
		return url;
	}

	private void addFormula(Resource thisMoiety, CMLMolecule molecule) {
		CMLFormula formula = (CMLFormula)molecule.getFirstCMLChild(CMLFormula.TAG);
		if (formula != null) {
			String concise = formula.getConcise();
			Resource formulaBNode = model.createResource();
			thisMoiety.addProperty(cmlFormulaProperty, formulaBNode);
			formulaBNode.addProperty(rdfTypeProperty, model.createResource(CML_CORE_RDF_SCHEMA_NS+"ConciseFormula"));
			formulaBNode.addProperty(cmlValueProperty, model.createTypedLiteral(concise, XSDDatatype.XSDstring));
		}
	}

	private void addIdentifiers(Resource thisMoiety, CMLMolecule molecule) {
		List<Node> identifiers = CMLUtil.getQueryNodes(molecule, ".//cml:identifier", CML_XPATH);
		for (Node node : identifiers) {
			CMLIdentifier idr = (CMLIdentifier)node;
			String convention = idr.getConvention();
			Resource identifierBNode = model.createResource();
			thisMoiety.addProperty(cmlIdentifierProperty, identifierBNode);
			if ("iupac:inchi".equals(convention)) {
				identifierBNode.addProperty(rdfTypeProperty, model.createResource(IUPAC_RDF_SCHEMA_NS+"InChI"));
			} else if ("daylight:smiles".equals(convention)) {
				identifierBNode.addProperty(rdfTypeProperty, model.createResource(OPENBABEL_RDF_SCHEMA_NS+"SMILES"));
			}
			identifierBNode.addProperty(cmlValueProperty, model.createTypedLiteral(idr.getValue(), XSDDatatype.XSDstring));
		}
	}
	
	protected Property createProperty(String url) {
		 return model.createProperty(url);
	}
	
	protected Resource createResource(String url) {
		 return model.createResource(url);
	}
	
	protected void setNamespacePrefix(String prefix, String nsUrl) {
		model.setNsPrefix(prefix, nsUrl);
	}

	public static void main(String[] args) throws ValidityException, ParsingException, IOException {
		String inpath = "C:/workspace/crystaleye-trunk-data/www/crystaleye/summary/acta/c/2009/07-00/data/eg3019/eg3019sup1_I/moieties/eg3019sup1_I_moiety_1/eg3019sup1_I_moiety_1.complete.cml.xml";
		String inurl = "http://wwmm.ch.cam.ac.uk/crystaleye/summary/acta/c/2009/07-00/data/eg3019/eg3019sup1_I/moieties/eg3019sup1_I_moiety_1/eg3019sup1_I_moiety_1.complete.cml.xml";
		String outpath = "C:/workspace/crystaleye-trunk-data/www/crystaleye/summary/acta/c/2009/07-00/data/eg3019/eg3019sup1_I/moieties/eg3019sup1_I_moiety_1/eg3019sup1_I_moiety_1.n3";
		String outurl = "http://wwmm.ch.cam.ac.uk/crystaleye/summary/acta/c/2009/07-00/data/eg3019/eg3019sup1_I/moieties/eg3019sup1_I_moiety_1/eg3019sup1_I_moiety_1.n3";
		File infile = new File(inpath);
		File outfile = new File(outpath);

		MoietyCml2RdfTool tool = new MoietyCml2RdfTool();
		tool.convert(infile, outfile, inurl, outurl);
	}

}
