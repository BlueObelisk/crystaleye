package wwmm.crystaleye.site.feeds;

import static wwmm.crystaleye.CrystalEyeConstants.ATOM_1_NS;

import java.util.List;

import nu.xom.Attribute;
import nu.xom.Element;
import nu.xom.Text;
import wwmm.crystaleye.CrystalEyeUtils;

public class AtomEntry {

	String title;
	String link;
	String id;
	String summary;
	List<AtomEnclosure> enclosures;
	Element content;

	public AtomEntry(String title, String link, String id, String summary, List<AtomEnclosure> enclosures, Element content) {
		this.title = title;
		this.link = link;
		this.id = id;
		this.summary = summary;
		this.enclosures = enclosures;
		this.content = content;
	}

	public Element create() {
		if (title == null) {
			throw new RuntimeException("Title provided is null - one must be provided.");
		}
		if (link == null) {
			throw new RuntimeException("Link provided is null - one must be provided.");			
		}
		if (id == null) {
			throw new RuntimeException("Id provided is null - one must be provided.");
		}
		if (summary == null) {
			throw new RuntimeException("Summary provided is null - one must be provided.");
		}

		Element entry = new Element("entry", ATOM_1_NS);
		Element title = new Element("title", ATOM_1_NS);
		title.appendChild(new Text(this.title));
		entry.appendChild(title);
		Element link = new Element("link", ATOM_1_NS);
		link.addAttribute(new Attribute("href", this.link));
		entry.appendChild(link);
		Element id = new Element("id", ATOM_1_NS);
		id.appendChild(new Text(this.id));
		entry.appendChild(id);
		Element summary = new Element("summary", ATOM_1_NS);
		summary.appendChild(new Text(this.summary));
		entry.appendChild(summary);	
		Element updated = new Element("updated", ATOM_1_NS);
		updated.appendChild(new Text(CrystalEyeUtils.getDate()));
		entry.appendChild(updated);

		for (AtomEnclosure enc : enclosures) {
			entry.appendChild(enc.create());
		}
		if (content != null) {
			entry.appendChild(content);
		}
		return entry;
	}
}
