<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<title>${pageTitle}</title>
		<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
		<meta name="description" content="" />
		<meta name="keywords" content="" />
		<meta http-equiv="imagetoolbar" content="no" />
		<link href="../styles.css" rel="stylesheet" type="text/css"
			media="all" />
	</head>
	<body>
		<div id="allcontainer">
			<#include "/html/top-header.ftl">
			<#include "/html/bottom-header.ftl">
			<div id="main">
				<div id="contentcontainer">
					<div id="content">
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
							It is worth noting that sometimes the unit-cell doesn't appear in
							the Jmol applet. To force it to be shown you should right-click
							on the applet and follow the options style&gt;unitcell&gt;dotted.
						</p>
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
							<span style="color: blue;">[[P]]</span>
							indicates that the structure is polymeric.
						</p>
						<p>There are some other points worth noting:</p>
						<ul class="normal">
							<li>For inorganic structures or polymeric organometallic
								structures, we generate the unit cell with all atoms inside. For
								all other structures we generate the unique discrete molecules
								in the unit cell.</li>
							<li>No 2D images are generated for inorganic or polymeric
								organometallic structures.</li>
							<li>Bond orders and charges are not added to inorganic or
								polymeric organometallic structures.</li>
							<li>If the system cannot resolve the disorder for a molecule in
								the crystal structure, then bond orders and charges are not
								added to it. We do not generate SMILES or InChIs for these
								structures either.</li>
						</ul>
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
