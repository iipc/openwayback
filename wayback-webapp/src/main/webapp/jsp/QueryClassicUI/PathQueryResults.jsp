<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Date" %>
<%@ page import="org.archive.wayback.WaybackConstants" %>
<%@ page import="org.archive.wayback.core.SearchResult" %>
<%@ page import="org.archive.wayback.core.Timestamp" %>
<%@ page import="org.archive.wayback.core.UIResults" %>
<%@ page import="org.archive.wayback.query.UIQueryResults" %>
<%@ page import="org.archive.wayback.util.StringFormatter" %>
<%@ page import="org.archive.wayback.query.PathQuerySearchResultPartition" %>
<%@ page import="org.archive.wayback.query.PathQuerySearchResultPartitioner" %>
<jsp:include page="../../template/UI-header.jsp" />
<%

UIQueryResults results = (UIQueryResults) UIResults.getFromRequest(request);
StringFormatter fmt = results.getFormatter();
String searchString = results.getSearchUrl();

Date searchStartDate = results.getStartTimestamp().getDate();
Date searchEndDate = results.getEndTimestamp().getDate();

PathQuerySearchResultPartitioner partitioner = 
	new PathQuerySearchResultPartitioner(results.getResults(),
			results.getURIConverter());

int firstResult = results.getFirstResult();
int lastResult = results.getLastResult();
int resultCount = results.getResultsMatching();

int totalCaptures = partitioner.numResultsTotal();
int totalUrls = partitioner.numUrls();

%>
<%= fmt.format("PathPrefixQuery.showingResults",firstResult,lastResult,
				resultCount,searchString) %>
<br></br>
<%= fmt.format("PathQuery.resultRange",searchStartDate,searchEndDate) %>
<hr></hr>
<%
Iterator itr = partitioner.iterator();
while(itr.hasNext()) {
	PathQuerySearchResultPartition partition = 
		(PathQuerySearchResultPartition) itr.next();
	String url = partition.getUrl();
	String queryUrl = partition.queryUrl();
	String replayUrl = partition.replayUrl();
	int numCaptures = partition.getNumResults();
	Date firstDate = partition.getFirstDate();
	Date lastDate = partition.getLastDate();
	int numVersions = partition.getNumVersions();
	if(numCaptures == 1) {
		%>
		<a href="<%= replayUrl %>">
			<%= url %>
		</a>
		<span class="mainSearchText">
			<%= fmt.format("PathPrefixQuery.versionCount",numVersions) %>
		</span>
		<br></br>
		<span class="mainSearchText">
			<%= fmt.format("PathPrefixQuery.singleCaptureDate",firstDate) %>
		</span>
		<%
	} else {
		%>
		<a href="<%= queryUrl %>">
			<%= url %>
		</a>
		<span class="mainSearchText">
			<%= fmt.format("PathPrefixQuery.versionCount",numVersions) %>
		</span>
		<br></br>
		<span class="mainSearchText">
			<%= fmt.format("PathPrefixQuery.multiCaptureDate",numCaptures,firstDate,lastDate) %>
		</span>
		<%		
	}
	%>
	<br></br>
	<br></br>	
	<%
}
// show page indicators:
int curPage = results.getCurPage();
if(curPage > results.getNumPages()) {
	%>
	<hr></hr>
	<a href="<%= results.urlForPage(1) %>">First results</a>
	<%
} else if(results.getNumPages() > 1) {
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
