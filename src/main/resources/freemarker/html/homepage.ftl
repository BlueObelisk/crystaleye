<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<title>${pageTitle}</title>
		<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
		<meta name="description" content="" />
		<meta name="keywords" content="" />
		<meta http-equiv="imagetoolbar" content="no" />
		<link href="${pathToRoot}styles.css" rel="stylesheet" type="text/css" media="all" />
		<link rel="alternate" type="application/atom+xml" title="CrystalEye: All structures (Atom 1)"
			href="http://wwmm.ch.cam.ac.uk/crystaleye/feed/all/rss/atom_10/feed.xml" />
		<link rel="alternate" type="application/rss+xml" title="CrystalEye: All structures (RSS 1)"
			href="http://wwmm.ch.cam.ac.uk/crystaleye/feed/all/rss/rss_10/feed.xml" />
		<link rel="alternate" type="application/rss+xml" title="CrystalEye: All structures (RSS 2)"
			href="http://wwmm.ch.cam.ac.uk/crystaleye/feed/all/rss/rss_20/feed.xml" />
	</head>
	<body>
		<div id="allcontainer">
			<#include "/html/top-header.ftl">
			<#include "/html/bottom-header.ftl">
			<div id="main">
				<div id="contentcontainer">
					<div id="content">
						<p>
							<b>The aim of the CrystalEye project is to aggregate
								crystallography from web resources, and to provide methods to
								easily browse, search, and to keep up to date with the latest
								published information.</b>
						</p>
						<p>At present we are aggregating the crystallography from the
							supplementary data to articles at publishers websites. In the
							future we hope to extend this to aggregate from Institutional
							Repositories and also allow self deposition.</p>
						<p>
							The CrystalEye system is backed by a web-spider that scours
							specific locations for new crystallography each day. If the
							spider finds new data, then it is saved to our database and is
							then passed through the processing part of the system. The work
							this performs includes (elaborated on
							<a href="./faq/index.html#howdatarecommunicated" shape="rect">here</a>
							.):
						</p>
						<ul class="normal">
							<li>Converting the crystallographic data to CML (Chemical Markup
								Language).</li>
							<li>
								Generating webpages for easy
								<a href="./summary/index.html" shape="rect">browsing of the data</a>
								, with 2D and 3D renderings of the structures. Below is a
								screenshot of the CrystalEye table of contents for an issue of
								Acta Crystallographic Section E.
							</li>
						</ul>
						<div class="imageholder">
							<img src="./images/browse.gif" width="459" height="359" alt="" />
						</div>
						<ul class="normal">
							<li>
								Maintaining a set of
								<a href="./feed/index.html" shape="rect">RSS feeds</a>
								to allow users to be automatically notified when a structure of
								a particular category is found. Below is a screenshot of an RSS
								reader displaying the feed summarizing structures containing
								carbon-iodine bonds.
							</li>
						</ul>
						<div class="imageholder">
							<img src="./images/rss.gif" width="460" height="360" alt=""
								style="border: 1px solid #aaa;" />
						</div>
						<ul class="normal">
							<li>
								Maintaining a set of
								<a href="./bondlengths/index.html" shape="rect">clickable bond-length
									histograms</a>
								which allow users to easily find structures containing a bond
								type of a particular length.
							</li>
						</ul>
						<p>
							All of the data generated is 'Open', and is signified by the
							appearance of the
							<a href="http://okd.okfn.org/" shape="rect">
								<img alt="This material is Open Knowlege" border="0"
									src="http://m.okfn.org/images/ok_buttons/od_80x15_red_green.png" />
							</a>
							icon on all pages.
						</p>
						<p>
							For more explanation of the project, see the
							<a href="./faq/index.html" shape="rect">FAQ</a>
							section. Please direct all comments, suggestions and bugs to Nick
							Day at ned24@cam.ac.uk.
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
