package uk.ac.cam.ch.crystaleye.templates.feeds;

import static uk.ac.cam.ch.crystaleye.CrystalEyeConstants.CRYSTALEYE_DATE_FORMAT;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Date;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

public class Rss2 {
	
	String title;
	String subTitle;
	String author;
	String link;

	public Rss2(String title, String subTitle, String author, String link) {
		super();
		this.title = title;
		this.subTitle = subTitle;
		this.author = author;
		this.link = link;
	}

	public Document getFeed() throws ValidityException, ParsingException, IOException {
		Date date = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat(CRYSTALEYE_DATE_FORMAT);
		String dNow = formatter.format(date);
		
		String feed = "<rss xmlns:content=\"http://purl.org/rss/1.0/modules/content/\" " +
				"xmlns:taxo=\"http://purl.org/rss/1.0/modules/taxonomy/\" " +
				"xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" " +
				"xmlns:dc=\"http://purl.org/dc/elements/1.1/\" version=\"2.0\" " +
				"xmlns:cml=\"http://www.xml-cml.org/schema\"> " +
		  "<channel>"+
		    "<title>"+title+"</title>"+
		    "<link>"+link+"</link>"+
		    "<description>"+subTitle+"</description>"+
		    "<dc:creator>"+author+"</dc:creator>"+
		    "<dc:date>"+dNow+"</dc:date>"+
		  "</channel>"+
		"</rss>";

		return new Builder().build(new BufferedReader(new StringReader(feed)));
	}
}
