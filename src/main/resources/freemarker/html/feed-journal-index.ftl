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
							<b>Feeds by journal:</b>
						</p>
						<p>These feeds contain the latest published structures from a
							particular journal:</p>
						<ul class="normal">
							<#list publishers as publisher>
							<li>
								<span style="font-weight: bold; text-decoration: underline;">${publisher.title}</span>
							</li>
							<#list journals as journal>
							<ul class="normal">
								<li style="font-weight: bold;">
									<a href="./${publisher.abbreviation}/${journal.abbreviation}/feed.xml"
										shape="rect">
										${journal.title}
									</a>
								</li>
							</ul>
							</#list>
							</#list>
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
