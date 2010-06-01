<%@   page import="org.archive.wayback.webapp.AccessPoint"
%><%@ page import="org.archive.wayback.util.StringFormatter"
%><%
String toUrl = request.getParameter(AccessPoint.INTERSTITIAL_TARGET);
if(toUrl == null) {
	response.setStatus(400);
%>
    Bad request. require argument <%= AccessPoint.INTERSTITIAL_TARGET %>
<%
} else {
	String secsS = request.getParameter(AccessPoint.INTERSTITIAL_SECONDS);
	int secs = 5;
	try {
		secs = Integer.parseInt(secsS);
	} catch (NumberFormatException e) {
		
	}
	if(secs < 1) {
		secs = 5;
	}
	StringFormatter f = new StringFormatter(null,null);
	String safeTargetUrl = f.escapeHtml(toUrl);
	String safeTargetUrlJS = f.escapeJavaScript(toUrl);
	%>
	<script type="text/javascript">
	function go() {
		document.location.href = "<%= safeTargetUrlJS %>";
	}
	window.setTimeout("go()",<%= secs * 1000 %>);
	</script>
		<h2>Redirecting...</h2>
		<p>
		  Thanks for visiting the Wayback Machine. We're about to redirect you
		  to the page you requested:
		</p>
		<p><%= safeTargetUrl %></p>
		<p>
		  in <%= secs %> seconds.
		  Click <a href="<%= safeTargetUrl %>">here</a> to go now.
		</p>
<%	
}
%>
<