<%@ page import="org.archive.wayback.core.UIResults" %>
<%@ page import="org.archive.wayback.util.StringFormatter" %>
<%
UIResults results = UIResults.getFromRequest(request);
StringFormatter fmt = results.getFormatter();
String contextRoot = results.getContextPrefix();
String serverRoot = results.getServerPrefix();
%>
<!-- FOOTER -->
		<div align="center">
			<hr noshade size="1" align="center">
			
			<p>
				<a href="<%= contextRoot %>">
					<%= fmt.format("UIGlobal.homeLink") %>
				</a> |
				<a href="<%= contextRoot %>help.jsp">
					<%= fmt.format("UIGlobal.helpLink") %>
				</a>
			</p>
		</div>
	</body>
</html>
<!-- /FOOTER -->
