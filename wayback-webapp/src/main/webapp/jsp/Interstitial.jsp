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
    <jsp:include page="/WEB-INF/template/UI-header.jsp" flush="true" />

        <div id="positionHome">
            <section>
            <div id="logoHome">
                <a href="/index.jsp"><h1><span>Internet Archive's Wayback Machine</span></h1></a>
            </div>
            </section>
            <section>
            <div id="error">
	<script type="text/javascript">
	function go() {
		document.location.href = "<%= safeTargetUrlJS %>";
	}
	window.setTimeout("go()",<%= secs * 1000 %>);
	</script>
	    <h2 class="blue">Hello.</h2>
		<p class="code">Loading...</p>
		<p class="code shift">[Month 00, 0000]</p>
		<p class="code">Loading...</p>
		<p class="code shift"><%= safeTargetUrl %></p>
		<p class="impatient"><a href="<%= safeTargetUrl %>">Impatient?</a></p>
<%	
}
%>

            </div>
            </section>
            <div id="errorBorder"></div>

<jsp:include page="/WEB-INF/template/UI-footer.jsp" flush="true" />