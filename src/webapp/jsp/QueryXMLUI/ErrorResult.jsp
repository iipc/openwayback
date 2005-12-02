<?xml version="1.0" encoding="UTF-8"?>
<%@ page contentType="text/xml" %>
<%@ page import="org.archive.wayback.exception.WaybackException" %>
<%

WaybackException e = (WaybackException) request.getAttribute("exception");

%>
<wayback>
	<error>
		<title><%= (String) e.getTitle() %></title>
		<message><%= (String) e.getMessage() %></message>
		<details><%= (String) e.getDetails() %></details>
	</error>
</wayback>
