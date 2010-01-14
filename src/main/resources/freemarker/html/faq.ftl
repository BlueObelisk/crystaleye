<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<title>${pageTitle}</title>
		<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
		<meta name="description" content="" />
		<meta name="keywords" content="" />
		<meta http-equiv="imagetoolbar" content="no" />
		<link href="${pathToRoot}styles.css" rel="stylesheet" type="text/css"
			media="all" />
	</head>
	<body>
		<div id="allcontainer">
			<#include "/html/top-header.ftl">
			<#include "/html/bottom-header.ftl">
			<div id="main">
				<div id="contentcontainer">
					<div id="content">
						<h1 class="faq">Frequently Asked Questions</h1>
						<p class="faqIndex">
							<b>The data</b>
						</p>
						<p class="faqIndex">
							<a href="#crystcomefrom" shape="rect">Where does the crystallography
								come from?</a>
						</p>
						<p class="faqIndex">
							<a href="#aggcifanywhere" shape="rect">Where are you currently
								aggregating the crystallography from?</a>
						</p>
						<p class="faqIndex">
							<a href="#howsitgoing" shape="rect">How is the aggregation going?</a>
						</p>
						<p class="faqIndex">
							<a href="#authorcopyright" shape="rect">Do any authors wish their data
								to be copyrighted and withheld from the community?</a>
						</p>
						<br />
						<p class="faqIndex">
							<b>The CrystalEye system</b>
						</p>
						<p class="faqIndex">
							<a href="#converteverycif" shape="rect">Can you convert every CIF file
								you find into CML?</a>
						</p>
						<p class="faqIndex">
							<a href="#crystaleyemanagechemistry" shape="rect">How does CrystalEye
								manage chemistry?</a>
						</p>
						<p class="faqIndex">
							<a href="#howdatarecommunicated" shape="rect">How is the data
								recommunicated?</a>
						</p>
						<p class="faqIndex">
							<a href="#renderingsoftware" shape="rect">What 2D and 3D rendering
								software do you use?</a>
						</p>
						<p class="faqIndex">
							<a href="#othersoftware" shape="rect">What other software have you
								used?</a>
						</p>
						<p class="faqIndex">
							<a href="#allautomated" shape="rect">So it's all automated?</a>
						</p>
						<p class="faqIndex">
							<a href="#relatedccdc" shape="rect">How is this related to the
								Cambridge Crystallographic Data Centre (CCDC) and the CSD?</a>
						</p>
						<br />
						<p class="faqIndex">
							<b>Browsing the crystallography</b>
						</p>
						<p class="faqIndex">
							<a href="#howdoibrowse" shape="rect">How do I browse the
								crystallography?</a>
						</p>
						<p class="faqIndex">
							<a href="#colouredsymbols" shape="rect">What do the coloured ((DP)),
								((DU)) and [[P]] symbols mean on the issue pages?</a>
						</p>
						<p class="faqIndex">
							<a href="#differentclasses" shape="rect">What are the differences in
								the way organic, inorganic and organometallic structures are
								represented?</a>
						</p>
						<p class="faqIndex">
							<a href="#notaccessallstructs" shape="rect">Why can I not get access
								to the structures before year X?</a>
						</p>
						<br />
						<p class="faqIndex">
							<b>Miscellaneous</b>
						</p>
						<p class="faqIndex">
							<a href="#workingonproject" shape="rect">Who has worked/is working on
								this project?</a>
						</p>
						<br />
						<p style="font-weight: bold;">More to come soon (or even sooner if you email with a
							question) ...</p>
						<hr />
						<br />
						<h2>The data</h2>
						<a name="crystcomefrom" shape="rect">
							<span class="ulheader" style="font-weight: bold;">Where does the crystallography
								come from?</span>
						</a>
						<p>
							There are thousands of crystal structures published in online
							journals every month. When an author has a structure published,
							they are obliged to provide the (complete) output of the
							structure elucidation experiment (in the form of a
							<a href="http://www.iucr.org/iucr-top/cif/index.html" shape="rect">CIF</a>
							file) as supplementary material.
						</p>
						<p>
							As this supplementary data is a set of facts and is not part of
							the article full-text it does not fall under the copyright, and
							it
							<em>should</em>
							therefore be free to both view and download. We have created a
							web spider which 'listens' for new journal issues to be published
							and checks them for any CIF files. Upon finding a CIF file, it is
							downloaded and the data within is then recommunicated by passing
							it through the CrystalEye system.
						</p>
						<p>In the near future, we hope to extend the system to aggregate
							from Institutional Repositores, and also provide a method for
							self deposition.</p>
						<a name="aggcifanywhere" shape="rect">
							<span class="ulheader" style="font-weight: bold;">Where are you currently
								aggregating the crystallography from?</span>
						</a>
						<p>We are doing this for all publishers (we know of) who provide
							links to the supplementary CIFs from their sites, namely RSC,
							IUCr, ACS and the Chemical Society of Japan (only Chemistry
							Letters) and Elsevier (only Polyhedron). Wiley, Springer and
							Blackwell do not expose CIFs (thereby depriving the scientific
							community of data). We have also merged CIFs from the
							Crystallography Open Database with the data from the publisher's
							websites.</p>
						<p>However, even if the CIF files are free to download, that
							doesn't mean the website owner looks kindly on you sending a web
							spider to do it for you. Both the Royal Society of Chemistry and
							the International Union of Crystallography have kindly allowed us
							to do this. For other publishers we aggregate the CIFs by hand
							before passing them through the CrystalEye system.</p>
						<a name="howsitgoing" shape="rect">
							<span class="ulheader" style="font-weight: bold;">How is the aggregation going?</span>
						</a>
						<p>
							So far we have aggregated around
							<b>100,000</b>
							CIF files in this way (as of September 4
							<sup>th</sup>
							2007).
						</p>
						<a name="authorcopyright" shape="rect">
							<span class="ulheader" style="font-weight: bold;">Do any authors wish their data
								to be copyrighted and withheld from the community?</span>
						</a>
						<p>We haven't found any. We suggest that authors publishing CIFs
							use a Creative Commons license, making their views clear. A
							simple way to do this would be to add this into the software that
							produces CIFs.</p>
						<br />
						<h2>The CrystalEye system</h2>
						<a name="converteverycif" shape="rect">
							<span class="ulheader" style="font-weight: bold;">Can you convert every CIF file
								you find into CML?</span>
						</a>
						<p>
							As long as the CIF conforms to the
							<a href="http://www.iucr.org/iucr-top/cif/index.html#spec"
								shape="rect">CIF specification</a>
							we have no trouble parsing it - of course in the real world this
							isn't always going to happen. Our CIF parser has a small set of
							heuristics in it to fix commonly encountered minor problems, but
							there are some that we can't (and wouldn't want to) recover from.
						</p>
						<a name="crystaleyemanagechemistry" shape="rect">
							<span class="ulheader" style="font-weight: bold;">How does CrystalEye manage
								chemistry?</span>
						</a>
						<p>An accurate modern structure contains all the atoms (including
							hydrogen). We check that the atom count is the same as mentioned
							in _chemical_formula_moiety. If not, we flag the structure as
							problematic. Otherwise we use the author-assigned change on the
							moieties and our own heuristics for assigning double bonds. That
							works in most cases. Sometimes the authors omit the charges on a
							charged structure in which case we try to guess, but ultimately
							it is the author's statement. (Hopefully CrystalEye will help to
							raise the quality of chemical information in CIFs). It is a
							tragedy that authors do not use the _chemical_conn* records which
							are specifically for this.</p>
						<a name="howdatarecommunicated" shape="rect">
							<span class="ulheader" style="font-weight: bold;">How is the data recommunicated?</span>
						</a>
						<p>
							For each journal issue passed through the system, a set of
							webpages are generated to allow easy browsing of the
							crystallography within. Both 2D and 3D renderings of the
							structures are provided (e.g.
							<a href="./summary/acta/e/04-00/index.html" shape="rect">here</a>
							). The webpages further down provide access to the original CIF,
							as well as all the data files generated by the system from it.
							These include:
						</p>
						<ul class="normal">
							<li>CML (which also contains the CheckCIF data, the article DOI
								and the InChI and SMILES for the structure, and additional
								chemistry such as bond orders and charges),</li>
							<li>CheckCIF,</li>
							<li>ellipsoid plot,</li>
							<li>bond length, angle and torsion summaries.</li>
							<li>fragments (ring-nuclei, metal ligands, metal centres, metal
								clusters, ring-ring and ring-terminus linkers).</li>
						</ul>
						<p>
							The system also maintains a number of RSS and CMLRSS feeds which
							summarize the latest crystallography to have been published. You
							can subscribe to feeds by class, journal, atoms or bonds. So, if
							for instance you were interested in Ag-C bonds, you could
							subscribe to one of the feeds
							<a href="./feed/bonds/Ag-index.html" shape="rect">here</a>
							. Alternatively, if you were interested in structures containing
							a particular atom, you would go
							<a href="./feed/atoms/index.html" shape="rect">here</a>
							.
						</p>
						<a name="renderingsoftware" shape="rect">
							<span class="ulheader" style="font-weight: bold;">What 2D and 3D rendering
								software do you use?</span>
						</a>
						<p>
							For the 2D layouts we use the
							<a href="http://almost.cubic.uni-koeln.de/cdk/cdk_top" shape="rect">CDK</a>
							and for 3D we use
							<a href="http://jmol.sourceforge.net/" shape="rect">Jmol</a>
							. Both have large active (and very helpful) communities based at
							<a href="http://www.sourceforge.net" shape="rect">Sourceforge</a>
							.
						</p>
						<a name="othersoftware" shape="rect">
							<span class="ulheader" style="font-weight: bold;">What other software have you
								used?</span>
						</a>
						<p>
							All of the software used and created in this project is Open
							Source. I won't list them all here, but if you are interested,
							you can read through a description of the project on our group
							wiki
							<a href="http://wwmm.ch.cam.ac.uk/wikis/wwmm/index.php/CMLCrystBase"
								shape="rect">here</a>
							then you'll get an idea of the different software used for the
							various parts of the system.
						</p>
						<a name="allautomated" shape="rect">
							<span class="ulheader" style="font-weight: bold;">So it's all automated?</span>
						</a>
						<p>Yes, nothing is done by hand. The aggregation, file and website
							generation and RSS updating are all done robotically.</p>
						<a name="relatedccdc" shape="rect">
							<span class="ulheader" style="font-weight: bold;">How is this related to the
								Cambridge Crystallographic Data Centre (CCDC) and the CSD?</span>
						</a>
						<p>It isn't. CCDC are a not-for-profit organisation which for many
							years has aggregated crystal structures from the literature and
							applied a variety of cleaning methods to the data. It has records
							going back about 50 years and is available by subscription only.
							At one stage many journals required that a copy of the
							crystallographic data associated with a publication were
							deposited directly with CCDC (often by the journal editors) and
							some journals still do this. However, with the advent of CIF and
							ePublishing, several journals, most notably those of IUCr, run
							online checking facilities and directly publish the CIF as
							supplemental data. It is this Open Data publication that makes
							CrystalEye possible. CCDC produce their own version of the CIF
							file (for subscribers or one-off queries) which includes a unique
							6- or 8-character REFCOD. CrystalEye does not use this CIF, or
							the REFCOD or any other data or software from CCDC.</p>
						<p>The main differences between CrystalEye and CCDC are:</p>
						<table style="border: 1px solid;">
							<tr>
								<td style="background-color: #ccc;" rowspan="1" colspan="1">
									<b>CrystalEye</b>
								</td>
								<td style="background-color: #ccc;" rowspan="1" colspan="1">
									<b>CCDC</b>
								</td>
							</tr>
							<tr>
								<td rowspan="1" colspan="1">CIFs from 1991-present</td>
								<td rowspan="1" colspan="1">comprehensive for organics and
									organometallics</td>
							</tr>
							<tr>
								<td rowspan="1" colspan="1">Robotically cleaned</td>
								<td rowspan="1" colspan="1">cleaning includes humans and machines</td>
							</tr>
							<tr>
								<td rowspan="1" colspan="1">Chemistry generated from CIF</td>
								<td rowspan="1" colspan="1">Chemistry added by humans and
									machines</td>
							</tr>
							<tr>
								<td rowspan="1" colspan="1">Links directly to journal articles</td>
								<td rowspan="1" colspan="1">unknown</td>
							</tr>
							<tr>
								<td rowspan="1" colspan="1">RSS feeds on daily basis</td>
								<td rowspan="1" colspan="1">unknown</td>
							</tr>
							<tr>
								<td rowspan="1" colspan="1">Includes inorganic structures</td>
								<td rowspan="1" colspan="1">does not include inorganic structures</td>
							</tr>
							<tr>
								<td rowspan="1" colspan="1">Conserves all data from original CIF</td>
								<td rowspan="1" colspan="1">unknown</td>
							</tr>
							<tr>
								<td rowspan="1" colspan="1">Metadata from journal free text when
									publishers allow</td>
								<td rowspan="1" colspan="1">unknown</td>
							</tr>
							<tr>
								<td rowspan="1" colspan="1">Per-journal browsing facility</td>
								<td rowspan="1" colspan="1">unknown</td>
							</tr>
						</table>
						<p>CrystalEye will be introducing chemistry, and data-based search
							as we include the backlog.</p>
						<br />
						<h2>Browsing the crystallography</h2>
						<a name="howdoibrowse" shape="rect">
							<span class="ulheader" style="font-weight: bold;">How do I browse the
								crystallography?</span>
						</a>
						<p>An issue table of contents looks something like the image
							below.</p>
						<div class="centre">
							<img src="./images/whole.gif" width="405" height="313" alt="" />
						</div>
						<p>In this, each row in the table corresponds to one crystal
							structure found at the issue. A row might look like:</p>
						<div class="centre">
							<img src="./images/row.gif" width="578" height="35" alt="" />
						</div>
						<p>If you click on the left hand column the structure represented
							by that row will be shown in the 2D and 3D rendering section at
							the bottom of the webpage. The middle column provides a link back
							to the original article (by using the DOI). If you click on the
							right hand column you will be taken to another webpage
							summarizing the structure in closer detail.</p>
						<p>You can also navigate through the structures using the
							navigation arrows above the 3D image. The arrows above the 2D
							image are slightly different. These are for browsing the 2D
							images of different molecules within the same crystal structure.
							At present there is a bug that may cause the unit-cell not to
							appear automatically in the Jmol applet. To force it to be shown
							you should right-click on the applet and follow the options
							style&gt;unitcell&gt;dotted.</p>
						<a name="colouredsymbols" shape="rect">
							<span class="ulheader" style="font-weight: bold;">What do the coloured ((DP)),
								((DU)) and [[P]] symbols mean on the issue pages?</span>
						</a>
						<p>
							<span style="color: green;">((DP))</span>
							or
							<span style="color: red;">((DU))</span>
							next to the structure formula in the left hand column indicates
							that the crystal structure in the corresponding CIF file is
							disordered.
							<span style="color: green;">((DP))</span>
							indicates that our system could resolve the disorder and display
							the major occupied structure from the crystal.
							<span style="color: red;">((DU))</span>
							indicates that the system could not understand the disorder, and
							hence the structure shown will still contain all of the disorder
							information.
						</p>
						<p>
							<span style="color: blue;">[[P]]</span>
							indicates that the structure is a polymeric organometal. This is
							noted as polymeric structures are represented by the system by
							displaying the unit cell with all of its atoms, rather than
							trying to display a discrete moiety.
						</p>
						<a name="differentclasses" shape="rect">
							<span class="ulheader" style="font-weight: bold;">What are the differences in the
								way organic, inorganic and organometallic structures are
								represented?</span>
						</a>
						<p>For inorganic or polymeric organometallic structures we
							generate the unit cell with all atoms inside. For all other
							structures we generate the unique discrete molecules in the unit
							cell.</p>
						<p>The structures for which we generate the unit cells with all
							atoms are not assigned bond orders or charges. There is obviously
							no point in generating 2D images for such structures either.</p>
						<p>Those structures for which we generate the unique discrete
							molecules are assigned bond orders and charges by the system. If
							the system is able to do this, then it also generates 2D
							structure diagrams of the molecules.</p>
						<a name="notaccessallstructs" shape="rect">
							<span class="ulheader" style="font-weight: bold;">Why can I not get access to the
								structures before year X?</span>
						</a>
						<p>Since a major aspect of CrystalEye is the RSS feeds for current
							awareness, we don't want to flood readers with all past data at
							once. We are therefore concentrating on the latest journals
							(mainly 2007) so that the CMLRSS can be tried out. Simultaneously
							we shall be adding a search facility to the retrospective data
							and shall announce this shortly. When that happens all entries
							will be retrievable.</p>
						<br />
						<h2>Miscellaneous</h2>
						<a name="workingonproject" shape="rect">
							<span class="ulheader" style="font-weight: bold;">Who has worked/is working on
								this project?</span>
						</a>
						<p>CrystalEye was started by Nick Day as part of his PhD, working
							under the supervision of Peter Murray-Rust at the Unilever Centre
							for Molecular Informatics, University of Cambridge, UK.</p>
						<p>The following people and organisations have provided
							significant assistance in the development of CrystalEye, in the
							form of advice, bug reports, testing, feedback etc.</p>
						<ul class="normal">
							<li>Jim Downing</li>
							<li>Simon 'Billy' Tyrrell</li>
							<li>Mark Holt (summer student funded by the International Union
								of Crystallography)</li>
							<li>The International Union of Crystallography</li>
							<li>The Royal Society of Chemistry</li>
						</ul>
						<p>
							If you have any comments or questions, please direct them to
							<a href="mailto:ned24cam.ac.uk" shape="rect">ned24@cam.ac.uk</a>
							.
						</p>
					</div>
					<!--end content-->
				</div>
				<!--end contentcontainer-->
				<#include "/html/menu.ftl">
			</div>
			<!--end main-->
			<#include "/html/footer.ftl">
		</div>
		<!-- end allcontainer-->
	</body>
</html>
