<%@   page language="java" pageEncoding="utf-8" contentType="text/html;charset=utf-8"
%><%@ page import="java.util.Iterator"
%><%@ page import="java.util.ArrayList"
%><%@ page import="java.util.Date"
%><%@ page import="org.archive.wayback.ResultURIConverter"
%><%@ page import="org.archive.wayback.WaybackConstants"
%><%@ page import="org.archive.wayback.core.UIResults"
%><%@ page import="org.archive.wayback.core.UrlSearchResult"
%><%@ page import="org.archive.wayback.core.UrlSearchResults"
%><%@ page import="org.archive.wayback.core.WaybackRequest"
%><%@ page import="org.archive.wayback.util.StringFormatter"
%><jsp:include page="/WEB-INF/template/UI-header.jsp" flush="true" />
<%
UIResults results = UIResults.extractUrlQuery(request);
WaybackRequest wbRequest = results.getWbRequest();
UrlSearchResults uResults = results.getUrlResults();
ResultURIConverter uriConverter = results.getURIConverter();
StringFormatter fmt = wbRequest.getFormatter();

String searchString = wbRequest.getRequestUrl();




Date searchStartDate = wbRequest.getStartDate();
Date searchEndDate = wbRequest.getEndDate();

long firstResult = uResults.getFirstReturned();
long lastResult = uResults.getReturnedCount() + firstResult;

long totalCaptures = uResults.getMatchingCount();

%>
<%= fmt.format("PathPrefixQuery.showingResults",firstResult + 1,lastResult,
        totalCaptures,searchString) %>
<br/>

<hr></hr>
<%
Iterator<UrlSearchResult> itr = uResults.iterator();
while(itr.hasNext()) {
  UrlSearchResult result = itr.next();

  String urlKey = result.getUrlKey();
  String originalUrl = result.getOriginalUrl();
  String firstDateTSss = result.getFirstCaptureTimestamp();
  String lastDateTSss = result.getLastCaptureTimestamp();
  long numCaptures = result.getNumCaptures();
  long numVersions = result.getNumVersions();

  Date firstDate = result.getFirstCaptureDate();
  Date lastDate = result.getLastCaptureDate();
  
  if(numCaptures == 1) {
      String ts = result.getFirstCaptureTimestamp();
      String anchor = uriConverter.makeReplayURI(ts,originalUrl);
    %>
    <a onclick="SetAnchorDate('<%= ts %>');" href="<%= anchor %>">
      <%= urlKey %>
    </a>
    <span class="mainSearchText">
      <%= fmt.format("PathPrefixQuery.versionCount",numVersions) %>
    </span>
    <br/>
    <span class="mainSearchText">
      <%= fmt.format("PathPrefixQuery.singleCaptureDate",firstDate) %>
    </span>
    <%
    
  } else {
    String anchor = results.makeCaptureQueryUrl(originalUrl);
    %>
    <a href="<%= anchor %>">
      <%= urlKey %>
    </a>
    <span class="mainSearchText">
      <%= fmt.format("PathPrefixQuery.versionCount",numVersions) %>
    </span>
    <br/>
    <span class="mainSearchText">
      <%= fmt.format("PathPrefixQuery.multiCaptureDate",numCaptures,firstDate,lastDate) %>
    </span>
    <%    
  }
  %>
  <br/>
  <br/> 
  <%
}

// show page indicators:
int curPage = uResults.getCurPageNum();
if(curPage > uResults.getNumPages()) {
  %>
  <hr></hr>
  <a href="<%= results.urlForPage(1) %>">First results</a>
  <%
} else if(uResults.getNumPages() > 1) {
  %>
  <hr></hr>
  <%
  for(int i = 1; i <= uResults.getNumPages(); i++) {
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
%><jsp:include page="/WEB-INF/template/UI-footer.jsp" flush="true" />