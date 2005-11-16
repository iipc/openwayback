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
int resultCount = results.getResultCount();

String prettySearchStart = results.prettySearchStartDate();
String prettySearchEnd = results.prettySearchEndDate();

Iterator itr = results.resultsIterator();
%>

<b><%= resultCount %></b> results for <b><%= searchString %></b><br></br>
between <b><%= prettySearchStart %></b> and <b><%= prettySearchEnd %></b>
<hr></hr>

<%
boolean first = false;
String lastUrl = null;
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
		<hr></hr><b><%= url %></b><br></br>
		<%
	}
	if(0 != MD5.compareTo(lastMD5)) {
		lastMD5 = MD5;

		%>

		<a href="<%= replayUrl %>"><%= prettyDate %></a>
		<span style="color:black;"><%= origHost %></span>
		<span style="color:gray;"><%= httpResponse %></span>
		<span style="color:brown;"><%= mimeType %></span>
		<%= redirectFlag %>
		(new version)
		<br></br>

		<%
	} else {
		%>

		&nbsp;&nbsp;&nbsp;<a href="<%= replayUrl %>"><%= prettyDate %></a>
		<span style="color:green;"><%= origHost %></span>
		<span style="color:lightgray;">unchanged</span>
		<br></br>

		<%	
	}
}
%>
<jsp:include page="../../template/UI-footer.jsp" />
