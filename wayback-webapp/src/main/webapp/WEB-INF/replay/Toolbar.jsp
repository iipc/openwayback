<%@   page language="java" pageEncoding="utf-8" contentType="text/html;charset=utf-8"
%><%@ page import="java.util.Iterator"
%><%@ page import="java.util.ArrayList"
%><%@ page import="java.util.Date"
%><%@ page import="java.util.Calendar"
%><%@ page import="java.util.List"
%><%@ page import="java.text.ParseException"
%><%@ page import="org.archive.wayback.ResultURIConverter"
%><%@ page import="org.archive.wayback.WaybackConstants"
%><%@ page import="org.archive.wayback.core.CaptureSearchResult"
%><%@ page import="org.archive.wayback.core.CaptureSearchResults"
%><%@ page import="org.archive.wayback.core.UIResults"
%><%@ page import="org.archive.wayback.core.WaybackRequest"
%><%@ page import="org.archive.wayback.partition.CaptureSearchResultPartitionMap"
%><%@ page import="org.archive.wayback.partition.PartitionPartitionMap"
%><%@ page import="org.archive.wayback.partition.PartitionsToGraph"
%><%@ page import="org.archive.wayback.partition.ToolBarData"
%><%@ page import="org.archive.wayback.util.graph.Graph"
%><%@ page import="org.archive.wayback.util.graph.GraphEncoder"
%><%@ page import="org.archive.wayback.util.graph.GraphRenderer"
%><%@ page import="org.archive.wayback.util.partition.Partition"
%><%@ page import="org.archive.wayback.util.partition.Partitioner"
%><%@ page import="org.archive.wayback.util.partition.PartitionSize"
%><%@ page import="org.archive.wayback.util.StringFormatter"
%><%@ page import="org.archive.wayback.util.url.UrlOperations"
%><%
UIResults results = UIResults.extractReplay(request);
WaybackRequest wbRequest = results.getWbRequest();
ResultURIConverter uriConverter = results.getURIConverter();
StringFormatter fmt = wbRequest.getFormatter();

String staticPrefix = results.getStaticPrefix();
String queryPrefix = results.getQueryPrefix();
String replayPrefix = results.getReplayPrefix();

String graphJspPrefix = results.getContextConfig("graphJspPrefix");
if(graphJspPrefix == null) {
	graphJspPrefix = queryPrefix;
}
ToolBarData data = new ToolBarData(results);

String searchUrl = 
	UrlOperations.stripDefaultPortFromUrl(wbRequest.getRequestUrl());
String searchUrlSafe = fmt.escapeHtml(searchUrl);
String searchUrlJS = fmt.escapeJavaScript(searchUrl);
Date firstYearDate = data.yearPartitions.get(0).getStart();
Date lastYearDate = data.yearPartitions.get(data.yearPartitions.size()-1).getEnd();

int resultIndex = 1;
int imgWidth = 0;
int imgHeight = 27;
int monthWidth = 2;
int yearWidth = 25;

for (int year = 1996; year <= Calendar.getInstance().get(Calendar.YEAR); year++)
    imgWidth += yearWidth;

String yearFormatKey = "PartitionSize.dateHeader.yearGraphLabel";
String encodedGraph = data.computeGraphString(yearFormatKey,imgWidth,imgHeight);
String graphImgUrl = graphJspPrefix + "jsp/graph.jsp?graphdata=" + encodedGraph;
// TODO: this is archivalUrl specific:
String starLink = fmt.escapeHtml(queryPrefix + wbRequest.getReplayTimestamp() + 
		"*/" + searchUrl);
%>
<!-- BEGIN WAYBACK TOOLBAR INSERT -->

<script type="text/javascript" src="<%= staticPrefix %>js/disclaim-element.js" ></script>
<script type="text/javascript" src="<%= staticPrefix %>js/graph-calc.js" ></script>
<script type="text/javascript" src="<%= staticPrefix %>jflot/jquery.min.js" ></script>
<script type="text/javascript">
var firstDate = <%= firstYearDate.getTime() %>;
var lastDate = <%= lastYearDate.getTime() %>;
var wbPrefix = "<%= replayPrefix %>";
var wbCurrentUrl = "<%= searchUrlJS %>";

