<%@ page language="java" pageEncoding="utf-8" contentType="text/html;charset=utf-8"%>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.text.ParseException" %>
<%@ page import="org.archive.wayback.WaybackConstants" %>
<%@ page import="org.archive.wayback.core.CaptureSearchResult" %>
<%@ page import="org.archive.wayback.core.CaptureSearchResults" %>
<%@ page import="org.archive.wayback.core.UIResults" %>
<%@ page import="org.archive.wayback.core.WaybackRequest" %>
<%@ page import="org.archive.wayback.query.resultspartitioner.ResultsTimelinePartitionsFactory" %>
<%@ page import="org.archive.wayback.query.resultspartitioner.ResultsPartition" %>
<%@ page import="org.archive.wayback.util.StringFormatter" %>
<%

String contextRoot = request.getScheme() + "://" + request.getServerName() + ":" 
	+ request.getServerPort() + request.getContextPath();

UIResults results = UIResults.extractReplay(request);
WaybackRequest wbRequest = results.getWbRequest();
StringFormatter fmt = wbRequest.getFormatter();
CaptureSearchResults cResults = results.getCaptureResults();

String exactDateStr = results.getResult().getCaptureTimestamp();
Date exactDate = results.getResult().getCaptureDate();
String searchUrl = wbRequest.getRequestUrl();
String resolution = wbRequest.getTimelineResolution();

if(resolution == null) {
	resolution = WaybackRequest.REQUEST_RESOLUTION_AUTO;
}
String metaChecked = "";
if(wbRequest.isMetaMode()) {
	metaChecked = "checked";
}

CaptureSearchResult first = null;
CaptureSearchResult prev = null;
CaptureSearchResult next = null;
CaptureSearchResult last = null;

long resultCount = cResults.getReturnedCount();
int resultIndex = 1;
Iterator<CaptureSearchResult> it = cResults.iterator();
while(it.hasNext()) {
	CaptureSearchResult res = it.next();
	Date resDate = res.getCaptureDate();
	
	int compared = resDate.compareTo(exactDate);
	if(compared < 0) {
		resultIndex++;
		prev = res;
		if(first == null) {
			first = res;
		}
	} else if(compared > 0) {
		last = res;
		if(next == null) {
			next = res;
		}
	}
}
// string to indicate which select option is currently active
String yearsOptSelected = "";
String monthsOptSelected = "";
String daysOptSelected = "";
String hoursOptSelected = "";
String autoOptSelected = "";

String minResolution = ResultsTimelinePartitionsFactory.getMinResolution(cResults);

String optimal = "";
if(minResolution.equals(WaybackRequest.REQUEST_RESOLUTION_HOURS)) {
	optimal = fmt.format("TimelineView.timeRange.hours");
} else if(minResolution.equals(WaybackRequest.REQUEST_RESOLUTION_DAYS)) {
	optimal = fmt.format("TimelineView.timeRange.days");
} else if(minResolution.equals(WaybackRequest.REQUEST_RESOLUTION_MONTHS)) {
	optimal = fmt.format("TimelineView.timeRange.months");
} else if(minResolution.equals(WaybackRequest.REQUEST_RESOLUTION_TWO_MONTHS)) {
	  optimal = fmt.format("TimelineView.timeRange.twomonths");
} else if(minResolution.equals(WaybackRequest.REQUEST_RESOLUTION_YEARS)) {
	optimal = fmt.format("TimelineView.timeRange.years");
} else {
	optimal = fmt.format("TimelineView.timeRange.unknown");
}
String autoOptString = fmt.format("TimelineView.timeRange.auto",optimal);

