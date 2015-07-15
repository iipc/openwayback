<%@ page language="java" pageEncoding="utf-8" contentType="text/html;charset=utf-8"%>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.text.ParseException" %>
<%@ page import="org.archive.wayback.WaybackConstants" %>
<%@ page import="org.archive.wayback.core.CaptureSearchResult" %>
<%@ page import="org.archive.wayback.core.CaptureSearchResults" %>
<%@ page import="org.archive.wayback.core.UIResults" %>
<%@ page import="org.archive.wayback.core.WaybackRequest" %>
<%@ page import="org.archive.wayback.query.resultspartitioner.ResultsPartitionsFactory" %>
<%@ page import="org.archive.wayback.query.resultspartitioner.ResultsPartition" %>
<%@ page import="org.archive.wayback.util.StringFormatter" %>
<jsp:include page="/WEB-INF/template/UI-header.jsp" flush="true" />
<jsp:include page="/WEB-INF/template/CookieJS.jsp" flush="true" />
<%
UIResults results = UIResults.extractCaptureQuery(request);

WaybackRequest wbRequest = results.getWbRequest();
CaptureSearchResults cResults = results.getCaptureResults();
StringFormatter fmt = wbRequest.getFormatter();
String searchString = wbRequest.getRequestUrl();
List<String> closeMatches = cResults.getCloseMatches();


Date searchStartDate = wbRequest.getStartDate();
Date searchEndDate = wbRequest.getEndDate();
long firstResult = cResults.getFirstReturned();
long lastResult = cResults.getReturnedCount() + firstResult;
long resultCount = cResults.getMatchingCount();

ArrayList<ResultsPartition> partitions = 
	ResultsPartitionsFactory.get(cResults, wbRequest);
int numPartitions = partitions.size();
%>
<table border="0" cellpadding="5" width="100%" class="mainSearchBanner" cellspacing="0">
   <tr>
      <td>
            <%= fmt.format("PathQueryClassic.searchedFor",searchString) %>
      </td>
      <td align="right">
            <%= fmt.format("PathQuery.anchorWindowText") %>
            <jsp:include page="/WEB-INF/template/AnchorWindow.jsp" flush="true" />
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
    ArrayList<CaptureSearchResult> partitionResults = partition.getMatches();
%>
      <td nowrap class="mainBody" valign="top">
<%
    if(partitionResults.size() == 0) {
%>
         &nbsp;
<%
    } else {

      for(int j = 0; j < partitionResults.size(); j++) {
      
        CaptureSearchResult result = partitionResults.get(j);
      String url = result.getUrlKey();
      String captureTimestamp = result.getCaptureTimestamp();
      Date captureDate = result.getCaptureDate();
      String prettyDate = fmt.format("PathQuery.classicResultLinkText",
        captureDate);
      String origHost = result.getOriginalHost();
      String MD5 = result.getDigest();
      String redirectFlag = (0 == result.getRedirectUrl().compareTo("-")) 
        ? "" : fmt.format("PathPrefixQuery.redirectIndicator");
      String httpResponse = result.getHttpCode();
      String mimeType = result.getMimeType();
    
      String arcFile = result.getFile();
      String arcOffset = String.valueOf(result.getOffset());
    
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
         <a onclick="SetAnchorDate('<%= captureTimestamp %>');" href="<%= replayUrl %>"><%= prettyDate %></a> <%= updateStar %><br></br>
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
if(closeMatches != null && !closeMatches.isEmpty()) {
	WaybackRequest tmp = wbRequest.clone();
	
	
	%>
	<%= fmt.format("PathQuery.closeMatchesText") %><br>
	<%
	for(String closeMatch : closeMatches) {
		tmp.setRequestUrl(closeMatch);
		String link = fmt.escapeHtml(tmp.getContextPrefix() + "query?" +
				tmp.getQueryArguments());
		%>
		<a href="<%= link %>"><%= closeMatch %></a><br>
		<%
	}
}
// show page indicators:
if(cResults.getNumPages() > 1) {
  int curPage = cResults.getCurPageNum();
  %>
  <hr></hr>
  <%
  for(int i = 1; i <= cResults.getNumPages(); i++) {
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
<jsp:include page="/WEB-INF/template/UI-footer.jsp" flush="true" />
