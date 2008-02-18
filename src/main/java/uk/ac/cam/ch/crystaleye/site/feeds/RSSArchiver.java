package uk.ac.cam.ch.crystaleye.site.feeds;

import static uk.ac.cam.ch.crystaleye.CrystalEyeConstants.CRYSTALEYE_DATE_FORMAT;
import static uk.ac.cam.ch.crystaleye.CrystalEyeConstants.X_ATOM1;
import static uk.ac.cam.ch.crystaleye.CrystalEyeConstants.X_DC;
import static uk.ac.cam.ch.crystaleye.CrystalEyeConstants.X_RDF;
import static uk.ac.cam.ch.crystaleye.CrystalEyeConstants.X_RSS1;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.NodeFactory;
import nu.xom.Nodes;
import nu.xom.ParsingException;
import nux.xom.io.StaxParser;
import nux.xom.io.StaxUtil;
import nux.xom.io.StreamingSerializer;
import nux.xom.io.StreamingSerializerFactory;
import uk.ac.cam.ch.crystaleye.CrystalEyeRuntimeException;
import uk.ac.cam.ch.crystaleye.FileListing;
import uk.ac.cam.ch.crystaleye.Utils;

public class RSSArchiver {

	int numberOfDays;

	public void deleteOldFeedEntries(String feedMime, String feedDir, int numberOfDays) {
		this.numberOfDays = numberOfDays;
		String regex = "[^\\.]*\\."+feedMime;
		List<String> linkList = null;
		try {
			List<File> feedFiles = FileListing.byRegex(new File(feedDir), regex);
			for (File feedFile : feedFiles) {
				System.out.println("Deleting entries over "+numberOfDays+" of days old from: "+feedFile.getAbsolutePath());
				boolean isRSS1 = false;
				String feedPath = feedFile.getAbsolutePath();
				if (feedPath.contains("journal")) continue;
				String tempPath = feedPath+".tmp";
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
					if (reader.getLocalName().equals("feed")) {
						deleteOldAtom1Entries(ser, reader);
					} else if (reader.getLocalName().equals("RDF")) {
						linkList = deleteOldRss1Entries(ser, reader);
						isRSS1 = true;
					} else if (reader.getLocalName().equals("rss")) {
						deleteOldRss2Entries(ser, reader);
					} else {
						throw new RuntimeException("Reader cannot handle this type of RSS");
					}
					ser.writeEndDocument();

					out.close();
					in.close();
					reader.close();
				} catch (FileNotFoundException e) {
					throw new CrystalEyeRuntimeException("Could not find file: "+feedPath);
				} catch (XMLStreamException e) {
					throw new CrystalEyeRuntimeException("Error reading XML in file: "+feedPath);
				} catch (IOException e) {
					throw new CrystalEyeRuntimeException("Error reading XML in file: "+feedPath);
				} catch (ParsingException e) {
					e.printStackTrace();
					throw new CrystalEyeRuntimeException("Error reading XML in file: "+feedPath);
				} catch (ParseException e) {
					throw new CrystalEyeRuntimeException("Error parsing date in file: "+feedPath);
				} finally {
					if (out != null) {
						try {
							out.close();
						} catch (IOException e) {
							throw new CrystalEyeRuntimeException("Could not close outputstream: "+out);
						}
					}
					if (in != null) {
						try {
							in.close();
						} catch (IOException e) {
							throw new CrystalEyeRuntimeException("Could not close inputstream: "+in);
						}
					}
					if (reader != null) {
						try {
							reader.close();
						}catch (XMLStreamException e) {
							throw new CrystalEyeRuntimeException("Could not close XML reader: "+reader);
						}
					}
				}
				Utils.copyFile(tempPath, feedPath);
				new File(tempPath).delete();
				if (isRSS1) {
					removeRSS1HeaderLinks(feedPath, linkList);
				}
			}
		} catch (FileNotFoundException e) {
			throw new CrystalEyeRuntimeException("Could not find file: "+e.getMessage());
		}
	}

	private void removeRSS1HeaderLinks(String feedPath, List<String> linkList) {
		String tempPath = feedPath+".tmp";
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
					for (String link : linkList) {
						Nodes linkNodes = items.query(".//rdf:Seq/rdf:li[@resource='"+link+"']", X_RDF);
						for (int i = 0; i < linkNodes.size(); i++) {
							System.out.println("removing link node");
							linkNodes.get(0).detach();
						}
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
			while (reader.nextTag() == XMLStreamConstants.START_ELEMENT) {
				Document fragment = new StaxParser(reader, new NodeFactory()).buildFragment();
				ser.write(fragment.getRootElement());  
			}
			ser.writeEndDocument();

			out.close();
			in.close();
			reader.close();
		} catch (FileNotFoundException e) {
			throw new CrystalEyeRuntimeException("Could not find file: "+feedPath);
		} catch (XMLStreamException e) {
			throw new CrystalEyeRuntimeException("Error reading XML in file: "+feedPath);
		} catch (IOException e) {
			throw new CrystalEyeRuntimeException("Error reading XML in file: "+feedPath);
		} catch (ParsingException e) {
			throw new CrystalEyeRuntimeException("Error reading XML in file: "+feedPath);
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					throw new CrystalEyeRuntimeException("Could not close outputstream: "+out);
				}
			}
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					throw new CrystalEyeRuntimeException("Could not close inputstream: "+in);
				}
			}
			if (reader != null) {
				try {
					reader.close();
				}catch (XMLStreamException e) {
					throw new CrystalEyeRuntimeException("Could not close XML reader: "+reader);
				}
			}
		}
		Utils.copyFile(tempPath, feedPath);
		new File(tempPath).delete();
	}

	private void deleteOldAtom1Entries(StreamingSerializer ser, XMLStreamReader reader) throws IOException, ParsingException, XMLStreamException, ParseException {
		ser.writeStartTag((Element)new StaxParser(reader, new NodeFactory()).buildNode());
		while (reader.nextTag() == XMLStreamConstants.START_ELEMENT) {
			Document fragment = new StaxParser(reader, new NodeFactory()).buildFragment();
			ser.write(fragment.getRootElement());  
			if (reader.getLocalName().equals("entry")) break;
		}
		while (reader.nextTag() == XMLStreamConstants.START_ELEMENT) {
			Document entry = new StaxParser(reader, new NodeFactory()).buildFragment();
			if (entry.getRootElement().getLocalName().equals("entry")) {
				Nodes updatedNodes = entry.query(".//atom1:entry/atom1:updated", X_ATOM1);
				System.out.println("up "+updatedNodes.size());
				if (updatedNodes.size() == 1) {
					String entryDateString = updatedNodes.get(0).getValue();
					SimpleDateFormat formatter = new SimpleDateFormat(CRYSTALEYE_DATE_FORMAT);
					Date entryDate = (Date)formatter.parse(entryDateString);
					Date dateNow = new Date();
					long diffMillis =  dateNow.getTime()-entryDate.getTime();
					long diffDays = diffMillis/(24*60*60*1000);
					if (diffDays < numberOfDays) {
						ser.write(entry.getRootElement());  
					}
				} else {
					throw new CrystalEyeRuntimeException("Should be one updated node per entry.");
				}
			} else {
				ser.write(entry.getRootElement());
			}
		}
	}

	private List<String> deleteOldRss1Entries(StreamingSerializer ser, XMLStreamReader reader) throws IOException, ParsingException, XMLStreamException, ParseException { 
		List<String> linkList = new ArrayList<String>();
		ser.writeStartTag((Element)new StaxParser(reader, new NodeFactory()).buildNode());
		reader.nextTag();
		reader.require(XMLStreamConstants.START_ELEMENT, null, "channel");
		ser.writeStartTag((Element)new StaxParser(reader, new NodeFactory()).buildNode());
		while (reader.nextTag() == XMLStreamConstants.START_ELEMENT) {
			Document fragment = new StaxParser(reader, new NodeFactory()).buildFragment();
			ser.write(fragment.getRootElement());  
		} 
		ser.writeEndTag();
		while (reader.nextTag() == XMLStreamConstants.START_ELEMENT) {
			Document item = new StaxParser(reader, new NodeFactory()).buildFragment();
			Nodes dateNodes = item.query(".//dc:date", X_DC);
			if (dateNodes.size() == 1) {
				String entryDateString = dateNodes.get(0).getValue();
				SimpleDateFormat formatter = new SimpleDateFormat(CRYSTALEYE_DATE_FORMAT);
				Date entryDate = (Date)formatter.parse(entryDateString);
				Date dateNow = new Date();
				long diffMillis =  dateNow.getTime()-entryDate.getTime();
				long diffDays = diffMillis/(24*60*60*1000);
				if (diffDays < numberOfDays) {
					ser.write(item.getRootElement());  
				} else {
					Nodes linkNodes = item.query(".//rss1:link", X_RSS1);
					if (linkNodes.size() == 1) {
						linkList.add(linkNodes.get(0).getValue());
					} else {
						throw new CrystalEyeRuntimeException("Should be one link node per entry.");
					}
				}
			} else {
				throw new RuntimeException("Should be one date node per entry.");
			}
		}
		return linkList;
	}

	private void deleteOldRss2Entries(StreamingSerializer ser, XMLStreamReader reader) throws IOException, ParsingException, XMLStreamException, ParseException {
		ser.writeStartTag((Element)new StaxParser(reader, new NodeFactory()).buildNode());
		reader.nextTag();
		reader.require(XMLStreamConstants.START_ELEMENT, null, "channel");
		ser.writeStartTag((Element)new StaxParser(reader, new NodeFactory()).buildNode());
		while (reader.nextTag() == XMLStreamConstants.START_ELEMENT) {
			Document fragment = new StaxParser(reader, new NodeFactory()).buildFragment();
			ser.write(fragment.getRootElement());  
			if (reader.getLocalName().equals("date")) break;
		}
		while (reader.nextTag() == XMLStreamConstants.START_ELEMENT) {
			Document item = new StaxParser(reader, new NodeFactory()).buildFragment();
			Nodes dateNodes = item.query(".//dc:date", X_DC);
			if (dateNodes.size() == 1) {
				String entryDateString = dateNodes.get(0).getValue();
				SimpleDateFormat formatter = new SimpleDateFormat(CRYSTALEYE_DATE_FORMAT);
				Date entryDate = (Date)formatter.parse(entryDateString);
				Date dateNow = new Date();
				long diffMillis =  dateNow.getTime()-entryDate.getTime();
				long diffDays = diffMillis/(24*60*60*1000);
				if (diffDays < numberOfDays) {
					ser.write(item.getRootElement());  
				}
			} else {
				throw new RuntimeException("Should be one date node per entry.");
			}
		} 
		ser.writeEndTag();
	}
	
	public static void main(String[] args) {
		String mime = "xml";
		String dir = "e:/data-test/feed/atoms/Zr/cmlrss/atom_10";
		new RSSArchiver().deleteOldFeedEntries(mime, dir, 3);
	}
}
