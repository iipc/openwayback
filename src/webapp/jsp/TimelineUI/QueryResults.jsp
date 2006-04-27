<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.text.ParseException" %>
<%@ page import="org.archive.wayback.WaybackConstants" %>
<%@ page import="org.archive.wayback.core.SearchResult" %>
<%@ page import="org.archive.wayback.core.Timestamp" %>
<%@ page import="org.archive.wayback.core.WaybackRequest" %>
<%@ page import="org.archive.wayback.query.UIQueryResults" %>
<%@ page import="org.archive.wayback.query.resultspartitioner.ResultsTimelinePartitionsFactory" %>
<%@ page import="org.archive.wayback.query.resultspartitioner.ResultsPartition" %>
<%

String contextRoot = request.getScheme() + "://" + request.getLocalName() + ":" 
	+ request.getLocalPort() + request.getContextPath();

UIQueryResults results = (UIQueryResults) request.getAttribute("ui-results");

Timestamp searchStartTs = results.getStartTimestamp();
Timestamp searchEndTs = results.getEndTimestamp();
Timestamp exactTs = results.getExactRequestedTimestamp();
String searchUrl = results.getSearchUrl();

String prettySearchStart = searchStartTs.prettyDate();
String prettySearchEnd = searchEndTs.prettyDate();
String prettyExactTs = exactTs.prettyDateTime();
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
Iterator it = results.resultsIterator();
while(it.hasNext()) {
	SearchResult res = (SearchResult) it.next();
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

ArrayList partitions;
if(resolution.equals(WaybackConstants.REQUEST_RESOLUTION_HOURS)) {
	hoursOptSelected = "selected";
	partitions = ResultsTimelinePartitionsFactory.getHour(results.getResults());
} else if(resolution.equals(WaybackConstants.REQUEST_RESOLUTION_DAYS)) {
	daysOptSelected = "selected";
	partitions = ResultsTimelinePartitionsFactory.getDay(results.getResults());
} else if(resolution.equals(WaybackConstants.REQUEST_RESOLUTION_MONTHS)) {
	monthsOptSelected = "selected";
	partitions = ResultsTimelinePartitionsFactory.getMonth(results.getResults());
} else if(resolution.equals(WaybackConstants.REQUEST_RESOLUTION_YEARS)) {
	yearsOptSelected = "selected";
	partitions = ResultsTimelinePartitionsFactory.getYear(results.getResults());
} else {
	autoOptSelected = "selected";
	partitions = ResultsTimelinePartitionsFactory.getAuto(results.getResults());
}
int numPartitions = partitions.size();
ResultsPartition firstP = (ResultsPartition) partitions.get(0);
ResultsPartition lastP = (ResultsPartition) partitions.get(numPartitions -1);

String firstDate = firstP.getTitle();
String lastDate = lastP.getTitle();
String titleString = "";
%>
<html>
<head>
<base target="_top" />
<link rel="stylesheet" href="<%= contextRoot %>/css/timeline.css" type="text/css">
<script type="text/javascript">
  function changeResolution() {
    document.timeline.submit();
  }
  function changeMeta() {
    document.timeline.submit();
  }
</script>
</head>
<body>
<table cellspacing="0" border="0" cellpadding="0"  width="100%">
	<tr>
		<td width="1" nowrap></td>
		<td>
			<!-- Viewing -->
			<table cellspacing="0" border="0" cellpadding="0" width="100%">
				<tr>
					<td>
						<span class="smallboldfont">Viewing version <%= resultIndex %> of <%= resultCount %>&nbsp;</span>
					</td>
				</tr>
				<tr>
					<td nowrap><span class="title"> <%= prettyExactTs %> </span>&nbsp;&nbsp;</td>
				</tr>
			</table>
		</td>
		<td width="400">
			<table>
				<tr>
					<td width="50%"></td>
					<td>
						<table cellspacing="0" border="0" cellpadding="0"  width="100%">
							<tr>
								<td width="48%" nowrap><span class="smallfont"><%= firstDate %></span></td>
								<td align="center" valign="bottom" nowrap><img src="<%= contextRoot %>/images/mark.jpg"></td>
								<td width="48%" nowrap align="right"><span class="smallfont"><%= lastDate %></span></td>
							</tr>
						</table>
					</td>
					<td width="50%"></td>
				</tr>
				<tr>
					<td nowrap align="right"><%
						titleString = "";
						if(first != null) {
							titleString = "title=\"First version (" + results.resultToPrettyDateTime(first) + ")\"";
							%><a href="<%= results.resultToReplayUrl(first) %>"><%
						}
						%><img <%= titleString %> border=0 width=19 height=20 src="<%= contextRoot %>/images/first.jpg"><%
						if(first != null) {
							%></a><%
						}
						titleString = "";
						if(prev != null) {
							titleString = "title=\"Previous version (" + results.resultToPrettyDateTime(prev) + ")\"";
							%><a href="<%= results.resultToReplayUrl(prev) %>"><%
						}
						%><img <%= titleString %> border=0 width=13 height=20 src="<%= contextRoot %>/images/prev.jpg"><%
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
			prettyDateTime = results.resultToPrettyDateTime(result);
			
		} else if (numResults > 1) {
			imageUrl = contextRoot + "/images/mark_several.jpg";
		  	SearchResult result = (SearchResult) partitionResults.get(numResults - 1);
			replayUrl = results.resultToReplayUrl(result);
			prettyDateTime = results.resultToPrettyDateTime(result);

		}
		if((i > 0) && (i < numPartitions)) {

%><img border=0 width=1 height=16 src="<%= contextRoot %>/images/linemark.jpg"><%
		
		}

		if(replayUrl == null) {

%><img border=0 width=7 height=16 src="<%= imageUrl %>"><%
		
		} else {

%><a href="<%= replayUrl %>"><img border=0 width=7 height=16 title="<%= prettyDateTime %>" src="<%= imageUrl %>"></a><%

		}
	}

