package wwmm.crystaleye.checkcif;

import static wwmm.crystaleye.CrystalEyeConstants.CC_NS;
import static wwmm.crystaleye.CrystalEyeConstants.XHTML_NS;
import static wwmm.crystaleye.CrystalEyeConstants.X_CC;
import static wwmm.crystaleye.CrystalEyeConstants.X_XHTML;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nu.xom.Attribute;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Node;
import nu.xom.Nodes;
import nu.xom.ParsingException;
import nu.xom.Text;
import nu.xom.ValidityException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * <p>
 * Parses the CheckCIF HTML created by the IUCr CheckCIF service
 * into an XML Document.  Note that those CheckCIFs returned by 
 * the CheckCIF service and those published alongside articles at 
 * Acta Cryst. are slightly different.  Thus, there are two public 
 * methods provided here, one for the published CheckCIFs 
 * (<code>parsePublished()</code>) and one for those returned by 
 * the service (<code>parseService()</code>).
 * </p> 
 * 
 * <p>
 * Also note that the structure of CheckCIF HTML is horrible, with
 * virtually no nesting and no semantic naming going on.  This is
 * why the code looks rather labyrinthine.  In fact, I can't even 
 * remember what half of it does.  But it works.  For now.  I hope
 * Jim doesn't read this.
 * </p>
 * 
 * @author Nick Day
 * @version 1.1
 */
public class CheckCifParser {
	
	private static final Logger LOG = Logger.getLogger(CheckCifParser.class);

	// the XML Document in which HTML returned from the CheckCIF service
	// is placed so that it can be parsed.
	private Document doc;
	// the input CheckCIF as a String
	private String checkCifHtml;
	
	// the HTML body element.  CheckCIF HTML is nasty and has next to 
	// no nesting. All the elements are children of <body> and so this 
	// element is important during parsing.  
	private Element body;
	// does the CheckCIF contain data for any datablocks.
	private boolean containsDataBlocks = false;
	// does the CheckCIF contain any publication errors.
	private boolean containsPublErrors = false;
	// does the CheckCIF contain any Platon information.
	private boolean containsPlaton = false;
	// array of ints corresponding to the child nodes of the HTML
	// <body> element at which the datablocks start.  An array is
	// used, as a CheckCIF may contain data on more than one crystal
	// structure, and hence more than one datablock (as in CIF, one
	// datablock = one crystal structure).
	private Integer[] dbPos;
	// int of the child node of the HTML <body> element at which the
	// publication errors start.
	private int pubPos;
	// int of the child node of the HTML <body> element at which the
	// platon information starts.
	private int platPos;

	/**
	 * <p>
	 * Creates a new instance of the <code>CheckCifParser</code>
	 * class.
	 * </p>
	 * 
	 * @param checkCifFile - file containing CheckCIF HTML
	 * 
	 * @throws IOException if there is an error reading the provided
	 * file into a <code>String</code>.
	 */
	public CheckCifParser(File checkCifFile) throws IOException {
		this.checkCifHtml = FileUtils.readFileToString(checkCifFile);
	}

