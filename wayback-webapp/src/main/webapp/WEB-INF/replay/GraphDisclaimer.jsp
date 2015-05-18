<%@
 page language="java" pageEncoding="utf-8" contentType="text/html;charset=utf-8"
 %><%@
 page import="java.util.Iterator"
 %><%@
 page import="java.util.ArrayList"
 %><%@
 page import="java.util.Date"
 %><%@
 page import="java.util.List"
 %><%@
 page import="java.text.ParseException"
 %><%@
 page import="org.archive.wayback.ResultURIConverter"
 %><%@
 page import="org.archive.wayback.WaybackConstants"
 %><%@
 page import="org.archive.wayback.core.CaptureSearchResult"
 %><%@
 page import="org.archive.wayback.core.CaptureSearchResults"
 %><%@
 page import="org.archive.wayback.core.UIResults"
 %><%@
 page import="org.archive.wayback.core.WaybackRequest"
 %><%@
 page import="org.archive.wayback.partition.CaptureSearchResultPartitionMap"
 %><%@
 page import="org.archive.wayback.partition.PartitionPartitionMap"
 %><%@
 page import="org.archive.wayback.partition.PartitionsToGraph"
 %><%@
 page import="org.archive.wayback.util.graph.Graph"
 %><%@
 page import="org.archive.wayback.util.graph.GraphEncoder"
 %><%@
 page import="org.archive.wayback.util.graph.GraphRenderer"
 %><%@
 page import="org.archive.wayback.util.partition.Partition"
 %><%@
 page import="org.archive.wayback.util.partition.Partitioner"
 %><%@
 page import="org.archive.wayback.util.partition.PartitionSize"
 %><%@
 page import="org.archive.wayback.util.StringFormatter"
 %><%@
 page import="org.archive.wayback.util.url.UrlOperations"
 %><%
UIResults results = UIResults.extractReplay(request);
WaybackRequest wbRequest = results.getWbRequest();
ResultURIConverter uriConverter = results.getURIConverter();

String staticPrefix = results.getStaticPrefix();
String queryPrefix = results.getQueryPrefix();
String replayPrefix = results.getReplayPrefix();

StringFormatter fmt = wbRequest.getFormatter();

String graphJspPrefix = results.getContextConfig("graphJspPrefix");
if(graphJspPrefix == null) {
	graphJspPrefix = queryPrefix;
}
CaptureSearchResults cResults = results.getCaptureResults();

String exactDateStr = results.getResult().getCaptureTimestamp();
Date exactDate = results.getResult().getCaptureDate();
String searchUrl = 
	UrlOperations.stripDefaultPortFromUrl(wbRequest.getRequestUrl());
String searchUrlSafe = fmt.escapeHtml(searchUrl);
String searchUrlJS = fmt.escapeJavaScript(searchUrl);
String resolution = wbRequest.getTimelineResolution();

CaptureSearchResult first = null;
CaptureSearchResult prev = null;
CaptureSearchResult next = null;
CaptureSearchResult last = null;

Date firstDate = wbRequest.getStartDate();
Date lastDate = wbRequest.getEndDate();

long resultCount = cResults.getReturnedCount();
int resultIndex = 1;

CaptureSearchResultPartitionMap monthMap = 
	new CaptureSearchResultPartitionMap();
PartitionSize monthSize = Partitioner.monthSize;
Partitioner<CaptureSearchResult> monthPartitioner = 
	new Partitioner<CaptureSearchResult>(monthMap);

PartitionPartitionMap yearMap = 
	new PartitionPartitionMap();
PartitionSize yearSize = Partitioner.yearSize;
Partitioner<Partition<CaptureSearchResult>> yearPartitioner = 
	new Partitioner<Partition<CaptureSearchResult>>(yearMap);

List<Partition<Partition<CaptureSearchResult>>> yearPartitions = 
	yearPartitioner.getRange(yearSize,firstDate,lastDate);

int imgWidth = 500;
int imgHeight = 35;
Date firstYearDate = yearPartitions.get(0).getStart();
Date lastYearDate = yearPartitions.get(yearPartitions.size()-1).getEnd();

List<Partition<CaptureSearchResult>> monthPartitions = 
	monthPartitioner.getRange(monthSize,firstYearDate,lastYearDate);

Iterator<CaptureSearchResult> it = cResults.iterator();

monthPartitioner.populate(monthPartitions,it);

yearPartitioner.populate(yearPartitions,monthPartitions.iterator());

