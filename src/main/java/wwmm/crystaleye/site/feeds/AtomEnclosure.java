package wwmm.crystaleye.site.feeds;

import static wwmm.crystaleye.CrystalEyeConstants.ATOM_1_NS;
import nu.xom.Attribute;
import nu.xom.Element;

public class AtomEnclosure {

	String url;
	String title;
	int length = -1;
	String type;

	public AtomEnclosure(String url, String title, int length, String type) {
		this.url = url;
		this.length = length;
		this.type = type;
		this.title = title;
	}

	public Element create() {
		Element enclosure = new Element("link", ATOM_1_NS);
		if (url == null) {
			throw new RuntimeException("URL is null - one must be provided.");
		}
		if (type == null) {
			throw new RuntimeException("Type is null - one must be provided.");
		}
		if (length == -1) {
			throw new RuntimeException("A length must be provided.");
		}
		enclosure.addAttribute(new Attribute("rel", "enclosure"));
		enclosure.addAttribute(new Attribute("href", url));
		if (title != null) {
			enclosure.addAttribute(new Attribute("title", title));
		}
		enclosure.addAttribute(new Attribute("type", type));
		enclosure.addAttribute(new Attribute("length", ""+length));
		return enclosure;
	}
}
