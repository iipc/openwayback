<%@ page language="java" pageEncoding="utf-8" contentType="text/html;charset=utf-8"%>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.text.ParseException" %>
<%@ page import="org.archive.wayback.WaybackConstants" %>
<%@ page import="org.archive.wayback.core.SearchResult" %>
<%@ page import="org.archive.wayback.core.Timestamp" %>
<%@ page import="org.archive.wayback.core.UIResults" %>
<%@ page import="org.archive.wayback.query.UIQueryResults" %>
<%@ page import="org.archive.wayback.query.resultspartitioner.ResultsPartitionsFactory" %>
<%@ page import="org.archive.wayback.query.resultspartitioner.ResultsPartition" %>
<%@ page import="org.archive.wayback.util.StringFormatter" %>
<jsp:include page="/template/UI-header.jsp" flush="true" />
<%

UIQueryResults results = (UIQueryResults) UIResults.getFromRequest(request);
StringFormatter fmt = results.getFormatter();
String searchString = results.getSearchUrl();

Date searchStartDate = results.getStartTimestamp().getDate();
Date searchEndDate = results.getEndTimestamp().getDate();
int firstResult = results.getFirstResult();
int lastResult = results.getLastResult();
int resultCount = results.getResultsMatching();

//Timestamp searchStartTs = results.getStartTimestamp();
//Timestamp searchEndTs = results.getEndTimestamp();
//String prettySearchStart = results.prettyDateFull(searchStartTs.getDate());
//String prettySearchEnd = results.prettyDateFull(searchEndTs.getDate());

ArrayList<ResultsPartition> partitions = ResultsPartitionsFactory.get(
		results.getResults(),results.getWbRequest());
int numPartitions = partitions.size();
%>
<table border="0" cellpadding="5" width="100%" class="mainSearchBanner" cellspacing="0">
   <tr>
      <td>
            <%= fmt.format("PathQueryClassic.searchedFor",searchString) %>
      </td>
      <td align="right">
            <%= fmt.format("PathQueryClassic.resultsSummary",resultCount) %>
      </td>
   </tr>
</table>
<br>


<table border="0" width="100%">
   <tr bgcolor="#CCCCCC">
      <td colspan="<%= numPartitions %>" align="center" class="mainCalendar">
         <%= fmt.format("PathQueryClassic.searchResults",searchStartDate,searchEndDate) %>
      </td>
   </tr>

<!--    RESULT COLUMN HEADERS -->
   <tr bgcolor="#CCCCCC">
<%
	for(int i = 0; i < numPartitions; i++) {
		ResultsPartition partition = partitions.get(i);
%>
      <td align="center" class="mainBigBody">
         <%= partition.getTitle() %>
      </td>
<%
	}
%>
   </tr>
<!--    /RESULT COLUMN HEADERS -->



<!--    RESULT COLUMN COUNTS -->
   <tr bgcolor="#CCCCCC">
<%
	for(int i = 0; i < numPartitions; i++) {
		ResultsPartition partition = (ResultsPartition) partitions.get(i);
%>
      <td align="center" class="mainBigBody">
         <%= fmt.format("ResultPartition.columnSummary",partition.resultsCount()) %>
      </td>
<%
	}
%>
   </tr>
<!--    /RESULT COLUMN COUNTS -->


<!--    RESULT COLUMN DATA -->
   <tr bgcolor="#EBEBEB">
<%
	boolean first = false;
	String lastMD5 = null;

	for(int i = 0; i < numPartitions; i++) {
		ResultsPartition partition = (ResultsPartition) partitions.get(i);
		ArrayList<SearchResult> partitionResults = partition.getMatches();
%>
      <td nowrap class="mainBody" valign="top">
<%
		if(partitionResults.size() == 0) {
%>
         &nbsp;
<%
		} else {

		  for(int j = 0; j < partitionResults.size(); j++) {
		  
		  	SearchResult result = partitionResults.get(j);
			String url = result.get(WaybackConstants.RESULT_URL);
			String captureDate = result.get(WaybackConstants.RESULT_CAPTURE_DATE);
			Timestamp captureTS = Timestamp.parseBefore(captureDate);
			String prettyDate = fmt.format("PathQuery.classicResultLinkText",
				captureTS.getDate());
			String origHost = result.get(WaybackConstants.RESULT_ORIG_HOST);
			String MD5 = result.get(WaybackConstants.RESULT_MD5_DIGEST);
			String redirectFlag = (0 == result.get(
				WaybackConstants.RESULT_REDIRECT_URL).compareTo("-")) 
				?	"" : fmt.format("PathPrefixQuery.redirectIndicator");
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
			String updateStar = updated ? "*" : "";
%>
         <a href="<%= replayUrl %>"><%= prettyDate %></a> <%= updateStar %><br></br>
<%
		  
		  }
		
		}
%>
      </td>
<%
	}
	
%>
   </tr>
<!--    /RESULT COLUMN DATA -->
</table>


<%
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
<jsp:include page="/template/UI-footer.jsp" flush="true" />
