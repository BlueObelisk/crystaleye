package uk.ac.cam.ch.crystaleye.templates.feeds;

import static uk.ac.cam.ch.crystaleye.CrystalEyeConstants.CRYSTALEYE_DATE_FORMAT;

import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Date;

import nu.xom.Document;
import uk.ac.cam.ch.crystaleye.IOUtils;
import uk.ac.cam.ch.crystaleye.site.feeds.AtomPubFeed;

public class AtomPubTemplate {

	String title;
	String subTitle;
	String author;
	String link;

	public AtomPubTemplate(String title, String subTitle, String author, String link) {
		super();
		this.title = title;
		this.subTitle = subTitle;
		this.author = author;
		this.link = link;
	}

	public AtomPubFeed getFeedSkeleton() {
		Date date = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat(CRYSTALEYE_DATE_FORMAT);
		String dNow = formatter.format(date);

		StringBuilder sb = new StringBuilder();
		sb.append("<feed xmlns=\"http://www.w3.org/2005/Atom\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\">");
		sb.append("<title>"+title+"</title>");
		sb.append("<link rel=\"self\" href=\""+link+"\" />");
		sb.append("<subtitle>"+subTitle+"</subtitle>");
		sb.append("<updated>"+dNow+"</updated>");
		sb.append("<author><name>"+author+"</name></author>");
		sb.append("<id>"+link+"</id>");
		sb.append("</feed>");
		Document feed = IOUtils.parseXmlFile(new StringReader(sb.toString()));
		return new AtomPubFeed(feed);
	}
}
