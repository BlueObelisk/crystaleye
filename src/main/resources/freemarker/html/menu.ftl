<div id="menu">
	<ul class="menu">
		<li <#if currentMenuSelected == "home">class="selected"</#if> >
			<a href="${crystaleyeSiteUrl}/index.html" shape="rect">
				<span>Home</span>
			</a>
		</li>
		<li <#if currentMenuSelected == "search">class="selected"</#if> >
			<a href="${wwmmSandboxSiteUrl}/crystaleye-search/"
				shape="rect">
				<span>Search</span>
			</a>
		</li>
		<li <#if currentMenuSelected == "summary">class="selected"</#if> >
			<a href="${crystaleyeSiteUrl}/summary/index.html" shape="rect">
				<span>Browse Issues</span>
			</a>
		</li>
		<li <#if currentMenuSelected == "feeds">class="selected"</#if> >
			<a href="${crystaleyeSiteUrl}/feed/index.html" shape="rect">
				<span>RSS feeds</span>
			</a>
		</li>
		<li <#if currentMenuSelected == "bondlengths">class="selected"</#if> >
			<a href="${crystaleyeSiteUrl}/bondlengths/index.html" shape="rect">
				<span>Bond Lengths</span>
			</a>
		</li>
		<li <#if currentMenuSelected == "greasemonkey">class="selected"</#if> >
			<a href="${crystaleyeSiteUrl}/gm/index.html" shape="rect">
				<span>Greasemonkey</span>
			</a>
		</li>
		<li class="last" <#if currentMenuSelected == "faq">class="selected"</#if> >
			<a href="${crystaleyeSiteUrl}/faq/index.html" shape="rect">
				<span>FAQ</span>
			</a>
		</li>
	</ul>
</div>
