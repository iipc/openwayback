<?xml version="1.0" encoding="UTF-8"?>
<%@ page language="java" pageEncoding="utf-8" contentType="text/xml;charset=utf-8"%>
<%@ page import="org.archive.wayback.exception.WaybackException" %>
<%@ page import="org.archive.wayback.core.UIResults" %>
<%@ page import="org.archive.wayback.util.StringFormatter" %>
<%

UIResults results = UIResults.extractException(request);
WaybackException e = results.getException();
StringFormatter fmt = results.getWbRequest().getFormatter();
//response.setStatus(e.getStatus());

%>
<wayback>
	<error>
		<title><%= UIResults.encodeXMLContent(fmt.format(e.getTitleKey())) %></title>
		<message><%= UIResults.encodeXMLContent(fmt.format(e.getMessageKey())) %></message>
	</error>
</wayback>
