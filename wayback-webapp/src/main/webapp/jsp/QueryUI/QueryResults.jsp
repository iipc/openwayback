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
int resultCount = results.getResultsReturned();

Timestamp searchStartTs = results.getStartTimestamp();
Timestamp searchEndTs = results.getEndTimestamp();
Date searchStartDate = searchStartTs.getDate();
Date searchEndDate = searchEndTs.getDate();

Iterator itr = results.resultsIterator();
%>
<%= fmt.format("PathQuery.resultsSummary",resultCount,searchString) %>
<br></br>
<%= fmt.format("PathQuery.resultRange",searchStartDate,searchEndDate) %>
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
		?	"" : fmt.format("PathQuery.redirectIndicator");
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
		<%= fmt.format("PathQuery.newVersionIndicator") %>

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
