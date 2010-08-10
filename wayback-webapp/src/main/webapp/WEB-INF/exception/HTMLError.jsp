<%@ page language="java" pageEncoding="utf-8" contentType="text/html;charset=utf-8"%>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="org.archive.wayback.exception.WaybackException" %>
<%@ page import="org.archive.wayback.ResultURIConverter" %>
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
<jsp:include page="/WEB-INF/template/UI-header.jsp" flush="true" />

        <div id="positionHome">
            <section>
            <div id="error">

                <h2><%= fmt.format(e.getTitleKey()) %></h2>
                <p><%= fmt.format(e.getMessageKey(),e.getMessage()) %></p>
<%
if(e instanceof ResourceNotInArchiveException) {
	ResourceNotInArchiveException niae = (ResourceNotInArchiveException) e;
	List<String> closeMatches = niae.getCloseMatches();
	if(closeMatches != null && !closeMatches.isEmpty()) {
%>
		        <p>Other possible close matches to try:</p>
		        <p>
<%
		WaybackRequest tmp = wbr.clone();
		for(String closeMatch : closeMatches) {
			tmp.setRequestUrl(closeMatch);
			String link = queryPrefix + "query?" +
				tmp.getQueryArguments();
%>
			    <a href="<%= link %>"><%= closeMatch %></a><br/>
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
		        <p>More options:</p>
			    <p>Try Searching all pages under <a href="<%= escapedLink %>"><%= escapedParentUrl %></a></p>
		<%
	}
} else if(e instanceof ResourceNotAvailableException) {
%>
	<div class="wm-nav-link-div">
	<%
	ResourceNotAvailableException rnae = (ResourceNotAvailableException) e;
	
	CaptureSearchResults cResults = rnae.getCaptureSearchResults();
	Iterator<CaptureSearchResult> itr = cResults.iterator();
	CaptureSearchResult prev = null;
	CaptureSearchResult next = null;
	while(itr.hasNext()) {
		CaptureSearchResult cur = itr.next();
		if(cur.isClosest()) {
			break;
		}
		prev = cur;
	}
	if(itr.hasNext()) {
		next = itr.next();
	}
	if((prev != null) || (next != null)) {
		String dateFormat = "{0,date,MMMM dd, yyyy HH:mm:ss}";
		ResultURIConverter conv = wbr.getAccessPoint().getUriConverter();
		%>
		<div>Or try another close version:</div>
		<%
		if(prev != null) {
			String safePrevReplay = fmt.escapeHtml(conv.makeReplayURI(prev.getCaptureTimestamp(),prev.getOriginalUrl()));
			%>
			<div>Previous:<a href="<%= safePrevReplay %>"><%= fmt.format(dateFormat,prev.getCaptureDate())%></a></div>
			<%
		}
		if(next != null) {
			String safeNextReplay = fmt.escapeHtml(conv.makeReplayURI(next.getCaptureTimestamp(),next.getOriginalUrl()));
			%>
			<div>Next:<a href="<%= safeNextReplay %>"><%= fmt.format(dateFormat,next.getCaptureDate())%></a></div>
			<%
		}
	}
	%>
	</div>
<%
}
%>

            </div>
            </section>
            <div id="errorBorder"></div>

<jsp:include page="/WEB-INF/template/UI-footer.jsp" flush="true" />