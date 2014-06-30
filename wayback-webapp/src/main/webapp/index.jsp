<%@ page language="java" pageEncoding="utf-8" contentType="text/html;charset=utf-8"%>
<%@ page import="java.util.ArrayList" %>
<%@ page import="org.archive.wayback.core.UIResults" %>
<%@ page import="org.archive.wayback.util.StringFormatter" %>
<jsp:include page="/WEB-INF/template/UI-header.jsp" flush="true" />
<%
UIResults results = UIResults.getGeneric(request);
StringFormatter fmt = results.getWbRequest().getFormatter();
Object names = request.getAttribute("AccessPointNames");
if(names != null) {
	if(names instanceof ArrayList) {
		ArrayList<String> accessPoints = (ArrayList<String>) names;
		if(accessPoints.size() > 0) {
			%>
			 You seem to be accessing this OpenWayback via an incorrect URL. Please try one of the following AccessPoints:<br></br>
			<%
		}
		for(String accessPoint : accessPoints) {
			%>
			 <a href="<%= request.getContextPath() + "/" + accessPoint %>/"><%= accessPoint %></a><br></br>
			<%
		}
	}
}
%>
<p>
	<%= fmt.format("UIGlobal.indexPage") %>
</p>
<jsp:include page="/WEB-INF/template/UI-footer.jsp" flush="true" />
