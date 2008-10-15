package wwmm.crystaleye.templates.webpages;

import static wwmm.crystaleye.CrystalEyeConstants.COMPLETE_CML_MIME;
import static wwmm.crystaleye.CrystalEyeConstants.RAW_CML_MIME;
import wwmm.crystaleye.util.CrystalEyeUtils.DisorderType;

public class SingleCifSummary {
	
	private String publisherTitle;
	private String journalTitle;
	private String year;
	private String issueNum;
	private String title;
	private String id;
	private String contactAuthor;
	private String authorEmail;
	private String doi;
	private String compoundClass;
	private String dateRecorded;
	private String formulaSum;
	private String formulaMoi;
	private String cellSetting;
	private String groupHM;
	private String groupHall;
	private String temp;
	private String rObs;
	private String rAll;
	private String wRObs;
	private String wRAll;
	private String crystComp;
	private String inchi;
	private String smiles;
	private DisorderType disordered;
	boolean polymeric;
	private String originalCifUrl;
	boolean notAllowedToRecommunicateCif;

	public SingleCifSummary(String publisherTitle, String journalTitle, String year, String issueNum, String title, String id, String contactAuthor, String authorEmail, String doi, String compoundClass, String dateRecorded, String formulaSum, String formulaMoi, String cellSetting, String groupHM, String groupHall, String temp, String obs, String all, String obs2, String all2, String crystComp, String inchi, String smiles, DisorderType disordered, boolean polymeric, String originalCifUrl, boolean notAllowedToRecommunicateCif) {
		super();
		this.publisherTitle = publisherTitle;
		this.journalTitle = journalTitle;
		this.year = year;
		this.issueNum = issueNum;
		this.title = title;
		this.id = id;
		this.contactAuthor = contactAuthor;
		this.authorEmail = authorEmail;
		this.doi = doi;
		this.compoundClass = compoundClass;
		this.dateRecorded = dateRecorded;
		this.formulaSum = formulaSum;
		this.formulaMoi = formulaMoi;
		this.cellSetting = cellSetting;
		this.groupHM = groupHM;
		this.groupHall = groupHall;
		this.temp = temp;
		rObs = obs;
		rAll = all;
		wRObs = obs2;
		wRAll = all2;
		this.crystComp = crystComp;
		this.inchi = inchi;
		this.smiles = smiles;
		this.disordered = disordered;
		this.polymeric = polymeric;
		this.originalCifUrl = originalCifUrl;
		this.notAllowedToRecommunicateCif = notAllowedToRecommunicateCif;
	}

	public SingleCifSummary() {
		;
	}
	
	private String getDisorderedSection() {
		String output = "";
		if (disordered.equals(DisorderType.UNPROCESSED)) {
			output = "<div style=\"border: 1px dashed red; text-align: center; font-size: 12px; background: #ffbbbb;\">"+
					"<p style=\"color: red;\">We could not resolve the disorder in this crystal structure.</p>"+
				"</div>";
		} else if (disordered.equals(DisorderType.PROCESSED)) {
			output = "<div style=\"border: 1px dashed green; text-align: center; font-size: 12px; background: #99ff99;\">"+
					"<p style=\"color: green;\">The structure displayed is the major occupied structure from the crystal.</p>"+
				"</div>";
		}
		return output;
	}
	
	private String getPolymericSection() {
		if (polymeric) {
			return "<div style=\"border: 1px dashed blue; text-align: center; font-size: 12px; background: #bbccff;\">"+
				"<p style=\"color: blue;\">This structure is polymeric.</p>"+
				"</div>";
		} else {
			return "";
		}
	}
	