ArrayList<ResultsPartition> partitions;
if(resolution.equals(WaybackRequest.REQUEST_RESOLUTION_HOURS)) {
	hoursOptSelected = "selected";
	partitions = ResultsTimelinePartitionsFactory.getHour(cResults,wbRequest);
} else if(resolution.equals(WaybackRequest.REQUEST_RESOLUTION_DAYS)) {
	daysOptSelected = "selected";
	partitions = ResultsTimelinePartitionsFactory.getDay(cResults,wbRequest);
} else if(resolution.equals(WaybackRequest.REQUEST_RESOLUTION_MONTHS)) {
	monthsOptSelected = "selected";
	partitions = ResultsTimelinePartitionsFactory.getMonth(cResults,wbRequest);
} else if(resolution.equals(WaybackRequest.REQUEST_RESOLUTION_TWO_MONTHS)) {
	  monthsOptSelected = "selected";
	  partitions = ResultsTimelinePartitionsFactory.getTwoMonth(cResults,wbRequest);
} else if(resolution.equals(WaybackRequest.REQUEST_RESOLUTION_YEARS)) {
	yearsOptSelected = "selected";
	partitions = ResultsTimelinePartitionsFactory.getYear(cResults,wbRequest);
} else {
	autoOptSelected = "selected";
	partitions = ResultsTimelinePartitionsFactory.getAuto(cResults,wbRequest);
}
int numPartitions = partitions.size();
ResultsPartition firstP = (ResultsPartition) partitions.get(0);
ResultsPartition lastP = (ResultsPartition) partitions.get(numPartitions -1);

String firstDate = firstP.getTitle();
String lastDate = lastP.getTitle();
String titleString = "";
%>
<!--
  ======================================
  BEGIN Wayback INSERTED TIMELINE BANNER

  The following HTML has been inserted
  by the Wayback application to enhance
  the viewing experience, and was not
  part of the original archived content.
  ======================================
-->
<div id="wm-ipp" style="position:relative;z-index:99999;border:1px solid;color:black;background-color:lightYellow;font-size:10px;font-family:sans-serif;padding:5px" >

<table cellspacing="0" border="0" cellpadding="0"  width="100%">
	<tr>
		<td width="1" nowrap></td>
		<td>
			<!-- Viewing -->
			<table cellspacing="0" border="0" cellpadding="0" width="100%">
				<tr>
					<td>
						<span><%= fmt.format("TimelineView.viewingVersion",resultIndex,resultCount) %>&nbsp;</span>
					</td>
				</tr>
				<tr>
					<td nowrap><span> <%= fmt.format("TimelineView.viewingVersionDate",exactDate) %> </span>&nbsp;&nbsp;</td>
				</tr>
			</table>
		</td>
		<td width="400" align="center">
			<table>
				<tr>
					<td width="50%"></td>
					<td>
						<table cellspacing="0" border="0" cellpadding="0"  width="100%">
							<tr>
								<td width="48%" nowrap><span><%= firstDate %></span></td>
								<td align="center" valign="bottom" nowrap><img style="display: inline;" wmSpecial="1" src="<%= contextRoot %>/images/mark.jpg"></td>
								<td width="48%" nowrap align="right"><span><%= lastDate %></span></td>
							</tr>
						</table>
					</td>
					<td width="50%"></td>
				</tr>
				<tr>
					<td nowrap align="right"><%
						titleString = "";
						if(first != null) {
							titleString = "title=\"" + 
								fmt.format("TimelineView.firstVersionTitle",
									first.getCaptureDate()) + "\"";
							%><a wmSpecial="1" href="<%= results.resultToReplayUrl(first) %>"><%
						}
						%><img style="display: inline;" <%= titleString %> wmSpecial="1" border=0 width=19 height=20 src="<%= contextRoot %>/images/first.jpg"><%
						if(first != null) {
							%></a><%
						}
						titleString = "";
						if(prev != null) {
							titleString = "title=\"" + 
								fmt.format("TimelineView.prevVersionTitle",
										prev.getCaptureDate()) + "\"";
							%><a wmSpecial="1" href="<%= results.resultToReplayUrl(prev) %>"><%
						}
						%><img style="display: inline;" <%= titleString %> wmSpecial="1" border=0 width=13 height=20 src="<%= contextRoot %>/images/prev.jpg"><%
						if(first != null) {
							%></a><%
						}
					%></td>
					<td nowrap><%
			
	for(int i = 0; i < numPartitions; i++) {
		ResultsPartition partition = (ResultsPartition) partitions.get(i);
		ArrayList partitionResults = partition.getMatches();
		int numResults = partitionResults.size();
		String imageUrl = contextRoot + "/images/line.jpg";
		String replayUrl = null;
		String prettyDateTime = null;
		if(numResults == 1) {
			imageUrl = contextRoot + "/images/mark_one.jpg";
		  	CaptureSearchResult result = (CaptureSearchResult) partitionResults.get(0);
			replayUrl = results.resultToReplayUrl(result);
			prettyDateTime = fmt.format("TimelineView.markDateTitle",result.getCaptureDate());
			
		} else if (numResults > 1) {
			imageUrl = contextRoot + "/images/mark_several.jpg";
			CaptureSearchResult result = (CaptureSearchResult) partitionResults.get(numResults - 1);
			replayUrl = results.resultToReplayUrl(result);
			prettyDateTime = fmt.format("TimelineView.markDateTitle",result.getCaptureDate());

		}
		if((i > 0) && (i < numPartitions)) {

%><img style="display: inline;" wmSpecial="1" border=0 width=1 height=16 src="<%= contextRoot %>/images/linemark.jpg"><%
		
		}

		if(replayUrl == null) {

%><img style="display: inline;" wmSpecial="1" border=0 width=7 height=16 src="<%= imageUrl %>"><%
		
		} else {

%><a wmSpecial="1" href="<%= replayUrl %>"><img style="display: inline;" wmSpecial="1" border=0 width=7 height=16 title="<%= prettyDateTime %>" src="<%= imageUrl %>"></a><%

		}
	}

