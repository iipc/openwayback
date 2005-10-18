<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="org.archive.wayback.core.ResourceResult" %>
<%@ page import="org.archive.wayback.core.Timestamp" %>
<%@ page import="org.archive.wayback.simplequeryui.UIResults" %>
<jsp:include page="../template/UI-header.txt" />
<%

UIResults results = (UIResults) request.getAttribute("ui-results");
String searchString = results.getSearchUrl();
int resultCount = results.getNumResults();

Timestamp searchStartTs = results.getStartTimestamp();
Timestamp searchEndTs = results.getEndTimestamp();
String prettySearchStart = searchStartTs.prettyDate();
String prettySearchEnd = searchEndTs.prettyDate();

Iterator itr = results.resultsIterator();
%>

<B><%= resultCount %></B> results for <B><%= searchString %></B><BR>
between <B><%= prettySearchStart %></B> and <B><%= prettySearchEnd %></B>
<HR>

<%
boolean first = false;
String lastMD5 = null;
while(itr.hasNext()) {
	ResourceResult result = (ResourceResult) itr.next();

	String prettyDate = result.getTimestamp().prettyDate();
	String origHost = result.getOrigHost();
	String MD5 = result.getMd5Fragment();
	String redirectFlag = result.isRedirect() ? "(redirect)" : "";
	String httpResponse = result.getHttpResponseCode();
	String mimeType = result.getMimeType();

	String replayUrl = results.resultToReplayUrl(result);

	boolean updated = false;
	if(lastMD5 == null) {
		lastMD5 = MD5;
		updated = true;
	} else if(0 != lastMD5.compareTo(MD5)) {
		updated = true;
		lastMD5 = MD5;
	}
	if(updated) {
		%>
		<A HREF="<%= replayUrl %>"><%= prettyDate %></A>
		<SPAN style="color:black;"><%= origHost %></SPAN>
		<SPAN style="color:gray;"><%= httpResponse %></SPAN>
		<SPAN style="color:brown;"><%= mimeType %></SPAN>
		<%= redirectFlag %>
		(new version)
		<BR>
		<%
	} else {
		%>
		&nbsp;&nbsp;&nbsp;<A HREF="<%= replayUrl %>"><%= prettyDate %></A>
		<SPAN style="color:green;"><%= origHost %></SPAN>
		<BR>
		<%
	}
}
%>
<jsp:include page="../template/UI-footer.txt" />
