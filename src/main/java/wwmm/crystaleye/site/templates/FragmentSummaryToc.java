package wwmm.crystaleye.site.templates;

public class FragmentSummaryToc {
	
	private String fragmentContent;
	private String fragCount;
	private String jmolLoadForFrags;
	private String imageLoadForFrags;

	public FragmentSummaryToc(String fragmentContent, String fragCount, String jmolLoadForFrags, String imageLoadForFrags) {
		super();
		this.fragmentContent = fragmentContent;
		this.fragCount = fragCount;
		this.jmolLoadForFrags = jmolLoadForFrags;
		this.imageLoadForFrags = imageLoadForFrags;
	}

	public String getWebpage() {
		String page = "<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>"+
			"<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">"+
			"<html xmlns=\"http://www.w3.org/1999/xhtml\">"+
				"<head>"+
					"<title>Fragment Summary</title>"+
					"<meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\" />"+
					"<script type=\"text/javascript\" src=\"../../../../../Jmol.js\"></script>"+
					"<script type=\"text/javascript\" src=\"../../../../../summary.js\"></script>"+
					"<script language=\"JavaScript\" type=\"text/javascript\">function changeTwodSrc(pngSrc) {document.getElementById('twod').src=pngSrc;}</script>"+
					"<link href=\"../../../../../display/summary.css\" rel=\"stylesheet\" type=\"text/css\" media=\"all\">"+
				"</head>"+
				"<body>"+
					"<script type=\"text/javascript\">jmolInitialize(\"../../../../../\");</script>"+
					"<div id=\"top\">" +
						"<h1 class=\"fragments\">"+
							"Fragments"+
						"</h1>"+
					"</div>"+
					"<div id=\"content\">"+
						"<div id=\"frags\">"+
							fragmentContent+
						"</div>"+
					"</div>"+
					"<div id=\"rendering\">"+
						"<div id=\"navigation\">"+
							"<div id=\"moietyStructNav\">"+
								"<button onclick=\"goToStructure(1);\"><<-</button>"+
								"<button onclick=\"previousStructure();\"><-</button>"+
								"<button onclick=\"nextStructure();\">-></button>"+
								"<button onclick=\"goToStructure("+fragCount+");\">->></button>"+
							"</div>"+
						"</div>"+
						"<div id=\"jmolContainer\">"+
							"<script type=\"text/javascript\">setMaxStructNum("+fragCount+");highlightFirstStructure();</script>"+
							"<script type=\"text/javascript\">"+
							  "jmolApplet([360, 280], "+this.jmolLoadForFrags+");"+
							"</script>"+
							"<img id=\"twod\" src="+this.imageLoadForFrags+" width=\"358\" height=\"278\" alt=\"\" />"+
						"</div>"+
					"</div>"+
				"</body>"+
			"</html>";	
		return page;
	}
}
