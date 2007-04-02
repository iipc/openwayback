<%@ page import="org.archive.wayback.core.UIResults" %>
<%@ page import="org.archive.wayback.util.StringFormatter" %>
<%
UIResults results = UIResults.getFromRequest(request);
StringFormatter fmt = results.getFormatter();
%>
<!-- FOOTER -->
		<div align="center">
			<hr noshade size="1" align="center">
			
			<p>
				<a href="<%= request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath() %>">
					<%= fmt.format("UIGlobal.homeLink") %>
				</a> |
				<a href="<%= request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath() %>/help.jsp">
					<%= fmt.format("UIGlobal.helpLink") %>
				</a>
			</p>
		</div>
		<!-- 
	</body>
</html>
 -->
<!-- /FOOTER -->
