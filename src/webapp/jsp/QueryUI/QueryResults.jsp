<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="org.archive.wayback.WaybackConstants" %>
<%@ page import="org.archive.wayback.core.SearchResult" %>
<%@ page import="org.archive.wayback.core.Timestamp" %>
<%@ page import="org.archive.wayback.query.UIQueryResults" %>
<jsp:include page="../../template/UI-header.jsp" />
<%

UIQueryResults results = (UIQueryResults) request.getAttribute("ui-results");
String searchString = results.getSearchUrl();
int resultCount = results.getResultsReturned();

Timestamp searchStartTs = results.getStartTimestamp();
Timestamp searchEndTs = results.getEndTimestamp();
String prettySearchStart = searchStartTs.prettyDate();
String prettySearchEnd = searchEndTs.prettyDate();

Iterator itr = results.resultsIterator();
%>

<b><%= resultCount %></b> results for <b><%= searchString %></b><br></br>
between <b><%= prettySearchStart %></b> and <b><%= prettySearchEnd %></b>
<hr></hr>

<%
boolean first = false;
String lastMD5 = null;
while(itr.hasNext()) {
	SearchResult result = (SearchResult) itr.next();

	String url = result.get(WaybackConstants.RESULT_URL);
	String prettyDate = result.get(WaybackConstants.RESULT_CAPTURE_DATE);
	String origHost = result.get(WaybackConstants.RESULT_ORIG_HOST);
	String MD5 = result.get(WaybackConstants.RESULT_MD5_DIGEST);
	String redirectFlag = (0 == result.get(
		WaybackConstants.RESULT_REDIRECT_URL).compareTo("-")) 
		?	"" : "(redirect)";
	String httpResponse = result.get(WaybackConstants.RESULT_HTTP_CODE);
	String mimeType = result.get(WaybackConstants.RESULT_MIME_TYPE);

	String arcFile = result.get(WaybackConstants.RESULT_ARC_FILE);
	String arcOffset = result.get(WaybackConstants.RESULT_OFFSET);

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
		<a href="<%= replayUrl %>"><%= prettyDate %></a>
		<span style="color:black;"><%= origHost %></span>
		<span style="color:gray;"><%= httpResponse %></span>
		<span style="color:brown;"><%= mimeType %></span>
<!--
		<span style="color:red;"><%= arcFile %></span>
		<span style="color:red;"><%= arcOffset %></span>
-->
		<%= redirectFlag %>
		(new version)
		<br></br>
		<%
	} else {
		%>
		&nbsp;&nbsp;&nbsp;<a href="<%= replayUrl %>"><%= prettyDate %></a>
		<span style="color:green;"><%= origHost %></span>
<!--
		<span style="color:red;"><%= arcFile %></span>
		<span style="color:red;"><%= arcOffset %></span>
-->
		<br></br>
		<%
	}
}
%>
<jsp:include page="../../template/UI-footer.jsp" />
