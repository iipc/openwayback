<%@ page import="org.archive.wayback.core.UIResults" %>
<%@ page import="org.archive.wayback.util.StringFormatter" %>
<jsp:include page="template/UI-header.jsp" />
<%
UIResults results = UIResults.getFromRequest(request);
StringFormatter fmt = results.getFormatter();
%>
<p>
	<%= fmt.format("UIGlobal.indexPage") %>
</p>
<jsp:include page="template/UI-footer.jsp" />
