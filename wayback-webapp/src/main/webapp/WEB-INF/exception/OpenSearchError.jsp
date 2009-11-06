<?xml version="1.0" encoding="UTF-8"?><%@
 page language="java" pageEncoding="utf-8" contentType="text/xml;charset=utf-8"
%><%@
 page import="org.archive.wayback.exception.WaybackException"
%><%@
 page import="org.archive.wayback.core.UIResults"
%><%@
 page import="org.archive.wayback.util.StringFormatter"
%><%

UIResults results = UIResults.extractException(request);
WaybackException e = results.getException();
StringFormatter fmt = results.getWbRequest().getFormatter();

%>
<rss version="2.0" xmlns:openSearch="http://a9.com/-/spec/opensearch/1.1/">
   <channel>
     <title>Wayback OpenSearch Error</title>
     <link>http://archive-access.sourceforge.net/projects/wayback</link>
     <description>OpenSearch Error</description>
     <openSearch:totalResults>1</openSearch:totalResults>
     <openSearch:startIndex>1</openSearch:startIndex>
     <openSearch:itemsPerPage>1</openSearch:itemsPerPage>
     <item>
       <title><%= UIResults.encodeXMLContent(fmt.format(e.getTitleKey())) %></title>
       <description><%= UIResults.encodeXMLContent(fmt.format(e.getMessageKey())) %></description>
     </item>
   </channel>
 </rss>