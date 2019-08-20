<%@   page language="java" pageEncoding="utf-8" contentType="text/html;charset=utf-8"
%><%@ page import="org.archive.wayback.core.UIResults"
%><%@ page import="org.archive.wayback.core.WaybackRequest"
%><%
UIResults results = UIResults.extractReplay(request);
WaybackRequest wbr = results.getWbRequest();
if(!wbr.isLiveWebRequest()) {
        %><jsp:include page="/WEB-INF/replay/Links.jsp" flush="true" /><%
}
%>
