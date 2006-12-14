<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.text.ParseException" %>
<%@ page import="org.archive.wayback.WaybackConstants" %>
<%@ page import="org.archive.wayback.core.SearchResult" %>
<%@ page import="org.archive.wayback.core.Timestamp" %>
<%@ page import="org.archive.wayback.query.UIQueryResults" %>
<%@ page import="org.archive.wayback.query.resultspartitioner.ResultsPartitionsFactory" %>
<%@ page import="org.archive.wayback.query.resultspartitioner.ResultsPartition" %>
<jsp:include page="../../template/UI-header.jsp" />
<%

UIQueryResults results = (UIQueryResults) request.getAttribute("ui-results");
String searchString = results.getSearchUrl();
int resultCount = results.getResultsReturned();

Timestamp searchStartTs = results.getStartTimestamp();
Timestamp searchEndTs = results.getEndTimestamp();
String prettySearchStart = searchStartTs.prettyDate();
String prettySearchEnd = searchEndTs.prettyDate();

ArrayList partitions = ResultsPartitionsFactory.get(results.getResults());
int numPartitions = partitions.size();
%>
<table border="0" cellpadding="5" width="100%" class="mainSearchBanner" cellspacing="0">
   <tr>
      <td>
            Searched for
            <a href="<%= searchString %>">
               <b>
                  <%= searchString %>
               </b>
            </a>
      </td>
      <td align="right">
            <b>
               <%= resultCount %>
            </b>
            Results
      </td>
   </tr>
</table>
<br>


<table border="0" width="100%">
   <tr bgcolor="#CCCCCC">
      <td colspan="<%= numPartitions %>" align="center" class="mainCalendar">
         Search Results for
         <b>
            <%= prettySearchStart %>
         </b>
         -
         <b>
            <%= prettySearchEnd %>
         </b>
      </td>
   </tr>

<!--    RESULT COLUMN HEADERS -->
   <tr bgcolor="#CCCCCC">
<%
	for(int i = 0; i < numPartitions; i++) {
		ResultsPartition partition = (ResultsPartition) partitions.get(i);
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
         <%= partition.resultsCount() %> pages
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
		ArrayList partitionResults = partition.getMatches();
%>
      <td nowrap class="mainBody" valign="top">
<%
		if(partitionResults.size() == 0) {
%>
         &nbsp;
<%
		} else {

		  for(int j = 0; j < partitionResults.size(); j++) {
		  
		  	SearchResult result = (SearchResult) partitionResults.get(j);
			String url = result.get(WaybackConstants.RESULT_URL);
			String captureDate = result.get(WaybackConstants.RESULT_CAPTURE_DATE);
			Timestamp captureTS = Timestamp.parseBefore(captureDate);
			String prettyDate = captureTS.prettyDate();
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
<jsp:include page="../../template/UI-footer.jsp" />
