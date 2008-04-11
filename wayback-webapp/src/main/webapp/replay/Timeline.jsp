<%@ page language="java" pageEncoding="utf-8" contentType="text/html;charset=utf-8"%>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.text.ParseException" %>
<%@ page import="org.archive.wayback.WaybackConstants" %>
<%@ page import="org.archive.wayback.core.SearchResult" %>
<%@ page import="org.archive.wayback.core.Timestamp" %>
<%@ page import="org.archive.wayback.core.UIResults" %>
<%@ page import="org.archive.wayback.core.WaybackRequest" %>
<%@ page import="org.archive.wayback.query.UIQueryResults" %>
<%@ page import="org.archive.wayback.query.resultspartitioner.ResultsTimelinePartitionsFactory" %>
<%@ page import="org.archive.wayback.query.resultspartitioner.ResultsPartition" %>
<%@ page import="org.archive.wayback.util.StringFormatter" %>
<%

String contextRoot = request.getScheme() + "://" + request.getServerName() + ":" 
	+ request.getServerPort() + request.getContextPath();

UIQueryResults results = (UIQueryResults) UIResults.getFromRequest(request);
StringFormatter fmt = results.getFormatter();

Timestamp searchStartTs = results.getStartTimestamp();
Timestamp searchEndTs = results.getEndTimestamp();
Timestamp exactTs = results.getExactRequestedTimestamp();
String searchUrl = results.getSearchUrl();
Date exactDate = exactTs.getDate();

String exactDateStr = exactTs.getDateStr();
WaybackRequest wbRequest = results.getWbRequest();
String resolution = wbRequest.get(WaybackConstants.REQUEST_RESOLUTION);
if(resolution == null) {
	resolution = WaybackConstants.REQUEST_RESOLUTION_AUTO;
}
String metaMode = wbRequest.get(WaybackConstants.REQUEST_META_MODE);
String metaChecked = "";
if(metaMode != null && metaMode.equals("yes")) {
	metaChecked = "checked";
}

String searchString = results.getSearchUrl();

SearchResult first = null;
SearchResult prev = null;
SearchResult next = null;
SearchResult last = null;

