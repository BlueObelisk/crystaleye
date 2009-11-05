<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<title>${pageTitle}</title>
		<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
		<meta name="description" content="" />
		<meta name="keywords" content="" />
		<meta http-equiv="imagetoolbar" content="no" />
		<link href="../../styles.css" rel="stylesheet" type="text/css"
			media="all" />
	</head>
	<body>
		<div id="allcontainer">
			<#include "/html/top-header.ftl">
			<#include "/html/bottom-header.ftl">
			<div id="main">
				<div id="contentcontainer">
					<div id="content">
						<p>
							These feeds summarise
							<em>all</em>
							of the latest published structures found by the CrystalEye
							system:
						</p>
						<ul class="normal">
							<li style="font-weight: bold;">All</li>
							<ul class="normal">
								<li>
									RSS (
									<a href="./rss/rss_10/feed.xml" shape="rect">rss1</a>
									|
									<a href="./rss/rss_20/feed.xml" shape="rect">rss2</a>
									|
									<a href="./rss/atom_10/feed.xml" shape="rect">atom1</a>
									)
								</li>
							</ul>
						</ul>
						<br />
						<br />
						<br />
						<br />
						<br />
						<br />
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
