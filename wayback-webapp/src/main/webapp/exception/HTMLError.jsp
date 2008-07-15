<%@ page language="java" pageEncoding="utf-8" contentType="text/html;charset=utf-8"%>
<%@ page import="org.archive.wayback.exception.WaybackException" %>
<%@ page import="org.archive.wayback.core.UIResults" %>
<%@ page import="org.archive.wayback.util.StringFormatter" %>
<%
UIResults results = UIResults.extractException(request);
WaybackException e = results.getException();
e.setupResponse(response);
%>
<jsp:include page="/template/UI-header.jsp" flush="true" />
<%

StringFormatter fmt = results.getWbRequest().getFormatter();

%>

<h2><%= fmt.format(e.getTitleKey()) %></h2>
<p><b><%= fmt.format(e.getMessageKey(),e.getMessage()) %></b></p>
<jsp:include page="/template/UI-footer.jsp" flush="true" />
