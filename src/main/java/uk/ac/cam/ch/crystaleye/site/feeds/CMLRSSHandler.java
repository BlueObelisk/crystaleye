package uk.ac.cam.ch.crystaleye.site.feeds;

import static uk.ac.cam.ch.crystaleye.CrystalEyeConstants.ATOM_1_NS;
import static uk.ac.cam.ch.crystaleye.CrystalEyeConstants.RDF_NS;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.NodeFactory;
import nu.xom.ParsingException;
import nu.xom.Text;
import nux.xom.io.StaxParser;
import nux.xom.io.StaxUtil;
import nux.xom.io.StreamingSerializer;
import nux.xom.io.StreamingSerializerFactory;
import uk.ac.cam.ch.crystaleye.Utils;
import uk.ac.cam.ch.crystaleye.site.feeds.CMLRSSEntry.FeedType;

public class CMLRSSHandler {

	String feedPath;
	FeedType feedType;
	List<CMLRSSEntryDetails> credList;

	private String tempPath = feedPath+".tmp";

	private CMLRSSHandler() {
		;
	}

	public CMLRSSHandler(String feedPath, FeedType feedType, List<CMLRSSEntryDetails> credList) {
		this.feedPath = feedPath;
		this.feedType = feedType;
		this.credList = credList;
	}

	public CMLRSSHandler(String feedPath, FeedType feedType, CMLRSSEntryDetails cred) {
		this.feedPath = feedPath;
		this.feedType = feedType;
		credList = new LinkedList<CMLRSSEntryDetails>();
		credList.add(cred);
	}

	public void addEntries() {
		if (feedPath == null || feedType == null || credList == null) {
			throw new IllegalArgumentException("Must provide a feed path, type and entry details before calling addEntry()");
		}
		if (feedType.equals(FeedType.ATOM_1)) {
			addAtom1Entries();
		} else if (feedType.equals(FeedType.RSS_2)) {
			addRss2Entries();
		} else if (feedType.equals(FeedType.RSS_1)) {
			addRss1Entries();
		} else {
			throw new RuntimeException("Should never reach here.");
		}
	}

