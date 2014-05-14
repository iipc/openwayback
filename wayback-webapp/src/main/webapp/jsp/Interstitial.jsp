<%@   page import="org.archive.wayback.webapp.AccessPoint"
%><%@ page import="org.archive.wayback.util.StringFormatter"
%><%@ page import="java.util.Date"
%><%
String toUrl = request.getParameter(AccessPoint.INTERSTITIAL_TARGET);
if(toUrl == null) {
	response.setStatus(400);
%>
    Bad request. require argument <%= AccessPoint.INTERSTITIAL_TARGET %>
<%
} else {
    String secsS = request.getParameter(AccessPoint.INTERSTITIAL_SECONDS);
    String dateString = request.getParameter(AccessPoint.INTERSTITIAL_DATE);
    String replayUrl = request.getParameter(AccessPoint.INTERSTITIAL_URL);
    long dateLong = 0;
    int secs = 5;
	try {
		secs = Integer.parseInt(secsS);
	} catch (NumberFormatException e) {
	}
    try {
        dateLong = Long.parseLong(dateString);
    } catch (NumberFormatException e) {
    }
	if(secs < 1) {
		secs = 5;
	}
	StringFormatter f = new StringFormatter(null);
    String safeReplayUrl = null;
    String prettyReplayDate = null;
    if(replayUrl != null) {
        safeReplayUrl = f.escapeHtml(replayUrl);    	
    }
    if(dateLong > 0) {
    	Date rd = new Date(dateLong);
    	prettyReplayDate = 
    		f.format("{0,date,H:mm:ss MMM d, yyyy}",rd);
    }
	String safeTargetUrl = f.escapeHtml(toUrl);
	String safeTargetUrlJS = f.escapeJavaScript(toUrl);
	%>
    <jsp:include page="/WEB-INF/template/UI-header.jsp" flush="true" />
        <!--  dateLong <%= dateLong %> -->
        <div id="positionHome">
            <section>
            <div id="logoHome">
                <a href="/index.jsp"><h1><span>OpenWayback</span></h1></a>
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
	    <h2 class="blue">Welcome to OpenWayback.</h2>
		<p class="code">Loading...</p>
		<%
		if(safeReplayUrl != null && prettyReplayDate != null) {
			%>
        <p class="code shift"><%= safeReplayUrl %></p>
        <p class="code">as close to the date:</p>
        <p class="code shift"><%= prettyReplayDate %></p>
        <p class="code">as is available..</p>
			<%
		}
		%>
		<p class="impatient"><a href="<%= safeTargetUrl %>">Impatient?</a></p>
<%	
}
%>

            </div>
            </section>
            <div id="errorBorder"></div>

<jsp:include page="/WEB-INF/template/UI-footer.jsp" flush="true" />