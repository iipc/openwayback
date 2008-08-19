<%@ page import="org.archive.wayback.util.bdb.BDBMap" %>
<%
String url = request.getParameter("url");
String time = request.getParameter("time");

// Put time-mapping for this id, or if no id, the ip-addr.
String id = request.getHeader("Proxy-Id");
if(id == null) {
	id = request.getRemoteAddr();
}
BDBMap.addTimestampForId(request.getContextPath(),id, time);

// Now redirect to the page the user wanted.
response.sendRedirect(url);
%>
