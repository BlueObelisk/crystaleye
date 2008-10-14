package wwmm.crystaleye.templates.webpages;

import java.util.Set;

public class BondLengthIndex {

	private Set<String> elements;

	private BondLengthIndex() {
		;
	}

	public BondLengthIndex(Set<String> elements) {
		this.elements = elements;
	}

	public String getWebpage() {
		StringBuilder sb = new StringBuilder();
		for (String element : elements) {
			sb.append("<li style='font-weight: bold;'><a href=\""+element+"-index.html\">"+element+"</a></li>");
		}
		String content = sb.toString();
		
		String page = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">"+
		"<html xmlns=\"http://www.w3.org/1999/xhtml\">"+
		"<head>"+
		"	<title>CrystalEye: RSS feeds</title>"+
		"	<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />"+
		"	<meta name=\"description\" content=\"\" />"+
		"	<meta name=\"keywords\" content=\"\" />"+
		"	<meta http-equiv=\"imagetoolbar\" content=\"no\" />"+
		"	<link href=\"../styles.css\" rel=\"stylesheet\" type=\"text/css\" media=\"all\" />"+
		"</head>"+
		"<body>"+
		"	<div id=\"allcontainer\">"+
		"		<div id=\"topheader\">"+
		"			<a href=\"http://www-ucc.ch.cam.ac.uk\"><img class=\"unilevercentre\" src=\"../images/ucc-logo.gif\" alt=\"Unilever Centre for Molecular Informatics\" height=\"78\" width=\"183\" /></a>"+
		"			<a href=\"http://www.cam.ac.uk\"><img class=\"universityofcambridge\" src=\"../images/universityofcambridge.gif\" alt=\"University of Cambridge\" height=\"49\" width=\"218\" /></a>"+
		"		</div>"+
		"		<!--end topheader-->"+
		"		<div id=\"bottomheader\">"+
		"			<h1>CrystalEye</h1>"+
		"		</div>"+
		"		<!--end bottomheader-->"+
		"		<div id=\"main\">"+
		"			<div id=\"contentcontainer\">"+
		"				<div id=\"content\">"+
		"					<h1 class='faq'>Bond length histograms:</h1>"+
		"					<p>Click on a link below to navigate to a page providing links to histograms for all bonds containing that atom:</p>"+
		"					<ul class=\"normal\">"+
		"						"+content+
		"					</ul>"+
		"				</div>"+
		"				<!--end content-->"+
		"			</div>"+
		"			<!--end contentcontainer-->"+
		"			<div id=\"menu\">"+
		"				<ul class=\"menu\">"+
		"					<li><a href=\"../index.html\"><span>Home</span></a></li>"+
		"					<li><a href=\"http://wwmm-sandbox.ch.cam.ac.uk/crystaleye-search/\"><span>Search</span></a></li>"+
		"					<li><a href=\"../summary/index.html\"><span>Browse Issues</span></a></li>"+
		"                   <li>"+
		"                      <a href='../feed/index.html'>"+
		"                         <span>RSS feeds</span>"+
		"                      </a>"+
		"                   </li>"+
		"                   <li class='selected'>"+
		"                      <a href='../bondlengths/index.html'>"+
		"                         <span>Bond Lengths</span>"+
		"                      </a>"+
		"                   </li>"+
		"                   <li>"+
		"                      <a href='../gm/index.html'>"+
		"                         <span>Greasemonkey</span>"+
		"                      </a>"+
		"                   </li>"+
		"					<li class=\"last\"><a href=\"../faq/index.html\"><span>FAQ</span></a></li>"+
		"				</ul>"+
		"			</div>"+
		"			<!--end menu-->"+
		"		</div>"+
		"		<!--end main-->"+
		"		<div id=\"footer\">"+
		"			<p><a href=\"mailto:ned24@cam.ac.uk\">Contact us</a></p>"+
		"		</div>"+
		"		<!--end footer-->"+
		"	</div>"+
		"	<!-- end allcontainer-->"+
		"</body>"+
		"</html>";
		return page;
	}
}
