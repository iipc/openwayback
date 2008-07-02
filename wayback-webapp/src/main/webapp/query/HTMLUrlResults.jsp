<%@ page language="java" pageEncoding="utf-8" contentType="text/html;charset=utf-8"%>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Date" %>
<%@ page import="org.archive.wayback.WaybackConstants" %>
<%@ page import="org.archive.wayback.core.Timestamp" %>
<%@ page import="org.archive.wayback.core.UIResults" %>
<%@ page import="org.archive.wayback.core.UrlSearchResult" %>
<%@ page import="org.archive.wayback.query.UIUrlQueryResults" %>
<%@ page import="org.archive.wayback.util.StringFormatter" %>
<jsp:include page="/template/UI-header.jsp" flush="true" />
<%

UIUrlQueryResults results = (UIUrlQueryResults) UIResults.getFromRequest(request);
StringFormatter fmt = results.getFormatter();

String searchString = results.getSearchUrl();



Date searchStartDate = results.getStartTimestamp().getDate();
Date searchEndDate = results.getEndTimestamp().getDate();

//PathQuerySearchResultPartitioner partitioner = 
//  new PathQuerySearchResultPartitioner(results.getResults(),
//      results.getURIConverter());

long firstResult = results.getFirstResult();
long lastResult = results.getLastResult();
long resultCount = results.getResultsMatching();

long totalCaptures = results.getResultsMatching();

%>
<%= fmt.format("PathPrefixQuery.showingResults",firstResult,lastResult,
        resultCount,searchString) %>
<br/>

<hr></hr>
<%
Iterator<UrlSearchResult> itr = results.resultsIterator();
while(itr.hasNext()) {
  UrlSearchResult result = itr.next();

  String urlKey = result.getUrlKey();
  String originalUrl = result.getOriginalUrl();
  String firstDateTS = result.getFirstCaptureTimestamp();
  String lastDateTS = result.getLastCaptureTimestamp();
  long numCaptures = result.getNumCaptures();
  long numVersions = result.getNumVersions();

  Date firstDate = results.timestampToDate(firstDateTS);
  Date lastDate = results.timestampToDate(lastDateTS);
  
  if(numCaptures == 1) {
    String anchor = results.makeReplayUrl(originalUrl,firstDateTS);
    %>
    <a href="<%= anchor %>">
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
