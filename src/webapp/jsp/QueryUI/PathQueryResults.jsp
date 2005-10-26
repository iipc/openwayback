<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="org.archive.wayback.core.ResourceResult" %>
<%@ page import="org.archive.wayback.core.Timestamp" %>
<%@ page import="org.archive.wayback.simplequeryui.UIResults" %>
<jsp:include page="../../template/UI-header.jsp" />
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
String lastUrl = null;
String lastMD5 = null;
while(itr.hasNext()) {
	ResourceResult result = (ResourceResult) itr.next();

	String url = result.getUrl();
	String prettyDate = result.getTimestamp().prettyDate();
	String origHost = result.getOrigHost();
	String MD5 = result.getMd5Fragment();
	String redirectFlag = result.isRedirect() ? "(redirect)" : "";
	String httpResponse = result.getHttpResponseCode();
	String mimeType = result.getMimeType();

	String replayUrl = results.resultToReplayUrl(result);

	boolean newUrl = false;
	if(lastUrl == null) {
		lastUrl = url;
		lastMD5 = "";
		newUrl = true;
	} else if(0 != lastUrl.compareTo(url)) {
		newUrl = true;
		lastMD5 = "";
		lastUrl = url;
	}
	if(newUrl) {
		%>
		<HR><B><%= url %></B><BR>
		<%
	}
	if(0 != MD5.compareTo(lastMD5)) {
		lastMD5 = MD5;

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
		<SPAN style="color:lightgray;">unchanged</SPAN>
		<BR>

		<%	
	}
}
%>
<jsp:include page="../../template/UI-footer.jsp" />
