<%@ page language="java" pageEncoding="utf-8" contentType="text/html;charset=utf-8"%>
<%@ page import="org.archive.wayback.exception.WaybackException" %>
<%@ page import="org.archive.wayback.core.UIResults" %>
<%@ page import="org.archive.wayback.util.StringFormatter" %>
<%

WaybackException e = (WaybackException) request.getAttribute("exception");
UIResults results = UIResults.getFromRequest(request);
StringFormatter fmt = results.getFormatter();
response.setStatus(e.getStatus());

%>
// Javascript wayback retrieval error:
//
// Title:   <%= fmt.format(e.getTitleKey()) %>
// Message: <%= fmt.format(e.getMessageKey()) %>