	/**
	 * Parses CheckCIF HTML that has been published alongside an
	 * article in a journal published by Acta Crystallographica
	 * (see the table of contents for Acta Cryst. E for examples). 
	 * 
	 * @return an XML Document containing the data items from the
	 * provided CheckCIF.
	 */
	public Document parsePublished() {
		// need to remove all <pre> tags otherwise they appear almost at 
		// random and mess up all my lovely xpaths.
		Pattern p = Pattern.compile("<pre>|</pre>", Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(checkCifHtml);
		checkCifHtml = m.replaceAll("");
		setDocument();

		// this is the XML doc that will eventually be returned.
		Document checkcifXml = new Document(new Element("checkCif", CC_NS));
		Element deposited = new Element("deposited", CC_NS);

		Element alerts = new Element("alerts", CC_NS);
		Nodes nodes = this.doc.query("//x:a[contains(@href,'javascript:makeHelpWindow')]", X_XHTML);
		if (nodes.size() != 0) {
			for (int i = 0; i < nodes.size(); i++) {
				Element alert = new Element("alert", CC_NS);
				Node thisNode = nodes.get(i);
				String alertCode = thisNode.getValue();
				alert.addAttribute(new Attribute("code", alertCode));
				int j = thisNode.getParent().indexOf(thisNode);
				Element alertText = new Element("alertText", CC_NS);
				String text = thisNode.getParent().getChild(j+1).getValue().trim();
				alertText.appendChild(new Text(text));
				alert.appendChild(alertText);

				/* 
				 *   if there is an author comment along with the alert, then
				 *   extract that and add it to the XML
				 */
				Element tableTest = (Element)thisNode.getParent().getChild(j+2);
				if (tableTest != null) {
					if ("table".equalsIgnoreCase(tableTest.getLocalName())) {
						Node copy = tableTest.copy();
						Nodes responseNode = copy.query("/x:table/x:tr/x:td/x:font[contains(@color,'green')]", X_XHTML);
						if (responseNode != null) {
							String authorResponse = (responseNode.get(0).getValue()).substring(16).trim();
							Element response = new Element("authorResponse", CC_NS);
							response.appendChild(new Text(authorResponse));
							alert.appendChild(response);
						}
					}
				}
				alerts.appendChild(alert);
			}			
		} else {
			Element alert = new Element("alert", CC_NS);
			Text text = new Text("No errors found in this datablock");
			alert.appendChild(text);
			alerts.appendChild(alert);
		}
		deposited.appendChild(alerts);
		checkcifXml.getRootElement().appendChild(deposited);
		return checkcifXml;
	}
	
	/**
	 * Parses CheckCIF HTML that has been returned from the IUCr 
	 * CheckCIF service into an XML Document.
	 * 
	 * @return XML Document containing the data-items from the
	 * provided CheckCIF.
	 */
	public Document parseService() {
		setDocument();
		setBlockStartPositions();
		// this is the XML doc that will eventually be returned.
		Document checkcifXml = new Document(new Element("checkCif", CC_NS));
		Element calc = new Element("calculated", CC_NS);
		Element data;
		if (containsDataBlocks) {
			List<Document> dataDocs = this.getDatablocks();
			for (Document d : dataDocs) {
				data = new Element("dataBlock", CC_NS);
				Nodes nodes = d.query("/x:datablock/x:font[@size=\"+2\"]/x:b", X_XHTML);
				String blockId = (nodes.get(0).getValue()).substring(11);
				Element root = d.getRootElement();
				Element alerts = new Element("alerts", CC_NS);
				data.appendChild(alerts);

				if (d.getValue().contains("No errors found in this datablock")) {
					Element alert = new Element("alert", CC_NS);
					Text text = new Text("No errors found in this datablock");
					alert.appendChild(text);
					alerts.appendChild(alert);
				} else {
					//get alert elements and add them to the new checkcif document
					nodes = d.query("/x:datablock/x:a[contains(@href,'javascript:makeHelpWindow')]", X_XHTML);
					if (nodes.size() != 0) {
						data.addAttribute(new Attribute("id", blockId));
						for (int i = 0; i < nodes.size(); i++) {
							Element alert = new Element("alert", CC_NS);
							String alertCode = nodes.get(i).getValue();
							alert.addAttribute(new Attribute("code", alertCode));
							int j = root.indexOf(nodes.get(i));
							Element alertText = new Element("alertText", CC_NS);
							String text = root.getChild(j+1).getValue().trim();
							alertText.appendChild(new Text(text));
							alert.appendChild(alertText);
							alerts.appendChild(alert);
						}
					}

					// get non-alert elements and add them to the new checkcif document
					nodes = d.query("/x:datablock/x:table", X_XHTML);
					Element comparison = new Element("comparison", CC_NS);
					Node table;
					if (nodes.size() != 0 ) {
						// get bond precision value and add them
						table = nodes.get(0);
						table.detach();
						Nodes tts = table.query("/x:table/x:tr/x:td/x:tt", X_XHTML);
						Element bondPrecision = new Element("bondPrecision", CC_NS);
						bondPrecision.appendChild(new Text(tts.get(1).getValue().trim()));
						data.appendChild(bondPrecision);

						// get comparison values and add them
						table = nodes.get(2);
						table.detach();
						Nodes rows = table.query("/x:table/x:tr", X_XHTML);
						Node row;
						// start at 1 in the loop as the first row is just the headers for
						// the columns in the HTML
						for (int i = 1; i < rows.size(); i++) {
							row = rows.get(i);
							row.detach();
							tts = row.query("/x:tr/x:td/x:tt", X_XHTML);
							Element property = new Element("property", CC_NS);
							Attribute title = new Attribute("title", tts.get(0).getValue().trim());
							property.addAttribute(title);
							Element calculated = new Element("calculated", CC_NS);
							calculated.appendChild(new Text(tts.get(1).getValue().trim()));
							Element reported = new Element("reported", CC_NS);
							reported.appendChild((new Text(tts.get(2).getValue().trim())));
							property.appendChild(calculated);
							property.appendChild(reported);
							comparison.appendChild(property);
						}
						data.appendChild(comparison);

						// get data completeness value
						table = nodes.get(4);
						table.detach();
						tts = table.query("/x:table/x:tr/x:td/x:tt", X_XHTML);
						Element dataComp= new Element("dataCompleteness", CC_NS);
						dataComp.appendChild(new Text(tts.get(0).getValue().substring(18).trim()));
						data.appendChild(dataComp);
					}
				}
				calc.appendChild(data);
				checkcifXml.getRootElement().appendChild(calc);
			}
		}

		if (containsPublErrors) {
			//Document publDoc = this.getPublErrorDoc();
			// NYI
		}

		if (containsPlaton) {
			Document platonDoc = this.getPlatonDoc();
			Nodes nodes = platonDoc.query("//x:img[contains(@src,'.gif')]/@src", X_XHTML);
			Element platon = null;
			for (int i = 0; i < nodes.size(); i++) {
				platon = new Element("platon", CC_NS);
				String link = nodes.get(i).getValue();
				Matcher m = Pattern.compile("http://dynhost1.iucr.org/tmp/\\d*/platon_(.*)te.gif").matcher(link);
				String id = "";
				if (m.find()) {
					id = m.group(1);
				}
				Element plLink = new Element("link", CC_NS);
				plLink.appendChild(new Text(link));
				platon.appendChild(plLink);
				Nodes blockNode = checkcifXml.query("//c:dataBlock[@id=\"" + id + "\"]", X_CC);
				if (blockNode.size() != 0) {
					((Element)(blockNode.get(0))).appendChild(platon);
				}
			}
		}
		return checkcifXml;
	}
	
	/**
	 * Sets an instance variable to contain the a tidied version of the
	 * CheckCIF HTML returned by the CheckCIF service. 
	 * 
	 */
	private void setDocument() {
		try {
			XMLReader tagsoup = XMLReaderFactory.createXMLReader("org.ccil.cowan.tagsoup.Parser");
			Builder builder = new Builder(tagsoup);
			this.doc = builder.build(new BufferedReader(new StringReader(checkCifHtml)));
			setBodyElement();
		} catch (SAXException e) {
			throw new RuntimeException("Error reading CheckCIF XML.", e);
		} catch (ValidityException e) {
			throw new RuntimeException("Checkcif HTML string is not valid XML.", e);
		} catch (ParsingException e) {
			throw new RuntimeException("Could not parse Checkcif HTML string.", e);
		} catch (IOException e) {
			throw new RuntimeException("Could not read Checkcif HTML string.", e);
		}
	}

	/**
	 * Simply finds the HTML body element in the CheckCIF HTML returned
	 * by the CheckCIF service and sets an instance variable for it.  The
	 * body element is important for parsing, as the sections of interest
	 * are direct children of the body element.
	 * 
	 */
	private void setBodyElement() {
		Nodes nodes = doc.query("/x:html/x:body", X_XHTML);
		if (nodes.size() != 0) {
			body = (Element) nodes.get(0);
		} else {
			throw new RuntimeException("ERROR: could not find body element in Checkcif HTML.");
		}
	}

	/**
	 * Go through the CheckCIF HTML returned by the CheckCIF service and set 
	 * instance variable to point to the starts of the various sections of
	 * interest.  These are the starting positions for:
	 * 
	 * 1. the data for each datablock (dbPos).
	 * 2. the publication errors (pubPos).
	 * 3. the platon information (platPos).
	 * 
	 */
	private void setBlockStartPositions() {
		Nodes syntaxError = doc.query("//x:h2[text()='Syntax problems']", X_XHTML);
		if (syntaxError.size() == 0) {
			Nodes dataStartPos = doc.query("/x:html/x:body/x:font[@size='+2']/x:b[contains(text(),'Datablock:')]/parent::x:*", X_XHTML);
			dbPos = new Integer[dataStartPos.size()];
			if (dataStartPos.size() != 0) {
				for (int i = 0; i < dataStartPos.size();i++) {
					containsDataBlocks = true;
					int pos = body.indexOf(dataStartPos.get(i));
					dbPos[i] = pos;
				}
			} else {
				throw new RuntimeException("No datablocks found in this checkCIF");
			}

			Nodes publStartPos = doc.query("/x:html/x:body/x:font[@size=\"+2\"]/x:b[contains(text(),'checkCIF publication errors')]/parent::x:*", X_XHTML);
			if (publStartPos.size() != 0) {
				pubPos = body.indexOf(publStartPos.get(0));
				containsPublErrors = true;
			} else {
				LOG.info("No publication errors reported in this checkCIF");
			}

			Nodes platonStartPos = doc.query("/x:html/x:body/x:font/x:b[contains(text(),'PLATON version')]/parent::x:*", X_XHTML);
			if (platonStartPos.size() != 0) {
				platPos = body.indexOf(platonStartPos.get(0));
				containsPlaton = true;
			} else {
				LOG.info("No PLATON results reported in this checkCIF");
			}
		} else {
			LOG.warn("Syntax errors in the CIF, no checkCIF/PLATON output can be produced.");
		}
	}

	/**
	 * Goes through the CheckCIF HTML returned by the CheckCIF service
	 * and creates and returns XML <code>Document</code>s for the data
	 * about each datablock. 
	 * 
	 * @return a list of XML <code>Document</code>s, where each contains
	 * the information for a separate datablock in the CheckCIF.
	 */
	private List<Document> getDatablocks() {
		List<Document> datablockList = new ArrayList<Document>();
		if (dbPos.length>1) {
			for (int i = 0; i < dbPos.length-1; i++) {
				int start = dbPos[i];
				int end = dbPos[i+1];
				Document newDoc1 = new Document(new Element("datablock", XHTML_NS));
				for (int j = start; j < end; j++) {
					Node n = body.getChild(j).copy();
					newDoc1.getRootElement().appendChild(n);
				}
				datablockList.add(newDoc1);
			}
		}
		Document newDoc2 = new Document(new Element("datablock", XHTML_NS));
		if (containsPublErrors) {
			for (int j = dbPos[dbPos.length-1]; j < pubPos; j++) {
				Node n = body.getChild(j).copy();
				newDoc2.getRootElement().appendChild(n);
			}
		} else if (containsPlaton) {
			for (int j = dbPos[dbPos.length-1]; j < platPos; j++) {
				Node n = body.getChild(j).copy();
				newDoc2.getRootElement().appendChild(n);
			}
		} else {
			for (int j = dbPos[dbPos.length-1]; j < body.getChildCount(); j++) {
				Node n = body.getChild(j).copy();
				newDoc2.getRootElement().appendChild(n);
			}
		}
		datablockList.add(newDoc2);	

		return datablockList;
	}

	/**
	 * Gets the publication errors section from the CheckCIF.
	 * 
	 * @return an XML <code>Document</code> containing the publication
	 * errors section.
	 */
	@SuppressWarnings("unused")
	private Document getPublErrorDoc() {
		Document publErrorDoc = new Document(new Element("publErrorBlock", XHTML_NS));
		if (containsPlaton) {
			for (int j = pubPos; j < platPos; j++) {
				Node n = body.getChild(j).copy();
				publErrorDoc.getRootElement().appendChild(n);
			}
		} else {
			for (int j = pubPos; j < body.getChildCount(); j++) {
				Node n = body.getChild(j).copy();
				publErrorDoc.getRootElement().appendChild(n);
			}
		}
		return publErrorDoc;
	}

	/**
	 * Gets the Platon information from the CheckCIF.
	 * 
	 * @return an XML <code>Document</code> containing the Platon
	 * information section.
	 */
	private Document getPlatonDoc() {
		Document platonDoc = new Document(new Element("platonBlock", XHTML_NS));
		for (int j = platPos; j < body.getChildCount(); j++) {
			Node n = body.getChild(j).copy();
			platonDoc.getRootElement().appendChild(n);
		}

		return platonDoc;
	}

}
