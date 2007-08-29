<%@ page import="org.archive.wayback.core.UIResults" %>
<%@ page import="org.archive.wayback.util.StringFormatter" %>
<jsp:include page="/template/UI-header.jsp" flush="true" />
<%
UIResults results = UIResults.getFromRequest(request);
StringFormatter fmt = results.getFormatter();
String url = "http://archive-access.sourceforge.net/projects/wayback/faq.html";
%>
<%= fmt.format("UIGlobal.helpPage",url) %>
<jsp:include page="/template/UI-footer.jsp" flush="true" />
