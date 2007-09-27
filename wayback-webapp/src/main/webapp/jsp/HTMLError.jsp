<%@ page import="org.archive.wayback.exception.WaybackException" %>
<%@ page import="org.archive.wayback.core.UIResults" %>
<%@ page import="org.archive.wayback.util.StringFormatter" %>
<%
WaybackException e = (WaybackException) request.getAttribute("exception");
e.setupResponse(response);
%>
<jsp:include page="/template/UI-header.jsp" flush="true" />
<%

UIResults results = UIResults.getFromRequest(request);
StringFormatter fmt = results.getFormatter();

%>

<h2><%= fmt.format(e.getTitleKey()) %></h2>
<p><b><%= fmt.format(e.getMessageKey()) %></b></p>
<jsp:include page="/template/UI-footer.jsp" flush="true" />
