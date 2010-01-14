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
		<link rel="alternate" type="application/atom+xml" title="${journalFullTitle} (Atom 1)"
			href="http://wwmm.ch.cam.ac.uk/crystaleye/feed/journal/${publisherAbbreviation}/${journalAbbreviation}/rss/atom_10/feed.xml" />
		<link rel="alternate" type="application/rss+xml" title="${journalFullTitle} (RSS 1)"
			href="http://wwmm.ch.cam.ac.uk/crystaleye/feed/journal/${publisherAbbreviation}/${journalAbbreviation}/rss/rss_10/feed.xml" />
		<link rel="alternate" type="application/rss+xml" title="${journalFullTitle} (RSS 2)"
			href="http://wwmm.ch.cam.ac.uk/crystaleye/feed/journal/${publisherAbbreviation}/${journalAbbreviation}/rss/rss_20/feed.xml" />
	</head>
	<body>
		<div id="allcontainer">
			<#include "/html/top-header.ftl">
			<#include "/html/bottom-header.ftl">
			<div id="main">
				<div id="contentcontainer">
					<div id="content">
						<p style="font-weight: bold;">${publisherFullTitle}: ${journalFullTitle}</p>
						<p>List of issues:</p>
						<ul>
							<#if years??>
							<#list years as year>
							<li>
								<p>
									<span>${year.num}:</span>
									<#if year.issues??>
									<#list year.issues as issue>
									<a
										href="./${publisherAbbreviation}/${journalAbbreviation}/${year.num}/${issue.num}/index.html"
										shape="rect">${issue.num}</a>
									</#list>
									</#if>
								</p>
							</li>
							</#list>
							</#if>
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
