<%@ page language="java" pageEncoding="utf-8" contentType="text/html;charset=utf-8"%>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Date" %>
<%@ page import="org.archive.wayback.exception.WaybackException" %>
<%@ page import="org.archive.wayback.exception.ResourceNotInArchiveException"%>
<%@ page import="org.archive.wayback.exception.ResourceNotAvailableException"%>
<%@ page import="org.archive.wayback.core.CaptureSearchResult" %>
<%@ page import="org.archive.wayback.core.CaptureSearchResults" %>
<%@ page import="org.archive.wayback.core.UIResults" %>
<%@ page import="org.archive.wayback.core.WaybackRequest" %>
<%@ page import="org.archive.wayback.util.StringFormatter" %>
<%@ page import="org.archive.wayback.util.url.UrlOperations" %>
<%@ page import="org.archive.wayback.partition.PartitionsToGraph" %>

<%@ page import="org.archive.wayback.util.partition.Partitioner" %>
<%@ page import="org.archive.wayback.util.partition.Partition" %>
<%@ page import="org.archive.wayback.util.partition.PartitionSize" %>
<%@ page import="org.archive.wayback.partition.PartitionPartitionMap" %>
<%@page import="org.archive.wayback.exception.ResourceNotAvailableException"%>
<%
UIResults results = UIResults.extractException(request);
WaybackException e = results.getException();
WaybackRequest wbr = results.getWbRequest();
e.setupResponse(response);
String staticPrefix = wbr.getAccessPoint().getStaticPrefix();
String queryPrefix = wbr.getAccessPoint().getQueryPrefix();
String replayPrefix = wbr.getAccessPoint().getReplayPrefix();
String requestUrl = wbr.getRequestUrl();

StringFormatter fmt = results.getWbRequest().getFormatter();
%>

<h2><%= fmt.format(e.getTitleKey()) %></h2>
<p><b><%= fmt.format(e.getMessageKey(),e.getMessage()) %></b></p>
<%
if(e instanceof ResourceNotInArchiveException) {
	ResourceNotInArchiveException niae = (ResourceNotInArchiveException) e;
	List<String> closeMatches = niae.getCloseMatches();
	if(closeMatches != null && !closeMatches.isEmpty()) {
%>
		<p>
		Other possible close matches to try:<br></br>
<%
		WaybackRequest tmp = wbr.clone();
		for(String closeMatch : closeMatches) {
			tmp.setRequestUrl(closeMatch);
			String link = queryPrefix + "query?" +
				tmp.getQueryArguments();
%>
			<a href="<%= link %>"><%= closeMatch %></a><br>
<%
		}
	}
	String parentUrl = UrlOperations.getUrlParentDir(requestUrl);
	if(parentUrl != null) {
		WaybackRequest tmp = wbr.clone();
		tmp.setRequestUrl(parentUrl);
		tmp.setUrlQueryRequest();
		String link = queryPrefix + "query?" +
			tmp.getQueryArguments();
		String escapedLink = fmt.escapeHtml(link);
		String escapedParentUrl = fmt.escapeHtml(parentUrl);
		%>
		</p>
		<p>
		More options:<br></br>
			Try Searching all pages under <a href="<%= escapedLink %>"><%= escapedParentUrl %></a>
		</p>
		<%
	}
} else if(e instanceof ResourceNotAvailableException) {
%>
	<div class="wm-nav-link-div">
	<%
	CaptureSearchResults cResults = results.getCaptureResults();
	Date firstDate = cResults.getFirstResultDate();
	Date lastDate = cResults.getLastResultDate();
	PartitionPartitionMap yearMap = 
		new PartitionPartitionMap();
	PartitionSize yearSize = Partitioner.yearSize;
	Partitioner<Partition<CaptureSearchResult>> yearPartitioner = 
		new Partitioner<Partition<CaptureSearchResult>>(yearMap);

	List<Partition<Partition<CaptureSearchResult>>> yearPartitions = 
		yearPartitioner.getRange(yearSize,firstDate,lastDate);

	String navs[] = PartitionsToGraph.getNavigators(fmt,results.getResult());
	String links[] = PartitionsToGraph.getNavigatorLinks(yearPartitions,results.getURIConverter());
	String searchUrl = wbr.getRequestUrl();
	String starLink = fmt.escapeHtml(queryPrefix + "*/" + searchUrl);
	links[PartitionsToGraph.NAV_CURRENT] = starLink;
		for(int i = 0; i < navs.length; i++) {
			if(i > 0) {
				%>&nbsp;<%
			}
			if(links[i] == null) {
				%><%= navs[i] %><%				
			} else {
				%>&nbsp;<a href="<%= links[i] %>"><%= navs[i] %></a>&nbsp;<%
			}
		}
	%>
	</div>
<%
}
%>