%></td>
					<td nowrap><%
						titleString = "";
						if(next != null) {
							titleString = "title=\"" + 
								fmt.format("TimelineView.nextVersionTitle",
									next.getCaptureDate()) + "\"";
							%><a wmSpecial="1" href="<%= results.resultToReplayUrl(next) %>"><%
						}
						%><img style="display: inline;" wmSpecial="1" <%= titleString %> border=0 width=13 height=20 src="<%= contextRoot %>/images/next.jpg"><%
						if(first != null) {
							%></a><%
						}
						titleString = "";
						if(last != null) {
							titleString = "title=\"" + 
								fmt.format("TimelineView.lastVersionTitle",
									last.getCaptureDate()) + "\"";
							%><a wmSpecial="1" href="<%= results.resultToReplayUrl(last) %>"><%
						}
						%><img style="display: inline;" wmSpecial="1" <%= titleString %> border=0 width=19 height=20 src="<%= contextRoot %>/images/last.jpg"><%
						if(first != null) {
							%></a><%
						}
					%></td>
				</tr>
			</table>
		</td>
		<td align="right" width="400">
			<!-- Resolution -->
			<!--
			 need to get cookie data passing set up before this can be re-enabled:
			<form wmSpecial="1" name="timeline" method="GET" target="_top" action="<%= contextRoot + "/frameset" %>">
				<input type="hidden" name="url" value="<%= searchUrl %>">
				<input type="hidden" name="exactdate" value="<%= exactDateStr %>">
				<input type="hidden" name="type" value="urlclosestquery">
				<%= fmt.format("TimelineView.timeRange") %>
				<select NAME="resolution" SIZE="1" onChange="changeResolution()">
					<option <%= yearsOptSelected %> value="years">
						<%= fmt.format("TimelineView.timeRange.years") %>
					</option>
					<option <%= monthsOptSelected %> value="months">
						<%= fmt.format("TimelineView.timeRange.months") %>
					</option>
					<option <%= daysOptSelected %>  value="days">
						<%= fmt.format("TimelineView.timeRange.days") %>
					</option>
					<option <%= hoursOptSelected %> value="hours">
						<%= fmt.format("TimelineView.timeRange.hours") %>
					</option>
					<option <%= autoOptSelected %> value="auto"><%= autoOptString %></option>
				</select>&nbsp;<%= 
					fmt.format("TimelineView.metaDataCheck") 
				%><input type="checkbox" name="<%= WaybackRequest.REQUEST_META_MODE%>" value="<%= WaybackRequest.REQUEST_YES %>" <%=
					metaChecked 
				%> onClick="changeMeta()">&nbsp;
			</form>
      -->
      <a wmSpecial="1" href="<%= contextRoot %>/help.jsp" target="_top"><%=
      fmt.format("UIGlobal.helpLink")
      %></a>
		</td>
		<td>
			<img style="display: inline;" wmSpecial="1" alt='' height='1' src='<%= contextRoot %>/images/1px.gif' width='5'>
		</td>
	</tr>
</table>
</div>
<!--
  ======================================
  END Wayback INSERTED TIMELINE BANNER
  ======================================
-->
