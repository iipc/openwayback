<%@ page import="org.archive.wayback.exception.WaybackException" %>
<%

WaybackException e = (WaybackException) request.getAttribute("exception");

%>
// Javascript wayback retrieval error:
//
// Title:   <%= (String) e.getTitle() %>
// Message: <%= (String) e.getMessage() %>
// Details: <%= (String) e.getDetails() %>
