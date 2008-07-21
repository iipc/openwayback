<%@ page language="java" pageEncoding="utf-8" contentType="text/html;charset=utf-8"%>
<%@ page import="org.archive.wayback.exception.WaybackException" %>
<%@ page import="org.archive.wayback.core.UIResults" %>
<%@ page import="org.archive.wayback.util.StringFormatter" %>
<%
UIResults results = UIResults.extractException(request);
WaybackException e = results.getException();
StringFormatter fmt = results.getWbRequest().getFormatter();
response.setStatus(e.getStatus());

%>
/* CSS wayback retrieval error:

 Title:   <%= fmt.format(e.getTitleKey()) %>
 Message: <%= fmt.format(e.getMessageKey()) %>
 
 */
