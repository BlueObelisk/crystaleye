package wwmm.crystaleye.site.templates;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import wwmm.crystaleye.site.BondLengthsManager;

public class BondLengthElementIndex {

	private String bondsFolderPath;
	private String element;
	private Set<String> ligands;

	private BondLengthElementIndex() {
		;
	}

	public BondLengthElementIndex(String bondsFolderPath, String element, Set<String> ligands) {
		this.bondsFolderPath = bondsFolderPath;
		this.element = element;
		this.ligands = ligands;
	}

	public String getWebpage() {
		StringBuilder sb = new StringBuilder();
		for (String ligand : ligands) {
			List<String> list = new ArrayList<String>();
			list.add(element);
			list.add(ligand);
			Collections.sort(list);
			String filename = list.get(0)+"-"+list.get(1)+BondLengthsManager.AFTER_PROTOCOL+".svg";

			sb.append("<li style=\"font-weight: bold;\">"+element+"-"+ligand+"</li>\n");
			sb.append("<ul class=\"normal\">\n");
			sb.append("<li><a href='./"+list.get(0)+"-"+list.get(1)+".svg'>All</a></li>\n");
			if (new File(bondsFolderPath+"/"+filename).exists()) {
				sb.append("<li><a href='./"+filename+"'>After protocol</a></li>\n");
			}
			sb.append("</ul>\n");
		}
		String content = sb.toString();

		String page = "<!DOCTYPE html PUBLIC '-//W3C//DTD XHTML 1.0 Strict//EN' 'http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd'>"+
		"<html xmlns='http://www.w3.org/1999/xhtml'>"+
		"<head>"+
		"<title>CrystalEye: Bond feeds for "+element+"</title>"+
		"<meta http-equiv='Content-Type' content='text/html; charset=utf-8' />"+
		"<meta name='description' content='' />"+
		"<meta name='keywords' content='' />"+
		"<meta http-equiv='imagetoolbar' content='no' />"+
		"<link href='../styles.css' rel='stylesheet' type='text/css' media='all' />"+
		"</head>"+
		"<body>"+
		"<div id='allcontainer'>"+
		"<div id='topheader'>"+
		"<a href='http://www-ucc.ch.cam.ac.uk'>"+
		"<img class='unilevercentre' src='../images/ucc-logo.gif' alt='Unilever Centre for Molecular Informatics' height='78' width='183' />"+
		"</a>"+
		"<a href='http://www.cam.ac.uk'>"+
		"<img class='universityofcambridge' src='../images/universityofcambridge.gif' alt='University of Cambridge' height='49' width='218' />"+
		"</a>"+
		"</div>"+
		"<!--end topheader-->"+
		"<div id='bottomheader'>"+
		"<h1>CrystalEye</h1>"+
		"</div>"+
		"<!--end bottomheader-->"+
		"<div id='main'>"+
		"<div id='contentcontainer'>"+
		"<div id='content'>"+
		"<p>Follow the links below to view histograms of bond lengths containing <b>"+element+"</b>.</p>" +
		"<ul class='normal'>" +
		"<li>'All' links lead to histograms containing data for all bonds of non-disordered, unconstrained atoms.</li>" +
		"<li>'After protocol' links lead to histograms containing data for all bonds of non-disordered, unconstrained atoms, where the structure was at a temperature <= 200K with an r-factor <= 0.05.</li>" +
		"</ul>"+
		"<p>Note that the bins in each histogram are clickable.  Upon clicking you will be taken to a page where you can browse the structures " +
		"that the bin represented.  In the 3D representation of a structure, the atoms in the relevant bonds are highlighted by a yellow halo.</p>"+
		"<ul class='normal'>"+
		content+
		"</ul>"+
		"</div>"+
		"<!--end content-->"+
		"</div>"+
		"<!--end contentcontainer-->"+
		"<div id='menu'>"+
		"<ul class='menu'>"+
		"<li>"+
		"<a href='../index.html'>"+
		"<span>Home</span>"+
		"</a>"+
		"</li>"+
		"<li>"+
		"<a href='http://wwmm-sandbox.ch.cam.ac.uk/crystaleye-search/'>"+
		"<span>Search</span>"+
		"</a>"+
		"</li>"+
		"<li>"+
		"<a href='../summary/index.html'>"+
		"<span>Browse Issues</span>"+
		"</a>"+
		"</li>"+
		"<li>"+
		"<a href='../feed/index.html'>"+
		"<span>RSS feeds</span>"+
		"</a>"+
		"</li>"+
		"<li class='selected'>"+
		"<a href='../bondlengths/index.html'>"+
		"<span>Bond Lengths</span>"+
		"</a>"+
		"</li>"+
		"<li>"+
		"<a href='../gm/index.html'>"+
		"<span>Greasemonkey</span>"+
		"</a>"+
		"</li>"+
		"<li class='last'>"+
		"<a href='../faq/index.html'>"+
		"<span>FAQ</span>"+
		"</a>"+
		"</li>"+
		"</ul>"+
		"</div>"+
		"<!--end menu-->"+
		"</div>"+
		"<!--end main-->"+
		"<div id='footer'>"+
		"<p>"+
		"<a href='mailto:ned24@cam.ac.uk'>Contact us</a>"+
		"</p>"+
		"</div>"+
		"<!--end footer-->"+
		"</div>"+
		"<!-- end allcontainer-->"+
		"</body>"+
		"</html>";
		return page;
	}
}
