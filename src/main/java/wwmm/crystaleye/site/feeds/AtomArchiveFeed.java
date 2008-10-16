package wwmm.crystaleye.site.feeds;

import static wwmm.crystaleye.CrystalEyeConstants.ATOM_1_NS;
import static wwmm.crystaleye.CrystalEyeConstants.X_ATOM1;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;
import nu.xom.Text;
import nu.xom.XPathContext;
import wwmm.crystaleye.util.Utils;

public class AtomArchiveFeed {

	Document feed;
	Element root;

	public static final String FH_NS = "http://purl.org/syndication/history/1.0";
	public static final XPathContext X_FH = new XPathContext("fh", FH_NS);

	public static final String NEXT_ARCHIVE_REL = "next-archive";
	public static final String PREV_ARCHIVE_REL = "prev-archive";
	public static final String CURRENT_REL = "current";
	public static final String SELF_REL = "self";

	public AtomArchiveFeed(File file) {
		feed = Utils.parseXmlFile(file);
		root = feed.getRootElement();
	}

	public AtomArchiveFeed(Document feed) {
		this.feed = feed;
		root = feed.getRootElement();
	}

	public void addEntries(List<AtomEntry> entries) {
		for (AtomEntry entry : entries) {
			addEntry(entry);
		}
	}

	public void addEntry(AtomEntry entry) {
		int idx = getBeforeEntryIndex();
		Element el = entry.create();		
		root.insertChild(el, idx);
	}

	private int getBeforeEntryIndex() {
		Nodes lastBarEntryNodes = root.query("./atom1:entry", X_ATOM1);
		int idx = -1;
		if (lastBarEntryNodes.size() > 0) {
			Element el = (Element)lastBarEntryNodes.get(0);
			idx = el.getParent().indexOf(el);
		} else {
			idx = root.getChildCount();
		}
		return idx;
	}

	public List<Element> getEntries() {
		List<Element> list = new ArrayList<Element>();
		Nodes entries = feed.query("./atom1:feed/atom1:entry", X_ATOM1);
		for (int i = 0; i < entries.size(); i++) {
			Element el = (Element)entries.get(i);
			list.add(el);
		}
		return list;
	}

	public void setAsCurrentDoc(String prevArchiveLink) {
		removeFhArchiveElement();
		removeLinkElement(NEXT_ARCHIVE_REL);
		if (prevArchiveLink != null) {
			setLinkElement("prev-archive", prevArchiveLink);
		}
	}

	public void setAsArchiveDoc(String currentDocLink, String prevArchiveLink, String nextArchiveLink) {
		addFhArchiveElement();
		if (currentDocLink == null) {
			throw new IllegalArgumentException("currentDocLink is null - one must be provided.");
		} else {
			setLinkElement(CURRENT_REL, currentDocLink);
		}
		if (prevArchiveLink != null) {
			setLinkElement(PREV_ARCHIVE_REL, prevArchiveLink);
		}
		if (nextArchiveLink != null) {
			setLinkElement(NEXT_ARCHIVE_REL, nextArchiveLink);
		}
	}

	public void setId(String id) {
		Element el = (Element)root.query("./atom1:id", X_ATOM1).get(0);
		el.detach();
		Element newId = new Element("id", ATOM_1_NS);
		newId.appendChild(new Text(id));
		int idx = getBeforeEntryIndex();
		root.insertChild(newId, idx);
	}

	public void setLinkElement(String rel, String href) {
		Nodes nodes = root.query("./atom1:link[@rel='"+rel+"']", X_ATOM1);
		if (nodes.size() > 0) {
			Element el = (Element)nodes.get(0);
			el.getAttribute("href").setValue(href);
		} else {
			Element el = new Element("link", ATOM_1_NS);
			el.addAttribute(new Attribute("rel", rel));
			el.addAttribute(new Attribute("href", href));
			int idx = getBeforeEntryIndex();
			root.insertChild(el, idx);
		}
	}

	public void removeLinkElement(String rel) {
		Nodes nodes = root.query("./atom1:link[@rel='"+rel+"']", X_ATOM1);
		detachNodes(nodes);
	}

	public void removeFhArchiveElement() {
		Nodes nodes = root.query("./fh:archive", X_FH);
		detachNodes(nodes);
	}

	public void addFhArchiveElement() {
		Element fh = new Element("archive", FH_NS);
		root.appendChild(fh);
	}

	public Document getFeed() {
		return feed;
	}

	public void detachNodes(Nodes nodes) {
		for (int i = 0; i < nodes.size(); i++) {
			nodes.get(i).detach();
		}
	}

	public static void main(String[] args) {
		String path = "e:/feed.xml";
		AtomArchiveFeed a = new AtomArchiveFeed(Utils.parseXmlFile(path));
		a.addEntries(new ArrayList<AtomEntry>());
	}
}
