package wwmm.crystaleye.templates.feeds;

import static wwmm.crystaleye.CrystalEyeConstants.CRYSTALEYE_DATE_FORMAT;

import java.io.BufferedReader;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.IOUtils;

import nu.xom.Document;
import wwmm.crystaleye.site.feeds.AtomArchiveFeed;
import wwmm.crystaleye.util.Utils;

public class AtomArchiveTemplate {

	String title;
	String subTitle;
	String author;
	String link;

	public AtomArchiveTemplate(String title, String subTitle, String author, String link) {
		super();
		this.title = title;
		this.subTitle = subTitle;
		this.author = author;
		this.link = link;
	}

	public AtomArchiveFeed getFeedSkeleton() {
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

		Document feed = null;
		StringReader sr = null;
		BufferedReader br = null;
		try {
			sr = new StringReader(sb.toString());
			br = new BufferedReader(sr);
			feed = Utils.parseXmlFile(br);
		} finally {
			IOUtils.closeQuietly(sr);
			IOUtils.closeQuietly(br);
		}
		return new AtomArchiveFeed(feed);
	}
}
