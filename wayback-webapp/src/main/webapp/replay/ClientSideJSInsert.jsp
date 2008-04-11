<%@ page language="java" pageEncoding="utf-8" contentType="text/html;charset=utf-8"%>
<%@ page import="java.util.Date" %>
<%@ page import="org.archive.wayback.ResultURIConverter" %>
<%@ page import="org.archive.wayback.core.Timestamp" %>
<%@ page import="org.archive.wayback.core.UIResults" %>
<%@ page import="org.archive.wayback.core.WaybackRequest" %>
<%@ page import="org.archive.wayback.query.UIQueryResults" %>
<%@ page import="org.archive.wayback.util.StringFormatter" %>
<%
UIQueryResults results = (UIQueryResults) UIResults.getFromRequest(request);
ResultURIConverter uriConverter = results.getURIConverter();
String requestDate = results.getExactRequestedTimestamp().getDateStr();
String contextPath = uriConverter.makeReplayURI(requestDate, "");
String contextRoot = request.getScheme() + "://" + request.getServerName() + ":" 
  + request.getServerPort() + request.getContextPath();

String jsUrl = contextRoot + "/replay/client-rewrite.js";
%>
<script type="text/javascript">
  var sWayBackCGI = "<%= contextPath %>";
</script>
<script type="text/javascript" src="<%= jsUrl %>" ></script>
