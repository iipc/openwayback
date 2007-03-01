<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Date" %>
<%@ page import="org.archive.wayback.WaybackConstants" %>
<%@ page import="org.archive.wayback.core.SearchResult" %>
<%@ page import="org.archive.wayback.core.Timestamp" %>
<%@ page import="org.archive.wayback.core.UIResults" %>
<%@ page import="org.archive.wayback.query.UIQueryResults" %>
<%@ page import="org.archive.wayback.util.StringFormatter" %>
<jsp:include page="../../template/UI-header.jsp" />
<%

UIQueryResults results = (UIQueryResults) UIResults.getFromRequest(request);
StringFormatter fmt = results.getFormatter();
String searchString = results.getSearchUrl();

Date searchStartDate = results.getStartTimestamp().getDate();
Date searchEndDate = results.getEndTimestamp().getDate();

int firstResult = results.getFirstResult();
int lastResult = results.getLastResult();
int resultCount = results.getResultsMatching();

Iterator itr = results.resultsIterator();
%>
<%= fmt.format("PathPrefixQuery.showingResults",firstResult,lastResult,
				resultCount,searchString) %>
<br></br>
<%= fmt.format("PathQuery.resultRange",searchStartDate,searchEndDate) %>
<hr></hr>
<%
boolean first = false;
String lastUrl = null;
String lastMD5 = null;
while(itr.hasNext()) {
	SearchResult result = (SearchResult) itr.next();

	String url = result.get(WaybackConstants.RESULT_URL);
	// TODO: Localization
	String prettyDate = result.get(WaybackConstants.RESULT_CAPTURE_DATE);
	String origHost = result.get(WaybackConstants.RESULT_ORIG_HOST);
	String MD5 = result.get(WaybackConstants.RESULT_MD5_DIGEST);
	String redirectFlag = (0 == result.get(
		WaybackConstants.RESULT_REDIRECT_URL).compareTo("-")) 
		?	"" : fmt.format("PathQuery.redirectIndicator");
	String httpResponse = result.get(WaybackConstants.RESULT_HTTP_CODE);
	String mimeType = result.get(WaybackConstants.RESULT_MIME_TYPE);

	String arcFile = result.get(WaybackConstants.RESULT_ARC_FILE);
	String arcOffset = result.get(WaybackConstants.RESULT_OFFSET);

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
<!--
		<span style="color:red;"><%= arcFile %></span>
		<span style="color:red;"><%= arcOffset %></span>
-->
		<%= redirectFlag %>
		<%= fmt.format("PathQuery.newVersionIndicator") %>
		<br></br>

		<%
	} else {
		%>

		&nbsp;&nbsp;&nbsp;<a href="<%= replayUrl %>"><%= prettyDate %></a>
		<span style="color:green;"><%= origHost %></span>
		<span style="color:lightgray;"><%= fmt.format("PathPrefixQuery.unchangedIndicator")%></span>
<!--
		<span style="color:red;"><%= arcFile %></span>
		<span style="color:red;"><%= arcOffset %></span>
-->
		<br></br>

		<%	
	}
}
// show page indicators:
if(results.getNumPages() > 1) {
	int curPage = results.getCurPage();
	%>
	<hr></hr>
	<%
	for(int i = 1; i <= results.getNumPages(); i++) {
		if(i == curPage) {
			%>
			<b><%= i %></b>
			<%		
		} else {
			%>
			<a href="<%= results.urlForPage(i) %>"><%= i %></a>
			<%
		}
	}
}
%>

<jsp:include page="../../template/UI-footer.jsp" />
