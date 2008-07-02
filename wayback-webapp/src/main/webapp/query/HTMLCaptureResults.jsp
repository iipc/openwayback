<%@ page language="java" pageEncoding="utf-8" contentType="text/html;charset=utf-8"%>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Date" %>
<%@ page import="org.archive.wayback.WaybackConstants" %>
<%@ page import="org.archive.wayback.core.CaptureSearchResult" %>
<%@ page import="org.archive.wayback.core.Timestamp" %>
<%@ page import="org.archive.wayback.core.UIResults" %>
<%@ page import="org.archive.wayback.query.UICaptureQueryResults" %>
<%@ page import="org.archive.wayback.util.StringFormatter" %>
<jsp:include page="/template/UI-header.jsp" flush="true" />
<%

UICaptureQueryResults results = (UICaptureQueryResults) UIResults.getFromRequest(request);
StringFormatter fmt = results.getFormatter();

String searchString = results.getSearchUrl();

  int resultCount = results.getResultsReturned();

  Timestamp searchStartTs = results.getStartTimestamp();
  Timestamp searchEndTs = results.getEndTimestamp();
  Date searchStartDate = searchStartTs.getDate();
  Date searchEndDate = searchEndTs.getDate();

  Iterator<CaptureSearchResult> itr = results.resultsIterator();
  %>
  <%= fmt.format("PathQuery.resultsSummary",resultCount,searchString) %>
  <br></br>
  <%= fmt.format("PathQuery.resultRange",searchStartDate,searchEndDate) %>
  <hr></hr>
  <%
  boolean first = false;
  String lastMD5 = null;
  while(itr.hasNext()) {
	  CaptureSearchResult result = (CaptureSearchResult) itr.next();

    String url = result.getUrlKey();

    String prettyDate = result.getCaptureTimestamp();
    String origHost = result.getOriginalHost();
    String MD5 = result.getDigest();
    String redirectFlag = (0 == result.getRedirectUrl().compareTo("-")) 
      ? "" : fmt.format("PathQuery.redirectIndicator");
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

      <br/>
      <%
    } else {
      %>
      &nbsp;&nbsp;&nbsp;<a href="<%= replayUrl %>"><%= prettyDate %></a>
      <span style="color:green;"><%= origHost %></span>
  <!--
      <span style="color:red;"><%= arcFile %></span>
      <span style="color:red;"><%= arcOffset %></span>
  -->
      <br/>
      <%
    }
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

<jsp:include page="/template/UI-footer.jsp" flush="true" />
