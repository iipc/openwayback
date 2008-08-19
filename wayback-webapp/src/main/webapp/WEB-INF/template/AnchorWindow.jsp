<%@
 page language="java" pageEncoding="utf-8" contentType="text/html;charset=utf-8"
%><%@
 page import="org.archive.wayback.core.UIResults"
%><%@
 page import="org.archive.wayback.core.WaybackRequest"
%><%@ page import="org.archive.wayback.util.html.SelectHTML"
%><%
SelectHTML window = new SelectHTML("foo");
window.setProps("onchange=\"SetAnchorWindow(this.value); location.reload(true);\"");
window.addOption("none","0");
window.addOption("1 minute","60");
window.addOption("1 day","86400");
window.addOption("1 week","604800");
window.addOption("1 month","2592000");
window.addOption("1 year","31536000");
window.addOption("10 years","315360000");
UIResults results = UIResults.getGeneric(request);
WaybackRequest wbr = results.getWbRequest();
window.setActive(wbr.get(WaybackRequest.REQUEST_ANCHOR_WINDOW));
%><jsp:include page="/WEB-INF/template/CookieJS.jsp" flush="true" /><%= window.draw() %>