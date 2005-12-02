<jsp:include page="../../template/UI-header.jsp" />
<%@ page import="org.archive.wayback.cdx.indexer.PipelineStatus" %>
<%
PipelineStatus status = (PipelineStatus) request.getAttribute("pipelinestatus");
%>
<h2>Wayback Machine Status and Configuration</h2>
<table width="80%" border="0" cellspacing="0" cellpadding="0">
	<tr class="mainBody">
		<td>Installation Name:</td><td><%= status.getInstallationName() %></td>
	</tr>
	<tr>
		<td>Arc Directory:</td><td><%= status.getArcPath() %></td>
	</tr>
	<tr>
		<td>Index Directory:</td><td><%= status.getDatabasePath() %></td>
	</tr>
	<tr>
		<td>Index Name:</td><td><%= status.getDatabaseName() %></td>
	</tr>
	<tr>
		<td>Pipeline Path:</td><td><%= status.getPipelineWorkPath() %></td>
	</tr>
	<tr>
		<td>Pipeline Active:</td><td><%= status.isPipelineActive() ? "Yes" : "No" %></td>
	</tr>
	<tr>
		<td>ARCs Queued For Index:</td><td><%= status.getNumQueuedForIndex() %></td>
	</tr>
	<tr>
		<td>ARCs Queued For Merge:</td><td><%= status.getNumQueuedForMerge() %></td>
	</tr>
	<tr>
		<td>ARCs Merged:</td><td><%= status.getNumIndexed() %></td>
	</tr>
</table>
<jsp:include page="../../template/UI-footer.jsp" />
