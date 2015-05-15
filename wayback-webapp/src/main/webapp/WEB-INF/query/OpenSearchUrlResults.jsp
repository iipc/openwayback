<?xml version="1.0" encoding="UTF-8"?><%@
 page language="java" pageEncoding="utf-8" contentType="text/xml;charset=utf-8"
%><%@
 page import="java.util.Iterator"
%><%@
 page import="java.util.ArrayList"
%><%@
 page import="java.util.Date"
%><%@
 page import="java.util.Map"
%><%@
 page import="java.util.Enumeration"
%><%@
 page import="org.archive.wayback.core.UrlSearchResult"
%><%@
 page import="org.archive.wayback.core.UrlSearchResults"
%><%@
 page import="org.archive.wayback.core.SearchResults"
%><%@
 page import="org.archive.wayback.core.UIResults"
%><%@
 page import="org.archive.wayback.core.WaybackRequest"
%><%@
 page import="org.archive.wayback.requestparser.OpenSearchRequestParser"
%><%@
 page import="org.archive.wayback.util.StringFormatter"
%><%
UIResults uiResults = UIResults.extractUrlQuery(request);

WaybackRequest wbRequest = uiResults.getWbRequest();
StringFormatter fmt = wbRequest.getFormatter();
UrlSearchResults results = uiResults.getUrlResults();
Iterator<UrlSearchResult> itr = results.iterator();

String staticPrefix = uiResults.getStaticPrefix();
String queryPrefix = uiResults.getQueryPrefix();
String replayPrefix = uiResults.getReplayPrefix();

String searchString = wbRequest.getRequestUrl();
long firstResult = results.getFirstReturned();
long shownResultCount = results.getReturnedCount();
long lastResult = results.getReturnedCount() + firstResult;
long resultCount = results.getMatchingCount();
String searchTerms = "";
Map<String,String[]> queryMap = request.getParameterMap();
String arr[] = queryMap.get(OpenSearchRequestParser.SEARCH_QUERY);
if(arr != null && arr.length > 1) {
    searchTerms = arr[0];
}
%>
<rss version="2.0" 
      xmlns:opensearch="http://a9.com/-/spec/opensearch/1.1/"
      xmlns:atom="http://www.w3.org/2005/Atom">
   <channel>
     <title><%= fmt.format("PathQuery.rssResultsTitle") %></title>
     <link><%= queryPrefix %>></link>
     <description><%= fmt.format("PathQueryClassic.searchedFor",searchString) %></description>
     <opensearch:totalResults><%= resultCount %></opensearch:totalResults>
     <opensearch:startIndex><%= firstResult %></opensearch:startIndex>
     <opensearch:itemsPerPage><%= shownResultCount %></opensearch:itemsPerPage>
     <atom:link rel="search" type="application/opensearchdescription+xml" href="<%= staticPrefix %>/opensearchdescription.xml"/>
     <opensearch:Query role="request" searchTerms="<%= UIResults.encodeXMLContent(searchTerms) %>" startPage="<%= wbRequest.getPageNum() %>" />
<%
  while(itr.hasNext()) {
    %>
     <item>
    <%
    UrlSearchResult result = itr.next();

    String originalUrl = result.getOriginalUrl();
    String title = UIResults.encodeXMLEntity(originalUrl);
    
    String queryUrl = UIResults.encodeXMLEntity(
            uiResults.makeCaptureQueryUrl(originalUrl));

    String requestUrl = UIResults.encodeXMLEntity(
            wbRequest.getRequestUrl());
    long numCaptures = result.getNumCaptures();
    long numVersions = result.getNumVersions();

    Date firstDate = result.getFirstCaptureDate();
    Date lastDate = result.getLastCaptureDate();
    %>
      <title><%= title %></title>
      <link><%= queryUrl %></link>
      <description>
        <%= requestUrl %>
        <span class="mainSearchText">
          <%= fmt.format("PathPrefixQuery.versionCount",numVersions) %>
        </span>
        <span class="mainSearchText">
          <%= fmt.format("PathPrefixQuery.multiCaptureDate",numCaptures,firstDate,lastDate) %>
        </span>

      </description>
    </item>
    <%
  }
%>  
   </channel>
 </rss>
