<%@ page language="java" pageEncoding="utf-8" contentType="text/html;charset=utf-8"%>
<%@ page import="java.util.ArrayList" %>
<%@ page import="org.archive.wayback.core.UIResults" %>
<%@ page import="org.archive.wayback.util.StringFormatter" %>
<%@ page import="org.archive.wayback.accesspoint.proxy.ProxyAccessPoint" %>
<%@ page import="org.archive.wayback.accesspoint.AccessPointConfigs" %>
<%@ page import="org.archive.wayback.accesspoint.AccessPointConfig" %>
<%
ProxyAccessPoint accessPoint = (ProxyAccessPoint)request.getAttribute("proxyAccessPoint");
String contextRoot = accessPoint.getReplayPrefix();
String referrer = request.getHeader("Referer");
String logoPath = contextRoot + "images/logo_bw.gif";
if (referrer == null) {
	referrer = "Wayback";
}
%>

<p style="text-align: center"><img src="<%= logoPath %>"/><h2>Wayback Proxy Mode Configuration</h2><img src="<%= logoPath %>"/>
<h2>Go To: <a href="<%= contextRoot + ProxyAccessPoint.SWITCH_COLLECTION_PATH%>"><%= referrer %></a></h2>
</p>
<p>Your browser is running with Wayback Machine as proxy mode, but it doesn't know which archived collection to use</p>
<p>When prompted, the <i>Username</i> is the <i>Collection Id</i> or <i>Collection Name</i> for your collection</p>
<i>The password is ignored and may be left blank</i></p>
<p>The following collections are available:</p>

<table>
<tr>
<td><i>Collection Id</i></td>
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