var curYear = -1;
var curMonth = -1;
var yearCount = 15;
var firstYear = 1996;
var imgWidth=<%= imgWidth %>;
var yearImgWidth = <%= yearWidth %>;
var monthImgWidth = <%= monthWidth %>;
var trackerVal = "none";
var displayDay = "<%= fmt.format("ToolBar.curDayText",data.curResult.getCaptureDate()) %>";
var displayMonth = "<%= fmt.format("ToolBar.curMonthText",data.curResult.getCaptureDate()) %>";
var displayYear = "<%= fmt.format("ToolBar.curYearText",data.curResult.getCaptureDate()) %>";
var prettyMonths = ["Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"];

function showTrackers(val) {
	if(val == trackerVal) {
		return;
	}
	if(val == "inline") {
		document.getElementById("displayYearEl").style.color = "#ec008c";
		document.getElementById("displayMonthEl").style.color = "#ec008c";
		document.getElementById("displayDayEl").style.color = "#ec008c";		
	} else {
		document.getElementById("displayYearEl").innerHTML = displayYear;
		document.getElementById("displayYearEl").style.color = "#ff0";
		document.getElementById("displayMonthEl").innerHTML = displayMonth;
		document.getElementById("displayMonthEl").style.color = "#ff0";
		document.getElementById("displayDayEl").innerHTML = displayDay;
		document.getElementById("displayDayEl").style.color = "#ff0";
	}
   document.getElementById("wbMouseTrackYearImg").style.display = val;
   document.getElementById("wbMouseTrackMonthImg").style.display = val;
   trackerVal = val;
}
function getElementX2(obj) {
	var thing = jQuery(obj);
	if((thing == undefined) 
			|| (typeof thing == "undefined") 
			|| (typeof thing.offset == "undefined")) {
		return getElementX(obj);
	}
	return Math.round(thing.offset().left);
}
function trackMouseMove(event,element) {

   var eventX = getEventX(event);
   var elementX = getElementX2(element);
   var xOff = eventX - elementX;
	if(xOff < 0) {
		xOff = 0;
	} else if(xOff > imgWidth) {
		xOff = imgWidth;
	}
   var monthOff = xOff % yearImgWidth;

   var year = Math.floor(xOff / yearImgWidth);
	var yearStart = year * yearImgWidth;
   var monthOfYear = Math.floor(monthOff / monthImgWidth);
   if(monthOfYear > 11) {
       monthOfYear = 11;
   }
   // 1 extra border pixel at the left edge of the year:
   var month = (year * 12) + monthOfYear;
   var day = 1;
	if(monthOff % 2 == 1) {
		day = 15;
	}
	var dateString = 
		zeroPad(year + firstYear) + 
		zeroPad(monthOfYear+1,2) +
		zeroPad(day,2) + "000000";

	var monthString = prettyMonths[monthOfYear];
	document.getElementById("displayYearEl").innerHTML = year + 1996;
	document.getElementById("displayMonthEl").innerHTML = monthString;
	// looks too jarring when it changes..
	//document.getElementById("displayDayEl").innerHTML = zeroPad(day,2);

	var url = wbPrefix + dateString + '/' +  wbCurrentUrl;
	document.getElementById('wm-graph-anchor').href = url;

   //document.getElementById("wmtbURL").value="evX("+eventX+") elX("+elementX+") xO("+xOff+") y("+year+") m("+month+") monthOff("+monthOff+") DS("+dateString+") Moy("+monthOfYear+") ms("+monthString+")";
   if(curYear != year) {
       var yrOff = year * yearImgWidth;
       document.getElementById("wbMouseTrackYearImg").style.left = yrOff + "px";
       curYear = year;
   }
   if(curMonth != month) {
       var mtOff = year + (month * monthImgWidth) + 1;
       document.getElementById("wbMouseTrackMonthImg").style.left = mtOff + "px";
       curMonth = month;
   }
}
</script>