String yearFormatKey = "PartitionSize.dateHeader.yearGraphLabel";
Graph graph = PartitionsToGraph.partsOfPartsToGraph(yearPartitions,fmt,yearFormatKey,imgWidth,imgHeight);
String encodedGraph = GraphEncoder.encode(graph);
String imgUrl = graphJspPrefix + "jsp/graph.jsp?graphdata=" + encodedGraph;
// TODO: this is archivalUrl specific:
String starLink = fmt.escapeHtml(queryPrefix + "*/" + searchUrl);
%>
<!-- BEGIN WAYBACK TIMELINE DISCLAIMER INSERT -->
<style type="text/css">
#wm-disclaim {
display:none;
line-height:normal !important;
border:1px solid #000 !important;
padding:5px !important;
position:relative !important;
z-index:99999 !important;
color:#000 !important;
background-color:#efefef !important;
font-size:12px !important;
font-family:helvetica !important;
text-align:left !important;
}
.wm-disclaim-value {
font-size: 18px !important;
font-weight: bold !important;
color: #333 !important;
}
.wm-disclaim-label {
font-size: 18px !important;
color: #999 !important;
}
#wm-disclaim a {
color:#00f !important;
text-decoration:underline !important;
font-size:12px !important;
}
#wm-disclaim-hide {
float:right !important;
margin:0 0 5px 5px !important;
border:1px solid #ccc !important;
padding:1px 5px !important;
cursor:default !important;
font-size:10px !important;
font-weight:bold !important;
color:#666 !important;
}
#wm-disclaim-hide:hover {
border:1px outset #ccc !important;
}
#wm-disclaim-hide:focus, #wm-disclaim-hide:active {
border:1px inset #ccc !important;
}
.wm-nav-link-div {
padding:3px !important;
text-align:center !important;
}
</style>
<script type="text/javascript" src="<%= staticPrefix %>js/graph-calc.js" ></script>
<script type="text/javascript">
var firstDate = <%= firstYearDate.getTime() %>;
var lastDate = <%= lastYearDate.getTime() %>;
var wbPrefix = "<%= replayPrefix %>";
var wbCurrentUrl = "<%= searchUrlJS %>";
</script>
<div id="wm-disclaim" dir="ltr" >
	<table width="100%" border="0" cellpadding="0" cellspacing="3">
		<tr>
			<!-- OPENWAYBACK LOGO -->
			<td rowspan="2" valign="top" align="left"><a href="<%= queryPrefix %>"><img style="padding-right:15px;" src="<%= staticPrefix %>images/OpenWayback-banner.png" border="0"></a></td>
			<!-- /OPENWAYBACK LOGO -->
			<td width="99%">
				<table width="100%" border="0" cellpadding="0" cellspacing="0">
					<tr>
						<td>
							<table width="100%" border="0" cellpadding="0" cellspacing="0">
								<tr>
									<td width="90%" style="text-align:left; vertical-align:top;">
										<span class="wm-disclaim-label"><%= fmt.format("GraphTimeline.urlLabel") %></span> <span class="wm-disclaim-value"><%= searchUrlSafe %></span>
									</td>
									<td width="10%">
										<!-- URL FORM -->
										<table border="0" cellpadding="0" cellspacing="0">
											<tr>
												<form action="<%= queryPrefix %>query" method="get">
													<td class="wm-disclaim-label"><%= fmt.format("GraphTimeline.searchLabel") %></td>
													<td><input type="hidden" name="<%= WaybackRequest.REQUEST_TYPE %>" value="<%= WaybackRequest.REQUEST_CAPTURE_QUERY %>"><input type="text" name="<%= WaybackRequest.REQUEST_URL %>" value="http://" size="24" maxlength="256"></td>
													<td><input type="submit" name="submit" value="<%= fmt.format("GraphTimeline.searchButtonText") %>"></td>
												</form>
											</tr>
										</table>
										<!-- /URL FORM -->
									</td>
								</tr>
							</table>
						</td>
					</tr>
					<tr>
						<td>
							<table border="0" cellpadding="0" cellspacing="0">
								<tr>
									<td width="300" title="<%= fmt.format("GraphTimeline.dateLongFormat",exactDate) %>" class="wm-disclaim-label"><%= fmt.format("GraphTimeline.dateLabel") %>&nbsp;<span class="wm-disclaim-value"><%= fmt.spaceToNBSP(fmt.format("GraphTimeline.dateShortFormat",exactDate)) %></span></td>
									<td width="40%">
										<div class="wm-nav-link-div">
											<a id="wm-graph-anchor" href="">
											    <img width="<%= imgWidth %>" 
											    	height="<%= imgHeight %>" 
													src="<%= imgUrl %>" 
													border="0"
													onmousemove="document.getElementById('wm-graph-anchor').href= wbPrefix + calcTimestamp(event,this,firstDate,lastDate) + '/' +  wbCurrentUrl"></img>
									        </a>
									  	</div>			
										<div class="wm-nav-link-div">
										<%
										String navs[] = PartitionsToGraph.getNavigators(fmt,results.getResult());
										String links[] = PartitionsToGraph.getNavigatorLinks(yearPartitions,uriConverter);
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
									</td>
									<td width="%1" align="right" valign="bottom">
										<div id="wm-disclaim-hide" onclick="document.getElementById('wm-disclaim').style.display='none'">
										<%= fmt.format("GraphTimeline.hideButton") %>
										</div>
									</td>
								</tr>
							</table>
						</td>
					</tr>
				</table>
			</td>
		</tr>
	</table>
</div>
<script type="text/javascript" src="<%= staticPrefix %>js/disclaim-element.js" ></script>
<script type="text/javascript">
  var wmDisclaimBanner = document.getElementById("wm-disclaim");
  if(wmDisclaimBanner != null) {
    disclaimElement(wmDisclaimBanner);
  }
</script>
<!-- END OPENWAYBACK TIMELINE DISCLAIMER INSERT -->
