package ned24.sandbox.crystaleye;

import org.xmlcml.molutil.ChemicalElement;

import wwmm.crystaleye.util.Utils;

public class GenerateBondsFeedIndexes {
	public static void main(String[] args) {
		for (int i = 1; i < 105; i++) {
			ChemicalElement iEl = ChemicalElement.getElement(i);
			String iStr = iEl.getSymbol();
			String content = "";	
			for (int j = 1; j < 105; j++) {
				ChemicalElement jEl = ChemicalElement.getElement(j);
				if (jEl.getAtomicNumber() > iEl.getAtomicNumber()) {
					String jStr = jEl.getSymbol();
					content += "<li style=\"font-weight: bold;\">"+iStr+"-"+jStr+"</li>\n" +
					"<ul class=\"normal\">\n" +
					"<li>RSS (<a href='./"+iStr+"-"+jStr+"/rss/rss_10/feed.xml'>rss1</a> | <a href='./"+iStr+"-"+jStr+"/rss/rss_20/feed.xml'>rss2</a> | <a href='./"+iStr+"-"+jStr+"/rss/atom_10/feed.xml'>atom1</a>)</li>\n"+
					"<li>CMLRSS (<a href='./"+iStr+"-"+jStr+"/cmlrss/rss_10/feed.xml'>rss1</a> | <a href='./"+iStr+"-"+jStr+"/cmlrss/rss_20/feed.xml'>rss2</a> | <a href='./"+iStr+"-"+jStr+"/cmlrss/atom_10/feed.xml'>atom1</a>)</li>\n"+
					"</ul>\n";
				} else {
					String jStr = jEl.getSymbol();
					content += "<li style=\"font-weight: bold;\">"+iStr+"-"+jStr+"</li>\n" +
					"<ul class=\"normal\">\n" +
					"<li>RSS (<a href='./"+jStr+"-"+iStr+"/rss/rss_10/feed.xml'>rss1</a> | <a href='./"+jStr+"-"+iStr+"/rss/rss_20/feed.xml'>rss2</a> | <a href='./"+jStr+"-"+iStr+"/rss/atom_10/feed.xml'>atom1</a>)</li>\n"+
					"<li>CMLRSS (<a href='./"+jStr+"-"+iStr+"/cmlrss/rss_10/feed.xml'>rss1</a> | <a href='./"+jStr+"-"+iStr+"/cmlrss/rss_20/feed.xml'>rss2</a> | <a href='./"+jStr+"-"+iStr+"/cmlrss/atom_10/feed.xml'>atom1</a>)</li>\n"+
					"</ul>\n";
				}
				String page = getPage(content, iStr);
				Utils.writeText(page, "e:\\test\\feed\\bonds\\"+iStr+"-index.html");
			}
		}		
	}

	static String getPage(String content, String symbol) {
		String page = "<!DOCTYPE html PUBLIC '-//W3C//DTD XHTML 1.0 Strict//EN' 'http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd'>"+
		"<html xmlns='http://www.w3.org/1999/xhtml'>"+
		"<head>"+
		"<title>CrystalEye: Bond feeds for"+symbol+"</title>"+
		"<meta http-equiv='Content-Type' content='text/html; charset=utf-8'>"+
		"<meta name='description' content=''>"+
		"<meta name='keywords' content=''>"+
		"<meta http-equiv='imagetoolbar' content='no'>"+
		"<link href='../../styles.css' rel='stylesheet' type='text/css' media='all'>"+
		"</head>"+
		"<body>"+
		"<div id='allcontainer'>"+

		"<div id='topheader'>"+
		"<a href='http://www-ucc.ch.cam.ac.uk'><img class='unilevercentre' src='../../images/ucc-logo.gif' alt='Unilever Centre for Molecular Informatics' height='78' width='183'></a>"+
		"<a href='http://www.cam.ac.uk'><img class='universityofcambridge' src='../../images/universityofcambridge.gif' alt='University of Cambridge' height='49' width='218'></a>"+
		"</div>"+
		"<!--end topheader-->"+

		"<div id='bottomheader'>"+
		"<h1>CrystalEye (beta)</h1>"+
		"</div>"+
		"<!--end bottomheader-->"+

		"<div id='main'>"+
		"<div id='contentcontainer'>"+
		"<div id='content'>"+
		"<p>Subscribe to feeds containing molecules with bonds between "+symbol+" and another element:</p>"+
		"<ul class='normal'>"+
		content+
		"</ul>"+
		"</div>"+
		"<!--end content-->"+
		"</div>"+
		"<!--end contentcontainer-->"+

		"<div id='menu'>"+
		"<ul class='menu'>"+
		"<li><a href='../../index.html'><span>Home</span></a></li>"+
		"<li><a href='../../summary/index.html'><span>Browse Issues</span></a></li>"+
		"<li class='selected'><a href='../index.html'><span>RSS feeds</span></a></li>"+
		"<li class='last'><a href='../../faq/index.html'><span>FAQ</span></a></li>"+
		"</ul>"+
		"</div>"+
		"<!--end menu-->"+
		"</div>"+
		"<!--end main-->"+

		"<div id='footer'>"+
		"<p><a href='mailto:ned24@cam.ac.uk'>Contact us</a></p>"+
		"</div>"+
		"<!--end footer-->"+

		"</div>"+
		"<!-- end allcontainer-->"+
		"</body>"+
		"</html>";
		return page;
	}
}
