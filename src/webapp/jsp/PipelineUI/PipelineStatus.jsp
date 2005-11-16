<jsp:include page="../../template/UI-header.jsp" />
<%@ page import="org.archive.wayback.cdx.indexer.PipelineStatus" %>
<%
PipelineStatus status = (PipelineStatus) request.getAttribute("pipelinestatus");
%>
<H2>Pipeline Status</H2>
<HR>
Queued For Index:<B><%= status.getNumQueuedForIndex() %></B><BR>
Queued For Merge:<B><%= status.getNumQueuedForMerge() %></B><BR>
<jsp:include page="../../template/UI-footer.jsp" />
