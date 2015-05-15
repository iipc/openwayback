<%@ page language="java" pageEncoding="utf-8" contentType="text/html;charset=utf-8"%>
<%@ page import="org.archive.wayback.core.WaybackRequest"%>
<%@ page import="org.archive.wayback.util.html.SelectHTML"%>
<%@ page import="org.archive.wayback.util.StringFormatter"%>
<%@ page import="org.archive.wayback.core.UIResults"%>
<%
UIResults results = UIResults.getGeneric(request);
WaybackRequest wbRequest = results.getWbRequest();
StringFormatter fmt = wbRequest.getFormatter();

SelectHTML window = new SelectHTML("foo");
window.setProps("onchange=\"SetAnchorWindow(this.value); location.reload(true);\"");
window.addOption(fmt.format("UIGlobal.DateLabelNone"),"0");
window.addOption("1 ".concat(fmt.format("UIGlobal.DateLabelMinute")),"60");
window.addOption("1 ".concat(fmt.format("UIGlobal.DateLabelDay")),"86400");
window.addOption("1 ".concat(fmt.format("UIGlobal.DateLabelWeek")),"604800");
window.addOption("1 ".concat(fmt.format("UIGlobal.DateLabelMonth")),"2592000");
window.addOption("1 ".concat(fmt.format("UIGlobal.DateLabelYear")),"31536000");
window.addOption("10 ".concat(fmt.format("UIGlobal.DateLabelYears")),"315360000");

WaybackRequest wbr = results.getWbRequest();
window.setActive(wbr.get(WaybackRequest.REQUEST_ANCHOR_WINDOW));
%><jsp:include page="/WEB-INF/template/CookieJS.jsp" flush="true" /><%= window.draw() %>