package uk.ac.cam.ch.crystaleye.site.templates;

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
	
	public void setEntries() {
		
	}

	public Document getFeed() throws ValidityException, ParsingException, IOException {
		Date date = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat(CRYSTALEYE_DATE_FORMAT);
		String dNow = formatter.format(date);
		
		String feed = "<?xml version='1.0' encoding='UTF-8'?>\n"+
		"<feed xmlns='http://www.w3.org/2005/Atom' xmlns:cml='http://www.xml-cml.org/schema' xmlns:dc='http://purl.org/dc/elements/1.1/' xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#' xmlns:sy='http://purl.org/rss/1.0/modules/syndication/' xmlns:taxo='http://purl.org/rss/1.0/modules/taxonomy/'>\n"+
		"<title>"+title+"</title>\n"+
		"<link rel='self' href='"+link+"' />\n"+
		"<subtitle>"+subTitle+"</subtitle>\n"+
		"<updated>"+dNow+"</updated>\n"+
		"<dc:creator>"+author+"</dc:creator>\n"+
		"<id>"+link+"</id>\n"+
		
		/*
		"<entry>\n"+
		"<title type='html'>X-ray powder difraction structural characterization of Pb(1-x)Ba(x)Zr(0.65)Ti(0.35)O3 ceramic.</title>\n"+
		"<link href='http://wwmm.ch.cam.ac.uk/crystaleye/summary/acta/b/2007/05-00/data/kd5005/kd5005sup1_PBZT-40-300K-cubic/kd5005sup1_PBZT-40-300K-cubic.cif.summary.html' rel='alternate'>\n"+
		"</link>\n"+
		"<link href='http://wwmm.ch.cam.ac.uk/crystaleye/summary/acta/b/2007/05-00/data/kd5005/kd5005sup1_PBZT-40-300K-cubic/kd5005sup1_PBZT-40-300K-cubic.complete.cml.xml' rel='self'>\n"+
		"</link>\n"+
		"<author>\n"+
		"<name>Chris Talbot</name>\n"+
		"</author>\n"+
		"<id>http://wwmm.ch.cam.ac.uk/crystaleye/summary/acta/b/2007/05-00/data/kd5005/kd5005sup1_PBZT-40-300K-cubic/kd5005sup1_PBZT-40-300K-cubic.cif.summary.html</id>\n"+
		"<summary type='text'>CrystalEye CMLRSS summary of DataBlock PBZT-40-300K-cubic in CIF KD5005 (DOI:10.1107/S0108768107022197) from issue 05-00/2007 of Acta Crystallographica, Section B.</summary>\n"+
		"<creator xmlns='http://purl.org/dc/elements/1.1/'>Chris Talbot</creator>\n"+
		"<updated>2007-08-08T18:42:22Z</updated>\n"+
		"</entry>\n"+
		*/
		
		"</feed>";
		return new Builder().build(new BufferedReader(new StringReader(feed)));
	}
}
