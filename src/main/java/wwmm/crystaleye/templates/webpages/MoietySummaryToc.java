package wwmm.crystaleye.templates.webpages;

public class MoietySummaryToc {
	
	private String moiContent;
	private String moiCount;
	private String jmolLoadForMois;
	private String imageLoadForMois;

	public MoietySummaryToc(String moiContent, String moiCount, String jmolLoadForMois, String imageLoadForMois) {
		super();
		this.moiContent = moiContent;
		this.moiCount = moiCount;
		this.jmolLoadForMois = jmolLoadForMois;
		this.imageLoadForMois = imageLoadForMois;
	}

	public String getWebpage() {
		String page = "<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>"+
			"<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">"+
			"<html xmlns=\"http://www.w3.org/1999/xhtml\">"+
				"<head>"+
					"<title>Moieties Summary</title>"+
					"<meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\" />"+
					"<script type=\"text/javascript\" src=\"../../../Jmol.js\"></script>"+
					"<script type=\"text/javascript\" src=\"../../../summary.js\"></script>"+
					"<script language=\"JavaScript\" type=\"text/javascript\">function changeTwodSrc(pngSrc) {document.getElementById('twod').src=pngSrc;}</script>"+
					"<link href=\"../../../display/summary.css\" rel=\"stylesheet\" type=\"text/css\" media=\"all\">"+
				"</head>"+
				"<body>"+
					"<script type=\"text/javascript\">jmolInitialize(\"../../../\");</script>"+
					"<div id=\"top\">" +
						"<h1 class=\"fragments\">"+
							"Moieties"+
						"</h1>"+
					"</div>"+
					"<div id=\"content\">"+
						"<div id=\"frags\">"+
							moiContent+
						"</div>"+
					"</div>"+
					"<div id=\"rendering\">"+
						"<div id=\"navigation\">"+
							"<div id=\"moietyStructNav\">"+
								"<button onclick=\"goToStructure(1);\"><<-</button>"+
								"<button onclick=\"previousStructure();\"><-</button>"+
								"<button onclick=\"nextStructure();\">-></button>"+
								"<button onclick=\"goToStructure("+moiCount+");\">->></button>"+
							"</div>"+
						"</div>"+
						"<div id=\"jmolContainer\">"+
							"<script type=\"text/javascript\">setMaxStructNum("+moiCount+");highlightFirstStructure();</script>"+
							"<script type=\"text/javascript\">"+
							  "jmolApplet([360, 280], "+this.jmolLoadForMois+");"+
							"</script>"+
							"<img id=\"twod\" src="+this.imageLoadForMois+" width=\"358\" height=\"278\" alt=\"\" />"+
						"</div>"+
					"</div>"+
				"</body>"+
			"</html>";
		return page;
	}
}
