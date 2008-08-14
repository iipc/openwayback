<%@ page language="java" pageEncoding="utf-8" contentType="text/html;charset=utf-8"%>
<%@ page import="org.archive.wayback.core.UIResults" %>
<%@ page import="org.archive.wayback.util.StringFormatter" %>
<jsp:include page="/WEB-INF/template/UI-header.jsp" flush="true" />
<%

UIResults results = UIResults.getGeneric(request);
StringFormatter fmt = results.getWbRequest().getFormatter();

%>
<form action="../../replay">
<%= fmt.format("AdvancedSearch.url") %>
<input type="TEXT" name="url" width="80">
<br></br>
<%= fmt.format("AdvancedSearch.exactDate") %>
<input type="TEXT" name="exactdate" width="80">
<br></br>
<%= fmt.format("AdvancedSearch.earliestDate") %>
<input type="TEXT" name="startdate" width="80">
<br></br>
<%= fmt.format("AdvancedSearch.latestDate") %>
<input type="TEXT" name="enddate" width="80">
<br></br>
<input type="HIDDEN" name="type" value="replay">
<input type="SUBMIT" value="<%= fmt.format("AdvancedSearch.submitButton") %>">
</form>
<jsp:include page="/WEB-INF/template/UI-footer.jsp" flush="true" />
