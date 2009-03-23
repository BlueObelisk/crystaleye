package uk.ac.cam.ch.crystaleye.process;

import static uk.ac.cam.ch.crystaleye.CrystalEyeConstants.CC_NS;
import static uk.ac.cam.ch.crystaleye.CrystalEyeConstants.XHTML_NS;
import static uk.ac.cam.ch.crystaleye.CrystalEyeConstants.X_CC;
import static uk.ac.cam.ch.crystaleye.CrystalEyeConstants.X_XHTML;

import java.io.BufferedReader;
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

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public class CheckCifParser {
	
	private static final Logger LOG = Logger.getLogger(CheckCifParser.class);

	Document doc;
	String checkCifHtml;

	Element body;

	boolean containsDataBlocks = false;
	boolean containsPublErrors = false;
	boolean containsPlaton = false;

	Integer[] dbPos;
	int pubPos;
	int platPos;

	public CheckCifParser(String checkCifHtml) {
		this.checkCifHtml = checkCifHtml;
	}

	public Document parseDeposited() {

		// need to remove all <pre> tags otherwise they appear almost at random and mess up
		// all my lovely xpaths.
		Pattern p = Pattern.compile("<pre>|</pre>", Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(checkCifHtml);
		checkCifHtml = m.replaceAll("");
		setDocument();

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

	protected void setDocument() {
		try {
			XMLReader tagsoup = XMLReaderFactory.createXMLReader("org.ccil.cowan.tagsoup.Parser");
			Builder builder = new Builder(tagsoup);
			this.doc = builder.build(new BufferedReader(new StringReader(checkCifHtml)));
			setBodyElement();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (ValidityException e) {
			System.err.println("Checkcif HTML string is not valid XML: "+e);
		} catch (ParsingException e) {
			System.err.println("Could not parse Checkcif HTML string: "+e);
		} catch (IOException e) {
			System.err.println("Could not read Checkcif HTML string: "+e);
		}
	}

	public Document parseCalculated() {
		setDocument();
		setBlockStartPositions();
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
					//platon.addAttribute(new Attribute("blockId", id));
				}
				Element plLink = new Element("link", CC_NS);
				plLink.appendChild(new Text(link));
				platon.appendChild(plLink);
				//checkcifXml.getRootElement().appendChild(platon);
				Nodes blockNode = checkcifXml.query("//c:dataBlock[@id=\"" + id + "\"]", X_CC);
				if (blockNode.size() != 0) {
					((Element)(blockNode.get(0))).appendChild(platon);
				}
			}
		}
		return checkcifXml;
	}

	private void setBodyElement() {
		Nodes nodes = doc.query("/x:html/x:body", X_XHTML);
		if (nodes.size() != 0) {
			body = (Element) nodes.get(0);
		} else {
			System.err.println("ERROR: could not find body element in Checkcif HTML.");
		}
	}

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
				System.err.println("No datablocks found in this checkCIF");
			}

			Nodes publStartPos = doc.query("/x:html/x:body/x:font[@size=\"+2\"]/x:b[contains(text(),'checkCIF publication errors')]/parent::x:*", X_XHTML);
			if (publStartPos.size() != 0) {
				pubPos = body.indexOf(publStartPos.get(0));
				//logger.info("PublicationError block start position at node: "+pubPos);
				containsPublErrors = true;
			} else {
				LOG.info("No publication errors reported in this checkCIF");
			}

			Nodes platonStartPos = doc.query("/x:html/x:body/x:font/x:b[contains(text(),'PLATON version')]/parent::x:*", X_XHTML);
			if (platonStartPos.size() != 0) {
				platPos = body.indexOf(platonStartPos.get(0));
				//logger.info("PLATON block start position at node: "+platPos);
				containsPlaton = true;
			} else {
				LOG.info("No PLATON results reported in this checkCIF");
			}
		} else {
			System.err.println("Syntax errors in the CIF, no checkCIF/PLATON output can be produced.");
		}
	}

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

	private Document getPlatonDoc() {
		Document platonDoc = new Document(new Element("platonBlock", XHTML_NS));
		for (int j = platPos; j < body.getChildCount(); j++) {
			Node n = body.getChild(j).copy();
			platonDoc.getRootElement().appendChild(n);
		}

		return platonDoc;

	}

}
