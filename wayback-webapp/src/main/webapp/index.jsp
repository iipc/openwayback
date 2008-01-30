<%@ page import="java.util.ArrayList" %>
<%@ page import="org.archive.wayback.core.UIResults" %>
<%@ page import="org.archive.wayback.util.StringFormatter" %>
<jsp:include page="template/UI-header.jsp" flush="true" />
<%
UIResults results = UIResults.getFromRequest(request);
StringFormatter fmt = results.getFormatter();
Object names = request.getAttribute("AccessPointNames");
if(names != null) {
	if(names instanceof ArrayList) {
		ArrayList<String> accessPoints = (ArrayList<String>) names;
		if(accessPoints.size() > 0) {
			%>
			 You seems to be accessing this Wayback via an incorrect URL. Please try one of the following AccessPoints:<br></br>
			<%
		}
		for(String accessPoint : accessPoints) {
			%>
			 <a href="<%= accessPoint %>/"><%= accessPoint %></a><br></br>
			<%
		}
	}
}
%>
<p>
	<%= fmt.format("UIGlobal.indexPage") %>
</p>
<jsp:include page="template/UI-footer.jsp" flush="true" />
