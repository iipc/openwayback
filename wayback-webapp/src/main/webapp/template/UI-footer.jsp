<%@ page language="java" pageEncoding="utf-8" contentType="text/html;charset=utf-8"%>
<%@ page import="org.archive.wayback.core.UIResults" %>
<%@ page import="org.archive.wayback.util.StringFormatter" %>
<%
UIResults results = UIResults.getGeneric(request);
StringFormatter fmt = results.getWbRequest().getFormatter();
String contextRoot = results.getWbRequest().getContextPrefix();
String serverRoot = results.getWbRequest().getServerPrefix();
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
