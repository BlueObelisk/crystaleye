package uk.ac.cam.ch.crystaleye.site.feeds;

import static uk.ac.cam.ch.crystaleye.CrystalEyeConstants.ATOM_1_NS;
import static uk.ac.cam.ch.crystaleye.CrystalEyeConstants.XHTML_NS;

import java.util.List;

import nu.xom.Attribute;
import nu.xom.Element;

public class AtomHtmlContent {

	List<String> imgUrls;

	public AtomHtmlContent(List<String> imgUrls) {
		this.imgUrls = imgUrls;
	}

	public Element create() {
		Element content = new Element("content", ATOM_1_NS);
		if (imgUrls == null) {
			throw new RuntimeException("SRC is null - one must be provided.");
		}
		content.addAttribute(new Attribute("type", "xhtml"));
		
		Element div = new Element("div", XHTML_NS);
		content.appendChild(div);
		for (String s : imgUrls) {
			Element img = new Element("img", XHTML_NS);
			div.appendChild(img);
			img.addAttribute(new Attribute("src", s));
		}
		return content;
	}
}
