<%@   page language="java" pageEncoding="utf-8" contentType="text/html;charset=utf-8"
%><%@ page import="org.archive.wayback.core.UIResults"
%><%
UIResults results = UIResults.extractReplay(request);
String staticPrefix = results.getStaticPrefix();
%>
<link rel="stylesheet" href="<%= staticPrefix %>css/normalize.css" type="text/css">
<link rel="stylesheet" href="<%= staticPrefix %>css/jquery.mCustomScrollbar.css" type="text/css">
