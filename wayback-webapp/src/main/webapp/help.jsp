<%@ page language="java" pageEncoding="utf-8" contentType="text/html;charset=utf-8"%>
<%@ page import="org.archive.wayback.core.UIResults" %>
<%@ page import="org.archive.wayback.util.StringFormatter" %>
<jsp:include page="/WEB-INF/template/UI-header.jsp" flush="true" />
<%
UIResults results = UIResults.getGeneric(request);
StringFormatter fmt = results.getWbRequest().getFormatter();
String url = fmt.format("UIGlobal.helpUrl");
%>
<%= fmt.format("UIGlobal.helpPage",url) %>
<jsp:include page="/WEB-INF/template/UI-footer.jsp" flush="true" />
