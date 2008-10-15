package wwmm.crystaleye.site.feeds;

import static wwmm.crystaleye.CrystalEyeConstants.ATOM_1_NS;
import static wwmm.crystaleye.CrystalEyeConstants.DC_NS;
import static wwmm.crystaleye.CrystalEyeConstants.RDF_NS;
import static wwmm.crystaleye.CrystalEyeConstants.RSS_1_NS;
import nu.xom.Attribute;
import nu.xom.Element;
import nu.xom.Node;
import nu.xom.Text;

import org.xmlcml.cml.base.CMLConstants;

import wwmm.crystaleye.util.CrystalEyeUtils;
import wwmm.crystaleye.util.XmlIOUtils;


public class CMLRSSEntry implements CMLConstants {

	public enum FeedType {
		ATOM_1,
		RSS_1,
		RSS_2;
	}
	
	String title;
	String author;
	String id;
	String summary;
	String htmlLink;
	String cmlLink;
	String cmlFilePath;

	private CMLRSSEntry() {
		;
	}

	public CMLRSSEntry(CMLRSSEntryDetails cred) {
		title = cred.getTitle();
		author = cred.getAuthor();
		id = cred.getId();
		summary = cred.getSummary();
		htmlLink = cred.getHtmlLink();
		cmlLink = cred.getCmlLink();
		cmlFilePath = cred.getCmlFilePath();
	}

	public Element createAtom1Entry() {
		Element entry = createAtom1Element("entry");
		//root.appendChild(entry);

		Element titleEl = createAtom1Element("title");
		entry.appendChild(titleEl);
		titleEl.addAttribute(new Attribute("type", "html"));
		titleEl.appendChild(new Text(title));

		Element linkSelfEl = createAtom1Element("link");
		entry.appendChild(linkSelfEl);
		linkSelfEl.addAttribute(new Attribute("href", htmlLink));
		linkSelfEl.addAttribute(new Attribute("rel", "alternate"));

		Element linkAlternateEl = createAtom1Element("link");
		entry.appendChild(linkAlternateEl);
		linkAlternateEl.addAttribute(new Attribute("href", cmlLink));
		linkAlternateEl.addAttribute(new Attribute("rel", "self"));

		Element authorEl = createAtom1Element("author");
		entry.appendChild(authorEl);
		Element nameEl = createAtom1Element("name");
		authorEl.appendChild(nameEl);
		nameEl.appendChild(new Text(author));

		Element idEl = createAtom1Element("id");
		entry.appendChild(idEl);
		idEl.appendChild(new Text(id));

		Element summaryEl = createAtom1Element("summary");
		entry.appendChild(summaryEl);
		summaryEl.addAttribute(new Attribute("type", "text"));
		summaryEl.appendChild(new Text(summary));

		Node node = XmlIOUtils.parseXmlFile(cmlFilePath).getRootElement();
		entry.appendChild(node.copy());

		Element creator = createDCElement("creator");
		entry.appendChild(creator);
		creator.appendChild(new Text(author));

		Element updated = createAtom1Element("updated");
		updated.appendChild(new Text(CrystalEyeUtils.getDate()));
		entry.appendChild(updated);

		return entry;
	}

	public Element createRss1Entry() {
		Element item = createRss1Element("item");
		//root.appendChild(item);
		Attribute about = new Attribute("about", htmlLink);
		about.setNamespace("rdf", RDF_NS);
		item.addAttribute(about);

		Element titleEl = createRss1Element("title");
		item.appendChild(titleEl);
		titleEl.appendChild(new Text(title));

		Element linkEl = createRss1Element("link");
		linkEl.appendChild(new Text(htmlLink));
		item.appendChild(linkEl);

		Element descriptionEl = createRss1Element("description");
		item.appendChild(descriptionEl);
		descriptionEl.appendChild(new Text(summary));
		
		Element dateEl = new Element("date");
		item.appendChild(dateEl);
		dateEl.setNamespaceURI(DC_NS);
		dateEl.setNamespacePrefix("dc");
		dateEl.appendChild(new Text(CrystalEyeUtils.getDate()));

		Node node = XmlIOUtils.parseXmlFile(cmlFilePath).getRootElement();
		item.appendChild(node.copy());

		return item;
	}

	public Element createRss2Entry() {					
		Element item = new Element("item");

		Element titleEl = new Element("title");
		item.appendChild(titleEl);
		titleEl.appendChild(new Text(title));

		Element linkEl = new Element("link");
		item.appendChild(linkEl);
		linkEl.appendChild(new Text(htmlLink));

		Element descriptionEl = new Element("description");
		item.appendChild(descriptionEl);
		descriptionEl.appendChild(new Text(summary));

		Element guidEl = new Element("guid");
		item.appendChild(guidEl);
		guidEl.appendChild(new Text(htmlLink));

		Element dateEl = new Element("date");
		item.appendChild(dateEl);
		dateEl.setNamespaceURI(DC_NS);
		dateEl.setNamespacePrefix("dc");
		dateEl.appendChild(new Text(CrystalEyeUtils.getDate()));

		Element element = XmlIOUtils.parseXmlFile(cmlFilePath).getRootElement();
		item.appendChild(element.copy());

		return item;
	}

	private Element createRss1Element(String name) {
		return new Element(name, RSS_1_NS);
	}

	private Element createAtom1Element(String name) {
		return new Element(name, ATOM_1_NS);
	}

	private Element createDCElement(String name) {
		return new Element(name, DC_NS);
	}
}