	public String getWebpage() {
		String disorderedSection = getDisorderedSection();
		String polymericSection = getPolymericSection();
//		 make smiles td
		String smilesSection = "";
		if (!"".equals(smiles) && smiles != null) {
			smilesSection = "<tr>"+
								"<td>"+
									"<p class=\"title\">SMILES:</p>"+
								"</td>"+
								"<td>"+
									"<p>"+smiles+"</p>"+
								"</td>"+
							"</tr>";
		}
		String inchiSection = "";
		if (!"".equals(inchi) && inchi != null) {
			inchiSection = "<tr>"+
								"<td>"+
									"<p class=\"title\">InChI:</p>"+
								"</td>"+
								"<td>"+
									"<p>"+
										inchi+
									"</p>"+
								"</td>"+
							"</tr>";
		}
		String cifLinkSection = "";
		if (!notAllowedToRecommunicateCif) {
			cifLinkSection = "<tr>"+
								"<td colspan=\"2\" bgcolor=\"#c0ffc0\">"+
									"CIF ("+
									"<a href=\"./"+id+".cif\">cached</a>"+
									" / "+	
									"<a href=\""+originalCifUrl+"\">original</a>"+
									")"+
								"</td>"+
							"</tr>";
		}
		
		String page = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">"+
			"<html>"+
			"<head>" +
				"<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />" +
				"<title>"+title+"</title>" +
				"<link rel=\"stylesheet\" type=\"text/css\" href=\"../../../display/eprints.css\" title=\"screen stylesheet\" media=\"screen\" />" +
				"<link rel=\"Top\" href=\"http://wwmm.ch.cam.ac.uk\" />" +
				"<script src=\"../../../Jmol.js\" type=\"text/javascript\"></script>"+
				"<script src=\"../../../summary.js\" type=\"text/javascript\"></script>"+
				"<script type=\"text/javascript\">var structureId=\""+id+COMPLETE_CML_MIME+"\";</script>"+
			"</head>"+
			"<body topmargin=\"0\" rightmargin=\"0\" leftmargin=\"0\" height=\"100%\" id=\"page_abstract\" bgcolor=\"#ffffff\" marginheight=\"0\" marginwidth=\"0\" text=\"#000000\">"+
				"<script type=\"text/javascript\">"+
					"jmolInitialize(\"../../../\");"+
				"</script>"+
				"<table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" height=\"100%\" width=\"800\">"+
					"<tbody>"+
						"<tr>"+
							"<td align=\"left\">"+
								"<h1 class=\"pagetitle\">"+
									"<span class=\"field_title\" style=\"font-size: 18px; text-align: center; display:block;\">"+
											title+
									"</span>"+
								"</h1>"+
								"<div style=\"width: 395px; float: left; margin-right: 5px;\">"+
									"<!-- Open Knowledge Link -->"+
										"<a href=\"http://okd.okfn.org/\">"+
									  	"<img alt=\"This material is Open Knowlege\" border=\"0\" src=\"http://m.okfn.org/images/ok_buttons/od_80x15_red_green.png\" /></a>"+
									  	"<br /><br />"+
								  	"<!-- /Open Knowledge Link -->"+
									"<a href=\"../../../index.html\">&lt;&lt; Table of Contents</a><br /><br />"+
									"<strong>Publisher: </strong>"+
									publisherTitle+
									"<br />"+
									"<strong>Journal: </strong>"+
									journalTitle+
									"<br />"+
									"<strong>Year/Issue: </strong>"+
									year+"/"+issueNum+
									"<br /><br />"+
									"<strong>Article (via DOI): </strong>"+
									"<span style=\"font-size: 0.9em;\">"+
										"<a href=\"http://dx.doi.org/"+doi+"\">"+
											doi+
										"</a>"+
									"</span>"+
									"<br />"+
									"<strong>Compound Class:</strong>"+
									compoundClass+
									"<br />"+
									"<strong>Date Recorded:</strong>"+
									dateRecorded+
									"<br />"+
									"<p>"+
										"<strong>Contact Author: </strong>"+
											contactAuthor+
										"<br />"+
										"<strong>e-mail: </strong>"+
										"<a href=\"mailto:"+authorEmail.toLowerCase()+"\">"+authorEmail.toLowerCase()+"</a>"+
									"</p>"+
									"<table border=\"0\" cellpadding=\"3\">"+
										"<tbody>"+
											"<tr>"+
												"<td colspan=\"2\">"+
													"<h3>"+
														"Data collection parameters"+
													"</h3>"+
												"</td>"+
											"</tr>"+
											"<tr>"+
												"<td bgcolor=\"#d0d0d0\">"+
													"Chemical formula sum"+
												"</td>"+
												"<td bgcolor=\"#d0d0d0\">"+
													"<span class='formula'>"+
														formulaSum+
													"</span>"+
												"</td>"+
											"</tr>"+
											"<tr>"+
												"<td bgcolor=\"#d0d0d0\">"+
													"Chemical formula moiety"+
												"</td>"+
												"<td bgcolor=\"#d0d0d0\">"+
													formulaMoi+
												"</td>"+
											"</tr>"+
											"<tr>"+
												"<td bgcolor=\"#d0d0d0\">"+
													"Crystal system"+
												"</td>"+
												"<td bgcolor=\"#d0d0d0\">"+
													cellSetting+
												"</td>"+
											"</tr>"+
											"<tr>"+
												"<td bgcolor=\"#d0d0d0\">"+
													"Space group H-M"+
												"</td>"+
												"<td bgcolor=\"#d0d0d0\">"+
													groupHM+
												"</td>"+
											"</tr>"+
											"<tr>"+
												"<td bgcolor=\"#d0d0d0\">"+
													"Space group Hall"+
												"</td>"+
												"<td bgcolor=\"#d0d0d0\">"+
													groupHall+
												"</td>"+
											"</tr>"+
											"<tr>"+
												"<td bgcolor=\"#d0d0d0\">"+
													"Data collection temperature"+
												"</td>"+
												"<td bgcolor=\"#d0d0d0\">"+
													temp+
												"</td>"+
											"</tr>"+
											"<tr>"+
												"<td colspan=\"2\">"+
													"<h3>Refinement results</h3>"+
												"</td>"+
											"</tr>"+
											"<tr>"+
												"<td bgcolor=\"#d0d0d0\">"+
													"R Factor (Obs)"+
												"</td>"+
												"<td bgcolor=\"#d0d0d0\">"+
													rObs+
												"</td>"+
											"</tr>"+
											"<tr>"+
												"<td bgcolor=\"#d0d0d0\">"+
													"R Factor (All)"+
												"</td>"+
												"<td bgcolor=\"#d0d0d0\">"+
													rAll+
												"</td>"+
											"</tr>"+
											"<tr>"+
												"<td bgcolor=\"#d0d0d0\">"+
													"Weighted R Factor (Obs)"+
												"</td>"+
												"<td bgcolor=\"#d0d0d0\">"+
													wRObs+
												"</td>"+
											"</tr>"+
											"<tr>"+
												"<td bgcolor=\"#d0d0d0\">"+
													"Weighted R Factor (All)"+
												"</td>"+
												"<td bgcolor=\"#d0d0d0\">"+
													wRAll+
												"</td>"+
											"</tr>"+
											"<tr>"+
												"<td colspan=\"2\">"+
													"<h2>Available Resources</h2>"+
												"</td>"+
											"</tr>"+
												crystComp+
											"<tr>"+
												"<td colspan=\"2\">"+
													"<h3>Result files</h3>"+
												"</td>"+
											"</tr>"+
											"<tr>"+
												"<td colspan=\"2\" bgcolor=\"#c0ffc0\">"+
													"<a href=\"./"+id+RAW_CML_MIME+"\">"+
														"Raw CML"+
													"</a>"+
												"</td>"+
											"</tr>"+
											"<tr>"+
												"<td colspan=\"2\" bgcolor=\"#c0ffc0\">"+
													"<a href=\"./"+id+COMPLETE_CML_MIME+"\">"+
														"Complete CML"+
													"</a>"+
												"</td>"+
											"</tr>"+
											cifLinkSection+
											"<tr>"+
												"<td colspan=\"2\">"+
													"<h3>Validation</h3>"+
												"</td>"+
											"</tr>"+
											"<tr>"+
												"<td colspan=\"2\" bgcolor=\"#c0ffc0\">"+
													"<a href=\"./"+id+".calculated.checkcif.html\">"+
														"CheckCIF"+
													"</a>"+
												"</td>"+
											"</tr>"+
											"<tr>"+
												"<td colspan=\"2\">"+
													"<h3>"+
														"Images"+
													"</h3>"+
												"</td>"+
											"</tr>"+
											"<tr>"+
												"<td colspan=\"2\" bgcolor=\"#c0ffc0\">"+
													"<a href=\"./"+id+".platon.jpeg\">"+
														"Ellipsoid"+
													"</a>"+
												"</td>"+
											"</tr>"+
										"</tbody>"+
									"</table>"+
								"</div>"+
								"<div style=\"width: 395px; float: right;\">"+
									disorderedSection+
									polymericSection+
									"<p>"+
										"<script type=\"text/javascript\">"+
											"jmolApplet(395,\"load./"+id+COMPLETE_CML_MIME+"; set forceAutoBond true; \");"+
										"</script>"+
										"<form action=\"javascript:showUnitCells()\">"+
											"<p style=\"margin-bottom:5px;\">Show no. of unit cells along axis:</p>"+
											"<p style=\"display: inline; font-family: courier;\">a: </p><input style=\"width: 25px;\" type=\"text\" id=\"aAxis\" value=\"1\"/><br />"+
											"<p style=\"display: inline; font-family: courier;\">b: </p><input style=\"width: 25px;\" type=\"text\" id=\"bAxis\" value=\"1\"/><br />"+
											"<p style=\"display: inline; font-family: courier;\">c: </p><input style=\"width: 25px;\" type=\"text\" id=\"cAxis\" value=\"1\"/><br />"+
											"<button type=\"submit\" name=\"enter\" style=\"margin-top:5px;\">enter</button>"+
										"</form>"+
										"<form action=\"javascript:showcmd()\">"+
											"<p style=\"margin-bottom:5px;\">Enter Jmol script:</p>"+
											"<textarea autocomplete=\"off\" id=\"textArea\" cols=\"40 \" rows=\"4\" wrap=\"off\">load./"+id+COMPLETE_CML_MIME+"</textarea>"+
											"<button type=\"submit\" name=\"enter\" style=\"margin-top:5px;\">enter</button>"+
										"</form>"+
										"<button onclick=\"javascript:loadInJmol('load./"+id+COMPLETE_CML_MIME+"');\">reset</button>"+
									"</p>"+
								"</div>"+
								"<br />"+
							"</td>"+
						"</tr>"+
						"<tr>"+
							"<td>"+
								"<table id=\"identifiers\" style=\"margin-top: 20px;\">"+
									inchiSection+
									smilesSection+
								"</table>"+
							"</td>"+
						"</tr>"+
					"</tbody>"+
				"</table>"+
			"</body>"+
		"</html>";
		return page;
	}
}
