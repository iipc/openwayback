<%@ page language="java" pageEncoding="utf-8" contentType="text/html;charset=utf-8"%>
<%@ page import="java.util.List" %>
<%@ page import="org.archive.wayback.exception.WaybackException" %>
<%@ page import="org.archive.wayback.exception.ResourceNotInArchiveException"%>
<%@ page import="org.archive.wayback.core.UIResults" %>
<%@ page import="org.archive.wayback.core.WaybackRequest" %>
<%@ page import="org.archive.wayback.util.StringFormatter" %>
<%@ page import="org.archive.wayback.util.url.UrlOperations" %>
<%
UIResults results = UIResults.extractException(request);
WaybackException e = results.getException();
WaybackRequest wbr = results.getWbRequest();
e.setupResponse(response);
String contextRoot = wbr.getContextPrefix();
String requestUrl = wbr.getRequestUrl();
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
		<p>
		Other possible close matches to try:<br></br>
<%
		WaybackRequest tmp = wbr.clone();
		for(String closeMatch : closeMatches) {
			tmp.setRequestUrl(closeMatch);
			String link = tmp.getContextPrefix() + "query?" +
				tmp.getQueryArguments();
%>
			<a href="<%= link %>"><%= closeMatch %></a><br>
<%
		}
	}
	String parentUrl = UrlOperations.getUrlParentDir(requestUrl);
	if(parentUrl != null) {
		WaybackRequest tmp = wbr.clone();
		tmp.setRequestUrl(parentUrl);
		tmp.setUrlQueryRequest();
		String link = tmp.getContextPrefix() + "query?" +
			tmp.getQueryArguments();
		String escapedLink = fmt.escapeHtml(link);
		String escapedParentUrl = fmt.escapeHtml(parentUrl);
		%>
		</p>
		<p>
		More options:<br></br>
			Try Searching all pages under <a href="<%= escapedLink %>"><%= escapedParentUrl %></a>
		</p>
		<%
	}
}
%>
<jsp:include page="/WEB-INF/template/UI-footer.jsp" flush="true" />