	private void addAtom1Entries() {
		StreamingSerializerFactory factory = new StreamingSerializerFactory();
		InputStream in = null;
		FileOutputStream out = null;
		XMLStreamReader reader = null;
		try {
			out = new FileOutputStream(tempPath);
			StreamingSerializer ser = factory.createXMLSerializer(out, "UTF-8");
			ser.writeXMLDeclaration();
			in = new FileInputStream(feedPath);
			reader = StaxUtil.createXMLStreamReader(in, null);
			reader.require(XMLStreamConstants.START_DOCUMENT, null, null);
			reader.nextTag();
			reader.require(XMLStreamConstants.START_ELEMENT, null, "feed");
			ser.writeStartTag((Element)new StaxParser(reader, new NodeFactory()).buildNode());
			while (reader.nextTag() == XMLStreamConstants.START_ELEMENT) {
				boolean write = true;
				if (reader.getLocalName().equals("updated")) {
					Date date = new Date();
					SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
					String dNow = formatter.format(date);
					dNow += "+00:00";
					Element updated = new Element("updated", ATOM_1_NS);
					updated.appendChild(new Text(dNow));
					ser.write(updated);
					write = false;
				}
				Document fragment = new StaxParser(reader, new NodeFactory()).buildFragment();
				if (write) {
					ser.write(fragment.getRootElement());  
				}
				if (reader.getLocalName().equals("id")) break;
			}
			for (CMLRSSEntryDetails cred : credList) {
				ser.write(new CMLRSSEntry(cred).createAtom1Entry());
			}
			while (reader.nextTag() == XMLStreamConstants.START_ELEMENT) {
				Document fragment = new StaxParser(reader, new NodeFactory()).buildFragment();
				ser.write(fragment.getRootElement());  
			}
			ser.writeEndDocument();

			out.close();
			in.close();
			reader.close();
		} catch (FileNotFoundException e) {
			throw new RuntimeException("Could not find file: "+feedPath);
		} catch (XMLStreamException e) {
			throw new RuntimeException("Error reading XML in file: "+feedPath);
		} catch (IOException e) {
			throw new RuntimeException("Error reading XML in file: "+feedPath);
		} catch (ParsingException e) {
			throw new RuntimeException("Error reading XML in file: "+feedPath);
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					throw new RuntimeException("Could not close outputstream: "+out);
				}
			}
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					throw new RuntimeException("Could not close inputstream: "+in);
				}
			}
			if (reader != null) {
				try {
					reader.close();
				}catch (XMLStreamException e) {
					throw new RuntimeException("Could not close XML reader: "+reader);
				}
			}
		}
		Utils.copyFile(tempPath, feedPath);
		new File(tempPath).delete();
	}

	private void addRss1Entries() {		
		StreamingSerializerFactory factory = new StreamingSerializerFactory();
		InputStream in = null;
		FileOutputStream out = null;
		XMLStreamReader reader = null;
		try {
			out = new FileOutputStream(tempPath);
			StreamingSerializer ser = factory.createXMLSerializer(out, "UTF-8");
			ser.writeXMLDeclaration();
			in = new FileInputStream(feedPath);
			reader = StaxUtil.createXMLStreamReader(in, null);
			reader.require(XMLStreamConstants.START_DOCUMENT, null, null);
			reader.nextTag();
			reader.require(XMLStreamConstants.START_ELEMENT, null, "RDF");
			ser.writeStartTag((Element)new StaxParser(reader, new NodeFactory()).buildNode());
			reader.nextTag();
			reader.require(XMLStreamConstants.START_ELEMENT, null, "channel");
			ser.writeStartTag((Element)new StaxParser(reader, new NodeFactory()).buildNode());
			while (reader.nextTag() == XMLStreamConstants.START_ELEMENT) {
				boolean write = true;
				boolean build = true;
				if (reader.getLocalName().equals("items")) {
					Element items = new StaxParser(reader, new NodeFactory()).buildFragment().getRootElement();
					Element seq = (Element)items.getFirstChildElement("Seq", RDF_NS);
					for (CMLRSSEntryDetails cred : credList) {
						Element li = new Element("li", RDF_NS);
						seq.insertChild(li, 0);
						li.addAttribute(new Attribute("resource", cred.getHtmlLink()));
					}
					ser.write(items);
					write = false;
					build = false;
				}
				if (build) {
					Document fragment = new StaxParser(reader, new NodeFactory()).buildFragment();
					if (write) {
						ser.write(fragment.getRootElement());  
					} 
				}  
			} 
			ser.writeEndTag();
			for (CMLRSSEntryDetails cred : credList) {
				ser.write(new CMLRSSEntry(cred).createRss1Entry());
			}
			while (reader.nextTag() == XMLStreamConstants.START_ELEMENT) {
				Document fragment = new StaxParser(reader, new NodeFactory()).buildFragment();
				ser.write(fragment.getRootElement());  
			}
			ser.writeEndDocument();

			out.close();
			in.close();
			reader.close();
		} catch (FileNotFoundException e) {
			throw new RuntimeException("Could not find file: "+feedPath);
		} catch (XMLStreamException e) {
			e.printStackTrace();
			throw new RuntimeException("Error reading XML in file: "+feedPath);
		} catch (IOException e) {
			throw new RuntimeException("Error reading XML in file: "+feedPath);
		} catch (ParsingException e) {
			throw new RuntimeException("Error reading XML in file: "+feedPath);
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					throw new RuntimeException("Could not close outputstream: "+out);
				}
			}
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					throw new RuntimeException("Could not close inputstream: "+in);
				}
			}
			if (reader != null) {
				try {
					reader.close();
				}catch (XMLStreamException e) {
					throw new RuntimeException("Could not close XML reader: "+reader);
				}
			}
		}
		Utils.copyFile(tempPath, feedPath);
		new File(tempPath).delete();
	}	

	private void addRss2Entries() {
		StreamingSerializerFactory factory = new StreamingSerializerFactory();
		InputStream in = null;
		FileOutputStream out = null;
		XMLStreamReader reader = null;
		try {
			out = new FileOutputStream(tempPath);
			StreamingSerializer ser = factory.createXMLSerializer(out, "UTF-8");
			ser.writeXMLDeclaration();
			in = new FileInputStream(feedPath);
			reader = StaxUtil.createXMLStreamReader(in, null);
			reader.require(XMLStreamConstants.START_DOCUMENT, null, null);
			reader.nextTag();
			reader.require(XMLStreamConstants.START_ELEMENT, null, "rss");
			ser.writeStartTag((Element)new StaxParser(reader, new NodeFactory()).buildNode());
			reader.nextTag();
			reader.require(XMLStreamConstants.START_ELEMENT, null, "channel");
			ser.writeStartTag((Element)new StaxParser(reader, new NodeFactory()).buildNode());
			while (reader.nextTag() == XMLStreamConstants.START_ELEMENT && !reader.getLocalName().equals("item")) {
				Document fragment = new StaxParser(reader, new NodeFactory()).buildFragment();
				ser.write(fragment.getRootElement());   
			}
			for (CMLRSSEntryDetails cred : credList) {
				ser.write(new CMLRSSEntry(cred).createRss2Entry());
			}
			if (reader.getLocalName().equals("item")) {
				Document fragment = new StaxParser(reader, new NodeFactory()).buildFragment();
				ser.write(fragment.getRootElement());
			}
			while (reader.nextTag() == XMLStreamConstants.START_ELEMENT) {
				Document fragment2 = new StaxParser(reader, new NodeFactory()).buildFragment();
				ser.write(fragment2.getRootElement());   
			}
			ser.writeEndTag();
			ser.writeEndDocument();

			out.close();
			in.close();
			reader.close();
		} catch (FileNotFoundException e) {
			throw new RuntimeException("Could not find file: "+feedPath);
		} catch (XMLStreamException e) {
			throw new RuntimeException("Error reading XML in file: "+feedPath);
		} catch (IOException e) {
			throw new RuntimeException("Error reading XML in file: "+feedPath);
		} catch (ParsingException e) {
			throw new RuntimeException("Error reading XML in file: "+feedPath);
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					throw new RuntimeException("Could not close outputstream: "+out);
				}
			}
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					throw new RuntimeException("Could not close inputstream: "+in);
				}
			}
			if (reader != null) {
				try {
					reader.close();
				}catch (XMLStreamException e) {
					throw new RuntimeException("Could not close XML reader: "+reader);
				}
			}
		}
		Utils.copyFile(tempPath, feedPath);
		new File(tempPath).delete();
	}
}