<style type="text/css">body{margin-top:0!important;padding-top:0!important;min-width:800px!important;}#wm-ipp a:hover{text-decoration:underline!important;}</style>
<div id="wm-ipp" style="display:none; position:relative;padding:0 5px;min-height:70px;min-width:800px; z-index:9000;">
<div id="wm-ipp-inside" style="position:fixed;padding:0!important;margin:0!important;width:97%;min-width:780px;border:5px solid #000;border-top:none;background-image:url(<%= staticPrefix %>images/toolbar/wm_tb_bk_trns.png);text-align:center;-moz-box-shadow:1px 1px 3px #333;-webkit-box-shadow:1px 1px 3px #333;box-shadow:1px 1px 3px #333;font-size:11px!important;font-family:'Lucida Grande','Arial',sans-serif!important;">
   <table style="border-collapse:collapse;margin:0;padding:0;width:100%;"><tbody><tr>
   <td style="padding:10px;vertical-align:top;min-width:110px;">
   <a href="<%= queryPrefix %>" title="Wayback Machine home page" style="background-color:transparent;border:none;"><img src="<%= staticPrefix %>images/toolbar/wayback-toolbar-logo.png" alt="Wayback Machine" width="110" height="39" border="0"/></a>
   </td>
   <td style="padding:0!important;text-align:center;vertical-align:top;width:100%;">

       <table style="border-collapse:collapse;margin:0 auto;padding:0;width:570px;"><tbody><tr>
       <td style="padding:3px 0;" colspan="2">
           <form target="_top" method="get" action="<%= queryPrefix %>query" name="wmtb" id="wmtb" style="margin:0!important;padding:0!important;"><input type="text" name="<%= WaybackRequest.REQUEST_URL %>" id="wmtbURL" value="<%= searchUrlSafe %>" maxlength="256" style="width:400px;font-size:11px;font-family:'Lucida Grande','Arial',sans-serif;"/><input type="hidden" name="<%= WaybackRequest.REQUEST_TYPE %>" value="<%= WaybackRequest.REQUEST_REPLAY_QUERY %>"><input type="hidden" name="<%= WaybackRequest.REQUEST_DATE %>" value="<%= data.curResult.getCaptureTimestamp() %>"><input type="submit" value="Go" style="font-size:11px;font-family:'Lucida Grande','Arial',sans-serif;margin-left:5px;"/><span id="wm_tb_options" style="display:block;"></span></form>
       </td>
       <td style="vertical-align:bottom;padding:5px 0 0 0!important;" rowspan="2">
           <table style="border-collapse:collapse;width:110px;color:#99a;font-family:'Helvetica','Lucida Grande','Arial',sans-serif;"><tbody>
			
           <!-- NEXT/PREV MONTH NAV AND MONTH INDICATOR -->
           <tr style="width:110px;height:16px;font-size:10px!important;">
           	<td style="padding-right:9px;font-size:11px!important;font-weight:bold;text-transform:uppercase;text-align:right;white-space:nowrap;overflow:visible;" nowrap="nowrap">
               <%
               	if(data.monthPrevResult == null) {
                       %>
                       <%= fmt.format("ToolBar.noPrevMonthText",ToolBarData.addMonth(data.curResult.getCaptureDate(),-1)) %>
                       <%
               	} else {
		                %>
		                <a href="<%= data.makeReplayURL(data.monthPrevResult) %>" style="text-decoration:none;color:#33f;font-weight:bold;background-color:transparent;border:none;" title="<%= fmt.format("ToolBar.prevMonthTitle",data.monthPrevResult.getCaptureDate()) %>"><strong><%= fmt.format("ToolBar.prevMonthText",data.monthPrevResult.getCaptureDate()).toUpperCase() %></strong></a>
		                <%
               	}
               %>
               </td>
               <td id="displayMonthEl" style="background:#000;color:#ff0;font-size:11px!important;font-weight:bold;text-transform:uppercase;width:34px;height:15px;padding-top:1px;text-align:center;" title="<%= fmt.format("ToolBar.curMonthTitle",data.curResult.getCaptureDate()) %>"><%= fmt.format("ToolBar.curMonthText",data.curResult.getCaptureDate()).toUpperCase() %></td>
				<td style="padding-left:9px;font-size:11px!important;font-weight:bold;text-transform:uppercase;white-space:nowrap;overflow:visible;" nowrap="nowrap">
               <%
               	if(data.monthNextResult == null) {
                       %>
                       <%= fmt.format("ToolBar.noNextMonthText",ToolBarData.addMonth(data.curResult.getCaptureDate(),1)) %>
                       <%
               	} else {
		                %>
		                <a href="<%= data.makeReplayURL(data.monthNextResult) %>" style="text-decoration:none;color:#33f;font-weight:bold;background-color:transparent;border:none;" title="<%= fmt.format("ToolBar.nextMonthTitle",data.monthNextResult.getCaptureDate()) %>"><strong><%= fmt.format("ToolBar.nextMonthText",data.monthNextResult.getCaptureDate()).toUpperCase() %></strong></a>
		                <%
               	}
               %>
               </td>
           </tr>

           <!-- NEXT/PREV CAPTURE NAV AND DAY OF MONTH INDICATOR -->
           <tr>
               <td style="padding-right:9px;white-space:nowrap;overflow:visible;text-align:right!important;vertical-align:middle!important;" nowrap="nowrap">
               <%
               	if(data.prevResult == null) {
                       %>
                       <img src="<%= staticPrefix %>images/toolbar/wm_tb_prv_off.png" alt="Previous capture" width="14" height="16" border="0" />
                       <%
               	} else {
		                %>
		                <a href="<%= data.makeReplayURL(data.prevResult) %>" title="<%= fmt.format("ToolBar.prevTitle",data.prevResult.getCaptureDate()) %>" style="background-color:transparent;border:none;"><img src="<%= staticPrefix %>images/toolbar/wm_tb_prv_on.png" alt="Previous capture" width="14" height="16" border="0" /></a>
		                <%
               	}
               %>
               </td>
               <td id="displayDayEl" style="background:#000;color:#ff0;width:34px;height:24px;padding:2px 0 0 0;text-align:center;font-size:24px;font-weight: bold;" title="<%= fmt.format("ToolBar.curDayTitle",data.curResult.getCaptureDate()) %>"><%= fmt.format("ToolBar.curDayText",data.curResult.getCaptureDate()) %></td>
				<td style="padding-left:9px;white-space:nowrap;overflow:visible;text-align:left!important;vertical-align:middle!important;" nowrap="nowrap">
               <%
               	if(data.nextResult == null) {
                       %>
                       <img src="<%= staticPrefix %>images/toolbar/wm_tb_nxt_off.png" alt="Next capture" width="14" height="16" border="0"/>
                       <%
               	} else {
		                %>
		                <a href="<%= data.makeReplayURL(data.nextResult) %>" title="<%= fmt.format("ToolBar.nextTitle",data.nextResult.getCaptureDate()) %>" style="background-color:transparent;border:none;"><img src="<%= staticPrefix %>images/toolbar/wm_tb_nxt_on.png" alt="Next capture" width="14" height="16" border="0"/></a>
		                <%
               	}
               %>
			    </td>
           </tr>

           <!-- NEXT/PREV YEAR NAV AND YEAR INDICATOR -->
           <tr style="width:110px;height:13px;font-size:9px!important;">
				<td style="padding-right:9px;font-size:11px!important;font-weight: bold;text-align:right;white-space:nowrap;overflow:visible;" nowrap="nowrap">
               <%
               	if(data.yearPrevResult == null) {
                       %>
                       <%= fmt.format("ToolBar.noPrevYearText",ToolBarData.addYear(data.curResult.getCaptureDate(),-1)) %>
                       <%
               	} else {
		                %>
		                <a href="<%= data.makeReplayURL(data.yearPrevResult) %>" style="text-decoration:none;color:#33f;font-weight:bold;background-color:transparent;border:none;" title="<%= fmt.format("ToolBar.prevYearTitle",data.yearPrevResult.getCaptureDate()) %>"><strong><%= fmt.format("ToolBar.prevYearText",data.yearPrevResult.getCaptureDate()) %></strong></a>
		                <%
               	}
               %>
               </td>
               <td id="displayYearEl" style="background:#000;color:#ff0;font-size:11px!important;font-weight: bold;padding-top:1px;width:34px;height:13px;text-align:center;" title="<%= fmt.format("ToolBar.curYearTitle",data.curResult.getCaptureDate()) %>"><%= fmt.format("ToolBar.curYearText",data.curResult.getCaptureDate()) %></td>
				<td style="padding-left:9px;font-size:11px!important;font-weight: bold;white-space:nowrap;overflow:visible;" nowrap="nowrap">
               <%
               	if(data.yearNextResult == null) {
                       %>
                       <%= fmt.format("ToolBar.noNextYearText",ToolBarData.addYear(data.curResult.getCaptureDate(),1)) %>
                       <%
               	} else {
		                %>
		                <a href="<%= data.makeReplayURL(data.yearNextResult) %>" style="text-decoration:none;color:#33f;font-weight:bold;background-color:transparent;border:none;" title="<%= fmt.format("ToolBar.nextYearTitle",data.yearNextResult.getCaptureDate()) %>"><strong><%= fmt.format("ToolBar.nextYearText",data.yearNextResult.getCaptureDate()) %></strong></a>
		                <%
               	}
               %>
				</td>
           </tr>
           </tbody></table>
       </td>

       </tr>
       <tr>
       <td style="vertical-align:middle;padding:0!important;">
           <a href="<%= starLink %>" style="color:#33f;font-size:11px;font-weight:bold;background-color:transparent;border:none;" title="<%= fmt.format("ToolBar.numCapturesTitle") %>"><strong><%= fmt.format("ToolBar.numCapturesText",data.getResultCount()) %></strong></a>
           <div style="margin:0!important;padding:0!important;color:#666;font-size:9px;padding-top:2px!important;white-space:nowrap;" title="<%= fmt.format("ToolBar.captureRangeTitle") %>"><%= fmt.format("ToolBar.captureRangeText",data.getFirstResultDate(),data.getLastResultDate()) %></div>
       </td>
       <td style="padding:0!important;">
       <a style="position:relative; white-space:nowrap; width:<%= imgWidth %>px;height:<%= imgHeight %>px;" href="" id="wm-graph-anchor">
       <div id="wm-ipp-sparkline" style="position:relative; white-space:nowrap; width:<%= imgWidth %>px;height:<%= imgHeight %>px;background-color:#fff;cursor:pointer;border-right:1px solid #ccc;" title="<%= fmt.format("ToolBar.sparklineTitle") %>">
			<img id="sparklineImgId" style="position:absolute; z-index:9012; top:0px; left:0px;"
				onmouseover="showTrackers('inline');" 
				onmouseout="showTrackers('none');"
				onmousemove="trackMouseMove(event,this)"
				alt="sparklines"
				width="<%= imgWidth %>"
				height="<%= imgHeight %>"
				border="0"
				src="<%= graphImgUrl %>"></img>
			<img id="wbMouseTrackYearImg" 
				style="display:none; position:absolute; z-index:9010;"
				width="<%= yearWidth %>" 
				height="<%= imgHeight %>"
				border="0"
				src="<%= staticPrefix %>images/toolbar/transp-yellow-pixel.png"></img>
			<img id="wbMouseTrackMonthImg"
				style="display:none; position:absolute; z-index:9011; " 
				width="<%= monthWidth %>"
				height="<%= imgHeight %>" 
				border="0"
				src="<%= staticPrefix %>images/toolbar/transp-red-pixel.png"></img>
       </div>
		</a>

       </td>
       </tr></tbody></table>
   </td>
   <td style="text-align:right;padding:5px;width:65px;font-size:11px!important;">
       <a href="javascript:;" onclick="document.getElementById('wm-ipp').style.display='none';" style="display:block;padding-right:18px;background:url(<%= staticPrefix %>images/toolbar/wm_tb_close.png) no-repeat 100% 0;color:#33f;font-family:'Lucida Grande','Arial',sans-serif;margin-bottom:23px;background-color:transparent;border:none;" title="<%= fmt.format("ToolBar.closeTitle") %>"><%= fmt.format("ToolBar.closeText") %></a>
       <a href="<%= fmt.format("UIGlobal.helpUrl") %>" style="display:block;padding-right:18px;background:url(<%= staticPrefix %>images/toolbar/wm_tb_help.png) no-repeat 100% 0;color:#33f;font-family:'Lucida Grande','Arial',sans-serif;background-color:transparent;border:none;" title="<%= fmt.format("ToolBar.helpTitle") %>"><%= fmt.format("ToolBar.helpText") %></a>
   </td>
   </tr></tbody></table>

</div>
</div>
<script type="text/javascript">
 var wmDisclaimBanner = document.getElementById("wm-ipp");
 if(wmDisclaimBanner != null) {
   disclaimElement(wmDisclaimBanner);
 }
</script>
<!-- END WAYBACK TOOLBAR INSERT -->
