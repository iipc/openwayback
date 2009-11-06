<%@ page language="java" pageEncoding="utf-8" contentType="text/html;charset=utf-8"%>
<%@ page import="java.util.List" %>
<%@ page import="org.archive.wayback.exception.WaybackException" %>
<%@ page import="org.archive.wayback.exception.ResourceNotInArchiveException"%>
<%@ page import="org.archive.wayback.core.UIResults" %>
<%@ page import="org.archive.wayback.core.WaybackRequest" %>
<%@ page import="org.archive.wayback.util.StringFormatter" %>
<%
UIResults results = UIResults.extractException(request);
WaybackException e = results.getException();
e.setupResponse(response);
String contextRoot = results.getWbRequest().getContextPrefix();

%>

<jsp:include page="/WEB-INF/template/UI-header.jsp" flush="true" />
<%

StringFormatter fmt = results.getWbRequest().getFormatter();

%>

<h2><%= fmt.format(e.getTitleKey()) %></h2>
<p><b><%= fmt.format(e.getMessageKey(),e.getMessage()) %></b></p>
<%
if(e instanceof ResourceNotInArchiveException) {
	ResourceNotInArchiveException niae = (ResourceNotInArchiveException) e;
	List<String> closeMatches = niae.getCloseMatches();
	if(closeMatches != null && !closeMatches.isEmpty()) {
%>
		Other requests to try:<br>
<%
		WaybackRequest tmp = results.getWbRequest().clone();
		for(String closeMatch : closeMatches) {
			tmp.setRequestUrl(closeMatch);
			String link = tmp.getContextPrefix() + "query?" +
				tmp.getQueryArguments();
%>
			<a href="<%= link %>"><%= closeMatch %></a><br>
<%
		}
	}
}
%>
<jsp:include page="/WEB-INF/template/UI-footer.jsp" flush="true" />
