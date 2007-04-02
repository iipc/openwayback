<?xml version="1.0" encoding="UTF-8"?>
<%@ page contentType="text/xml" %>
<%@ page import="org.archive.wayback.exception.WaybackException" %>
<%@ page import="org.archive.wayback.core.UIResults" %>
<%@ page import="org.archive.wayback.util.StringFormatter" %>
<%

WaybackException e = (WaybackException) request.getAttribute("exception");
UIResults results = UIResults.getFromRequest(request);
StringFormatter fmt = results.getFormatter();
//response.setStatus(e.getStatus());

%>
<wayback>
	<error>
		<title><%= UIResults.encodeXMLContent(fmt.format(e.getTitleKey())) %></title>
		<message><%= UIResults.encodeXMLContent(fmt.format(e.getMessageKey())) %></message>
	</error>
</wayback>
