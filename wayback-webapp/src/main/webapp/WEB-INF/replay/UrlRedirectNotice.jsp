<%@ page language="java" pageEncoding="utf-8" contentType="text/html;charset=utf-8"%>
<%@ page import="java.util.Date" %>
<%@ page import="java.lang.StringBuffer" %>
<%@ page import="org.archive.wayback.archivalurl.ArchivalUrlDateRedirectReplayRenderer" %>
<%@ page import="org.archive.wayback.ResultURIConverter" %>
<%@ page import="org.archive.wayback.core.UIResults" %>
<%@ page import="org.archive.wayback.core.WaybackRequest" %>
<%@ page import="org.archive.wayback.core.CaptureSearchResult" %>
<%@ page import="org.archive.wayback.util.StringFormatter" %>
<%
UIResults results = UIResults.extractReplay(request);

WaybackRequest wbr = results.getWbRequest();
StringFormatter fmt = wbr.getFormatter();
CaptureSearchResult cResult = results.getResult();
ResultURIConverter uric = results.getURIConverter();

String sourceUrl = cResult.getOriginalUrl();
String targetUrl = cResult.getRedirectUrl();
String captureTS = cResult.getCaptureTimestamp();
Date captureDate = cResult.getCaptureDate();

String dateSpec = 
	ArchivalUrlDateRedirectReplayRenderer.makeFlagDateSpec(captureTS, wbr);

String targetReplayUrl = uric.makeReplayURI(dateSpec,targetUrl);

String safeSource = fmt.escapeHtml(sourceUrl);
String safeTarget = fmt.escapeHtml(targetUrl);
String safeTargetJS = fmt.escapeJavaScript(targetUrl);
String safeTargetReplayUrl = fmt.escapeHtml(targetReplayUrl);
String safeTargetReplayUrlJS = fmt.escapeJavaScript(targetReplayUrl);

String prettyDate = fmt.format("MetaReplay.captureDateDisplay",captureDate);
int secs = 5;

%>
<jsp:include page="/WEB-INF/template/UI-header.jsp" flush="true" />
	<script type="text/javascript">
	function go() {
		document.location.href = "<%= safeTargetReplayUrlJS %>";
	}
	window.setTimeout("go()",<%= secs * 1000 %>);
	</script>
		<h2>Redirecting...</h2>
		<p>The URL you requested:</p>
		<p><%= safeSource %></p>
		<p>redirected to the URL:</p>
		<p><%= safeTarget %></p>
		<p>
		when it was crawled at <%= prettyDate %>. You will be redirected
		to that target in <%= secs %> seconds.
		Click <a href="<%= safeTargetReplayUrl %>">here</a> to go now.
		</p>
<jsp:include page="/WEB-INF/template/UI-footer.jsp" flush="true" />
		