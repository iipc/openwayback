<%@ page language="java" pageEncoding="utf-8" contentType="text/html;charset=utf-8"%>
<%@ page import="java.util.ArrayList" %>
<%@ page import="org.archive.wayback.core.UIResults" %>
<%@ page import="org.archive.wayback.core.WaybackRequest" %>
<%@ page import="org.archive.wayback.util.StringFormatter" %>
<%@ page import="org.archive.wayback.accesspoint.proxy.ProxyAccessPoint" %>
<%@ page import="org.archive.wayback.accesspoint.AccessPointConfigs" %>
<%@ page import="org.archive.wayback.accesspoint.AccessPointConfig" %>
<%
ProxyAccessPoint accessPoint = (ProxyAccessPoint)request.getAttribute("proxyAccessPoint");
StringFormatter fmt = wbRequest.getFormatter();

String contextRoot = accessPoint.getReplayPrefix();
String referrer = request.getHeader("Referer");
String logoPath = contextRoot + "images/logo_bw.gif";
if (referrer == null) {
	referrer = "Wayback";
}
%>

<p style="text-align: center"><img src="<%= logoPath %>"/><h2><%= fmt.format("ReplayView.noCacheTitle") %></h2><img src="<%= logoPath %>"/>
<h2><%= fmt.format("ReplayView.noCacheGoto") %><a href="<%= contextRoot + ProxyAccessPoint.SWITCH_COLLECTION_PATH%>"><%= referrer %></a></h2>
</p>
<%= fmt.format("ReplayView.noChacheText") %>

<table>
<tr>
<td><i><%= fmt.format("ReplayView.noChacheId") %></i></td>
</tr>

<%

AccessPointConfigs accessPointConfigs = accessPoint.getAccessPointConfigs();

if (accessPointConfigs != null) {
	for (AccessPointConfig theConfig : accessPointConfigs.getAccessPointConfigs().values()) {
	%>
	<tr>
	  <td>
	  <%= theConfig.getBeanName() %>
	  </td>
	</tr>
	<%   
	}
}

%>

</table>
