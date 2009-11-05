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
						<p>
							<b>CrystalEye Greasemonkey:</b>
						</p>
						<p>
							<span>
								<b>Installation:</b>
							</span>
							<br />
							<span>
								To use CrystalEye Greasemonkey, you will need to be using the
								<a href="http://www.mozilla-europe.org/en/products/firefox/"
									shape="rect">Mozilla Firefox</a>
								browser, and have the
								<a href="https://addons.mozilla.org/en-US/firefox/addon/748"
									shape="rect">Greasemonkey extension</a>
								installed (a very simple one-click installation from this
								webpage). Once you have done this, you can install the script
								from
								<a href="http://userscripts.org/scripts/show/11439" shape="rect">this
									page</a>
								at Userscripts (again, a one-click webpage installation).
							</span>
						</p>
						<p>
							You can then enable/disable the script by going to the 'Tools'
							menu in the top-left of the Firefox window, and selecting
							<em>Greasemonkey</em>
							and then click on
							<em>Manage User Scripts</em>
							. From this window you can enable/disable/uninstall the script.
						</p>
						<p>
							<span>
								<b>Explanation:</b>
							</span>
							<br />
							Greasemonkey is a Mozilla Firefox extension that allows users to
							install scripts that make on-the-fly changes to specific web
							pages. This Greasemonkey script (which was created by modifying
							<a
								href="http://baoilleach.blogspot.com/2007/04/add-quotes-from-postgenomic-and.html"
								shape="rect">Noel O'Boyle's</a>
							extension to
							<a
								href="http://pbeltrao.blogspot.com/2006/05/postgenomics-script-for-firefox-i-am.html"
								shape="rect">Pedro Beltr�o's</a>
							original script) is activated whenever you browse a publisher's
							website that is scraped by CrystalEye.
						</p>
						<p>If you were browsing the table of contents for a particular
							issue of Acta Crystallographica B, without the Greasemonkey
							script installed, the top of an abstract for an article
							containing crystallography might look like this:</p>
						<div class="centre">
							<img style="border: 1px solid #aaa;" src="./images/no-gm.gif"
								alt="Abstract without CrystalEye Greasemonkey alteration"
								height="150" width="486" />
						</div>
						<p>However, with the script installed, whenever a DOI is found in
							a page it asks the CrystalEye site whether it has this DOI listed
							as containing one or more crystal structures. If so, then
							CrystalEye returns its URLs of the descriptions of those
							structures corresponding to the given DOI. The Greasemonkey then
							adds the CrystalEye logo, with one or more numbered links
							pointing to the relevant URLs (as below):</p>
						<div class="centre">
							<img style="border: 1px solid #aaa;" src="./images/with-gm.gif"
								alt="Abstract after CrystalEye Greasemonkey alteration" height="163"
								width="489" />
						</div>
						<p>Clicking one of these links will take you to a page displaying
							the structure and providing links to its data:</p>
						<div class="centre">
							<img style="border: 1px solid #aaa;" src="./images/summary-page.gif"
								alt="Example of a crystal structure summary page." height="472"
								width="506" />
						</div>
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
