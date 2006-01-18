<?xml version="1.0" encoding="UTF-8"?>
<%@ page contentType="text/xml" %>
<%@ page import="org.archive.wayback.query.UIQueryResults" %>
<%@ page import="org.archive.wayback.exception.WaybackException" %>
<%

WaybackException e = (WaybackException) request.getAttribute("exception");

%>
<wayback>
	<error>
		<title><%= UIQueryResults.encodeXMLContent((String) e.getTitle()) %></title>
		<message><%= UIQueryResults.encodeXMLContent((String) e.getMessage()) %></message>
		<details><%= UIQueryResults.encodeXMLContent((String) e.getDetails()) %></details>
	</error>
</wayback>
