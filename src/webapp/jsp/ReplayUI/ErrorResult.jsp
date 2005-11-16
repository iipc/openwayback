<%@ page import="org.archive.wayback.exception.WaybackException" %>
<jsp:include page="../../template/UI-header.jsp" />
<%

WaybackException e = (WaybackException) request.getAttribute("exception");

%>

<h2><%= (String) e.getTitle() %></h2>
<p><b><%= (String) e.getMessage() %></b></p>
<p><%= (String) e.getDetails() %></p>
<jsp:include page="../../template/UI-footer.jsp" />