%></td>
					<td nowrap><%
						titleString = "";
						if(next != null) {
							titleString = "title=\"Next version (" + results.resultToPrettyDateTime(next) + ")\"";
							%><a href="<%= results.resultToReplayUrl(next) %>"><%
						}
						%><img <%= titleString %> border=0 width=13 height=20 src="<%= contextRoot %>/images/next.jpg"><%
						if(first != null) {
							%></a><%
						}
						titleString = "";
						if(last != null) {
							titleString = "title=\"Last version (" + results.resultToPrettyDateTime(last) + ")\"";
							%><a href="<%= results.resultToReplayUrl(last) %>"><%
						}
						%><img <%= titleString %> border=0 width=19 height=20 src="<%= contextRoot %>/images/last.jpg"><%
						if(first != null) {
							%></a><%
						}
					%></td>
				</tr>
			</table>
		</td>
		<td align="right">
			<!-- Resolution -->
			<form name="timeline" method="GET" target="_top" action="<%= contextRoot + "/frameset" %>">
				<input type="hidden" name="url" value="<%= searchUrl %>">
				<input type="hidden" name="exactdate" value="<%= exactDateStr %>">
				<input type="hidden" name="type" value="urlclosestquery">
				Resolution:
				<select NAME="resolution" SIZE="1" onChange="changeResolution()">
					<option <%= yearsOptSelected %> value="years">Years</option>
					<option <%= monthsOptSelected %> value="months">Months</option>
					<option <%= daysOptSelected %>  value="days">Days</option>
					<option <%= hoursOptSelected %> value="hours">Hours</option>
					<option <%= autoOptSelected %> value="auto">Auto</option>
				</select>&nbsp;Metadata:<input type="checkbox" name="metamode" value="yes" <%= metaChecked %> onClick="changeMeta()">&nbsp<a href="help.php" target="_top">Help</a>
			</form>
		</td>
		<td>
			<img alt='' height='1' src='images/1px.gif' width='5'>
		</td>
	</tr>
</table>
</body>
</html>
