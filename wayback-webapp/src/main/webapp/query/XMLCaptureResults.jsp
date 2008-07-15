<?xml version="1.0" encoding="UTF-8"?>
<%@ page language="java" pageEncoding="utf-8" contentType="text/xml;charset=utf-8"%>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.Enumeration" %>
<%@ page import="org.archive.wayback.core.CaptureSearchResult" %>
<%@ page import="org.archive.wayback.core.CaptureSearchResults" %>
<%@ page import="org.archive.wayback.core.SearchResults" %>
<%@ page import="org.archive.wayback.core.UIResults" %>
<%
UIResults uiResults = UIResults.extractCaptureQuery(request);

CaptureSearchResults results = uiResults.getCaptureResults();
Iterator<CaptureSearchResult> itr = results.iterator();
%>
<wayback>
  <request>
<%
  Map<String,String> p = results.getFilters();
  Iterator<String> kitr = p.keySet().iterator();
  while(kitr.hasNext()) {
    String key = kitr.next();
    String oKey = UIResults.encodeXMLEntity(key);
    String oValue = UIResults.encodeXMLContent(p.get(key));
    %>
    <<%= oKey %>><%= oValue %></<%= oKey %>>
    <%
  }
%>
    <<%= SearchResults.RESULTS_TYPE %>><%= SearchResults.RESULTS_TYPE_CAPTURE %></<%= SearchResults.RESULTS_TYPE %>>
  </request>
  <results>
<%
  while(itr.hasNext()) {
    %>
    <result>
    <%
    CaptureSearchResult result = itr.next();
    Map<String,String> p2 = result.toCanonicalStringMap();
    kitr = p2.keySet().iterator();
    
    while(kitr.hasNext()) {
       String key = kitr.next();
       String oKey = UIResults.encodeXMLEntity(key);
       String oValue = UIResults.encodeXMLContent(p2.get(key));
      %>
      <<%= oKey %>><%= oValue %></<%= oKey %>>
      <%
    }
    %>
    </result>
    <%
  }
%>  
  </results>
</wayback>
