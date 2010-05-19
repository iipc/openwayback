<%@ page language="java" pageEncoding="utf-8" contentType="text/html;charset=utf-8"%>
<%@ page import="org.archive.wayback.core.WaybackRequest" %>
<%@ page import="org.archive.wayback.core.UIResults" %>
<%@ page import="org.archive.wayback.util.StringFormatter" %>
<jsp:include page="/WEB-INF/template/UI-header.jsp" flush="true" />
<%

UIResults results = UIResults.getGeneric(request);
WaybackRequest wbRequest = results.getWbRequest();
StringFormatter fmt = wbRequest.getFormatter();

String staticPrefix = wbRequest.getAccessPoint().getStaticPrefix();
String queryPrefix = wbRequest.getAccessPoint().getQueryPrefix();
String replayPrefix = wbRequest.getAccessPoint().getReplayPrefix();


%>
<form action="<%= queryPrefix %>query">
<%= fmt.format("AdvancedSearch.searchTypeLabel") %>
<select name="type">
  <option value="urlquery"><%= fmt.format("AdvancedSearch.searchTypeExactOption") %></option>
  <option value="prefixquery"><%= fmt.format("AdvancedSearch.searchTypePrefixOption") %></option>
</select>
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
<input type="SUBMIT" value="<%= fmt.format("AdvancedSearch.submitButton") %>">
</form>
<jsp:include page="/WEB-INF/template/UI-footer.jsp" flush="true" />
