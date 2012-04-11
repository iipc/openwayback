<%@ include file="header.inc"%>

<p>Enter a URL or SURT fragment to edit:</p>
<form action="<c:url value="/admin"/>" method="GET"><input size="50" name="surt"
	value="http://(" /> <input type="submit" value="Go!" />
</form>

<h2>Browse</h2>

<ul>
	<li><a href="admin?surt=http://(">http://(</a></li>
  <li><a href="admin?surt=https://(">https://(</a></li>
  <li><a href="admin?surt=ftp://(">ftp://(</a></li>
</ul>

<%@ include file="footer.inc"%>