int resultCount = results.getResultsReturned();
int resultIndex = 1;
Iterator<SearchResult> it = results.resultsIterator();
while(it.hasNext()) {
	SearchResult res = it.next();
	String resDateStr = res.get(WaybackConstants.RESULT_CAPTURE_DATE);
	int compared = resDateStr.compareTo(exactDateStr.substring(0,resDateStr.length()));
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

String minResolution = ResultsTimelinePartitionsFactory.getMinResolution(
							results.getResults());

String optimal = "";
if(minResolution.equals(WaybackConstants.REQUEST_RESOLUTION_HOURS)) {
	optimal = fmt.format("TimelineView.timeRange.hours");
} else if(minResolution.equals(WaybackConstants.REQUEST_RESOLUTION_DAYS)) {
	optimal = fmt.format("TimelineView.timeRange.days");
} else if(minResolution.equals(WaybackConstants.REQUEST_RESOLUTION_MONTHS)) {
	optimal = fmt.format("TimelineView.timeRange.months");
} else if(minResolution.equals(WaybackConstants.REQUEST_RESOLUTION_TWO_MONTHS)) {
	  optimal = fmt.format("TimelineView.timeRange.twomonths");
} else if(minResolution.equals(WaybackConstants.REQUEST_RESOLUTION_YEARS)) {
	optimal = fmt.format("TimelineView.timeRange.years");
} else {
	optimal = fmt.format("TimelineView.timeRange.unknown");
}
String autoOptString = fmt.format("TimelineView.timeRange.auto",optimal);

ArrayList<ResultsPartition> partitions;
if(resolution.equals(WaybackConstants.REQUEST_RESOLUTION_HOURS)) {
	hoursOptSelected = "selected";
	partitions = ResultsTimelinePartitionsFactory.getHour(results.getResults(),
		wbRequest);
} else if(resolution.equals(WaybackConstants.REQUEST_RESOLUTION_DAYS)) {
	daysOptSelected = "selected";
	partitions = ResultsTimelinePartitionsFactory.getDay(results.getResults(),
		wbRequest);
} else if(resolution.equals(WaybackConstants.REQUEST_RESOLUTION_MONTHS)) {
	monthsOptSelected = "selected";
	partitions = ResultsTimelinePartitionsFactory.getMonth(results.getResults(),
		wbRequest);
} else if(resolution.equals(WaybackConstants.REQUEST_RESOLUTION_TWO_MONTHS)) {
	  monthsOptSelected = "selected";
	  partitions = ResultsTimelinePartitionsFactory.getTwoMonth(results.getResults(),
	    wbRequest);
} else if(resolution.equals(WaybackConstants.REQUEST_RESOLUTION_YEARS)) {
	yearsOptSelected = "selected";
	partitions = ResultsTimelinePartitionsFactory.getYear(results.getResults(),
		wbRequest);
} else {
	autoOptSelected = "selected";
	partitions = ResultsTimelinePartitionsFactory.getAuto(results.getResults(),
		wbRequest);
}
int numPartitions = partitions.size();
ResultsPartition firstP = (ResultsPartition) partitions.get(0);
ResultsPartition lastP = (ResultsPartition) partitions.get(numPartitions -1);

String firstDate = firstP.getTitle();
String lastDate = lastP.getTitle();
String titleString = "";
%>
<script type="text/javascript">
  function changeResolution() {
    document.timeline.submit();
  }
  function changeMeta() {
    document.timeline.submit();
  }

var open = true;
  
function handleDragClick() {
	var daDiv = document.getElementById("wm-ipp");
	var daDraggerDiv = document.getElementById("wm-dragger");
	while(daDraggerDiv.hasChildNodes()) {
		daDraggerDiv.removeChild(daDraggerDiv.firstChild);
	}
	var newText;
	if(open) {
		open = false;
		daDiv.style.width = "100px";
		newText = "<";
	} else {
		open = true;
		daDiv.style.width = "100%";
		newText = ">";
	}
	daDraggerDiv.appendChild(document.createTextNode(newText));
}


</script>

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
								<td align="center" valign="bottom" nowrap><img wmSpecial="1" src="<%= contextRoot %>/images/mark.jpg"></td>
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
									results.resultToDate(first)) + "\"";
							%><a wmSpecial="1" href="<%= results.resultToReplayUrl(first) %>"><%
						}
						%><img <%= titleString %> wmSpecial="1" border=0 width=19 height=20 src="<%= contextRoot %>/images/first.jpg"><%
						if(first != null) {
							%></a><%
						}
						titleString = "";
						if(prev != null) {
							titleString = "title=\"" + 
								fmt.format("TimelineView.prevVersionTitle",
									results.resultToDate(prev)) + "\"";
							%><a wmSpecial="1" href="<%= results.resultToReplayUrl(prev) %>"><%
						}
						%><img <%= titleString %> wmSpecial="1" border=0 width=13 height=20 src="<%= contextRoot %>/images/prev.jpg"><%
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
		  	SearchResult result = (SearchResult) partitionResults.get(0);
			replayUrl = results.resultToReplayUrl(result);
			prettyDateTime = fmt.format("TimelineView.markDateTitle",results.resultToDate(result));
			
		} else if (numResults > 1) {
			imageUrl = contextRoot + "/images/mark_several.jpg";
		  	SearchResult result = (SearchResult) partitionResults.get(numResults - 1);
			replayUrl = results.resultToReplayUrl(result);
			prettyDateTime = fmt.format("TimelineView.markDateTitle",results.resultToDate(result));

		}
		if((i > 0) && (i < numPartitions)) {

%><img wmSpecial="1" border=0 width=1 height=16 src="<%= contextRoot %>/images/linemark.jpg"><%
		
		}

		if(replayUrl == null) {

%><img wmSpecial="1" border=0 width=7 height=16 src="<%= imageUrl %>"><%
		
		} else {

%><a wmSpecial="1" href="<%= replayUrl %>"><img wmSpecial="1" border=0 width=7 height=16 title="<%= prettyDateTime %>" src="<%= imageUrl %>"></a><%

		}
	}

%></td>
					<td nowrap><%
						titleString = "";
						if(next != null) {
							titleString = "title=\"" + 
								fmt.format("TimelineView.nextVersionTitle",
									results.resultToDate(next)) + "\"";
							%><a wmSpecial="1" href="<%= results.resultToReplayUrl(next) %>"><%
						}
						%><img wmSpecial="1" <%= titleString %> border=0 width=13 height=20 src="<%= contextRoot %>/images/next.jpg"><%
						if(first != null) {
							%></a><%
						}
						titleString = "";
						if(last != null) {
							titleString = "title=\"" + 
								fmt.format("TimelineView.lastVersionTitle",
									results.resultToDate(last)) + "\"";
							%><a wmSpecial="1" href="<%= results.resultToReplayUrl(last) %>"><%
						}
						%><img wmSpecial="1" <%= titleString %> border=0 width=19 height=20 src="<%= contextRoot %>/images/last.jpg"><%
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
				%><input type="checkbox" name="metamode" value="yes" <%=
					metaChecked 
				%> onClick="changeMeta()">&nbsp
			</form>
      -->
      <a wmSpecial="1" href="<%= contextRoot %>/help.jsp" target="_top"><%=
      fmt.format("UIGlobal.helpLink")
      %></a>
		</td>
		<td>
			<img wmSpecial="1" alt='' height='1' src='<%= contextRoot %>/images/1px.gif' width='5'>
		</td>
	</tr>
</table>
</div>
<script type="text/javascript">
	var daDiv = document.getElementById("wm-ipp");
	var daParentNode = daDiv.parentNode;
	var daBody = document.body;
	daParentNode.removeChild(daDiv);
	daBody.insertBefore(daDiv,daBody.firstChild);
</script>
