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
						<h1 class="faq">RSS feeds</h1>
						<p>Follow the links below to find RSS feeds summarizing the latest
							crystallography in various categories. Note that the feeds
							contain entries for the crystallography from the last 21 days for
							each category.</p>
						<p>
							For each category you can either subscribe to the RSS feed, or
							the CMLRSS feed. The RSS feeds provides the latest
							crystallographic content by pointing you to a webpage summarizing
							the structure (e.g.
							<a
								href="../summary/acta/e/2007/04-00/data/at2230/at2230sup1_IV/at2230sup1_IV.cif.summary.html"
								shape="rect">here</a>
							). The CMLRSS feed has these webpage links, but each entry also
							contains the CML of the corresponding structure. This can be read
							and displayed directly by CMLRSS readers.
						</p>
						<p>We support different versions of RSS, so all categories can be
							subscribed to in either RSS versions 1 or 2 or Atom version 1.
						</p>
						<span class="ulheader" style="font-weight: bold;">Categories:</span>
						<ul class="normal">
							<li>
								<a href="./all/index.html" shape="rect">All</a>
							</li>
							<li>
								<a href="./journal/index.html" shape="rect">Journal</a>
							</li>
							<li>
								<a href="./class/index.html" shape="rect">Compound Class</a>
							</li>
							<li>
								<a href="./atoms/index.html" shape="rect">Atoms</a>
							</li>
							<li>
								<a href="./bonds/index.html" shape="rect">Bonds</a>
							</li>
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
